package net.p3pp3rf1y.sophisticatedstorage.client;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLevelEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.model.loading.v1.CustomUnbakedBlockStateModel;
import net.fabricmc.fabric.api.client.model.loading.v1.UnbakedModelDeserializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.ClientTooltipComponentCallback;
import net.fabricmc.fabric.api.client.rendering.v1.ModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.PictureInPictureRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.fabricmc.fabric.api.resource.v1.reloader.ResourceReloaderKeys;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.item.ItemModels;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.special.SpecialModelRenderers;
import net.minecraft.client.renderer.state.level.BlockOutlineRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.util.ARGB;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.p3pp3rf1y.sophisticatedcore.client.render.BlockHighlightRenderHelper;
import net.p3pp3rf1y.sophisticatedcore.fabric.client.network.ClientPacketDistributor;
import net.p3pp3rf1y.sophisticatedcore.fabric.client.network.ClientPayloadHandlers;
import net.p3pp3rf1y.sophisticatedcore.util.VoxelOutliner;
import net.p3pp3rf1y.sophisticatedstorage.SophisticatedStorage;
import net.p3pp3rf1y.sophisticatedstorage.block.ISimpleMaterialHolder;
import net.p3pp3rf1y.sophisticatedstorage.block.LimitedBarrelBlock;
import net.p3pp3rf1y.sophisticatedstorage.block.StorageBlockBase;
import net.p3pp3rf1y.sophisticatedstorage.client.gui.PaintbrushOverlay;
import net.p3pp3rf1y.sophisticatedstorage.client.gui.ToolInfoOverlay;
import net.p3pp3rf1y.sophisticatedstorage.client.init.ModBlockColors;
import net.p3pp3rf1y.sophisticatedstorage.client.init.ModParticles;
import net.p3pp3rf1y.sophisticatedstorage.client.init.StorageTintSources;
import net.p3pp3rf1y.sophisticatedstorage.client.render.*;
import net.p3pp3rf1y.sophisticatedstorage.init.ModBlocks;
import net.p3pp3rf1y.sophisticatedstorage.init.ModBlocksClient;
import net.p3pp3rf1y.sophisticatedstorage.init.ModItems;
import net.p3pp3rf1y.sophisticatedstorage.init.ModItemsClient;
import net.p3pp3rf1y.sophisticatedstorage.item.ChestBlockItem;
import net.p3pp3rf1y.sophisticatedstorage.item.LinkedStorageTooltip;
import net.p3pp3rf1y.sophisticatedstorage.item.PaintbrushItem;
import net.p3pp3rf1y.sophisticatedstorage.item.StorageContentsTooltip;
import net.p3pp3rf1y.sophisticatedstorage.item.TankBlockItem;
import net.p3pp3rf1y.sophisticatedstorage.network.RequestPlayerSettingsPayload;
import net.p3pp3rf1y.sophisticatedstorage.network.ScrolledToolPayload;
import net.p3pp3rf1y.sophisticatedstorage.util.GenericWoodStorageHelper;

public class ClientEventHandler {
	private ClientEventHandler() {
	}

	private static final Identifier CHEST_RL = Identifier.fromNamespaceAndPath(SophisticatedStorage.MOD_ID, "chest");
	private static final Identifier CHEST_LEFT_RL = Identifier.fromNamespaceAndPath(SophisticatedStorage.MOD_ID, "chest_left");
	private static final Identifier CHEST_RIGHT_RL = Identifier.fromNamespaceAndPath(SophisticatedStorage.MOD_ID, "chest_right");
	public static final ModelLayerLocation CHEST_LAYER = new ModelLayerLocation(CHEST_RL, "main");
	public static final ModelLayerLocation CHEST_LEFT_LAYER = new ModelLayerLocation(CHEST_LEFT_RL, "main");
	public static final ModelLayerLocation CHEST_RIGHT_LAYER = new ModelLayerLocation(CHEST_RIGHT_RL, "main");
	public static final ModelLayerLocation CHEST_LOCK_LAYER = new ModelLayerLocation(CHEST_RL, "lock");
	public static final ModelLayerLocation CHEST_LOCK_LEFT_LAYER = new ModelLayerLocation(CHEST_LEFT_RL, "lock");
	public static final ModelLayerLocation CHEST_LOCK_RIGHT_LAYER = new ModelLayerLocation(CHEST_RIGHT_RL, "lock");
	private static final int PAINTBRUSH_CAN_APPLY_HIGHLIGHT_COLOR = 0x69c53b;
	private static final int PAINTBRUSH_MISSING_ITEMS_HIGHLIGHT_COLOR = 0xc53b3b;

	public static void registerHandlers() {
		ModBlocksClient.init();
		ModItemsClient.init();
		ModParticles.registerProviders();
		StorageTintSources.register();
		ModBlockColors.registerBlockColorHandlers();
		ClientPayloadHandlers.register(SophisticatedStorage.MOD_ID);

		registerModelLoaders();
		registerBlockStateModels();
		registerBarrelItemModel();
		registerSpecialModelRenderers();
		registerLayerDefinitions();
		registerEntityRenderers();
		registerPictureInPictureRenderers();
		registerTooltipComponents();
		registerOverlays();
		registerReloadListeners();

		ClientLevelEvents.AFTER_CLIENT_LEVEL_CHANGE.register((client, level) -> ClientStorageContentsTooltip.onWorldLoad());
		ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> ClientStorageContentsTooltip.onWorldLoad());
		ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> ClientPacketDistributor.sendToServer(new RequestPlayerSettingsPayload()));
		ClientTickEvents.START_CLIENT_TICK.register(client -> ControllerTargetHighlighter.highlightTargets());
		AttackBlockCallback.EVENT.register(ClientEventHandler::onLimitedBarrelClicked);
		LevelRenderEvents.BEFORE_BLOCK_OUTLINE.register(ClientEventHandler::onBeforeBlockOutline);
	}

	private static void registerPictureInPictureRenderers() {
		PictureInPictureRendererRegistry.register(ctx -> new DecorationTablePreviewRenderer());
		PictureInPictureRendererRegistry.register(ctx -> new StorageBlockPreviewPipRenderer());
	}

	private static void registerBlockStateModels() {
		CustomUnbakedBlockStateModel.register(BarrelUnbakedModelBase.UnbakedBlockStateModel.ID, BarrelUnbakedModelBase.UnbakedBlockStateModel.CODEC);
		CustomUnbakedBlockStateModel.register(ChestBlockStateModel.Unbaked.ID, ChestBlockStateModel.Unbaked.CODEC);
		CustomUnbakedBlockStateModel.register(ShulkerBoxBlockStateModel.Unbaked.ID, ShulkerBoxBlockStateModel.Unbaked.CODEC);
		CustomUnbakedBlockStateModel.register(SimpleMaterialModel.UnbakedBlockStateModel.ID, SimpleMaterialModel.UnbakedBlockStateModel.CODEC);
	}

	private static void registerSpecialModelRenderers() {
		SpecialModelRenderers.ID_MAPPER.put(SophisticatedStorage.getIdentifier("tank"), TankItemRenderer.Unbaked.MAP_CODEC);
		SpecialModelRenderers.ID_MAPPER.put(ModBlocks.CHEST_ITEM.getId(), ChestItemRenderer.Unbaked.MAP_CODEC);
		SpecialModelRenderers.ID_MAPPER.put(ModBlocks.SHULKER_BOX_ITEM.getId(), ShulkerBoxItemRenderer.Unbaked.MAP_CODEC);
	}

	private static boolean onBeforeBlockOutline(LevelRenderContext context, BlockOutlineRenderState outlineRenderState) {
		Minecraft minecraft = Minecraft.getInstance();
		LocalPlayer player = minecraft.player;
		if (player == null || minecraft.gui.screen() != null || !(minecraft.hitResult instanceof BlockHitResult hitresult)) {
			return true;
		}

		ItemStack stack = player.getMainHandItem();
		Entity cameraEntity = minecraft.getCameraEntity();
		CollisionContext collisionContext = CollisionContext.of(cameraEntity != null ? cameraEntity : player);
		if (stack.getItem() instanceof ChestBlockItem && ChestBlockItem.isDoubleChest(stack)) {
			BlockPos otherPos = hitresult.getBlockPos().relative(player.getDirection().getClockWise());
			Level level = player.level();
			BlockState blockState = level.getBlockState(otherPos);
			if (!blockState.isAir() && level.getWorldBorder().isWithinBounds(otherPos)) {
				Vec3 cameraPos = context.levelState().cameraRenderState.pos;
				context.poseStack().pushPose();
				context.poseStack().translate(otherPos.getX() - cameraPos.x, otherPos.getY() - cameraPos.y, otherPos.getZ() - cameraPos.z);
				context.submitNodeCollector().submitShapeOutline(context.poseStack(), blockState.getShape(level, otherPos, collisionContext), RenderTypes.lines(),
						ARGB.colorFromFloat(0.4F, 0, 0, 0), minecraft.getWindow().getAppropriateLineWidth(), outlineRenderState.isTranslucent());
				context.poseStack().popPose();
			}
		}

		if (stack.getItem() instanceof PaintbrushItem) {
			Level level = player.level();
			BlockPos pos = hitresult.getBlockPos();
			BlockState blockState = level.getBlockState(pos);

			if (blockState.getBlock() instanceof StorageBlockBase || blockState.getBlock() == ModBlocks.CONTROLLER.get()
					|| level.getBlockEntity(pos) instanceof ISimpleMaterialHolder) {
				var itemRequirementsOpt = PaintbrushOverlay.getItemRequirementsFor(stack, player, level, pos);
				if (itemRequirementsOpt.isPresent()) {
					var itemRequirements = itemRequirementsOpt.get();
					int color = itemRequirements.itemsMissing().isEmpty() ? PAINTBRUSH_CAN_APPLY_HIGHLIGHT_COLOR : PAINTBRUSH_MISSING_ITEMS_HIGHLIGHT_COLOR;
					Vec3 cameraPos = context.levelState().cameraRenderState.pos;
					context.poseStack().pushPose();
					context.poseStack().translate(pos.getX() - cameraPos.x, pos.getY() - cameraPos.y, pos.getZ() - cameraPos.z);
					BlockHighlightRenderHelper.submitThickEdges(context.submitNodeCollector(), context.poseStack(), color,
							VoxelOutliner.linesFromVoxelShapeSimplified(blockState.getShape(level, pos, collisionContext), pos), pos);
					context.poseStack().popPose();
					return false;
				}
			}
		}

		return true;
	}

	public static boolean onMouseScrolled(double scrollDeltaY) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.gui.screen() != null) {
			return false;
		}
		LocalPlayer player = mc.player;
		if (player == null || !player.isShiftKeyDown()) {
			return false;
		}
		ItemStack stack = player.getMainHandItem();
		if (stack.getItem() != ModItems.STORAGE_TOOL.get() && !(stack.getItem() instanceof TankBlockItem)) {
			return false;
		}
		ClientPacketDistributor.sendToServer(new ScrolledToolPayload(scrollDeltaY > 0));
		return true;
	}

	private static InteractionResult onLimitedBarrelClicked(Player player, Level level, InteractionHand hand, BlockPos pos, Direction direction) {
		if (!level.isClientSide()) {
			return InteractionResult.PASS;
		}

		BlockState state = level.getBlockState(pos);
		if (!(state.getBlock() instanceof LimitedBarrelBlock limitedBarrel)) {
			return InteractionResult.PASS;
		}
		if (limitedBarrel.isLookingAtFront(player, pos, state)) {
			if (player.isCreative()) {
				return InteractionResult.FAIL;
			} else if (player.getDestroySpeed(state) < 2 && Minecraft.getInstance().gameMode != null) {
				Minecraft.getInstance().gameMode.destroyDelay = 5;
			}
		}
		return InteractionResult.PASS;
	}

	private static void registerReloadListeners() {
		ResourceLoader clientResources = ResourceLoader.get(PackType.CLIENT_RESOURCES);
		Identifier chestTextureManagerId = SophisticatedStorage.getIdentifier("chest_texture_manager");
		clientResources.registerReloadListener(chestTextureManagerId, StorageTextureManager.INSTANCE);
		clientResources.addListenerOrdering(chestTextureManagerId, ResourceReloaderKeys.Client.MODELS);
		clientResources.addListenerOrdering(chestTextureManagerId, ResourceReloaderKeys.Client.BLOCK_ENTITY_RENDER_DISPATCHER);

		Identifier barrelCacheInvalidationId = SophisticatedStorage.getIdentifier("barrel_cache_invalidation");
		clientResources.registerReloadListener(barrelCacheInvalidationId, (ResourceManagerReloadListener) ClientEventHandler::invalidateBarrelCache);
		clientResources.addListenerOrdering(barrelCacheInvalidationId, ResourceReloaderKeys.Client.MODELS);
	}

	private static void registerModelLoaders() {
		UnbakedModelDeserializer.register(Identifier.fromNamespaceAndPath(SophisticatedStorage.MOD_ID, "barrel"), BarrelUnbakedModel.Loader.INSTANCE);
		UnbakedModelDeserializer.register(Identifier.fromNamespaceAndPath(SophisticatedStorage.MOD_ID, "limited_barrel"), LimitedBarrelUnbakedModel.Loader.INSTANCE);
		UnbakedModelDeserializer.register(Identifier.fromNamespaceAndPath(SophisticatedStorage.MOD_ID, "simple_composite"), SimpleCompositeUnbakedModel.Loader.INSTANCE);
		UnbakedModelDeserializer.register(Identifier.fromNamespaceAndPath(SophisticatedStorage.MOD_ID, "simple_material"), SimpleMaterialModel.Loader.INSTANCE);
	}

	private static void invalidateBarrelCache(ResourceManager resourceManager) {
		GenericWoodStorageHelper.invalidateCache();
		BarrelUnbakedModelBase.invalidateCache();
		BarrelBlockStateModelBase.invalidateCache();
	}

	private static void registerLayerDefinitions() {
		ModelLayerRegistry.registerModelLayer(CHEST_LAYER, ChestRenderer::createSingleBodyLayer);
		ModelLayerRegistry.registerModelLayer(CHEST_LEFT_LAYER, ChestRenderer::createDoubleBodyLeftLayer);
		ModelLayerRegistry.registerModelLayer(CHEST_RIGHT_LAYER, ChestRenderer::createDoubleBodyRightLayer);
		ModelLayerRegistry.registerModelLayer(CHEST_LOCK_LAYER, ChestRenderer::createSingleLockLayer);
		ModelLayerRegistry.registerModelLayer(CHEST_LOCK_LEFT_LAYER, ChestRenderer::createDoubleLockLeftLayer);
		ModelLayerRegistry.registerModelLayer(CHEST_LOCK_RIGHT_LAYER, ChestRenderer::createDoubleLockRightLayer);
	}

	private static void registerTooltipComponents() {
		ClientTooltipComponentCallback.EVENT.register(component -> {
			if (component instanceof LinkedStorageTooltip linkedStorageTooltip) {
				return new ClientLinkedStorageTooltip(linkedStorageTooltip);
			}
			if (component instanceof StorageContentsTooltip storageContentsTooltip) {
				return new ClientStorageContentsTooltip(storageContentsTooltip);
			}
			return null;
		});
	}

	private static void registerOverlays() {
		HudElementRegistry.attachElementAfter(VanillaHudElements.HOTBAR,
				Identifier.fromNamespaceAndPath(SophisticatedStorage.MOD_ID, "storage_tool_info"), ToolInfoOverlay.HUD_TOOL_INFO);
		HudElementRegistry.attachElementAfter(VanillaHudElements.HOTBAR,
				Identifier.fromNamespaceAndPath(SophisticatedStorage.MOD_ID, "paintbrush_info"), PaintbrushOverlay.HUD_PAINTBRUSH_INFO);
	}

	private static void registerEntityRenderers() {
		BlockEntityRenderers.register(ModBlocks.TANK_BLOCK_ENTITY_TYPE.get(), TankRenderer::new);
		BlockEntityRenderers.register(ModBlocks.BARREL_BLOCK_ENTITY_TYPE.get(), BarrelRenderer::new);
		BlockEntityRenderers.register(ModBlocks.LIMITED_BARREL_BLOCK_ENTITY_TYPE.get(), LimitedBarrelRenderer::new);
		BlockEntityRenderers.register(ModBlocks.CHEST_BLOCK_ENTITY_TYPE.get(), ChestRenderer::new);
		BlockEntityRenderers.register(ModBlocks.SHULKER_BOX_BLOCK_ENTITY_TYPE.get(), ShulkerBoxRenderer::new);
		BlockEntityRenderers.register(ModBlocks.CONTROLLER_BLOCK_ENTITY_TYPE.get(), context -> new ControllerRenderer());
		BlockEntityRenderers.register(ModBlocks.STORAGE_LINK_BLOCK_ENTITY_TYPE.get(), context -> new SimpleMaterialOverlayRenderer<>());
		BlockEntityRenderers.register(ModBlocks.STORAGE_IO_BLOCK_ENTITY_TYPE.get(), context -> new SimpleMaterialOverlayRenderer<>());
		BlockEntityRenderers.register(ModBlocks.STORAGE_INPUT_BLOCK_ENTITY_TYPE.get(), context -> new SimpleMaterialOverlayRenderer<>());
		BlockEntityRenderers.register(ModBlocks.STORAGE_OUTPUT_BLOCK_ENTITY_TYPE.get(), context -> new SimpleMaterialOverlayRenderer<>());
		BlockEntityRenderers.register(ModBlocks.STORAGE_CONNECTOR_BLOCK_ENTITY_TYPE.get(), context -> new SimpleMaterialOverlayRenderer<>());
		BlockEntityRenderers.register(ModBlocks.DECORATION_TABLE_BLOCK_ENTITY_TYPE.get(), DecorationTableRenderer::new);
	}

	private static void registerBarrelItemModel() {
		ItemModels.ID_MAPPER.put(SophisticatedStorage.getIdentifier("barrel"), BarrelItemModel.Unbaked.MAP_CODEC);
		ItemModels.ID_MAPPER.put(SophisticatedStorage.getIdentifier("simple_material"), SimpleMaterialModel.SimpleMaterialItemModel.Unbaked.MAP_CODEC);
	}
}
