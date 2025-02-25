package me.wesley1808.servercore.mixin.features.activation_range.inactive_ticks;

import me.wesley1808.servercore.common.interfaces.activation_range.InactiveEntity;
import me.wesley1808.servercore.common.utils.ActivationRange;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;

/**
 * From: Spigot (Entity-Activation-Range.patch)
 * License: GPL-3.0 (licenses/GPL.md)
 */

@Mixin(Mob.class)
public abstract class MobMixin extends LivingEntity implements InactiveEntity {

    private MobMixin(EntityType<? extends LivingEntity> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    public void inactiveTick() {
        this.noActionTime++;
        ActivationRange.updateGoalSelectors((Mob) (Object) this);
    }
}
