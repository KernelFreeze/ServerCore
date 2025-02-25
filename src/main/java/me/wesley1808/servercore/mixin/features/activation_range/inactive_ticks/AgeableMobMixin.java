package me.wesley1808.servercore.mixin.features.activation_range.inactive_ticks;

import me.wesley1808.servercore.common.interfaces.activation_range.InactiveEntity;
import me.wesley1808.servercore.common.utils.ActivationRange;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;

/**
 * From: Spigot (Entity-Activation-Range.patch)
 * License: GPL-3.0 (licenses/GPL.md)
 */

@Mixin(AgeableMob.class)
public abstract class AgeableMobMixin extends PathfinderMob implements InactiveEntity {
    private AgeableMobMixin(EntityType<? extends PathfinderMob> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    public void inactiveTick() {
        this.noActionTime++;
        ActivationRange.updateAge((AgeableMob) (Object) this);
        ActivationRange.updateGoalSelectors(this);
    }
}
