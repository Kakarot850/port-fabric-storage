package net.p3pp3rf1y.sophisticatedstorage.mixin.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Hud;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;
import net.p3pp3rf1y.sophisticatedstorage.item.TankBlockItem;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Hud.class)
public abstract class GuiMixin {
	@Shadow
	@Final
	private Minecraft minecraft;

	@Shadow
	private int toolHighlightTimer;

	@Shadow
	private ItemStack lastToolHighlight;

	@Unique
	private @Nullable Component sophisticatedStorage_lastHighlightTip;

	@ModifyVariable(
			method = "extractSelectedItemName(Lnet/minecraft/client/gui/GuiGraphicsExtractor;)V",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/gui/Hud;getFont()Lnet/minecraft/client/gui/Font;",
					ordinal = 0
			),
			ordinal = 0
	)
	private MutableComponent sophisticatedStorage_modifyHighlightTip(MutableComponent str) {
		if (this.lastToolHighlight.getItem() instanceof TankBlockItem tankItem) {
			Component tip = tankItem.getHighlightTip(this.lastToolHighlight, str);
			return tip instanceof MutableComponent mutable ? mutable : tip.copy();
		}
		return str;
	}

	@Inject(method = "tick()V", at = @At("RETURN"))
	private void sophisticatedStorage_refreshTankHighlightTipTimer(CallbackInfo ci) {
		if (this.minecraft.player == null) {
			sophisticatedStorage_lastHighlightTip = null;
			return;
		}
		if (!this.lastToolHighlight.isEmpty() && this.lastToolHighlight.getItem() instanceof TankBlockItem tankItem) {
			Component tip = tankItem.getHighlightTip(this.lastToolHighlight, this.lastToolHighlight.getHoverName());
			if (!tip.equals(sophisticatedStorage_lastHighlightTip)) {
				this.toolHighlightTimer = (int) (40.0 * this.minecraft.options.notificationDisplayTime().get());
				sophisticatedStorage_lastHighlightTip = tip;
			}
		} else {
			sophisticatedStorage_lastHighlightTip = null;
		}
	}
}
