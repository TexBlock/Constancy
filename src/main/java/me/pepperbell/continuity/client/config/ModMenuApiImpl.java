package me.pepperbell.continuity.client.config;

import net.minecraftforge.client.ConfigScreenHandler;

public class ModMenuApiImpl {
	public ConfigScreenHandler.ConfigScreenFactory getModConfigScreenFactory() {
		return new ConfigScreenHandler.ConfigScreenFactory(parent -> new ContinuityConfigScreen(parent, ContinuityConfig.INSTANCE));
	}
}
