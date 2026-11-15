package net.p3pp3rf1y.sophisticatedstorage.client.render;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.mojang.datafixers.util.Either;
import net.fabricmc.fabric.api.client.model.loading.v1.UnbakedModelDeserializer;
import net.minecraft.client.renderer.block.dispatch.ModelState;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelDebugName;
import net.minecraft.client.resources.model.ResolvableModel;
import net.minecraft.client.resources.model.ResolvedModel;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.client.resources.model.cuboid.CuboidModel;
import net.minecraft.client.resources.model.cuboid.CuboidModelElement;
import net.minecraft.client.resources.model.cuboid.ItemTransforms;
import net.minecraft.client.resources.model.cuboid.UnbakedCuboidGeometry;
import net.minecraft.client.resources.model.geometry.QuadCollection;
import net.minecraft.client.resources.model.geometry.UnbakedGeometry;
import net.minecraft.client.resources.model.sprite.TextureSlots;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SimpleCompositeUnbakedModel implements UnbakedModel, ResolvableModel {
	private final SimpleCompositeUnbakedGeometry geometry;
	private final FabricModelHelper.StandardModelParameters parameters;

	private SimpleCompositeUnbakedModel(SimpleCompositeUnbakedGeometry geometry, FabricModelHelper.StandardModelParameters parameters) {
		this.geometry = geometry;
		this.parameters = parameters;
	}

	@Override
	public @Nullable Identifier parent() {
		return parameters.parent();
	}

	@Override
	public @Nullable Boolean ambientOcclusion() {
		return parameters.ambientOcclusion();
	}

	@Override
	public UnbakedModel.@Nullable GuiLight guiLight() {
		return parameters.guiLight();
	}

	@Override
	public @Nullable ItemTransforms transforms() {
		return parameters.itemTransforms();
	}

	@Override
	public TextureSlots.Data textureSlots() {
		return parameters.textures();
	}

	@Nullable
	@Override
	public UnbakedGeometry geometry() {
		return geometry;
	}

	@Override
	public void resolveDependencies(ResolvableModel.Resolver resolver) {
		if (parameters.parent() != null) {
			resolver.markDependency(parameters.parent());
		}
		geometry.resolveDependencies(resolver);
	}

	public Map<String, Either<Identifier, UnbakedModel>> children() {
		return geometry.children();
	}

	private static class SimpleCompositeUnbakedGeometry implements UnbakedGeometry {
		private final ImmutableMap<String, Either<Identifier, UnbakedModel>> children;

		private SimpleCompositeUnbakedGeometry(ImmutableMap<String, Either<Identifier, UnbakedModel>> children) {
			this.children = children;
		}

		public Map<String, Either<Identifier, UnbakedModel>> children() {
			return children;
		}

		@Override
		public QuadCollection bake(TextureSlots slots, ModelBaker baker, ModelState state, ModelDebugName debugName) {
			List<CuboidModelElement> allElements = new ArrayList<>();
			addAllChildElements(baker, debugName, allElements);
			return UnbakedCuboidGeometry.bake(allElements, slots, baker, state, debugName);
		}

		private void addAllChildElements(ModelBaker baker, ModelDebugName debugName, List<CuboidModelElement> elements) {
			children.forEach((key, value) -> {
				ResolvedModel model = value.map(baker::getModel,
						inline -> FabricModelHelper.resolveInlineModel(baker, inline, () -> debugName.debugName() + "_" + key));
				addModelElements(baker, debugName, elements, model);
			});
		}

		private void addModelElements(ModelBaker baker, ModelDebugName debugName, List<CuboidModelElement> elements, ResolvedModel child) {
			if (child.wrapped() instanceof SimpleCompositeUnbakedModel compositeModel) {
				compositeModel.geometry.addAllChildElements(baker, debugName, elements);
			} else if (child.wrapped() instanceof CuboidModel cuboidModel && cuboidModel.geometry() instanceof UnbakedCuboidGeometry cuboidGeometry) {
				elements.addAll(cuboidGeometry.elements());
			}

			ResolvedModel parent = child.parent();
			if (parent != null) {
				addModelElements(baker, debugName, elements, parent);
			}
		}

		public void resolveDependencies(ResolvableModel.Resolver resolver) {
			children.values().forEach(child -> child.ifLeft(resolver::markDependency).ifRight(model -> {
				Identifier parent = model.parent();
				if (parent != null) {
					resolver.markDependency(parent);
				}
				if (model instanceof ResolvableModel resolvable) {
					resolvable.resolveDependencies(resolver);
				}
			}));
		}
	}

	@SuppressWarnings("java:S6548")
	public static final class Loader implements UnbakedModelDeserializer {
		public static final Loader INSTANCE = new Loader();

		private Loader() {
		}

		@Override
		public SimpleCompositeUnbakedModel deserialize(JsonObject jsonObject, JsonDeserializationContext deserializationContext) {
			ImmutableMap.Builder<String, Either<Identifier, UnbakedModel>> childrenBuilder = ImmutableMap.builder();
			readChildren(jsonObject, "parts", childrenBuilder, deserializationContext);
			ImmutableMap<String, Either<Identifier, UnbakedModel>> children = childrenBuilder.build();
			if (children.isEmpty()) {
				throw new JsonParseException("Simple Composite model requires a \"parts\" element with at least one element.");
			}
			FabricModelHelper.StandardModelParameters parameters = FabricModelHelper.StandardModelParameters.parse(jsonObject, deserializationContext);
			return new SimpleCompositeUnbakedModel(new SimpleCompositeUnbakedGeometry(children), parameters);
		}

		private static void readChildren(JsonObject jsonObject, String name, ImmutableMap.Builder<String, Either<Identifier, UnbakedModel>> children,
				JsonDeserializationContext context) {
			if (jsonObject.has(name)) {
				JsonObject childrenJsonObject = jsonObject.getAsJsonObject(name);

				for (Map.Entry<String, JsonElement> entry : childrenJsonObject.entrySet()) {
					JsonElement jsonElement = entry.getValue();
					Either<Identifier, UnbakedModel> child = switch (jsonElement) {
						case JsonPrimitive reference -> Either.left(Identifier.parse(reference.getAsString()));
						case JsonObject inline -> Either.right((UnbakedModel) context.deserialize(inline, UnbakedModel.class));
						default -> throw new IllegalArgumentException("");
					};

					children.put(entry.getKey(), child);
				}
			}
		}
	}
}
