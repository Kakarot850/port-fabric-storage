package net.p3pp3rf1y.sophisticatedstorage.common;

import com.google.common.collect.Queues;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityLevelChangeEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.CommonLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.p3pp3rf1y.sophisticatedcore.fabric.network.PacketDistributor;
import net.p3pp3rf1y.sophisticatedcore.inventory.InventoryHandler;
import net.p3pp3rf1y.sophisticatedcore.network.SyncPlayerSettingsPayload;
import net.p3pp3rf1y.sophisticatedcore.settings.main.PlayerMainSettingsSavedData;
import net.p3pp3rf1y.sophisticatedcore.upgrades.infinity.InfinityUpgradeItem;
import net.p3pp3rf1y.sophisticatedcore.util.InventoryHelper;
import net.p3pp3rf1y.sophisticatedcore.util.ItemBase;
import net.p3pp3rf1y.sophisticatedcore.util.WorldHelper;
import net.p3pp3rf1y.sophisticatedstorage.Config;
import net.p3pp3rf1y.sophisticatedstorage.SophisticatedStorage;
import net.p3pp3rf1y.sophisticatedstorage.block.*;
import net.p3pp3rf1y.sophisticatedstorage.client.gui.StorageTranslationHelper;
import net.p3pp3rf1y.sophisticatedstorage.init.ModItems;
import net.p3pp3rf1y.sophisticatedstorage.util.GenericWoodStorageHelper;

import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicInteger;

public class CommonEventHandler {
	private static final int AVERAGE_MAX_ITEM_ENTITY_DROP_COUNT = 20;

	private final Queue<TickTask> pendingTickTasks = Queues.newConcurrentLinkedQueue();

	public void registerHandlers() {
		ServerEntityLevelChangeEvents.AFTER_PLAYER_CHANGE_LEVEL.register((player, origin, destination) -> sendPlayerSettingsToClient(player));
		ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> sendPlayerSettingsToClient(newPlayer));
		PlayerBlockBreakEvents.BEFORE.register(this::onBeforeBlockBreak);
		AttackBlockCallback.EVENT.register(this::onLimitedBarrelLeftClicked);
		UseBlockCallback.EVENT.register(this::onSneakItemBlockInteraction);
		ServerTickEvents.END_LEVEL_TICK.register(this::onLevelTick);
		CommonLifecycleEvents.TAGS_LOADED.register((registries, client) -> onTagsUpdated());
	}

	private void onTagsUpdated() {
		GenericWoodStorageHelper.invalidateCache();
	}

	private InteractionResult onLimitedBarrelLeftClicked(Player player, Level level, InteractionHand hand, BlockPos pos, Direction direction) {
		if (!player.isCreative()) {
			return InteractionResult.PASS;
		}

		BlockState state = level.getBlockState(pos);
		if (!(state.getBlock() instanceof LimitedBarrelBlock limitedBarrel)) {
			return InteractionResult.PASS;
		}
		if (limitedBarrel.tryToTakeItem(state, level, pos, player)) {
			return InteractionResult.SUCCESS;
		}
		return InteractionResult.PASS;
	}

	private InteractionResult onSneakItemBlockInteraction(Player player, Level level, InteractionHand hand, BlockHitResult hitResult) {
		if (!player.isShiftKeyDown() || player.isSpectator()) {
			return InteractionResult.PASS;
		}

		BlockPos pos = hitResult.getBlockPos();
		BlockState state = level.getBlockState(pos);
		if (!(state.getBlock() instanceof ISneakItemInteractionBlock sneakItemInteractionBlock)) {
			return InteractionResult.PASS;
		}
		if (sneakItemInteractionBlock.trySneakItemInteraction(player, hand, state, level, pos, hitResult, player.getItemInHand(hand))) {
			return InteractionResult.SUCCESS;
		}
		return InteractionResult.PASS;
	}

	private void sendPlayerSettingsToClient(Player player) {
		if (player instanceof ServerPlayer serverPlayer) {
			String name = SophisticatedStorage.MOD_ID;
			PacketDistributor.sendToPlayer(serverPlayer, new SyncPlayerSettingsPayload(name, PlayerMainSettingsSavedData.get().get(player.getUUID(), name)));
		}
	}

	private void onLevelTick(ServerLevel serverLevel) {
		if (pendingTickTasks.isEmpty()) {
			return;
		}

		Iterator<TickTask> it = pendingTickTasks.iterator();

		while (it.hasNext()) {
			TickTask tickTask = it.next();
			if (tickTask.getTick() <= serverLevel.getServer().getTickCount()) {
				tickTask.run();
				it.remove();
			}
		}
	}

	private boolean onBeforeBlockBreak(Level level, Player player, BlockPos pos, BlockState state, BlockEntity blockEntity) {
		return handleBreakStorageWithInfinityUpgrade(level, player, pos, state)
				&& handleTooManyDropsBreak(level, player, pos, state);
	}

	private boolean handleBreakStorageWithInfinityUpgrade(Level level, Player player, BlockPos pos, BlockState state) {
		if (!(state.getBlock() instanceof StorageBlockBase)) {
			return true;
		}

		StorageBlockEntity storageBlockEntity = WorldHelper.getBlockEntity(level, pos, StorageBlockEntity.class).orElse(null);
		if (storageBlockEntity != null && storageBlockEntity.getStorageWrapper().getUpgradeHandler().getTypeWrappers(InfinityUpgradeItem.TYPE).stream()
				.anyMatch(w -> !w.checkPermission(player))) {
			if (!level.isClientSide()) {
				player.sendOverlayMessage(
						StorageTranslationHelper.INSTANCE.translStatusMessage("infinity_upgrade_only_admin_break").withStyle(ChatFormatting.RED));
			}
			scheduleRenderUpdate(storageBlockEntity, level, pos, state);
			return false;
		}
		return true;
	}

	private boolean handleTooManyDropsBreak(Level level, Player player, BlockPos pos, BlockState state) {
		if (!(state.getBlock() instanceof WoodStorageBlockBase) || player.isShiftKeyDown()) {
			return true;
		}

		WoodStorageBlockEntity wbe = WorldHelper.getBlockEntity(level, pos, WoodStorageBlockEntity.class).orElse(null);
		if (wbe == null) {
			return true;
		}
		WoodStorageBlockEntity contentsOwner = wbe;
		if (wbe instanceof ChestBlockEntity chest) {
			ChestBlockEntity mainChest = chest.getMainChestBlockEntity();
			if (mainChest != null) {
				contentsOwner = mainChest;
			}
		}

		if (!contentsOwner.shouldDropContents() || Boolean.TRUE.equals(Config.COMMON.dropPacked.get())) {
			return true;
		}

		AtomicInteger droppedItemEntityCount = new AtomicInteger(0);

		int startCountingFromSlot;
		InventoryHandler inventoryHandler;
		if (wbe instanceof ChestBlockEntity cbe && !cbe.isMainChest() && level.getBlockState(pos).getBlock() instanceof ChestBlock chestBlock) {
			startCountingFromSlot = chestBlock.getNumberOfInventorySlots();
			inventoryHandler = cbe.getMainStorageWrapper().getInventoryHandler();
		} else {
			startCountingFromSlot = 0;
			inventoryHandler = wbe.getStorageWrapper().getInventoryHandler();
		}

		InventoryHelper.iterate(inventoryHandler, (slot, stack) -> {
			if (stack.isEmpty() || slot < startCountingFromSlot) {
				return;
			}
			droppedItemEntityCount
					.addAndGet((int) Math.ceil(stack.getCount() / (double) Math.min(stack.getMaxStackSize(), AVERAGE_MAX_ITEM_ENTITY_DROP_COUNT)));
		}, () -> false, false);

		if (droppedItemEntityCount.get() > Config.SERVER.tooManyItemEntityDrops.get()) {
			ItemBase packingTapeItem = ModItems.PACKING_TAPE.get();
			Component packingTapeItemName = packingTapeItem.getName(new ItemStack(packingTapeItem)).copy().withStyle(ChatFormatting.GREEN);
			if (player instanceof ServerPlayer serverPlayer) {
				serverPlayer.sendSystemMessage(StorageTranslationHelper.INSTANCE.translStatusMessage("too_many_item_entity_drops",
						state.getCloneItemStack(level, pos, true).getHoverName().copy().withStyle(ChatFormatting.GREEN),
						Component.literal(String.valueOf(droppedItemEntityCount.get())).withStyle(ChatFormatting.RED), packingTapeItemName));
			}
			scheduleRenderUpdate(wbe, level, pos, state);
			return false;
		}
		return true;
	}

	private void scheduleRenderUpdate(StorageBlockEntity storageBe, LevelAccessor level, BlockPos pos, BlockState state) {
		if (level instanceof ServerLevel serverLevel) {
			serverLevel.scheduleTick(pos, state.getBlock(), 2);
			pendingTickTasks.add(new TickTask(serverLevel.getServer().getTickCount() + 2, () -> {
				storageBe.setUpdateBlockRender();
				WorldHelper.notifyBlockUpdate(storageBe);
			}));
		}
	}
}
