package org.thinkingstudio.continuity.ported.resource;

import net.minecraft.resource.*;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.event.AddPackFindersEvent;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.resource.PathPackResources;

public class ResourceManagerHelper {
    public static void registerBuiltinResourcePack(String modId, IEventBus modEventBus, Identifier id, Text displayName, ResourceType resourceType) {
        var resourcePath = ModList.get().getModFileById(modId).getFile().findResource("resourcepacks/" + id.getPath());
        var pack = new PathPackResources(ModList.get().getModFileById(modId).getFile().getFileName() + ":" + resourcePath, true, resourcePath);

        modEventBus.<AddPackFindersEvent>addListener(event -> {
            if (event.getPackType() == resourceType) {
                event.addRepositorySource(profileAdder -> profileAdder.accept(ResourcePackProfile.create(
                        id.getPath(),
                        displayName,
                        false,
                        (name) -> pack,
                        event.getPackType(),
                        ResourcePackProfile.InsertionPosition.TOP,
                        ResourcePackSource.BUILTIN
                )));
            }
        });
    }

    public static void registerReloadListener(IEventBus modEventBus, ResourceType resourceType, ResourceReloader listener) {
        if (resourceType == ResourceType.CLIENT_RESOURCES) {
            modEventBus.<RegisterClientReloadListenersEvent>addListener(event -> {
                event.registerReloadListener(listener);
            });
        } else if (resourceType == ResourceType.SERVER_DATA) {
            modEventBus.<AddReloadListenerEvent>addListener(event -> {
                event.addListener(listener);
            });
        }
    }
}
