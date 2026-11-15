package net.p3pp3rf1y.sophisticatedstorage;

import fuzs.forgeconfigapiport.fabric.api.v5.ConfigRegistry;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerChunkEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resources.Identifier;
import net.neoforged.fml.config.ModConfig;
import net.p3pp3rf1y.sophisticatedcore.linkedstorage.LinkedStorageEndpointAdapters;
import net.p3pp3rf1y.sophisticatedcore.linkedstorage.LinkedStorageHostFactories;
import net.p3pp3rf1y.sophisticatedstorage.block.StorageItemLinkedStorageEndpointAdapter;
import net.p3pp3rf1y.sophisticatedstorage.block.StorageLinkedStorageHostWrapper;
import net.p3pp3rf1y.sophisticatedstorage.block.TankItemLinkedStorageEndpointAdapter;
import net.p3pp3rf1y.sophisticatedstorage.block.TankLinkedStorageHostWrapper;
import net.p3pp3rf1y.sophisticatedstorage.common.CommonEventHandler;
import net.p3pp3rf1y.sophisticatedstorage.init.ModBlocks;
import net.p3pp3rf1y.sophisticatedstorage.init.ModDataComponents;
import net.p3pp3rf1y.sophisticatedstorage.init.ModItems;
import net.p3pp3rf1y.sophisticatedstorage.init.ModParticles;
import net.p3pp3rf1y.sophisticatedstorage.init.ModPayloads;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Fabric common entrypoint (replaces NeoForge's {@code @Mod} constructor).
 */
public class SophisticatedStorage implements ModInitializer {
	public static final String MOD_ID = "sophisticatedstorage";
	public static final Logger LOGGER = LogManager.getLogger(MOD_ID);
	private static String networkProtocolVersion = "1";
	private final CommonEventHandler commonEventHandler = new CommonEventHandler();

	@Override
	public void onInitialize() {
		networkProtocolVersion = FabricLoader.getInstance().getModContainer(MOD_ID)
				.map(c -> c.getMetadata().getVersion().getFriendlyString())
				.orElse("1");
		ConfigRegistry.INSTANCE.register(MOD_ID, ModConfig.Type.SERVER, Config.SERVER_SPEC);
		ConfigRegistry.INSTANCE.register(MOD_ID, ModConfig.Type.CLIENT, Config.CLIENT_SPEC);
		ConfigRegistry.INSTANCE.register(MOD_ID, ModConfig.Type.COMMON, Config.COMMON_SPEC);
		Config.SERVER.initListeners();
		commonEventHandler.registerHandlers();
		ModDataComponents.register();
		ModBlocks.registerHandlers();
		ModItems.registerHandlers();
		ModParticles.registerParticles();
		ModPayloads.registerPayloads();
		LinkedStorageHostFactories.register(StorageLinkedStorageHostWrapper.FACTORY_ID, StorageLinkedStorageHostWrapper::create);
		LinkedStorageEndpointAdapters.register(new StorageItemLinkedStorageEndpointAdapter());
		LinkedStorageHostFactories.register(TankLinkedStorageHostWrapper.FACTORY_ID, TankLinkedStorageHostWrapper::create);
		LinkedStorageEndpointAdapters.register(new TankItemLinkedStorageEndpointAdapter());
		ModBlocks.registerDispenseBehavior();
		ModBlocks.registerCauldronInteractions();
		ServerChunkEvents.CHUNK_LOAD.register((serverLevel, chunk, generated) -> serverLevel.sophisticatedStorage_invalidateCapabilities(chunk.getPos()));
		ServerChunkEvents.CHUNK_UNLOAD.register((serverLevel, chunk) -> serverLevel.sophisticatedStorage_invalidateCapabilities(chunk.getPos()));
		ServerTickEvents.END_LEVEL_TICK.register(serverLevel -> serverLevel.sophisticatedStorage_cleanCapabilityListenerReferences());
	}

	public static Identifier getIdentifier(String regName) {
		return Identifier.parse(getRegistryName(regName));
	}

	public static String getRegistryName(String regName) {
		return MOD_ID + ":" + regName;
	}

	public static String getNetworkProtocolVersion() {
		return networkProtocolVersion;
	}
}
