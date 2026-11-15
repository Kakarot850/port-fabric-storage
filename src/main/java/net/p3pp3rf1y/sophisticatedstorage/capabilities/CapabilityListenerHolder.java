package net.p3pp3rf1y.sophisticatedstorage.capabilities;

import it.unimi.dsi.fastutil.longs.Long2ReferenceMap;
import it.unimi.dsi.fastutil.longs.Long2ReferenceOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.Set;

/**
 * Stand-in for NeoForge's {@code CapabilityListenerHolder}: tracks weak invalidation listeners grouped by chunk
 * and block position for a {@code ServerLevel}.
 */
public class CapabilityListenerHolder {
	private final Long2ReferenceMap<Long2ReferenceMap<Set<ListenerReference>>> byChunkThenBlock = new Long2ReferenceOpenHashMap<>();
	private final ReferenceQueue<ICapabilityInvalidationListener> queue = new ReferenceQueue<>();

	public void addListener(BlockPos pos, ICapabilityInvalidationListener listener) {
		pos = pos.immutable();
		var chunkHolder = byChunkThenBlock.computeIfAbsent(ChunkPos.pack(pos), l -> new Long2ReferenceOpenHashMap<>());
		var listenersSet = chunkHolder.computeIfAbsent(pos.asLong(), l -> new ObjectOpenHashSet<>());

		var reference = new ListenerReference(queue, pos, listener);
		if (!listenersSet.add(reference)) {
			reference.clear();
		}
	}

	public void invalidatePos(BlockPos pos) {
		var chunkHolder = byChunkThenBlock.get(ChunkPos.pack(pos));
		if (chunkHolder != null) {
			var caches = chunkHolder.get(pos.asLong());
			if (caches != null) {
				invalidateList(caches);
			}
		}
	}

	public void invalidateChunk(ChunkPos chunkPos) {
		var chunkHolder = byChunkThenBlock.get(chunkPos.pack());
		if (chunkHolder != null) {
			for (var caches : chunkHolder.values()) {
				invalidateList(caches);
			}
		}
	}

	private void invalidateList(Set<ListenerReference> caches) {
		caches.removeIf(ref -> {
			var listener = ref.get();
			return listener == null || !listener.onInvalidate();
		});
	}

	public void clean() {
		while (true) {
			ListenerReference ref = (ListenerReference) queue.poll();
			if (ref == null) {
				return;
			}

			var chunkHolder = byChunkThenBlock.get(ChunkPos.pack(ref.pos));
			if (chunkHolder == null) {
				continue;
			}

			var set = chunkHolder.get(ref.pos.asLong());
			if (set == null) {
				continue;
			}
			boolean removed = set.remove(ref);

			if (removed && set.isEmpty()) {
				chunkHolder.remove(ref.pos.asLong());
				if (chunkHolder.isEmpty()) {
					byChunkThenBlock.remove(ChunkPos.pack(ref.pos));
				}
			}
		}
	}

	private static class ListenerReference extends WeakReference<ICapabilityInvalidationListener> {
		private final BlockPos pos;
		private final int listenerHashCode;

		private ListenerReference(ReferenceQueue<ICapabilityInvalidationListener> queue, BlockPos pos, ICapabilityInvalidationListener listener) {
			super(listener, queue);
			this.pos = pos;
			this.listenerHashCode = System.identityHashCode(listener);
		}

		@Override
		public boolean equals(Object obj) {
			if (obj instanceof ListenerReference otherRef) {
				return otherRef.listenerHashCode == listenerHashCode && otherRef.get() == get();
			}
			return false;
		}

		@Override
		public int hashCode() {
			return listenerHashCode;
		}
	}
}
