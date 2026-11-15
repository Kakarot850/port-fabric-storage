package net.p3pp3rf1y.sophisticatedstorage.init;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.p3pp3rf1y.sophisticatedcore.fabric.registries.DeferredHolder;
import net.p3pp3rf1y.sophisticatedcore.fabric.registries.DeferredRegister;

import java.util.function.Function;
import java.util.function.Supplier;

public class DeferredRegisterBlocks extends DeferredRegister<Block> {
	protected DeferredRegisterBlocks(String namespace) {
		super(Registries.BLOCK, namespace);
	}

	public static DeferredRegisterBlocks createBlocks(String namespace) {
		return new DeferredRegisterBlocks(namespace);
	}

	public <I extends Block> DeferredHolder<Block, I> registerBlock(String name, Function<BlockBehaviour.Properties, ? extends I> func,
			Supplier<BlockBehaviour.Properties> properties) {
		return register(name, key -> func.apply(properties.get().setId(ResourceKey.create(Registries.BLOCK, key))));
	}

	public <I extends Block> DeferredHolder<Block, I> registerBlock(String name, Function<BlockBehaviour.Properties, ? extends I> func) {
		return registerBlock(name, func, BlockBehaviour.Properties::of);
	}
}
