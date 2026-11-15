package net.p3pp3rf1y.sophisticatedstorage.extensions.level;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.p3pp3rf1y.sophisticatedstorage.capabilities.ICapabilityInvalidationListener;

/**
 * Injected onto {@link net.minecraft.server.level.ServerLevel} via Loom interface injection.
 */
public interface SophisticatedStorageServerLevel {
	default void sophisticatedStorage_invalidateCapabilities(BlockPos pos) {
	}

	default void sophisticatedStorage_invalidateCapabilities(ChunkPos pos) {
	}

	default void sophisticatedStorage_registerCapabilityListener(BlockPos pos, ICapabilityInvalidationListener listener) {
	}

	default void sophisticatedStorage_cleanCapabilityListenerReferences() {
	}
}
