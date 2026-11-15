package net.p3pp3rf1y.sophisticatedstorage.mixin.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.p3pp3rf1y.sophisticatedstorage.client.ClientEventHandler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Intercepts unscreened hotbar scroll wheel events when the player holds shift with a storage tool or tank item.
 */
@Mixin(MouseHandler.class)
public abstract class MouseHandlerMixin {
	@Shadow
	@Final
	private Minecraft minecraft;

	@Inject(method = "onScroll", at = @At("HEAD"), cancellable = true)
	private void sophisticatedStorage_onScroll(long handle, double xoffset, double yoffset, CallbackInfo ci) {
		if (handle == this.minecraft.getWindow().handle()
				&& yoffset != 0.0D
				&& this.minecraft.getOverlay() == null
				&& this.minecraft.screen == null
				&& ClientEventHandler.onMouseScrolled(yoffset)) {
			ci.cancel();
		}
	}
}
