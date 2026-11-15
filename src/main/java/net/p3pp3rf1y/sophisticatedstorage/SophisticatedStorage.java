package net.p3pp3rf1y.sophisticatedstorage;

import fuzs.forgeconfigapiport.fabric.api.v5.ConfigRegistry;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerChunkEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resources.Identifier;
import net.neoforged.fml.config.ModConfig;
import net.p3pp3rf1y.sophisticatedstorage.init.ModBlocks;
import net.p3pp3rf1y.sophisticatedstorage.init.ModDataComponents;
import net.p3pp3rf1y.sophisticatedstorage.init.ModItems;
import net.p3pp3rf1y.sophisticatedstorage.init.ModParticles;
import net.p3pp3rf1y.sophisticatedstorage.init.ModPayloads;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Fabric common entrypoint (replaces NeoForge's {@code @Mod} constructor).
 * Registries, events, and networking are wired in as their subsystems are ported.
 */
public class SophisticatedStorage implements ModInitializer {
	public static final String MOD_ID = "sophisticatedstorage";
	public static final Logger LOGGER = LogManager.getLogger(MOD_ID);
	private static String networkProtocolVersion = "1";

	@Override
	public void onInitialize() {
		networkProtocolVersion = FabricLoader.getInstance().getModContainer(MOD_ID)
				.map(c -> c.getMetadata().getVersion().getFriendlyString())
				.orElse("1");
		ConfigRegistry.INSTANCE.register(MOD_ID, ModConfig.Type.SERVER, Config.SERVER_SPEC);
		ConfigRegistry.INSTANCE.register(MOD_ID, ModConfig.Type.CLIENT, Config.CLIENT_SPEC);
		ConfigRegistry.INSTANCE.register(MOD_ID, ModConfig.Type.COMMON, Config.COMMON_SPEC);
		Config.SERVER.initListeners();
		ModDataComponents.register();
		ModBlocks.registerHandlers();
		ModItems.registerHandlers();
		ModParticles.registerParticles();
		ModPayloads.registerPayloads();
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
