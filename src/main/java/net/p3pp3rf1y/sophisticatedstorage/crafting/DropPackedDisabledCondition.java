package net.p3pp3rf1y.sophisticatedstorage.crafting;

import com.mojang.serialization.MapCodec;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceCondition;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditionType;
import net.minecraft.resources.RegistryOps;
import net.p3pp3rf1y.sophisticatedstorage.Config;
import net.p3pp3rf1y.sophisticatedstorage.SophisticatedStorage;
import org.jspecify.annotations.Nullable;

public class DropPackedDisabledCondition implements ResourceCondition {
	private static final DropPackedDisabledCondition INSTANCE = new DropPackedDisabledCondition();
	public static final MapCodec<DropPackedDisabledCondition> CODEC = MapCodec.unit(INSTANCE).stable();
	public static final ResourceConditionType<DropPackedDisabledCondition> TYPE = ResourceConditionType.create(
			SophisticatedStorage.getIdentifier("drop_packed_disabled"), CODEC);

	@Override
	public boolean test(RegistryOps.@Nullable RegistryInfoLookup registryInfo) {
		return Boolean.FALSE.equals(Config.COMMON.dropPacked.get());
	}

	@Override
	public ResourceConditionType<?> getType() {
		return TYPE;
	}
}
