package net.p3pp3rf1y.sophisticatedstorage.extensions.block.entity;

/**
 * Injected onto {@link net.minecraft.world.level.block.entity.BlockEntity} via Loom interface injection.
 */
public interface SophisticatedStorageBlockEntity {
	default void sophisticatedStorage_invalidateCapabilities() {
	}
}
