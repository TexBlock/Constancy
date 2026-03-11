package me.pepperbell.continuity.client.util.biome;

import net.fabricmc.fabric.api.blockview.v2.FabricBlockView;
import net.minecraft.world.WorldView;
import net.minecraftforge.fml.ModList;
import org.jetbrains.annotations.Nullable;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockRenderView;
import net.minecraft.world.biome.Biome;

// TODO: Inline this class and always use the API once Canvas properly supports it.
public final class BiomeRetriever {
	private static final Provider PROVIDER = createProvider();

	private static Provider createProvider() {
		ClassLoader classLoader = BiomeRetriever.class.getClassLoader();

//		if (FabricLoader.getInstance().isModLoaded("canvas")) {
//			try {
//				Class<?> inputRegionClass = Class.forName("grondag.canvas.terrain.region.input.InputRegion", false, classLoader);
//				inputRegionClass.getMethod("getBiome", BlockPos.class);
//				return BiomeRetriever::getBiomeByInputRegion;
//			} catch (ClassNotFoundException | NoSuchMethodException e) {
//				//
//			}
//		}

		if (ModList.get().isLoaded("fabric-block-view-api-v2")) {
			try {
				Class<?> inputRegionClass = Class.forName("net.fabricmc.fabric.api.blockview.v2.FabricBlockView", false, classLoader);
				inputRegionClass.getMethod("getBiomeFabric", BlockPos.class);
				return BiomeRetriever::getBiomeByAPI;
			} catch (ClassNotFoundException | NoSuchMethodException e) {
				//
			}
		}

		return BiomeRetriever::getBiomeByVanilla;
	}

	@Nullable
	public static Biome getBiome(BlockRenderView blockView, BlockPos pos) {
		return PROVIDER.getBiome(blockView, pos);
	}

	public static void init() {
	}

	@Nullable
	private static Biome getBiomeByVanilla(BlockRenderView blockView, BlockPos pos) {
		if (blockView instanceof WorldView worldView) {
			return worldView.getBiome(pos).value();
		}
		return null;
	}

	@Nullable
	private static Biome getBiomeByAPI(BlockRenderView blockView, BlockPos pos) {
		if (((FabricBlockView) blockView).hasBiomes()) {
			return ((FabricBlockView) blockView).getBiomeFabric(pos).value();
		}
		return null;
	}

	// Canvas
//	@Nullable
//	private static Biome getBiomeByInputRegion(BlockRenderView blockView, BlockPos pos) {
//		if (blockView instanceof InputRegion inputRegion) {
//			return inputRegion.getBiome(pos);
//		}
//		return getBiomeByAPI(blockView, pos);
//	}

	private interface Provider {
		@Nullable
		Biome getBiome(BlockRenderView blockView, BlockPos pos);
	}
}
