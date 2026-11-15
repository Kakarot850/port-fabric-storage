package net.p3pp3rf1y.sophisticatedstorage.mixin.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.p3pp3rf1y.sophisticatedstorage.block.BarrelBlock;
import net.p3pp3rf1y.sophisticatedstorage.block.BarrelBlockClientExtensions;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Replaces NeoForge's {@code IClientBlockExtensions#addHitEffects} / {@code addDestroyEffects} for {@link BarrelBlock}
 * so breaking/destroy particles use barrel dynamic face tinting and suppress limited barrel front-face hit effects.
 */
@Mixin(ClientLevel.class)
public abstract class ClientLevelMixin {
	@Shadow
	@Final
	private Minecraft minecraft;

	@Inject(method = "addBreakingBlockEffect", at = @At("HEAD"), cancellable = true)
	private void sophisticatedStorage_addBreakingBlockEffect(BlockPos pos, Direction direction, CallbackInfo ci) {
		ClientLevel self = (ClientLevel) (Object) this;
		BlockState state = self.getBlockState(pos);
		if (state.getBlock() instanceof BarrelBlock barrelBlock
				&& BarrelBlockClientExtensions.addHitEffects(barrelBlock, state, self, pos, direction, this.minecraft.particleEngine)) {
			ci.cancel();
		}
	}

	@Inject(method = "addDestroyBlockEffect", at = @At("HEAD"), cancellable = true)
	private void sophisticatedStorage_addDestroyBlockEffect(BlockPos pos, BlockState state, CallbackInfo ci) {
		ClientLevel self = (ClientLevel) (Object) this;
		if (state.getBlock() instanceof BarrelBlock barrelBlock
				&& BarrelBlockClientExtensions.addDestroyEffects(barrelBlock, state, self, pos, this.minecraft.particleEngine)) {
			ci.cancel();
		}
	}
}
