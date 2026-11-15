package net.p3pp3rf1y.sophisticatedstorage.mixin.common;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.p3pp3rf1y.sophisticatedstorage.block.ICustomParticleBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
	@Redirect(
			method = "checkFallDamage(DZLnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;)V",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/server/level/ServerLevel;sendParticles(Lnet/minecraft/core/particles/ParticleOptions;DDDIDDDD)I"
			)
	)
	private int sophisticatedStorage_addLandingParticles(
			ServerLevel instance, ParticleOptions particle, double x, double y, double z, int count,
			double xDist, double yDist, double zDist, double speed,
			double ya, boolean onGround, BlockState onState, BlockPos pos) {
		if (onState.getBlock() instanceof ICustomParticleBlock particleBlock
				&& particleBlock.addLandingEffects(onState, instance, pos, onState, (LivingEntity) (Object) this, count)) {
			return 0;
		}
		return instance.sendParticles(particle, x, y, z, count, xDist, yDist, zDist, speed);
	}
}
