package com.supermartijn642.oregrowth.mixin;

import com.supermartijn642.oregrowth.content.OreGrowthRecipeManager;
import net.minecraft.world.item.crafting.RecipeManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Created 04/10/2023 by SuperMartijn642
 */
@Mixin(value = RecipeManager.class)
public class RecipeManagerMixin {

    @Inject(
        method = "apply",
        at = @At("HEAD")
    )
    private void apply(CallbackInfo ci){
        //noinspection DataFlowIssue
        OreGrowthRecipeManager.reloadRecipes((RecipeManager)(Object)this);
    }

    @Inject(
        method = "replaceRecipes",
        at = @At("HEAD")
    )
    private void replaceRecipes(CallbackInfo ci){
        //noinspection DataFlowIssue
        OreGrowthRecipeManager.reloadRecipes((RecipeManager)(Object)this);
    }
}
