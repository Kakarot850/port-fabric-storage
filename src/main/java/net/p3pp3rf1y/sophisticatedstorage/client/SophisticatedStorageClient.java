package net.p3pp3rf1y.sophisticatedstorage.client;

import fuzs.forgeconfigapiport.fabric.api.v5.client.ConfigScreenFactoryRegistry;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.p3pp3rf1y.sophisticatedstorage.SophisticatedStorage;

/**
 * Fabric client entrypoint (replaces NeoForge's {@code Dist.CLIENT} branch of the {@code @Mod} constructor).
 */
public class SophisticatedStorageClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		if (!FabricLoader.getInstance().isModLoaded("configured")) {
			ConfigScreenFactoryRegistry.INSTANCE.register(SophisticatedStorage.MOD_ID, ConfigurationScreen::new);
		}
		ClientEventHandler.registerHandlers();
	}
}
