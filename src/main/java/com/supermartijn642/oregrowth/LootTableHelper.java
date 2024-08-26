package com.supermartijn642.oregrowth;

import com.supermartijn642.core.TextComponents;
import com.supermartijn642.core.util.Triple;
import it.unimi.dsi.fastutil.Pair;
import net.minecraft.ChatFormatting;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.*;
import net.minecraft.world.level.storage.loot.predicates.*;
import net.minecraft.world.level.storage.loot.providers.number.BinomialDistributionGenerator;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;

import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created 24/08/2024 by SuperMartijn642
 */
public class LootTableHelper {

    private static final Function<LootTable,List<LootPool>> lootTable$pools;
    static {
        // The 'pools' field in 'LootTable' gets overwritten by a Forge patch, hence we can't use an access transformer for it
        try{
            Field field = LootTable.class.getDeclaredField("pools");
            field.setAccessible(true);
            lootTable$pools = lootTable -> {
                try{
                    //noinspection unchecked
                    return (List<LootPool>)field.get(lootTable);
                }catch(IllegalAccessException e){
                    throw new RuntimeException(e);
                }
            };
        }catch(NoSuchFieldException e){
            throw new RuntimeException("Failed to make 'LootTable#pools' field accessible!", e);
        }
    }

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
        LootTable table = lookup.apply(tableIdentifier);
        List<LootEntry> items = new ArrayList<>();
        // Extract the entries from each pool
        for(LootPool pool : lootTable$pools.apply(table)){
            // To calculate the chance of getting an entry, use the average number of rolls
            float rolls = averageValue(pool.rolls); // Assume luck is 0
            int totalWeight = Arrays.stream(pool.entries).mapToInt(LootTableHelper::containerWeight).sum();
            Arrays.stream(pool.entries)
                .flatMap(container -> itemsFromContainer(container, totalWeight, lookup)) // The chance for items in a container is the container's weight / total weight
                .map(entry -> entry.withChance(1 - Math.pow(1 - entry.chance, rolls))) // (1 - entry chance)^rolls gives chance of not getting item, then simply take 1 - that
                .forEach(items::add);
        }
        // Merge duplicate entries
        items = new ArrayList<>(items.stream().collect(Collectors.toMap(
            entry -> Triple.of(entry.stack.getItem(), entry.stack.getTag(), Pair.of(entry.stack.getCount(), entry.conditions)),
            Function.identity(),
            (first, second) -> first.withChance(1 - (1 - first.chance) * (1 - second.chance))
        )).values());
        return items;
    }

    private static int containerWeight(LootPoolEntryContainer container){
        if(container instanceof LootPoolSingletonContainer)
            return ((LootPoolSingletonContainer)container).weight;
        if(container instanceof AlternativesEntry)
            return Arrays.stream(((AlternativesEntry)container).children).mapToInt(LootTableHelper::containerWeight).max().orElse(0);
        if(container instanceof EntryGroup)
            return Arrays.stream(((EntryGroup)container).children).mapToInt(LootTableHelper::containerWeight).sum();
        if(container instanceof SequentialEntry)
            return Arrays.stream(((SequentialEntry)container).children).mapToInt(LootTableHelper::containerWeight).sum();
        return 1;
    }

    private static Stream<LootEntry> itemsFromContainer(LootPoolEntryContainer container, int totalWeight, Function<ResourceLocation,LootTable> lookup){
        List<LootEntryConditions> conditions = Arrays.stream(container.conditions).map(LootTableHelper::formatCondition).toList();
        if(container instanceof AlternativesEntry){
            MutableComponent not = TextComponents.translation("oregrowth.jei_category.conditions.none_of").get();
            Collection<LootEntryConditions> previousConditions = new LinkedHashSet<>();
            List<LootEntry> entries = new ArrayList<>();
            for(LootPoolEntryContainer child : ((AlternativesEntry)container).children){
                Stream<LootEntry> stream = itemsFromContainer(child, totalWeight, lookup);
                if(!previousConditions.isEmpty())
                    stream = stream.map(entry -> entry.prependConditions(Stream.concat(conditions.stream(), Stream.of(new LootEntryConditions(not, new ArrayList<>(previousConditions)))), conditions.size() + 1));
                stream.forEach(entries::add);
                Arrays.stream(child.conditions).map(LootTableHelper::formatCondition).forEach(previousConditions::add);
            }
            return entries.stream();
        }
        if(container instanceof EntryGroup){
            return Arrays.stream(((EntryGroup)container).children)
                .flatMap(c -> itemsFromContainer(c, totalWeight, lookup))
                .map(entry -> entry.prependConditions(conditions.stream(), conditions.size()));
        }
        if(container instanceof SequentialEntry){
            Collection<LootEntryConditions> previousConditions = new LinkedHashSet<>();
            return Arrays.stream(((SequentialEntry)container).children)
                .flatMap(c -> itemsFromContainer(c, totalWeight, lookup))
                .map(entry -> {
                    LootEntry newEntry = entry.prependConditions(Stream.concat(conditions.stream(), previousConditions.stream()), conditions.size() + previousConditions.size());
                    previousConditions.addAll(entry.conditions);
                    return newEntry;
                });
        }
        if(container instanceof LootItem)
            return Stream.of(new LootEntry(new ItemStack(((LootItem)container).item), (double)((LootItem)container).weight / totalWeight, conditions));
        if(container instanceof LootTableReference)
            return entriesInTable(((LootTableReference)container).name, lookup).stream().map(entry -> entry.withChance(entry.chance * ((LootTableReference)container).weight / totalWeight).prependConditions(conditions.stream(), conditions.size()));
        return Stream.empty();
    }

    private static LootEntryConditions formatCondition(LootItemCondition condition){
        if(condition instanceof AllOfCondition){
            List<LootEntryConditions> subs = Arrays.stream(((AllOfCondition)condition).terms).map(LootTableHelper::formatCondition).filter(Objects::nonNull).toList();
            if(subs.isEmpty())
                return null;
            if(subs.size() == 1)
                return subs.get(0);
            return new LootEntryConditions(TextComponents.translation("oregrowth.jei_category.conditions.all_of").get(), subs);
        }
        if(condition instanceof AnyOfCondition){
            List<LootEntryConditions> subs = Arrays.stream(((AnyOfCondition)condition).terms).map(LootTableHelper::formatCondition).filter(Objects::nonNull).toList();
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
            if(((WeatherCheck)condition).isRaining != null){
                //noinspection DataFlowIssue
                if(((WeatherCheck)condition).isRaining)
                    return new LootEntryConditions(TextComponents.translation("oregrowth.jei_category.conditions.raining").get(), List.of());
                else
                    return new LootEntryConditions(TextComponents.translation("oregrowth.jei_category.conditions.raining.not").get(), List.of());
            }
            if(((WeatherCheck)condition).isThundering != null){
                //noinspection DataFlowIssue
                if(((WeatherCheck)condition).isThundering)
                    return new LootEntryConditions(TextComponents.translation("oregrowth.jei_category.conditions.thundering").get(), List.of());
                else
                    return new LootEntryConditions(TextComponents.translation("oregrowth.jei_category.conditions.thundering.not").get(), List.of());
            }
            return null;
        }
        if(condition instanceof MatchTool){
            MutableComponent predicate = formatItemPredicate(((MatchTool)condition).predicate);
            if(predicate == null)
                return null;
            return new LootEntryConditions(TextComponents.translation("oregrowth.jei_category.conditions.match_tool").append(predicate).get(), List.of());
        }
        return null;
    }

    private static MutableComponent formatItemPredicate(ItemPredicate predicate){
        MutableComponent enchantments = null;
        if(predicate.enchantments.length == 1)
            //noinspection DataFlowIssue
            enchantments = TextComponents.translation(predicate.enchantments[0].enchantment.getDescriptionId()).color(ChatFormatting.GOLD).get();
        else if(predicate.enchantments.length == 2){
            //noinspection DataFlowIssue
            Component enchant1 = TextComponents.translation(predicate.enchantments[0].enchantment.getDescriptionId()).color(ChatFormatting.GOLD).get();
            //noinspection DataFlowIssue
            Component enchant2 = TextComponents.translation(predicate.enchantments[1].enchantment.getDescriptionId()).color(ChatFormatting.GOLD).get();
            enchantments = TextComponents.translation("oregrowth.jei_category.conditions.match_tool.two_items", enchant1, enchant2).get();
        }else if(predicate.enchantments.length > 2){
            //noinspection DataFlowIssue
            TextComponents.TextComponentBuilder builder = TextComponents.translation(predicate.enchantments[0].enchantment.getDescriptionId()).color(ChatFormatting.GOLD);
            for(int i = 1; i < predicate.enchantments.length - 1; i++)
                //noinspection DataFlowIssue
                builder = builder.string(", ").append(TextComponents.translation(predicate.enchantments[i].enchantment.getDescriptionId()).color(ChatFormatting.GOLD).get());
            //noinspection DataFlowIssue
            Component lastEnchant = TextComponents.translation(predicate.enchantments[predicate.enchantments.length - 1].enchantment.getDescriptionId()).color(ChatFormatting.GOLD).get();
            enchantments = TextComponents.translation("oregrowth.jei_category.conditions.match_tool.more_items", builder.get(), lastEnchant).get();
        }
        if(predicate.tag != null){
            if(enchantments == null)
                return TextComponents.translation("oregrowth.jei_category.conditions.match_tool.tag", predicate.tag.location()).get();
            else
                return TextComponents.translation("oregrowth.jei_category.conditions.match_tool.tag", predicate.tag.location()).translation("oregrowth.jei_category.conditions.match_tool.enchanted", enchantments).get();
        }
        if(predicate.items != null){
            if(predicate.items.isEmpty())
                return null;
            TextComponents.TextComponentBuilder itemsFormatted;
            List<TextComponents.TextComponentBuilder> items = predicate.items.stream().map(TextComponents::item).map(b -> b.color(ChatFormatting.GOLD)).sorted(Comparator.comparing(TextComponents.TextComponentBuilder::format)).toList();
            if(predicate.items.size() == 1)
                itemsFormatted = items.get(0);
            else if(predicate.items.size() == 2)
                itemsFormatted = TextComponents.translation("oregrowth.jei_category.conditions.match_tool.two_items", items.get(0), items.get(1));
            else if(predicate.items.size() > 2){
                TextComponents.TextComponentBuilder builder = items.get(0);
                for(int i = 1; i < predicate.items.size() - 1; i++)
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
