package net.p3pp3rf1y.sophisticatedstorage.compat.sawmill;

import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.world.item.Item;
import net.p3pp3rf1y.sophisticatedcore.common.gui.UpgradeContainerRegistry;
import net.p3pp3rf1y.sophisticatedcore.common.gui.UpgradeContainerType;
import net.p3pp3rf1y.sophisticatedcore.compat.ICompat;
import net.p3pp3rf1y.sophisticatedcore.compat.sawmill.SawmillUpgradeContainer;
import net.p3pp3rf1y.sophisticatedcore.compat.sawmill.SawmillUpgradeItem;
import net.p3pp3rf1y.sophisticatedcore.fabric.registries.DeferredHolder;
import net.p3pp3rf1y.sophisticatedstorage.Config;
import net.p3pp3rf1y.sophisticatedstorage.init.ModItems;

public class SawmillCompat implements ICompat {

	public static final DeferredHolder<Item, SawmillUpgradeItem> SAWMILL_UPGRADE = ModItems.ITEMS.registerItem("sawmill/sawmill_upgrade",
			properties -> new SawmillUpgradeItem(Config.SERVER.maxUpgradesPerStorage, properties));

	@Override
	public void init() {
		// Loading this class in initCompats() queues SAWMILL_UPGRADE into ModItems.ITEMS before ModItems.registerHandlers().
	}

	@Override
	public void setup() {
		UpgradeContainerType<SawmillUpgradeItem.Wrapper, SawmillUpgradeContainer> containerType = new UpgradeContainerType<>(SawmillUpgradeContainer::new);
		UpgradeContainerRegistry.register(SAWMILL_UPGRADE.getId(), containerType);
		if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
			SawmillCompatClient.registerUpgradeTab(containerType);
		}
	}
}
