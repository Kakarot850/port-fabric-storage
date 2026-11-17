package net.p3pp3rf1y.sophisticatedstorage.mixin.client;

import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.p3pp3rf1y.sophisticatedstorage.client.ClientEventHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Fabric fires {@code AttackBlockCallback} from both {@code startDestroyBlock} (new press) and, in Creative,
 * {@code continueDestroyBlock} (button held). The callback cannot tell them apart, so mark a fresh press here.
 */
@Mixin(MultiPlayerGameMode.class)
public abstract class MultiPlayerGameModeMixin {
	@Inject(method = "startDestroyBlock", at = @At("HEAD"))
	private void sophisticatedStorage_markFreshAttack(BlockPos pos, Direction direction, CallbackInfoReturnable<Boolean> cir) {
		ClientEventHandler.markFreshAttack(true);
	}

	@Inject(method = "continueDestroyBlock", at = @At("HEAD"))
	private void sophisticatedStorage_markHeldAttack(BlockPos pos, Direction direction, CallbackInfoReturnable<Boolean> cir) {
		ClientEventHandler.markFreshAttack(false);
	}
}
