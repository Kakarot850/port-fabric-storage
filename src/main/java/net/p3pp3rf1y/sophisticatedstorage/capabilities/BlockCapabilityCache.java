package net.p3pp3rf1y.sophisticatedstorage.capabilities;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.p3pp3rf1y.sophisticatedcore.fabric.capabilities.BlockCapability;
import org.jspecify.annotations.Nullable;

import java.util.Objects;
import java.util.function.BooleanSupplier;

/**
 * Stand-in for NeoForge's {@code BlockCapabilityCache}: caches a {@link BlockCapability} lookup at a fixed position
 * and context, invalidated automatically when {@code ServerLevel} capability listeners at that position are triggered.
 */
public final class BlockCapabilityCache<T, C extends @Nullable Object> {
	public static <T, C extends @Nullable Object> BlockCapabilityCache<T, C> create(BlockCapability<T, C> capability, ServerLevel level, BlockPos pos, C context) {
		return create(capability, level, pos, context, () -> true, () -> {});
	}

	public static <T, C extends @Nullable Object> BlockCapabilityCache<T, C> create(BlockCapability<T, C> capability, ServerLevel level, BlockPos pos, C context,
			BooleanSupplier isValid, Runnable invalidationListener) {
		Objects.requireNonNull(capability);
		Objects.requireNonNull(isValid);
		Objects.requireNonNull(invalidationListener);
		pos = pos.immutable();

		var cache = new BlockCapabilityCache<>(capability, level, pos, context, isValid, invalidationListener);
		level.sophisticatedStorage_registerCapabilityListener(pos, cache.listener);
		return cache;
	}

	private final BlockCapability<T, C> capability;
	private final ServerLevel level;
	private final BlockPos pos;
	private final C context;

	private boolean cacheValid = false;
	private @Nullable T cachedCap = null;
	private boolean canQuery = true;
	private final ICapabilityInvalidationListener listener;

	private BlockCapabilityCache(BlockCapability<T, C> capability, ServerLevel level, BlockPos pos, C context,
			BooleanSupplier isValid, Runnable invalidationListener) {
		this.capability = capability;
		this.level = level;
		this.pos = pos;
		this.context = context;

		this.listener = () -> {
			if (!cacheValid) {
				return isValid.getAsBoolean();
			}

			canQuery = false;
			cacheValid = false;
			cachedCap = null;

			if (isValid.getAsBoolean()) {
				invalidationListener.run();
				canQuery = true;
				return true;
			} else {
				return false;
			}
		};
	}

	public ServerLevel level() {
		return level;
	}

	public BlockPos pos() {
		return pos;
	}

	public C context() {
		return context;
	}

	public @Nullable T getCapability() {
		if (!canQuery) {
			throw new IllegalStateException("Do not call getCapability on an invalid cache or from the invalidation listener!");
		}

		if (!cacheValid) {
			if (!level.isLoaded(pos)) {
				cachedCap = null;
			} else {
				cachedCap = level.sophisticatedCore_getCapability(capability, pos, context);
			}
			cacheValid = true;
		}

		return cachedCap;
	}
}
