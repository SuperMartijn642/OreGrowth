package com.supermartijn642.oregrowth.content;

import com.supermartijn642.core.network.BasePacket;
import com.supermartijn642.core.network.PacketContext;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created 06/07/2025 by SuperMartijn642
 */
public class SyncOreGrowthRecipesPacket implements BasePacket {

    private Collection<OreGrowthRecipe> recipes;

    public SyncOreGrowthRecipesPacket(Collection<OreGrowthRecipe> recipes){
        this.recipes = recipes;
    }

    public SyncOreGrowthRecipesPacket(){
    }

    @Override
    public void write(FriendlyByteBuf buffer){
        buffer.writeInt(this.recipes.size());
        for(OreGrowthRecipe recipe : this.recipes)
            //noinspection deprecation
            OreGrowthRecipe.SERIALIZER.streamCodec().encode((RegistryFriendlyByteBuf)buffer, recipe);
    }

    @Override
    public void read(FriendlyByteBuf buffer){
        int count = Math.min(buffer.readInt(), 256);
        this.recipes = new ArrayList<>(count);
        for(int i = 0; i < count; i++)
            //noinspection deprecation
            this.recipes.add(OreGrowthRecipe.SERIALIZER.streamCodec().decode((RegistryFriendlyByteBuf)buffer));
    }

    @Override
    public void handle(PacketContext context){
        OreGrowthRecipeManager.get(true).setClientRecipes(this.recipes);
    }
}
