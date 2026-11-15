package net.p3pp3rf1y.sophisticatedstorage.init;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.p3pp3rf1y.sophisticatedcore.upgrades.FilterAttributes;
import net.p3pp3rf1y.sophisticatedcore.util.CodecHelper;
import net.p3pp3rf1y.sophisticatedstorage.SophisticatedStorage;
import net.p3pp3rf1y.sophisticatedstorage.block.BarrelMaterial;
import net.p3pp3rf1y.sophisticatedstorage.block.LimitedBarrelBlockEntity;
import net.p3pp3rf1y.sophisticatedstorage.item.BarrelBlockItem;
import net.p3pp3rf1y.sophisticatedstorage.item.PaintbrushItem;
import net.p3pp3rf1y.sophisticatedstorage.item.StorageToolItem;
import net.p3pp3rf1y.sophisticatedstorage.item.TankBlockItem;
import net.p3pp3rf1y.sophisticatedstorage.util.StreamCodecs;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

public class ModDataComponents {
	private ModDataComponents() {
	}

	private static <T> DataComponentType<T> register(String name, Supplier<DataComponentType<T>> factory) {
		return Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE, SophisticatedStorage.getIdentifier(name), factory.get());
	}

	public static final DataComponentType<Integer> TANK_HEIGHT = register("tank_height",
			() -> new DataComponentType.Builder<Integer>().persistent(Codec.intRange(1, 5)).networkSynchronized(ByteBufCodecs.VAR_INT).build());
	public static final DataComponentType<TankBlockItem.Mode> TANK_ITEM_MODE = register("tank_item_mode",
			() -> new DataComponentType.Builder<TankBlockItem.Mode>().persistent(TankBlockItem.Mode.CODEC).networkSynchronized(TankBlockItem.Mode.STREAM_CODEC)
					.build());
	public static final DataComponentType<ItemContainerContents> TANK_UPGRADES = register("tank_upgrades",
			() -> new DataComponentType.Builder<ItemContainerContents>().persistent(CodecHelper.LENIENT_ITEM_CONTAINER_CONTENTS_CODEC)
					.networkSynchronized(ItemContainerContents.STREAM_CODEC).cacheEncoding().build());
	public static final DataComponentType<ItemContainerContents> TANK_CONTAINERS = register("tank_containers",
			() -> new DataComponentType.Builder<ItemContainerContents>().persistent(CodecHelper.LENIENT_ITEM_CONTAINER_CONTENTS_CODEC)
					.networkSynchronized(ItemContainerContents.STREAM_CODEC).cacheEncoding().build());
	public static final DataComponentType<StorageToolItem.Mode> TOOL_MODE = register("tool_mode",
			() -> new DataComponentType.Builder<StorageToolItem.Mode>().persistent(StorageToolItem.Mode.CODEC)
					.networkSynchronized(StorageToolItem.Mode.STREAM_CODEC).build());

	public static final DataComponentType<BlockPos> CONTROLLER_POS = register("controller_pos",
			() -> new DataComponentType.Builder<BlockPos>().persistent(BlockPos.CODEC).networkSynchronized(BlockPos.STREAM_CODEC).build());

	public static final DataComponentType<Boolean> FLAT_TOP = register("flat_top",
			() -> new DataComponentType.Builder<Boolean>().persistent(Codec.BOOL).networkSynchronized(ByteBufCodecs.BOOL).build());

	public static final DataComponentType<Map<BarrelMaterial, Identifier>> BARREL_MATERIALS = register("barrel_materials",
			() -> new DataComponentType.Builder<Map<BarrelMaterial, Identifier>>().persistent(BarrelBlockItem.MATERIALS_CODEC)
					.networkSynchronized(BarrelBlockItem.MATERIALS_STREAM_CODEC).build());

	public static final DataComponentType<Identifier> SIMPLE_MATERIAL = register("simple_material",
			() -> new DataComponentType.Builder<Identifier>().persistent(Identifier.CODEC).networkSynchronized(Identifier.STREAM_CODEC).build());

	public static final DataComponentType<Map<Identifier, Integer>> REMAINING_PARTS = register("remaining_parts",
			() -> new DataComponentType.Builder<Map<Identifier, Integer>>().persistent(PaintbrushItem.REMAINING_PARTS_CODEC)
					.networkSynchronized(PaintbrushItem.REMAINING_PARTS_STREAM_CODEC).build());

	public static final DataComponentType<Boolean> DOUBLE_CHEST = register("double_chest",
			() -> new DataComponentType.Builder<Boolean>().persistent(Codec.BOOL).networkSynchronized(ByteBufCodecs.BOOL).build());

	public static final DataComponentType<Boolean> TIER_VISIBLE = register("tier_visible",
			() -> new DataComponentType.Builder<Boolean>().persistent(Codec.BOOL).networkSynchronized(ByteBufCodecs.BOOL).build());

	public static final DataComponentType<Boolean> LOCK_VISIBLE = register("lock_visible",
			() -> new DataComponentType.Builder<Boolean>().persistent(Codec.BOOL).networkSynchronized(ByteBufCodecs.BOOL).build());

	public static final DataComponentType<Boolean> UPGRADES_VISIBLE = register("upgrades_visible",
			() -> new DataComponentType.Builder<Boolean>().persistent(Codec.BOOL).networkSynchronized(ByteBufCodecs.BOOL).build());

	public static final DataComponentType<Boolean> COUNTS_VISIBLE = register("counts_visible",
			() -> new DataComponentType.Builder<Boolean>().persistent(Codec.BOOL).networkSynchronized(ByteBufCodecs.BOOL).build());

	public static final DataComponentType<Boolean> FILL_LEVELS_VISIBLE = register("fill_levels_visible",
			() -> new DataComponentType.Builder<Boolean>().persistent(Codec.BOOL).networkSynchronized(ByteBufCodecs.BOOL).build());

	public static final DataComponentType<Boolean> PACKED = register("packed",
			() -> new DataComponentType.Builder<Boolean>().persistent(Codec.BOOL).networkSynchronized(ByteBufCodecs.BOOL).build());

	public static final DataComponentType<WoodType> WOOD_TYPE = register("wood_type",
			() -> new DataComponentType.Builder<WoodType>().persistent(WoodType.CODEC).networkSynchronized(StreamCodecs.WOOD_TYPE_STREAM_CODEC).build());

	public static final DataComponentType<Boolean> LOCKED = register("locked",
			() -> new DataComponentType.Builder<Boolean>().persistent(Codec.BOOL).networkSynchronized(ByteBufCodecs.BOOL).build());

	public static final DataComponentType<Map<Integer, DyeColor>> SLOT_COLORS = register("slot_colors",
			() -> new DataComponentType.Builder<Map<Integer, DyeColor>>().persistent(LimitedBarrelBlockEntity.SLOT_COLORS_CODEC)
					.networkSynchronized(LimitedBarrelBlockEntity.SLOT_COLORS_STREAM_CODEC).build());

	public static final DataComponentType<Integer> FIRST_INVENTORY_SLOT = register("first_inventory_slot",
			() -> new DataComponentType.Builder<Integer>().persistent(Codec.INT).networkSynchronized(ByteBufCodecs.INT).build());

	private static final Codec<Set<Direction>> DIRECTION_SET_CODEC = CodecHelper.setOf(Direction.CODEC);

	private static final StreamCodec<FriendlyByteBuf, Set<Direction>> DIRECTION_SET_STREAM_CODEC = ByteBufCodecs.collection(HashSet::new,
			StreamCodec.of(FriendlyByteBuf::writeEnum, buf -> buf.readEnum(Direction.class)), Direction.values().length);

	public static final DataComponentType<Set<Direction>> PULL_DIRECTIONS = register("pull_directions",
			() -> new DataComponentType.Builder<Set<Direction>>().persistent(DIRECTION_SET_CODEC).networkSynchronized(DIRECTION_SET_STREAM_CODEC).build());

	public static final DataComponentType<Set<Direction>> PUSH_DIRECTIONS = register("push_directions",
			() -> new DataComponentType.Builder<Set<Direction>>().persistent(DIRECTION_SET_CODEC).networkSynchronized(DIRECTION_SET_STREAM_CODEC).build());

	public static final DataComponentType<FilterAttributes> OUTPUT_FILTER_ATTRIBUTES = register("output_filter_attributes",
			() -> new DataComponentType.Builder<FilterAttributes>().persistent(FilterAttributes.CODEC)
					.networkSynchronized(FilterAttributes.STREAM_CODEC).build());

	public static void register() {
		// Triggers static registration of data component types during mod initialization
	}
}
