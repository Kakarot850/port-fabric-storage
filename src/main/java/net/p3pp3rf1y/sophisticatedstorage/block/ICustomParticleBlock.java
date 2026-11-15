package net.p3pp3rf1y.sophisticatedstorage.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Implemented by storage blocks that spawn custom-tinted landing and running particles.
 */
public interface ICustomParticleBlock {
	boolean addLandingEffects(BlockState state1, ServerLevel level, BlockPos pos, BlockState state2, LivingEntity entity, int numberOfParticles);

	boolean addRunningEffects(BlockState state, Level level, BlockPos pos, Entity entity);
}
