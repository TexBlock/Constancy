package me.pepperbell.continuity.client.util;

import net.minecraft.resource.SynchronousResourceReloader;
import net.minecraftforge.eventbus.api.IEventBus;
import org.jetbrains.annotations.Nullable;

import me.pepperbell.continuity.client.ContinuityClient;
import net.fabricmc.fabric.api.renderer.v1.RendererAccess;
import net.fabricmc.fabric.api.renderer.v1.material.BlendMode;
import net.fabricmc.fabric.api.renderer.v1.material.MaterialFinder;
import net.fabricmc.fabric.api.renderer.v1.material.RenderMaterial;
import net.fabricmc.fabric.api.renderer.v1.model.SpriteFinder;
import net.fabricmc.fabric.api.util.TriState;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.render.model.BakedModelManager;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockRenderView;
import org.thinkingstudio.continuity.ported.resource.ResourceManagerHelper;

public final class RenderUtil {
	private static final BlockColors BLOCK_COLORS = MinecraftClient.getInstance().getBlockColors();
	private static final BakedModelManager MODEL_MANAGER = MinecraftClient.getInstance().getBakedModelManager();

	private static final ThreadLocal<MaterialFinder> MATERIAL_FINDER = ThreadLocal.withInitial(() -> RendererAccess.INSTANCE.getRenderer().materialFinder());

	private static SpriteFinder blockAtlasSpriteFinder;

	public static int getTintColor(@Nullable BlockState state, BlockRenderView blockView, BlockPos pos, int tintIndex) {
		if (state == null || tintIndex == -1) {
			return -1;
		}
		return 0xFF000000 | BLOCK_COLORS.getColor(state, blockView, pos, tintIndex);
	}

	public static RenderMaterial findOverlayMaterial(BlendMode blendMode, @Nullable BlockState tintBlock) {
		MaterialFinder finder = getMaterialFinder();
		finder.blendMode(blendMode);
		if (tintBlock != null) {
			finder.ambientOcclusion(TriState.of(canHaveAO(tintBlock)));
		} else {
			finder.ambientOcclusion(TriState.TRUE);
		}
		return finder.find();
	}

	public static boolean canHaveAO(BlockState state) {
		return state.getLuminance() == 0;
	}

	public static MaterialFinder getMaterialFinder() {
		return MATERIAL_FINDER.get().clear();
	}

	public static SpriteFinder getSpriteFinder() {
		return blockAtlasSpriteFinder;
	}

	public static class ReloadListener implements SynchronousResourceReloader {
		public static final Identifier ID = ContinuityClient.asId("render_util");
//		public static final List<Identifier> DEPENDENCIES = List.of(ResourceReloadListenerKeys.MODELS);
		private static final ReloadListener INSTANCE = new ReloadListener();

		public static void init(IEventBus modEventBus) {
			ResourceManagerHelper.registerReloadListener(modEventBus, ResourceType.CLIENT_RESOURCES, INSTANCE);
		}

		@Override
		public void reload(ResourceManager manager) {
			blockAtlasSpriteFinder = SpriteFinder.get(MODEL_MANAGER.getAtlas(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE));
		}

//		@Override
//		public Identifier getFabricId() {
//			return ID;
//		}
//
//		@Override
//		public Collection<Identifier> getFabricDependencies() {
//			return DEPENDENCIES;
//		}
	}
}
