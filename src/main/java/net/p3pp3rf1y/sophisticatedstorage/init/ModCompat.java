package net.p3pp3rf1y.sophisticatedstorage.init;

import net.p3pp3rf1y.sophisticatedcore.compat.CompatInfo;
import net.p3pp3rf1y.sophisticatedcore.compat.CompatModIds;
import net.p3pp3rf1y.sophisticatedcore.compat.CompatRegistry;
import net.p3pp3rf1y.sophisticatedstorage.SophisticatedStorage;
import net.p3pp3rf1y.sophisticatedstorage.compat.sawmill.SawmillCompat;
import net.p3pp3rf1y.sophisticatedstorage.compat.tomsstorage.TomsStorageCompat;
import net.p3pp3rf1y.sophisticatedstorage.compat.trashslot.TrashSlotCompat;

public class ModCompat {
	public static final String SAWMILL_MOD_ID = "sawmill";
	public static final String TOMS_STORAGE = "toms_storage";

	private ModCompat() {
	}

	public static void register() {
		CompatRegistry registry = CompatRegistry.getRegistry(SophisticatedStorage.MOD_ID);
		// Skipped on 26.1.2 Fabric: quark (NeoForge-only), sophisticatedbackpacks (not yet ported), chipped (excluded upstream).
		// Extra Supplier<Supplier<ICompat>> indirection prevents JVM from resolving optional compat class method handles when unloaded.
		registry.registerCompat(new CompatInfo(SAWMILL_MOD_ID), () -> SawmillCompat::new);
		registry.registerCompat(new CompatInfo(CompatModIds.TRASH_SLOT), () -> TrashSlotCompat::new);
		registry.registerCompat(new CompatInfo(TOMS_STORAGE, CompatRegistry.fromSpec(">=2.2.0")), () -> TomsStorageCompat::new);
	}
}
