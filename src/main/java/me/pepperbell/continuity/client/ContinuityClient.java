package me.pepperbell.continuity.client;

import net.minecraft.resource.ResourceType;
import net.minecraftforge.eventbus.api.IEventBus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import me.pepperbell.continuity.api.client.CachingPredicates;
import me.pepperbell.continuity.api.client.CtmLoader;
import me.pepperbell.continuity.api.client.CtmLoaderRegistry;
import me.pepperbell.continuity.api.client.CtmProperties;
import me.pepperbell.continuity.api.client.QuadProcessor;
import me.pepperbell.continuity.client.processor.BaseCachingPredicates;
import me.pepperbell.continuity.client.processor.CompactCtmQuadProcessor;
import me.pepperbell.continuity.client.processor.ProcessingDataKeys;
import me.pepperbell.continuity.client.processor.TopQuadProcessor;
import me.pepperbell.continuity.client.processor.overlay.SimpleOverlayQuadProcessor;
import me.pepperbell.continuity.client.processor.overlay.StandardOverlayQuadProcessor;
import me.pepperbell.continuity.client.processor.simple.CtmSpriteProvider;
import me.pepperbell.continuity.client.processor.simple.FixedSpriteProvider;
import me.pepperbell.continuity.client.processor.simple.HorizontalSpriteProvider;
import me.pepperbell.continuity.client.processor.simple.HorizontalVerticalSpriteProvider;
import me.pepperbell.continuity.client.processor.simple.RandomSpriteProvider;
import me.pepperbell.continuity.client.processor.simple.RepeatSpriteProvider;
import me.pepperbell.continuity.client.processor.simple.SimpleQuadProcessor;
import me.pepperbell.continuity.client.processor.simple.VerticalHorizontalSpriteProvider;
import me.pepperbell.continuity.client.processor.simple.VerticalSpriteProvider;
import me.pepperbell.continuity.client.properties.BaseCtmProperties;
import me.pepperbell.continuity.client.properties.CompactConnectingCtmProperties;
import me.pepperbell.continuity.client.properties.ConnectingCtmProperties;
import me.pepperbell.continuity.client.properties.OrientedConnectingCtmProperties;
import me.pepperbell.continuity.client.properties.PropertiesParsingHelper;
import me.pepperbell.continuity.client.properties.RandomCtmProperties;
import me.pepperbell.continuity.client.properties.RepeatCtmProperties;
import me.pepperbell.continuity.client.properties.TileAmountValidator;
import me.pepperbell.continuity.client.properties.overlay.BaseOverlayCtmProperties;
import me.pepperbell.continuity.client.properties.overlay.OrientedConnectingOverlayCtmProperties;
import me.pepperbell.continuity.client.properties.overlay.RandomOverlayCtmProperties;
import me.pepperbell.continuity.client.properties.overlay.RepeatOverlayCtmProperties;
import me.pepperbell.continuity.client.properties.overlay.StandardOverlayCtmProperties;
import me.pepperbell.continuity.client.resource.CustomBlockLayers;
import me.pepperbell.continuity.client.resource.ModelWrappingHandler;
import me.pepperbell.continuity.client.util.RenderUtil;
import me.pepperbell.continuity.client.util.biome.BiomeHolderManager;
import me.pepperbell.continuity.client.util.biome.BiomeRetriever;
import me.pepperbell.continuity.impl.client.ProcessingDataKeyRegistryImpl;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.thinkingstudio.continuity.ported.resource.ResourceManagerHelper;

public class ContinuityClient {
	public static final String ID = "continuity";
	public static final String NAME = "Continuity";
	public static final Logger LOGGER = LoggerFactory.getLogger(NAME);

	public void onInitializeClient(IEventBus modEventBus) {

		ProcessingDataKeyRegistryImpl.INSTANCE.init(modEventBus);
		BiomeHolderManager.init();
		BiomeRetriever.init();
		ProcessingDataKeys.init();
		ModelWrappingHandler.init(modEventBus);
		RenderUtil.ReloadListener.init(modEventBus);
		CustomBlockLayers.ReloadListener.init(modEventBus);

//		FabricLoader.getInstance().getModContainer(ID).ifPresent(container -> {
//			ResourceManagerHelper.registerBuiltinResourcePack(asId("default"), container, Text.translatable("resourcePack.continuity.default.name"), ResourcePackActivationType.NORMAL);
//			ResourceManagerHelper.registerBuiltinResourcePack(asId("glass_pane_culling_fix"), container, Text.translatable("resourcePack.continuity.glass_pane_culling_fix.name"), ResourcePackActivationType.NORMAL);
//		});

		ResourceManagerHelper.registerBuiltinResourcePack(ID, modEventBus, asId("default"), Text.translatable("resourcePack.continuity.default.name"), ResourceType.CLIENT_RESOURCES);
		ResourceManagerHelper.registerBuiltinResourcePack(ID, modEventBus, asId("glass_pane_culling_fix"), Text.translatable("resourcePack.continuity.glass_pane_culling_fix.name"), ResourceType.CLIENT_RESOURCES);

		CtmLoaderRegistry registry = CtmLoaderRegistry.get();
		CtmLoader<?> loader;

		// Standard simple methods

		loader = createLoader(
				OrientedConnectingCtmProperties::new,
				new TileAmountValidator.AtLeast<>(47),
				new SimpleQuadProcessor.Factory<>(new CtmSpriteProvider.Factory())
		);
		registry.registerLoader("ctm", loader);
		registry.registerLoader("glass", loader);

		loader = createLoader(
				CompactConnectingCtmProperties::new,
				new TileAmountValidator.AtLeast<>(5),
				new CompactCtmQuadProcessor.Factory(),
				false
		);
		registry.registerLoader("ctm_compact", loader);

		loader = createLoader(
				OrientedConnectingCtmProperties::new,
				new TileAmountValidator.Exactly<>(4),
				new SimpleQuadProcessor.Factory<>(new HorizontalSpriteProvider.Factory())
		);
		registry.registerLoader("horizontal", loader);
		registry.registerLoader("bookshelf", loader);

		loader = createLoader(
				OrientedConnectingCtmProperties::new,
				new TileAmountValidator.Exactly<>(4),
				new SimpleQuadProcessor.Factory<>(new VerticalSpriteProvider.Factory())
		);
		registry.registerLoader("vertical", loader);

		loader = createLoader(
				OrientedConnectingCtmProperties::new,
				new TileAmountValidator.Exactly<>(7),
				new SimpleQuadProcessor.Factory<>(new HorizontalVerticalSpriteProvider.Factory())
		);
		registry.registerLoader("horizontal+vertical", loader);
		registry.registerLoader("h+v", loader);

		loader = createLoader(
				OrientedConnectingCtmProperties::new,
				new TileAmountValidator.Exactly<>(7),
				new SimpleQuadProcessor.Factory<>(new VerticalHorizontalSpriteProvider.Factory())
		);
		registry.registerLoader("vertical+horizontal", loader);
		registry.registerLoader("v+h", loader);

		loader = createLoader(
				ConnectingCtmProperties::new,
				new TileAmountValidator.Exactly<>(1),
				new TopQuadProcessor.Factory()
		);
		registry.registerLoader("top", loader);

		loader = createLoader(
				RandomCtmProperties::new,
				new SimpleQuadProcessor.Factory<>(new RandomSpriteProvider.Factory())
		);
		registry.registerLoader("random", loader);

		loader = createLoader(
				RepeatCtmProperties::new,
				new RepeatCtmProperties.Validator<>(),
				new SimpleQuadProcessor.Factory<>(new RepeatSpriteProvider.Factory())
		);
		registry.registerLoader("repeat", loader);

		loader = createLoader(
				BaseCtmProperties::new,
				new TileAmountValidator.Exactly<>(1),
				new SimpleQuadProcessor.Factory<>(new FixedSpriteProvider.Factory())
		);
		registry.registerLoader("fixed", loader);

		// Standard overlay methods

		loader = createLoader(
				StandardOverlayCtmProperties::new,
				new TileAmountValidator.AtLeast<>(17),
				new StandardOverlayQuadProcessor.Factory()
		);
		registry.registerLoader("overlay", loader);

		loader = createLoader(
				OrientedConnectingOverlayCtmProperties::new,
				new TileAmountValidator.AtLeast<>(47),
				new SimpleOverlayQuadProcessor.Factory<>(new CtmSpriteProvider.Factory())
		);
		registry.registerLoader("overlay_ctm", loader);

		loader = createLoader(
				RandomOverlayCtmProperties::new,
				new SimpleOverlayQuadProcessor.Factory<>(new RandomSpriteProvider.Factory())
		);
		registry.registerLoader("overlay_random", loader);

		loader = createLoader(
				RepeatOverlayCtmProperties::new,
				new RepeatCtmProperties.Validator<>(),
				new SimpleOverlayQuadProcessor.Factory<>(new RepeatSpriteProvider.Factory())
		);
		registry.registerLoader("overlay_repeat", loader);

		loader = createLoader(
				BaseOverlayCtmProperties::new,
				new TileAmountValidator.Exactly<>(1),
				new SimpleOverlayQuadProcessor.Factory<>(new FixedSpriteProvider.Factory())
		);
		registry.registerLoader("overlay_fixed", loader);

		// Custom methods

		loader = createCustomLoader(
				OrientedConnectingOverlayCtmProperties::new,
				new TileAmountValidator.Exactly<>(4),
				new SimpleOverlayQuadProcessor.Factory<>(new HorizontalSpriteProvider.Factory())
		);
		registry.registerLoader("overlay_horizontal", loader);

		loader = createCustomLoader(
				OrientedConnectingOverlayCtmProperties::new,
				new TileAmountValidator.Exactly<>(4),
				new SimpleOverlayQuadProcessor.Factory<>(new VerticalSpriteProvider.Factory())
		);
		registry.registerLoader("overlay_vertical", loader);

		loader = createCustomLoader(
				OrientedConnectingOverlayCtmProperties::new,
				new TileAmountValidator.Exactly<>(7),
				new SimpleOverlayQuadProcessor.Factory<>(new HorizontalVerticalSpriteProvider.Factory())
		);
		registry.registerLoader("overlay_horizontal+vertical", loader);
		registry.registerLoader("overlay_h+v", loader);

		loader = createCustomLoader(
				OrientedConnectingOverlayCtmProperties::new,
				new TileAmountValidator.Exactly<>(7),
				new SimpleOverlayQuadProcessor.Factory<>(new VerticalHorizontalSpriteProvider.Factory())
		);
		registry.registerLoader("overlay_vertical+horizontal", loader);
		registry.registerLoader("overlay_v+h", loader);
	}

	private static <T extends CtmProperties> CtmLoader<T> createLoader(CtmProperties.Factory<T> propertiesFactory, QuadProcessor.Factory<T> processorFactory, CachingPredicates.Factory<T> predicatesFactory) {
		return new CtmLoader<>() {
			@Override
			public CtmProperties.Factory<T> getPropertiesFactory() {
				return propertiesFactory;
			}

			@Override
			public QuadProcessor.Factory<T> getProcessorFactory() {
				return processorFactory;
			}

			@Override
			public CachingPredicates.Factory<T> getPredicatesFactory() {
				return predicatesFactory;
			}
		};
	}

	private static <T extends BaseCtmProperties> CtmLoader<T> createLoader(CtmProperties.Factory<T> propertiesFactory, TileAmountValidator<T> validator, QuadProcessor.Factory<T> processorFactory, boolean isValidForMultipass) {
		return createLoader(wrapWithOptifineOnlyCheck(TileAmountValidator.wrapFactory(BaseCtmProperties.wrapFactory(propertiesFactory), validator)), processorFactory, new BaseCachingPredicates.Factory<>(isValidForMultipass));
	}

	private static <T extends BaseCtmProperties> CtmLoader<T> createLoader(CtmProperties.Factory<T> propertiesFactory, TileAmountValidator<T> validator, QuadProcessor.Factory<T> processorFactory) {
		return createLoader(propertiesFactory, validator, processorFactory, true);
	}

	private static <T extends BaseCtmProperties> CtmLoader<T> createLoader(CtmProperties.Factory<T> propertiesFactory, QuadProcessor.Factory<T> processorFactory, boolean isValidForMultipass) {
		return createLoader(wrapWithOptifineOnlyCheck(BaseCtmProperties.wrapFactory(propertiesFactory)), processorFactory, new BaseCachingPredicates.Factory<>(isValidForMultipass));
	}

	private static <T extends BaseCtmProperties> CtmLoader<T> createLoader(CtmProperties.Factory<T> propertiesFactory, QuadProcessor.Factory<T> processorFactory) {
		return createLoader(propertiesFactory, processorFactory, true);
	}

	private static <T extends BaseCtmProperties> CtmLoader<T> createCustomLoader(CtmProperties.Factory<T> propertiesFactory, TileAmountValidator<T> validator, QuadProcessor.Factory<T> processorFactory, boolean isValidForMultipass) {
		return createLoader(TileAmountValidator.wrapFactory(BaseCtmProperties.wrapFactory(propertiesFactory), validator), processorFactory, new BaseCachingPredicates.Factory<>(isValidForMultipass));
	}

	private static <T extends BaseCtmProperties> CtmLoader<T> createCustomLoader(CtmProperties.Factory<T> propertiesFactory, TileAmountValidator<T> validator, QuadProcessor.Factory<T> processorFactory) {
		return createCustomLoader(propertiesFactory, validator, processorFactory, true);
	}

	private static <T extends CtmProperties> CtmProperties.Factory<T> wrapWithOptifineOnlyCheck(CtmProperties.Factory<T> factory) {
		return (properties, resourceId, pack, packPriority, resourceManager, method) -> {
			if (PropertiesParsingHelper.parseOptifineOnly(properties, resourceId)) {
				return null;
			}
			return factory.createProperties(properties, resourceId, pack, packPriority, resourceManager, method);
		};
	}

	public static Identifier asId(String path) {
		return new Identifier(ID, path);
	}
}
