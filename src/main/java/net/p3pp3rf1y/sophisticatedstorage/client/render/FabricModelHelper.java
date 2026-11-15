package net.p3pp3rf1y.sophisticatedstorage.client.render;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.mojang.math.Transformation;
import net.fabricmc.fabric.api.client.renderer.v1.Renderer;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.MutableMesh;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.QuadEmitter;
import net.fabricmc.fabric.api.client.renderer.v1.sprite.SpriteFinder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.block.dispatch.ModelState;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelDebugName;
import net.minecraft.client.resources.model.ResolvedModel;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.client.resources.model.cuboid.ItemTransforms;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.geometry.QuadCollection;
import net.minecraft.client.resources.model.sprite.TextureSlots;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.data.AtlasIds;
import net.minecraft.resources.Identifier;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

/**
 * Shared Fabric 26.1.2 client helpers replacing NeoForge model baking/tinting extensions.
 */
public final class FabricModelHelper {
	private FabricModelHelper() {
	}

	public record StandardModelParameters(@Nullable Identifier parent, TextureSlots.Data textures,
			@Nullable ItemTransforms itemTransforms, @Nullable Boolean ambientOcclusion,
			UnbakedModel.@Nullable GuiLight guiLight) {
		public static StandardModelParameters parse(JsonObject jsonObject, JsonDeserializationContext context) {
			String parentName = GsonHelper.getAsString(jsonObject, "parent", "");
			Identifier parent = parentName.isEmpty() ? null : Identifier.parse(parentName);

			TextureSlots.Data textures = TextureSlots.Data.EMPTY;
			if (jsonObject.has("textures")) {
				textures = TextureSlots.parseTextureMap(GsonHelper.getAsJsonObject(jsonObject, "textures"));
			}

			ItemTransforms itemTransforms = null;
			if (jsonObject.has("display")) {
				itemTransforms = context.deserialize(GsonHelper.getAsJsonObject(jsonObject, "display"), ItemTransforms.class);
			}

			Boolean ambientOcclusion = null;
			if (jsonObject.has("ambientocclusion")) {
				ambientOcclusion = GsonHelper.getAsBoolean(jsonObject, "ambientocclusion");
			}

			UnbakedModel.GuiLight guiLight = null;
			if (jsonObject.has("gui_light")) {
				guiLight = UnbakedModel.GuiLight.getByName(GsonHelper.getAsString(jsonObject, "gui_light"));
			}

			return new StandardModelParameters(parent, textures, itemTransforms, ambientOcclusion, guiLight);
		}
	}

	public static ResolvedModel resolveInlineModel(ModelBaker baker, UnbakedModel model, ModelDebugName debugName) {
		return new ResolvedModel() {
			@Override
			public UnbakedModel wrapped() {
				return model;
			}

			@Override
			public @Nullable ResolvedModel parent() {
				Identifier parentId = model.parent();
				return parentId != null ? baker.getModel(parentId) : null;
			}

			@Override
			public String debugName() {
				return debugName.debugName();
			}
		};
	}

	public static ModelState composeRootTransformIntoModelState(ModelState state, Transformation rootTransform) {
		if (rootTransform.equals(Transformation.IDENTITY)) {
			return state;
		}
		Matrix4f centered = new Matrix4f().translation(-0.5F, -0.5F, -0.5F).mul(rootTransform.getMatrix()).translate(0.5F, 0.5F, 0.5F);
		Transformation composed = state.transformation().compose(new Transformation(centered));
		return new ModelState() {
			@Override
			public Transformation transformation() {
				return composed;
			}

			@Override
			public Matrix4fc faceTransformation(Direction face) {
				return state.faceTransformation(face);
			}

			@Override
			public Matrix4fc inverseFaceTransformation(Direction face) {
				return state.inverseFaceTransformation(face);
			}
		};
	}

	public static SpriteFinder getBlocksSpriteFinder() {
		TextureAtlas blocksAtlas = Minecraft.getInstance().getAtlasManager().getAtlasOrThrow(AtlasIds.BLOCKS);
		return blocksAtlas.spriteFinder();
	}

	public static void emitParts(List<BlockStateModelPart> parts, QuadEmitter emitter, Predicate<@Nullable Direction> cullTest,
			Map<Identifier, Integer> materialTintColors) {
		if (materialTintColors.isEmpty()) {
			for (BlockStateModelPart part : parts) {
				part.emitQuads(emitter, cullTest);
			}
			return;
		}
		for (BlockStateModelPart part : parts) {
			emitQuadsWithTint(part, emitter, cullTest, materialTintColors);
		}
	}

	public static void emitQuadsWithTint(BlockStateModelPart part, QuadEmitter emitter, Predicate<@Nullable Direction> cullTest,
			Map<Identifier, Integer> materialTintColors) {
		emitQuadFace(part.getQuads(null), null, emitter, materialTintColors);
		for (Direction dir : Direction.values()) {
			if (!cullTest.test(dir)) {
				emitQuadFace(part.getQuads(dir), dir, emitter, materialTintColors);
			}
		}
	}

	public static void emitQuadCollection(QuadCollection quads, QuadEmitter emitter, Predicate<@Nullable Direction> cullTest,
			Map<Identifier, Integer> materialTintColors) {
		emitQuadFace(quads.getQuads(null), null, emitter, materialTintColors);
		for (Direction dir : Direction.values()) {
			if (!cullTest.test(dir)) {
				emitQuadFace(quads.getQuads(dir), dir, emitter, materialTintColors);
			}
		}
	}

	private static void emitQuadFace(List<BakedQuad> quads, @Nullable Direction cullFace, QuadEmitter emitter,
			Map<Identifier, Integer> materialTintColors) {
		for (BakedQuad quad : quads) {
			emitter.cullFace(cullFace);
			emitter.fromBakedQuad(quad);
			Integer tintColor = materialTintColors.get(quad.materialInfo().sprite().contents().name());
			if (tintColor != null) {
				emitter.color(tintColor, tintColor, tintColor, tintColor);
				emitter.tintIndex(-1);
			}
			emitter.emit();
		}
	}

	public static List<BakedQuad> collectModelQuads(BlockStateModel model, @Nullable BlockAndTintGetter level, BlockPos pos, BlockState state,
			RandomSource rand) {
		List<BakedQuad> quads = new ArrayList<>();
		if (level != null) {
			MutableMesh mesh = Renderer.get().mutableMesh();
			model.emitQuads(mesh.emitter(), level, pos, state, rand, d -> false);
			SpriteFinder spriteFinder = getBlocksSpriteFinder();
			mesh.forEach(quad -> quads.add(quad.toBakedQuad(spriteFinder.find(quad))));
			return quads;
		}
		List<BlockStateModelPart> parts = new ArrayList<>();
		model.collectParts(rand, parts);
		for (BlockStateModelPart part : parts) {
			quads.addAll(part.getQuads(null));
			for (Direction dir : Direction.values()) {
				quads.addAll(part.getQuads(dir));
			}
		}
		return quads;
	}
}
