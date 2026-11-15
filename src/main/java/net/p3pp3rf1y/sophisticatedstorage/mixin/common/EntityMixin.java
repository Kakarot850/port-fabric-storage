package net.p3pp3rf1y.sophisticatedstorage.mixin.common;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.p3pp3rf1y.sophisticatedstorage.block.ICustomParticleBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Entity.class)
public abstract class EntityMixin {
	@Redirect(
			method = "spawnSprintParticle()V",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/level/Level;addParticle(Lnet/minecraft/core/particles/ParticleOptions;DDDDDD)V"
			)
	)
	private void sophisticatedStorage_addRunningParticles(
			Level instance, ParticleOptions particle, double x, double y, double z, double xd, double yd, double zd) {
		Entity self = (Entity) (Object) this;
		BlockPos pos = self.getOnPosLegacy();
		BlockState state = instance.getBlockState(pos);
		if (state.getBlock() instanceof ICustomParticleBlock particleBlock && particleBlock.addRunningEffects(state, instance, pos, self)) {
			return;
		}
		instance.addParticle(particle, x, y, z, xd, yd, zd);
	}
}
