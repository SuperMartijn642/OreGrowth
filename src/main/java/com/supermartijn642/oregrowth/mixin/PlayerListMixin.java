package com.supermartijn642.oregrowth.mixin;

import com.supermartijn642.oregrowth.OreGrowth;
import com.supermartijn642.oregrowth.content.OreGrowthRecipeManager;
import com.supermartijn642.oregrowth.content.SyncOreGrowthRecipesPacket;
import net.minecraft.network.Connection;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.players.PlayerList;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

/**
 * Created 06/07/2025 by SuperMartijn642
 */
@Mixin(PlayerList.class)
public class PlayerListMixin {

    @Final
    @Shadow
    private List<ServerPlayer> players;

    @Inject(
        method = "placeNewPlayer",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/server/players/PlayerList;sendPlayerPermissionLevel(Lnet/minecraft/server/level/ServerPlayer;)V",
            shift = At.Shift.BEFORE
        )
    )
    private void placeNewPlayer(Connection connection, ServerPlayer player, CommonListenerCookie commonListenerCookie, CallbackInfo ci){
        OreGrowth.CHANNEL.sendToPlayer(player, new SyncOreGrowthRecipesPacket(OreGrowthRecipeManager.get(false).getAllRecipes()));
    }

    @Inject(
        method = "reloadResources",
        at = @At("TAIL")
    )
    private void reloadResources(CallbackInfo ci){
        OreGrowth.CHANNEL.sendToAllPlayers(new SyncOreGrowthRecipesPacket(OreGrowthRecipeManager.get(false).getAllRecipes()));
    }
}
