package net.p3pp3rf1y.sophisticatedstorage.mixin.common;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.p3pp3rf1y.sophisticatedstorage.extensions.block.entity.SophisticatedStorageBlockEntity;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BlockEntity.class)
public abstract class BlockEntityMixin implements SophisticatedStorageBlockEntity {
	@Shadow
	protected @Nullable Level level;

	@Shadow
	@Final
	protected BlockPos worldPosition;

	@Override
	public void sophisticatedStorage_invalidateCapabilities() {
		if (this.level instanceof ServerLevel serverLevel) {
			serverLevel.sophisticatedStorage_invalidateCapabilities(this.worldPosition);
		}
	}

	@Inject(method = "setRemoved()V", at = @At("RETURN"))
	private void sophisticatedStorage_invalidateOnRemove(CallbackInfo ci) {
		sophisticatedStorage_invalidateCapabilities();
	}

	@Inject(method = "clearRemoved()V", at = @At("RETURN"))
	private void sophisticatedStorage_invalidateOnClearRemoved(CallbackInfo ci) {
		sophisticatedStorage_invalidateCapabilities();
	}
}
