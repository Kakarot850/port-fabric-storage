package net.p3pp3rf1y.sophisticatedstorage.mixin.common;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.p3pp3rf1y.sophisticatedstorage.block.StorageBlockBase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Level.class)
public abstract class LevelMixin {
	@Inject(
			method = "setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;II)Z",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/level/Level;setBlocksDirty(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/block/state/BlockState;)V"
			)
	)
	private void sophisticatedStorage_invalidateCapabilitiesOnBlockChange(BlockPos pos, BlockState blockState, int updateFlags, int updateLimit,
			CallbackInfoReturnable<Boolean> cir) {
		if ((Object) this instanceof ServerLevel serverLevel) {
			serverLevel.sophisticatedStorage_invalidateCapabilities(pos);
		}
	}

	@Inject(method = "updateNeighbourForOutputSignal(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/Block;)V", at = @At("RETURN"))
	private void sophisticatedStorage_notifyNeighborChange(BlockPos pos, Block changedBlock, CallbackInfo ci) {
		Level self = (Level) (Object) this;
		for (Direction direction : Direction.values()) {
			BlockPos relativePos = pos.relative(direction);
			if (self.hasChunkAt(relativePos)) {
				BlockState state = self.getBlockState(relativePos);
				if (state.getBlock() instanceof StorageBlockBase storageBlock) {
					storageBlock.onNeighborChange(state, self, relativePos, pos);
				}
			}
		}
	}
}
