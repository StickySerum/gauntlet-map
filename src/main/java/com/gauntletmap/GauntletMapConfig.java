package com.gauntletmap;

import java.awt.*;
import java.util.List;
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
		description = "(ALT + RIGHT CLICK TO FIX ANY SIZING PROBLEMS!) This allows you to change the overlay map tile size",
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
		name = "Resource display options",
		description = "Settings for showing resources in the Gauntlet",
		position = 1
	)
	String resourceStyleSelection = "resourceStyleSelection";

	@ConfigItem(
		position = 0,
		keyName = "showFishingSpots",
		name = "Show fishing spots",
		description = "This will display fishing spots on the map",
		section = resourceStyleSelection
	)
	default boolean showFishingSpots() { return true; }

	@ConfigItem(
		position = 1,
		keyName = "showGrymLeaves",
		name = "Show grym leaves",
		description = "This will display grym leaves on the map",
		section = resourceStyleSelection
	)
	default boolean showGrymLeaves() { return true; }

	@ConfigSection(
		name = "Demi boss options",
		description = "How to display demi boss rooms",
		position = 2
	)
	String demiBossStyleSelection = "demiBossStyleSelection";

	@ConfigItem(
		position = 1,
		keyName = "showDemiBossLocations",
		name = "Show demi boss locations",
		description = "This will show where the demi bosses are located on the map",
		section = demiBossStyleSelection
	)
	default boolean showDemiBosses()
	{
		return true;
	}

	@ConfigItem(
		position = 2,
		keyName = "demiBossOutlineColor",
		name = "Outline color",
		description = "Choose the color to outline demi boss nodes",
		section = demiBossStyleSelection
	)
	default Color demiBossOutlineColor()
	{
		return Color.YELLOW;
	}

	@ConfigItem(
			position = 3,
			keyName = "demiBossOutlineSize",
			name = "Outline size",
			description = "The size of the outline",
			section = demiBossStyleSelection
	)
	default int demiBossOutlineSize() { return 1; }

}
