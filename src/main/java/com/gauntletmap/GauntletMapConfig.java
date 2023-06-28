package com.gauntletmap;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("Gauntlet Map")
public interface GauntletMapConfig extends Config
{
	@ConfigItem(
		keyName = "showDemiBossLocations",
		name = "Show Demi Boss Locations",
		description = "This will show where the demi bosses are located on the map"
	)
	default boolean showDemiBosses()
	{
		return true;
	}
}
