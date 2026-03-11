package me.pepperbell.continuity.client.util.biome;

import java.util.Map;
import java.util.Set;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import net.minecraft.client.MinecraftClient;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.common.MinecraftForge;

public final class BiomeHolderManager {
	private static final Map<Identifier, BiomeHolder> HOLDER_CACHE = new Object2ObjectOpenHashMap<>();
	private static final Set<Runnable> REFRESH_CALLBACKS = new ReferenceOpenHashSet<>();

	private static DynamicRegistryManager registryManager;

	public static BiomeHolder getOrCreateHolder(Identifier id) {
		return HOLDER_CACHE.computeIfAbsent(id, BiomeHolder::new);
	}

	public static void addRefreshCallback(Runnable callback) {
		REFRESH_CALLBACKS.add(callback);
	}

	public static void init() {
//		ClientPlayConnectionEvents.JOIN.register(((handler, sender, client) -> {
//			registryManager = handler.getRegistryManager();
//			refreshHolders();
//		}));
		MinecraftForge.EVENT_BUS.<ClientPlayerNetworkEvent.LoggingIn>addListener(event -> {
			if (MinecraftClient.getInstance().world == null) {
				return;
			}

			registryManager = event.getPlayer().getWorld().getRegistryManager();
			refreshHolders();
		});
	}

	public static void refreshHolders() {
		if (registryManager == null) {
			return;
		}

		Map<Identifier, Identifier> compactIdMap = new Object2ObjectOpenHashMap<>();
		Registry<Biome> biomeRegistry = registryManager.get(RegistryKeys.BIOME);
		for (Identifier id : biomeRegistry.getIds()) {
			String path = id.getPath();
			String compactPath = path.replace("_", "");
			if (!path.equals(compactPath)) {
				Identifier compactId = id.withPath(compactPath);
				if (!biomeRegistry.containsId(compactId)) {
					compactIdMap.put(compactId, id);
				}
			}
		}

		for (BiomeHolder holder : HOLDER_CACHE.values()) {
			holder.refresh(biomeRegistry, compactIdMap);
		}

		for (Runnable callback : REFRESH_CALLBACKS) {
			callback.run();
		}
	}

	public static void clearCache() {
		HOLDER_CACHE.clear();
		REFRESH_CALLBACKS.clear();
	}
}
