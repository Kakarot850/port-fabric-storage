package net.p3pp3rf1y.sophisticatedstorage.mixin.common;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.p3pp3rf1y.sophisticatedstorage.capabilities.CapabilityListenerHolder;
import net.p3pp3rf1y.sophisticatedstorage.capabilities.ICapabilityInvalidationListener;
import net.p3pp3rf1y.sophisticatedstorage.extensions.level.SophisticatedStorageServerLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(ServerLevel.class)
public abstract class ServerLevelMixin implements SophisticatedStorageServerLevel {
	@Unique
	private final CapabilityListenerHolder sophisticatedStorage_capListenerHolder = new CapabilityListenerHolder();

	@Override
	public void sophisticatedStorage_invalidateCapabilities(BlockPos pos) {
		sophisticatedStorage_capListenerHolder.invalidatePos(pos);
	}

	@Override
	public void sophisticatedStorage_invalidateCapabilities(ChunkPos pos) {
		sophisticatedStorage_capListenerHolder.invalidateChunk(pos);
	}

	@Override
	public void sophisticatedStorage_registerCapabilityListener(BlockPos pos, ICapabilityInvalidationListener listener) {
		sophisticatedStorage_capListenerHolder.addListener(pos, listener);
	}

	@Override
	public void sophisticatedStorage_cleanCapabilityListenerReferences() {
		sophisticatedStorage_capListenerHolder.clean();
	}
}
