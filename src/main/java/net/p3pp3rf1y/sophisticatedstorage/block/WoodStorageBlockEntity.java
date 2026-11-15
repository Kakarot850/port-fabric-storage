package net.p3pp3rf1y.sophisticatedstorage.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Util;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.p3pp3rf1y.sophisticatedcore.fabric.transfer.ResourceHandler;
import net.p3pp3rf1y.sophisticatedcore.fabric.transfer.item.ItemResource;
import net.p3pp3rf1y.sophisticatedcore.renderdata.RenderDataHandler;
import net.p3pp3rf1y.sophisticatedstorage.item.WoodStorageBlockItem;
import org.jspecify.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;
import net.p3pp3rf1y.sophisticatedcore.SophisticatedCore;

public abstract class WoodStorageBlockEntity extends StorageBlockEntity {
	public static final String PACKED = "packed";
	@Nullable
	private WoodType woodType = null;

	private boolean packed = false;

	protected WoodStorageBlockEntity(BlockPos pos, BlockState state, BlockEntityType<? extends StorageBlockEntity> blockEntityType) {
		super(pos, state, blockEntityType);
	}

	@Override
	protected void saveSynchronizedData(ValueOutput out) {
		super.saveSynchronizedData(out);
		if (woodType != null) {
			out.putString("woodType", woodType.name());
		}
		out.putBoolean(PACKED, packed);
	}

	@Override
	public void loadSynchronizedData(ValueInput in) {
		super.loadSynchronizedData(in);
		woodType = in.read("woodType", WoodType.CODEC)
				.orElse(getStorageWrapper().hasMainColor() && getStorageWrapper().hasAccentColor() ? null : WoodType.ACACIA);
		packed = in.getBooleanOr(PACKED, false);
	}

	public Optional<WoodType> getWoodType() {
		return Optional.ofNullable(woodType);
	}

	public void setWoodType(WoodType woodType) {
		this.woodType = woodType;
		if (SophisticatedCore.isLogicalServerThread()) {
			setChanged();
		}
	}

	@Override
	public Component getDisplayName() {
		if (displayName != null) {
			return displayName;
		}
		return makeWoodStorageDescriptionId(getWoodType().orElse(null));
	}

	private Component makeWoodStorageDescriptionId(@Nullable WoodType wt) {
		String id = Util.makeDescriptionId("block", Objects.requireNonNull(BuiltInRegistries.BLOCK.getKey(getBlockState().getBlock())));
		return WoodStorageBlockItem.getDisplayName(id, wt);
	}

	public boolean isPacked() {
		return packed;
	}

	public void setPacked(boolean packed) {
		if (this.packed != packed) {
			// TODO(port): Slice 4 - invalidateCapabilities();
		}
		this.packed = packed;
		if (packed) {
			RenderDataHandler renderDataHandler = getStorageWrapper().getRenderDataHandler();
			renderDataHandler.removeAllUpgradeClientData();
			setUpdateBlockRender();
		}
	}

	@Override
	public boolean shouldDropContents() {
		return !isPacked() && !isLinkedStorage();
	}

	@Nullable
	@Override
	public ResourceHandler<ItemResource> getExternalItemHandler(@Nullable Direction side) {
		if (isPacked()) {
			return null;
		}
		return super.getExternalItemHandler(side);
	}

	@Override
	public boolean canConnectStorages() {
		return !packed && super.canConnectStorages();
	}

	@Override
	public boolean canBeConnected() {
		return !packed && super.canBeConnected();
	}

	@Override
	public boolean canBeLinked() {
		return !packed;
	}

	@Override
	protected boolean canRefreshUpgrades() {
		return super.canRefreshUpgrades() && !packed;
	}
}
