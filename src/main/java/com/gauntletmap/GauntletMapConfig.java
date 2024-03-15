package com.gauntletmap;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;
import net.runelite.client.config.Range;
import net.runelite.client.config.Units;

@ConfigGroup("Gauntlet Map")
public interface GauntletMapConfig extends Config
{

	@ConfigSection(
		name = "Overlay display options",
		description = "How the overlay is displayed on screen",
		position = 0
	)
	String overlayStyleSection = "overlayStyleSection";

	@ConfigItem(
		position = 0,
		keyName = "showOverlay",
		name = "Show map as overlay",
		description = "This will display the map on the screen as an overlay",
		section = overlayStyleSection
	)
	default boolean showOverlay() { return false; }

	@ConfigItem(
		position = 1,
		keyName = "overlayTileSize",
		name = "Overlay tile size",
		description = "This allows you to change the overlay map tile size",
		section = overlayStyleSection
	)
	@Units(Units.PIXELS)
	@Range(min = 1, max = 34)
	default int overlayTileSize() { return 34; }

	@ConfigItem(
		position = 2,
		keyName = "overlayOpacityPercentage",
		name = "Overlay opacity",
		description = "This allows you to change the overlay opacity",
		section = overlayStyleSection
	)
	@Units(Units.PERCENT)
	@Range(min = 1, max = 100)
	default int overlayOpacityPercentage() { return 100; }

	@ConfigSection(
		name = "Map guide options",
		description = "Settings for map guiding and highlighting in the Gauntlet",
		position = 1
	)
	String guideStyleSelection = "guideStyleSelection";

	@ConfigItem(
		position = 0,
		keyName = "showGuide",
		name = "Show map guide",
		description = "This will display guides to the rooms you choose",
		section = guideStyleSelection
	)
	default boolean useGuide() { return false; }

	@ConfigItem(
		position = 1,
		keyName = "guideRooms",
		name = "Guide rooms",
		description =
			"Enter a list of room numbers (1-49 with 25 being the Hunllef) " +
			"that you want guide markers for. For example, if you want to start by circling the Hunllef " +
			"you would enter 17, 18, 19, 26, 33, 32, ",
		section = guideStyleSelection
	)
	default String guideRooms() { return ""; }

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
