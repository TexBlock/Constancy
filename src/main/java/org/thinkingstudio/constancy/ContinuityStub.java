package org.thinkingstudio.constancy;

import me.pepperbell.continuity.client.ContinuityClient;
import net.minecraftforge.fml.IExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.network.NetworkConstants;

@Mod(ContinuityClient.ID)
public class ContinuityStub {

    @SuppressWarnings("removal")
    public ContinuityStub() {
        if (FMLLoader.getDist().isClient()) {
            ModLoadingContext.get().registerExtensionPoint(IExtensionPoint.DisplayTest.class, () -> new IExtensionPoint.DisplayTest(() -> NetworkConstants.IGNORESERVERONLY, (a, b) -> true));
        }
    }
}
