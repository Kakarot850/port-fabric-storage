package net.p3pp3rf1y.sophisticatedstorage.common.gui;

import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.menu.v1.ExtendedMenuProvider;
import net.fabricmc.fabric.api.menu.v1.ExtendedMenuType;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.p3pp3rf1y.sophisticatedcore.common.gui.SophisticatedMenuProvider;
import org.jspecify.annotations.Nullable;

import java.util.OptionalInt;
import java.util.function.Consumer;

public class StorageMenuHelper {
	private StorageMenuHelper() {
	}

	@FunctionalInterface
	public interface MenuFactory<T extends AbstractContainerMenu> {
		T create(int syncId, Inventory playerInventory, RegistryFriendlyByteBuf buffer);
	}

	public record MenuOpeningData(byte[] bytes) {
		public static final StreamCodec<RegistryFriendlyByteBuf, MenuOpeningData> STREAM_CODEC = StreamCodec.of(
				(buf, data) -> buf.writeByteArray(data.bytes()),
				buf -> new MenuOpeningData(buf.readByteArray())
		);
	}

	public static <T extends AbstractContainerMenu> ExtendedMenuType<T, MenuOpeningData> createMenuType(MenuFactory<T> factory) {
		return new ExtendedMenuType<>((syncId, playerInventory, data) -> {
			RegistryFriendlyByteBuf buffer = new RegistryFriendlyByteBuf(Unpooled.wrappedBuffer(data.bytes()), playerInventory.player.registryAccess());
			try {
				return factory.create(syncId, playerInventory, buffer);
			} finally {
				buffer.release();
			}
		}, MenuOpeningData.STREAM_CODEC);
	}

	public static OptionalInt openMenu(Player player, MenuProvider provider, BlockPos pos) {
		return openMenu(player, provider, buffer -> buffer.writeBlockPos(pos));
	}

	public static OptionalInt openMenu(Player player, MenuProvider provider, Consumer<FriendlyByteBuf> dataWriter) {
		if (!(player instanceof ServerPlayer serverPlayer)) {
			return OptionalInt.empty();
		}

		RegistryFriendlyByteBuf buffer = new RegistryFriendlyByteBuf(Unpooled.buffer(), serverPlayer.registryAccess());
		byte[] bytes;
		try {
			dataWriter.accept(buffer);
			bytes = new byte[buffer.readableBytes()];
			buffer.readBytes(bytes);
		} finally {
			buffer.release();
		}

		MenuOpeningData openingData = new MenuOpeningData(bytes);
		return serverPlayer.openMenu(new ExtendedMenuProvider<>() {
			@Override
			public MenuOpeningData getScreenOpeningData(ServerPlayer opener) {
				return openingData;
			}

			@Override
			public Component getDisplayName() {
				return provider.getDisplayName();
			}

			@Nullable
			@Override
			public AbstractContainerMenu createMenu(int syncId, Inventory playerInventory, Player opener) {
				return provider.createMenu(syncId, playerInventory, opener);
			}

			@Override
			public boolean shouldCloseCurrentScreen() {
				return !(provider instanceof SophisticatedMenuProvider smp) || smp.shouldTriggerClientSideContainerClosingOnOpen();
			}
		});
	}
}
