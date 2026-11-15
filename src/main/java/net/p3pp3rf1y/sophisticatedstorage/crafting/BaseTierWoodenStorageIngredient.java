package net.p3pp3rf1y.sophisticatedstorage.crafting;

import com.mojang.serialization.MapCodec;
import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredient;
import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredientSerializer;
import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.p3pp3rf1y.sophisticatedcore.util.BlockItemBase;
import net.p3pp3rf1y.sophisticatedstorage.SophisticatedStorage;
import net.p3pp3rf1y.sophisticatedstorage.init.ModBlocks;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class BaseTierWoodenStorageIngredient implements CustomIngredient {
	public static final BaseTierWoodenStorageIngredient INSTANCE = new BaseTierWoodenStorageIngredient();
	public static final MapCodec<BaseTierWoodenStorageIngredient> CODEC = MapCodec.unit(INSTANCE).stable();
	public static final StreamCodec<RegistryFriendlyByteBuf, BaseTierWoodenStorageIngredient> STREAM_CODEC = StreamCodec.unit(INSTANCE);
	public static final CustomIngredientSerializer<BaseTierWoodenStorageIngredient> SERIALIZER = new CustomIngredientSerializer<>() {
		private static final Identifier ID = SophisticatedStorage.getIdentifier("base_tier_wooden_storage");

		@Override
		public Identifier getIdentifier() {
			return ID;
		}

		@Override
		public MapCodec<BaseTierWoodenStorageIngredient> getCodec() {
			return CODEC;
		}

		@Override
		public StreamCodec<RegistryFriendlyByteBuf, BaseTierWoodenStorageIngredient> getStreamCodec() {
			return STREAM_CODEC;
		}
	};

	@Override
	public boolean test(@Nullable ItemStack stack) {
		return stack != null && stack.is(ModBlocks.BASE_TIER_WOODEN_STORAGE_TAG);
	}

	@Override
	public Stream<Holder<Item>> items() {
		return Stream.<Holder<Item>>of(ModBlocks.CHEST_ITEM.get().builtInRegistryHolder(), ModBlocks.BARREL_ITEM.get().builtInRegistryHolder());
	}

	@Override
	public SlotDisplay display() {
		List<ItemStack> items = new ArrayList<>();
		if (ModBlocks.CHEST_ITEM.get() instanceof BlockItemBase itemBase) {
			itemBase.addCreativeTabItems(items::add);
		}
		if (ModBlocks.BARREL_ITEM.get() instanceof BlockItemBase itemBase) {
			itemBase.addCreativeTabItems(items::add);
		}

		return new SlotDisplay.Composite(
				items.stream().map(ItemStackTemplate::fromNonEmptyStack).map(SlotDisplay.ItemStackSlotDisplay::new).map(SlotDisplay.class::cast).toList());
	}

	@Override
	public boolean requiresTesting() {
		return true;
	}

	@Override
	public CustomIngredientSerializer<?> getSerializer() {
		return SERIALIZER;
	}
}
