package net.p3pp3rf1y.sophisticatedstorage.init;

import net.minecraft.client.gui.screens.MenuScreens;
import net.p3pp3rf1y.sophisticatedstorage.client.gui.*;

public class ModBlocksClient {
	private ModBlocksClient() {
	}

	public static void init() {
		registerMenuScreens();
	}

	private static void registerMenuScreens() {
		MenuScreens.register(ModBlocks.STORAGE_CONTAINER_TYPE.get(), StorageScreen::constructScreen);
		MenuScreens.register(ModBlocks.TANK_CONTAINER_TYPE.get(), TankScreen::new);
		MenuScreens.register(ModBlocks.SETTINGS_CONTAINER_TYPE.get(), StorageSettingsScreen::constructScreen);
		MenuScreens.register(ModBlocks.LIMITED_BARREL_CONTAINER_TYPE.get(), LimitedBarrelScreen::new);
		MenuScreens.register(ModBlocks.LIMITED_BARREL_SETTINGS_CONTAINER_TYPE.get(), LimitedBarrelSettingsScreen::new);
		MenuScreens.register(ModBlocks.DECORATION_TABLE_CONTAINER_TYPE.get(), DecorationTableScreen::new);
	}
}
