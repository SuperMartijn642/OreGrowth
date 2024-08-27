package com.supermartijn642.oregrowth;

import com.supermartijn642.core.TextComponents;
import com.supermartijn642.core.util.Triple;
import it.unimi.dsi.fastutil.Pair;
import net.minecraft.ChatFormatting;
import net.minecraft.advancements.critereon.EnchantmentPredicate;
import net.minecraft.advancements.critereon.ItemEnchantmentsPredicate;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.*;
import net.minecraft.world.level.storage.loot.predicates.*;
import net.minecraft.world.level.storage.loot.providers.number.BinomialDistributionGenerator;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created 24/08/2024 by SuperMartijn642
 */
public class LootTableHelper {

    public record LootEntry(ItemStack stack, double chance, Collection<LootEntryConditions> conditions) {
        LootEntry withChance(double chance){
            return new LootEntry(this.stack, chance, this.conditions);
        }

        LootEntry prependConditions(Stream<LootEntryConditions> conditions, int count){
            Set<LootEntryConditions> merged = new LinkedHashSet<>(this.conditions.size() + count); // Use a set which maintains order
            conditions.forEach(merged::add);
            merged.addAll(this.conditions);
            return new LootEntry(this.stack, this.chance, merged);
        }
    }

    public record LootEntryConditions(MutableComponent component, List<LootEntryConditions> subConditions) {
        public List<MutableComponent> toComponents(){
            if(this.subConditions.isEmpty())
                return List.of(TextComponents.translation("oregrowth.jei_category.conditions.bullet").append(this.component).get());
            List<MutableComponent> components = new ArrayList<>();
            components.add(TextComponents.translation("oregrowth.jei_category.conditions.bullet").append(this.component).get());
            this.subConditions.stream()
                .map(LootEntryConditions::toComponents).flatMap(List::stream)
                .map(component -> TextComponents.translation("oregrowth.jei_category.conditions.bullet.spacing").append(component).get())
                .forEach(components::add);
            return components;
        }
    }

    public static List<LootEntry> entriesInTable(ResourceLocation tableIdentifier, Function<ResourceLocation,LootTable> lookup){
        return entriesInTable(lookup.apply(tableIdentifier), lookup);
    }

    private static List<LootEntry> entriesInTable(LootTable table, Function<ResourceLocation,LootTable> lookup){
        List<LootEntry> items = new ArrayList<>();
        // Extract the entries from each pool
        for(LootPool pool : table.pools){
            // To calculate the chance of getting an entry, use the average number of rolls
            float rolls = averageValue(pool.rolls); // Assume luck is 0
            int totalWeight = pool.entries.stream().mapToInt(LootTableHelper::containerWeight).sum();
            pool.entries.stream()
                .flatMap(container -> itemsFromContainer(container, totalWeight, lookup)) // The chance for items in a container is the container's weight / total weight
                .map(entry -> entry.withChance(1 - Math.pow(1 - entry.chance, rolls))) // (1 - entry chance)^rolls gives chance of not getting item, then simply take 1 - that
                .forEach(items::add);
        }
        // Merge duplicate entries
        items = new ArrayList<>(items.stream().collect(Collectors.toMap(
            entry -> Triple.of(entry.stack.getItem(), entry.stack.getComponentsPatch(), Pair.of(entry.stack.getCount(), entry.conditions)),
            Function.identity(),
            (first, second) -> first.withChance(1 - (1 - first.chance) * (1 - second.chance))
        )).values());
        return items;
    }

    private static int containerWeight(LootPoolEntryContainer container){
        if(container instanceof LootPoolSingletonContainer)
            return ((LootPoolSingletonContainer)container).weight;
        if(container instanceof AlternativesEntry)
            return ((AlternativesEntry)container).children.stream().mapToInt(LootTableHelper::containerWeight).max().orElse(0);
        if(container instanceof EntryGroup)
            return ((EntryGroup)container).children.stream().mapToInt(LootTableHelper::containerWeight).sum();
        if(container instanceof SequentialEntry)
            return ((SequentialEntry)container).children.stream().mapToInt(LootTableHelper::containerWeight).sum();
        return 1;
    }

    private static Stream<LootEntry> itemsFromContainer(LootPoolEntryContainer container, int totalWeight, Function<ResourceLocation,LootTable> lookup){
        List<LootEntryConditions> conditions = container.conditions.stream().map(LootTableHelper::formatCondition).toList();
        if(container instanceof AlternativesEntry){
            MutableComponent not = TextComponents.translation("oregrowth.jei_category.conditions.none_of").get();
            Collection<LootEntryConditions> previousConditions = new LinkedHashSet<>();
            List<LootEntry> entries = new ArrayList<>();
            for(LootPoolEntryContainer child : ((AlternativesEntry)container).children){
                Stream<LootEntry> stream = itemsFromContainer(child, totalWeight, lookup);
                if(!previousConditions.isEmpty())
                    stream = stream.map(entry -> entry.prependConditions(Stream.concat(conditions.stream(), Stream.of(new LootEntryConditions(not, new ArrayList<>(previousConditions)))), conditions.size() + 1));
                stream.forEach(entries::add);
                child.conditions.stream().map(LootTableHelper::formatCondition).forEach(previousConditions::add);
            }
            return entries.stream();
        }
        if(container instanceof EntryGroup){
            return ((EntryGroup)container).children.stream()
                .flatMap(c -> itemsFromContainer(c, totalWeight, lookup))
                .map(entry -> entry.prependConditions(conditions.stream(), conditions.size()));
        }
        if(container instanceof SequentialEntry){
            Collection<LootEntryConditions> previousConditions = new LinkedHashSet<>();
            return ((SequentialEntry)container).children.stream()
                .flatMap(c -> itemsFromContainer(c, totalWeight, lookup))
                .map(entry -> {
                    LootEntry newEntry = entry.prependConditions(Stream.concat(conditions.stream(), previousConditions.stream()), conditions.size() + previousConditions.size());
                    previousConditions.addAll(entry.conditions);
                    return newEntry;
                });
        }
        if(container instanceof LootItem)
            return Stream.of(new LootEntry(new ItemStack(((LootItem)container).item), (double)((LootItem)container).weight / totalWeight, conditions));
        if(container instanceof NestedLootTable)
            return entriesInTable(((NestedLootTable)container).contents.map(key -> lookup.apply(key.location()), Function.identity()), lookup).stream().map(entry -> entry.withChance(entry.chance * ((NestedLootTable)container).weight / totalWeight).prependConditions(conditions.stream(), conditions.size()));
        return Stream.empty();
    }

    private static LootEntryConditions formatCondition(LootItemCondition condition){
        if(condition instanceof AllOfCondition){
            List<LootEntryConditions> subs = ((AllOfCondition)condition).terms.stream().map(LootTableHelper::formatCondition).filter(Objects::nonNull).toList();
            if(subs.isEmpty())
                return null;
            if(subs.size() == 1)
                return subs.get(0);
            return new LootEntryConditions(TextComponents.translation("oregrowth.jei_category.conditions.all_of").get(), subs);
        }
        if(condition instanceof AnyOfCondition){
            List<LootEntryConditions> subs = ((AnyOfCondition)condition).terms.stream().map(LootTableHelper::formatCondition).filter(Objects::nonNull).toList();
            if(subs.isEmpty())
                return new LootEntryConditions(TextComponents.translation("oregrowth.jei_category.conditions.never").get(), List.of());
            if(subs.size() == 1)
                return subs.get(0);
            return new LootEntryConditions(TextComponents.translation("oregrowth.jei_category.conditions.any_of").get(), subs);
        }
        if(condition instanceof InvertedLootItemCondition){
            LootEntryConditions sub = formatCondition(((InvertedLootItemCondition)condition).term);
            if(sub == null)
                return null;
            return new LootEntryConditions(TextComponents.translation("oregrowth.jei_category.conditions.not").get(), List.of(sub));
        }
        if(condition instanceof WeatherCheck){
            if(((WeatherCheck)condition).isRaining.isPresent()){
                //noinspection OptionalGetWithoutIsPresent
                if(((WeatherCheck)condition).isRaining.get())
                    return new LootEntryConditions(TextComponents.translation("oregrowth.jei_category.conditions.raining").get(), List.of());
                else
                    return new LootEntryConditions(TextComponents.translation("oregrowth.jei_category.conditions.raining.not").get(), List.of());
            }
            if(((WeatherCheck)condition).isThundering.isPresent()){
                //noinspection OptionalGetWithoutIsPresent
                if(((WeatherCheck)condition).isThundering.get())
                    return new LootEntryConditions(TextComponents.translation("oregrowth.jei_category.conditions.thundering").get(), List.of());
                else
                    return new LootEntryConditions(TextComponents.translation("oregrowth.jei_category.conditions.thundering.not").get(), List.of());
            }
            return null;
        }
        if(condition instanceof MatchTool){
            if(((MatchTool)condition).predicate.isEmpty())
                return null;
            //noinspection OptionalGetWithoutIsPresent
            MutableComponent predicate = formatItemPredicate(((MatchTool)condition).predicate.get());
            if(predicate == null)
                return null;
            return new LootEntryConditions(TextComponents.translation("oregrowth.jei_category.conditions.match_tool").append(predicate).get(), List.of());
        }
        return null;
    }

    private static MutableComponent formatItemPredicate(ItemPredicate predicate){
        MutableComponent enchantments = null;
        List<Enchantment> actualEnchants = predicate.subPredicates().values().stream()
            .filter(ItemEnchantmentsPredicate.Enchantments.class::isInstance)
            .map(ItemEnchantmentsPredicate.Enchantments.class::cast)
            .flatMap(p -> p.enchantments.stream())
            .map(EnchantmentPredicate::enchantments)
            .filter(Optional::isPresent)
            .map(Optional::get)
            .flatMap(HolderSet::stream)
            .map(Holder::value)
            .distinct()
            .toList();
        if(actualEnchants.size() == 1)
            enchantments = TextComponents.fromTextComponent(actualEnchants.get(0).description()).color(ChatFormatting.GOLD).get();
        else if(actualEnchants.size() == 2){
            Component enchant1 = TextComponents.fromTextComponent(actualEnchants.get(0).description()).color(ChatFormatting.GOLD).get();
            Component enchant2 = TextComponents.fromTextComponent(actualEnchants.get(1).description()).color(ChatFormatting.GOLD).get();
            enchantments = TextComponents.translation("oregrowth.jei_category.conditions.match_tool.two_items", enchant1, enchant2).get();
        }else if(actualEnchants.size() > 2){
            TextComponents.TextComponentBuilder builder = TextComponents.fromTextComponent(actualEnchants.get(0).description()).color(ChatFormatting.GOLD);
            for(int i = 1; i < actualEnchants.size() - 1; i++)
                builder = builder.string(", ").append(TextComponents.fromTextComponent(actualEnchants.get(i).description()).color(ChatFormatting.GOLD).get());
            Component lastEnchant = TextComponents.fromTextComponent(actualEnchants.get(actualEnchants.size() - 1).description()).color(ChatFormatting.GOLD).get();
            enchantments = TextComponents.translation("oregrowth.jei_category.conditions.match_tool.more_items", builder.get(), lastEnchant).get();
        }
        if(predicate.items.isPresent() && predicate.items.get() instanceof HolderSet.Named){
            ResourceLocation tag = ((HolderSet.Named<Item>)predicate.items.get()).key().location();
            if(enchantments == null)
                return TextComponents.translation("oregrowth.jei_category.conditions.match_tool.tag", tag).get();
            else
                return TextComponents.translation("oregrowth.jei_category.conditions.match_tool.tag", tag).translation("oregrowth.jei_category.conditions.match_tool.enchanted", enchantments).get();
        }
        if(predicate.items.isPresent()){
            if(predicate.items.get().size() == 0)
                return null;
            TextComponents.TextComponentBuilder itemsFormatted;
            List<TextComponents.TextComponentBuilder> items = predicate.items.get().stream().map(Holder::value).map(TextComponents::item).map(b -> b.color(ChatFormatting.GOLD)).sorted(Comparator.comparing(TextComponents.TextComponentBuilder::format)).toList();
            if(predicate.items.get().size() == 1)
                itemsFormatted = items.get(0);
            else if(predicate.items.get().size() == 2)
                itemsFormatted = TextComponents.translation("oregrowth.jei_category.conditions.match_tool.two_items", items.get(0), items.get(1));
            else if(predicate.items.get().size() > 2){
                TextComponents.TextComponentBuilder builder = items.get(0);
                for(int i = 1; i < predicate.items.get().size() - 1; i++)
                    builder = builder.string(", ").append(items.get(i).get());
                itemsFormatted = TextComponents.translation("oregrowth.jei_category.conditions.match_tool.more_items", builder.get(), items.get(items.size() - 1));
            }else
                throw new AssertionError();
            if(enchantments != null)
                itemsFormatted = itemsFormatted.translation("oregrowth.jei_category.conditions.match_tool.enchanted", enchantments);
            return itemsFormatted.get();
        }
        return enchantments;
    }

    private static float averageValue(NumberProvider provider){
        if(provider instanceof BinomialDistributionGenerator)
            return averageValue(((BinomialDistributionGenerator)provider).n) * averageValue(((BinomialDistributionGenerator)provider).p);
        if(provider instanceof ConstantValue)
            return ((ConstantValue)provider).value;
        if(provider instanceof UniformGenerator)
            return (averageValue(((UniformGenerator)provider).min) + averageValue(((UniformGenerator)provider).max)) / 2;
        return -1;
    }
}
