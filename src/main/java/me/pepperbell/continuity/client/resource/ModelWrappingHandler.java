package me.pepperbell.continuity.client.resource;

import com.google.common.collect.ImmutableSet;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import com.google.common.collect.ImmutableMap;

import me.pepperbell.continuity.client.mixinterface.ModelLoaderExtension;
import me.pepperbell.continuity.client.model.CtmBakedModel;
import me.pepperbell.continuity.client.model.EmissiveBakedModel;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.block.BlockModels;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

import java.util.Map;
import java.util.Set;

public class ModelWrappingHandler {
	private final boolean wrapCtm;
	private final boolean wrapEmissive;
	private final ImmutableMap<ModelIdentifier, BlockState> blockStateModelIds;

	private ModelWrappingHandler(boolean wrapCtm, boolean wrapEmissive) {
		this.wrapCtm = wrapCtm;
		this.wrapEmissive = wrapEmissive;
		blockStateModelIds = createBlockStateModelIdMap();
	}

	@Nullable
	public static ModelWrappingHandler create(boolean wrapCtm, boolean wrapEmissive) {
		if (!wrapCtm && !wrapEmissive) {
			return null;
		}
		return new ModelWrappingHandler(wrapCtm, wrapEmissive);
	}

	private static ImmutableMap<ModelIdentifier, BlockState> createBlockStateModelIdMap() {
		ImmutableMap.Builder<ModelIdentifier, BlockState> builder = ImmutableMap.builder();
		// Match code of BakedModelManager#bake
		for (Block block : Registries.BLOCK) {
			Identifier blockId = block.getRegistryEntry().registryKey().getValue();
			for (BlockState state : block.getStateManager().getStates()) {
				ModelIdentifier modelId = BlockModels.getModelId(blockId, state);
				builder.put(modelId, state);
			}
		}
		return builder.build();
	}

	public BakedModel wrap(@Nullable BakedModel model, Identifier modelId) {
		if (model != null && !model.isBuiltin() && !modelId.equals(ModelLoader.MISSING_ID)) {
			if (wrapCtm) {
				if (modelId instanceof ModelIdentifier) {
					BlockState state = blockStateModelIds.get(modelId);
					if (state != null) {
						model = new CtmBakedModel(model, state);
					}
				}
			}
			if (wrapEmissive) {
				model = new EmissiveBakedModel(model);
			}
		}
		return model;
	}

	@ApiStatus.Internal
	public static void init(IEventBus modEventBus) {
		modEventBus.<ModelEvent.ModifyBakingResult>addListener(event -> {
			ModelLoader modelLoader = event.getModelBakery();
			ModelWrappingHandler wrappingHandler = ((ModelLoaderExtension) modelLoader).continuity$getModelWrappingHandler();
			if (wrappingHandler != null) {
				Map<Identifier, BakedModel> bakedModels = event.getModels();
				Set<Identifier> keys = ImmutableSet.copyOf(event.getModels().keySet());
				for (Identifier modelId : keys) {
					bakedModels.put(modelId, wrappingHandler.wrap(bakedModels.get(modelId), modelId));
				}
			}
		});
	}
}
