package me.wesley1808.servercore.mixin.optimizations.mob_spawning.distance_map;

import me.wesley1808.servercore.common.interfaces.IChunkMap;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LocalMobCapCalculator;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Iterator;
import java.util.List;

@Mixin(LocalMobCapCalculator.class)
public abstract class LocalMobCapCalculatorMixin {

    @Shadow
    @Final
    private ChunkMap chunkMap;

    @Inject(method = "getPlayersNear", at = @At("HEAD"), cancellable = true)
    private void servercore$cancelOp(ChunkPos chunkPos, CallbackInfoReturnable<List<ServerPlayer>> cir) {
        cir.setReturnValue(null); // Return nothing as we won't be using it.
    }

    @Redirect(
            method = "canSpawn",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/List;iterator()Ljava/util/Iterator;"
            )
    )
    private Iterator<ServerPlayer> servercore$useDistanceMap(List<ServerPlayer> list, MobCategory category, ChunkPos chunkPos) {
        return this.getPlayerIterator(list, chunkPos);
    }

    @Redirect(
            method = "addMob",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/List;iterator()Ljava/util/Iterator;"
            )
    )
    private Iterator<ServerPlayer> servercore$useDistanceMap(List<ServerPlayer> list, ChunkPos chunkPos, MobCategory category) {
        return this.getPlayerIterator(list, chunkPos);
    }

    private Iterator<ServerPlayer> getPlayerIterator(List<ServerPlayer> list, ChunkPos chunkPos) {
        return list == null ? ((IChunkMap) this.chunkMap).getDistanceMap().getPlayersInRange(chunkPos).iterator() : list.listIterator();
    }
}