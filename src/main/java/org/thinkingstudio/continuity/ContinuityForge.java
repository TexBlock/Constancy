package org.thinkingstudio.continuity;

import me.pepperbell.continuity.client.ContinuityClient;
import me.pepperbell.continuity.client.config.ModMenuApiImpl;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.IExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.network.NetworkConstants;

@Mod("continuity")
public class ContinuityForge {
    public ContinuityForge() {
        if (FMLLoader.getDist().isClient()) {
            IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
            ModLoadingContext.get().registerExtensionPoint(IExtensionPoint.DisplayTest.class, () -> new IExtensionPoint.DisplayTest(() -> NetworkConstants.IGNORESERVERONLY, (a, b) -> true));
            ModLoadingContext.get().registerExtensionPoint(ConfigScreenHandler.ConfigScreenFactory.class, () -> new ModMenuApiImpl().getModConfigScreenFactory());
            new ContinuityClient().onInitializeClient(modEventBus);
        }
    }
}
