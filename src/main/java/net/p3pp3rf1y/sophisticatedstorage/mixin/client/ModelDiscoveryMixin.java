package net.p3pp3rf1y.sophisticatedstorage.mixin.client;

import net.minecraft.client.resources.model.ModelDiscovery;
import net.minecraft.client.resources.model.ResolvableModel;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayDeque;
import java.util.List;
import java.util.Queue;

/**
 * Resolves {@link ResolvableModel#resolveDependencies(ResolvableModel.Resolver)} on non-root unbaked models
 * (mirroring NeoForge's {@code ModelDiscovery} patch without re-entering {@code Map#computeIfAbsent}).
 */
@Mixin(ModelDiscovery.class)
public abstract class ModelDiscoveryMixin {
	@Shadow
	@Final
	private ResolvableModel.Resolver resolver;

	@SuppressWarnings("rawtypes")
	@Shadow
	@Final
	private Queue parentDiscoveryQueue;

	@SuppressWarnings("rawtypes")
	@Shadow
	private void discoverDependencies(List toValidate) {
	}

	@Unique
	private final Queue<ResolvableModel> sophisticatedStorage_dependencyQueue = new ArrayDeque<>();

	@Unique
	private boolean sophisticatedStorage_discoveringDependencies = false;

	@Inject(method = "createAndQueueWrapper", at = @At("HEAD"))
	private void sophisticatedStorage_queueUnbakedDependencies(Identifier id, UnbakedModel rawModel, CallbackInfoReturnable<?> cir) {
		if (rawModel instanceof ResolvableModel resolvable) {
			this.sophisticatedStorage_dependencyQueue.add(resolvable);
		}
	}

	@SuppressWarnings("rawtypes")
	@Inject(method = "discoverDependencies", at = @At("TAIL"))
	private void sophisticatedStorage_discoverUnbakedDependencies(List toValidate, CallbackInfo ci) {
		if (this.sophisticatedStorage_discoveringDependencies) {
			return;
		}
		this.sophisticatedStorage_discoveringDependencies = true;
		try {
			while (!this.sophisticatedStorage_dependencyQueue.isEmpty() || !this.parentDiscoveryQueue.isEmpty()) {
				ResolvableModel resolvable;
				while ((resolvable = this.sophisticatedStorage_dependencyQueue.poll()) != null) {
					resolvable.resolveDependencies(this.resolver);
				}
				if (!this.parentDiscoveryQueue.isEmpty()) {
					this.discoverDependencies(toValidate);
				}
			}
		} finally {
			this.sophisticatedStorage_discoveringDependencies = false;
		}
	}
}
