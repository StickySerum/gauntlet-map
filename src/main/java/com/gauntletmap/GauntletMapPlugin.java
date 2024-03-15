package com.gauntletmap;

import com.google.inject.Provides;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import javax.inject.Inject;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.NpcID;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.GameObjectSpawned;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.NpcDespawned;
import net.runelite.api.events.NpcSpawned;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.ImageUtil;

@Slf4j
@PluginDescriptor(
	name = "Gauntlet Map"
)
@Getter
public class GauntletMapPlugin extends Plugin
{
	private static final Integer CORRUPTED_GAUNTLET_REGION_ID = 7768;
	private static final Integer GAUNTLET_REGION_ID = 7512;

	@Inject
	private Client client;

	@Inject
	private ClientToolbar clientToolbar;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private GauntletMapPanel panel;

	@Inject
	private GauntletMapConfig config;

	@Inject
	private GauntletMapSession session;

	@Inject
	private GauntletMapOverlay mapOverlay;

	@Inject
	private GauntletMapGuide guideOverlay;

	@Getter
	@Setter
	private Map<Integer, String> fileNameMap;

	@Getter
	@Setter
	private Map<Integer, List<Integer>> connectedRoomsMap;

	private NavigationButton navButton;

	@Override
	protected void startUp() throws Exception
	{
		this.panel = injector.getInstance(GauntletMapPanel.class);

		BufferedImage icon = ImageUtil.loadImageResource(GauntletMapPlugin.class, "hunllef_icon.png");
		
		navButton = NavigationButton.builder()
			.tooltip("Gauntlet Map")
			.icon(icon)
			.priority(99)
			.panel(panel)
			.build();
		
		clientToolbar.addNavigation(navButton);
		
		createStartingMaps();

		overlayManager.add(mapOverlay);

//		overlayManager.add(guideOverlay);
	}

	@Override
	protected void shutDown() throws Exception
	{
		clientToolbar.removeNavigation(navButton);
	}

	//NEED TO CHECK THESE METHODS FOR CHANGES @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@

	@Subscribe
	public void onNpcSpawned(NpcSpawned npcSpawned)
	{
		if (!client.isInInstancedRegion() || isNotInGauntlet())
		{
			return;
		}

		WorldPoint player = client.getLocalPlayer().getWorldLocation();

		switch (npcSpawned.getNpc().getId())
		{
			case NpcID.CORRUPTED_HUNLLEF:
				this.session.setCorrupted(true);
			case NpcID.CRYSTALLINE_HUNLLEF:
				this.session.hunllefSpawned(player, npcSpawned.getActor().getWorldLocation());

			case NpcID.CORRUPTED_BEAR:
			case NpcID.CRYSTALLINE_BEAR:
			case NpcID.CORRUPTED_DARK_BEAST:
			case NpcID.CRYSTALLINE_DARK_BEAST:
			case NpcID.CORRUPTED_DRAGON:
			case NpcID.CRYSTALLINE_DRAGON:
				this.session.updateDemiBossLocations(player, npcSpawned.getNpc());
				break;
		}
	}

	@Subscribe
	public void onNpcDespawned(NpcDespawned npcDespawned)
	{
		if (!client.isInInstancedRegion() || isNotInGauntlet())
		{
			return;
		}

		WorldPoint player = client.getLocalPlayer().getWorldLocation();

		switch (npcDespawned.getNpc().getId())
		{
			case NpcID.CORRUPTED_BEAR:
			case NpcID.CRYSTALLINE_BEAR:
			case NpcID.CORRUPTED_DARK_BEAST:
			case NpcID.CRYSTALLINE_DARK_BEAST:
			case NpcID.CORRUPTED_DRAGON:
			case NpcID.CRYSTALLINE_DRAGON:
				this.session.updateDemiBossLocations(player, npcDespawned.getNpc());
				break;
		}
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged gameStateChanged)
	{
		this.session.gameStateChanged(gameStateChanged, client);
	}

	@Subscribe
	public void onGameTick(GameTick gameTick)
	{

		if (!client.isInInstancedRegion() || isNotInGauntlet())
		{
			return;
		}

		if (this.session.isNewSession())
		{
			return;
		}

		this.session.updateCurrentRoom(client.getLocalPlayer().getWorldLocation());
	}

	@Subscribe
	public void onGameObjectSpawned(GameObjectSpawned gameObjectSpawned)
	{
		if (!client.isInInstancedRegion() || isNotInGauntlet())
		{
			return;
		}

		this.session.gameObjectSpawned(gameObjectSpawned.getGameObject().getId(), gameObjectSpawned.getGameObject());
	}

	private void createStartingMaps()
	{
		Map<Integer, String> fileNameMap = new TreeMap<>();
		Map<Integer, List<Integer>> connectedRoomsMap = new TreeMap<>();

		for (int i = 1; i <= 49; i++)
		{
			List<Integer> connectedRoomsList = new ArrayList<>();

			switch (i)
			{
				case 1:
					connectedRoomsList.addAll(Arrays.asList(2, 8));
					fileNameMap.put(i, "_top_left.png");
					break;
				case 7:
					connectedRoomsList.addAll(Arrays.asList(6, 14));
					fileNameMap.put(i, "_top_right.png");
					break;
				case 43:
					connectedRoomsList.addAll(Arrays.asList(36, 44));
					fileNameMap.put(i, "_bottom_left.png");
					break;
				case 49:
					connectedRoomsList.addAll(Arrays.asList(42, 48));
					fileNameMap.put(i, "_bottom_right.png");
					break;
				case 8:
				case 15:
				case 22:
				case 29:
				case 36:
					connectedRoomsList.addAll(Arrays.asList(i - 7, i + 1, i + 7));
					fileNameMap.put(i, "_left.png");
					break;
				case 14:
				case 21:
				case 28:
				case 35:
				case 42:
					connectedRoomsList.addAll(Arrays.asList(i - 7, i - 1, i + 7));
					fileNameMap.put(i, "_right.png");
					break;
				case 2:
				case 3:
				case 4:
				case 5:
				case 6:
					connectedRoomsList.addAll(Arrays.asList(i - 1, i + 1, i + 7));
					fileNameMap.put(i, "_top.png");
					break;
				case 44:
				case 45:
				case 46:
				case 47:
				case 48:
					connectedRoomsList.addAll(Arrays.asList(i - 7, i - 1, i + 1));
					fileNameMap.put(i, "_bottom.png");
					break;
				default:
					connectedRoomsList.addAll(Arrays.asList(i - 7, i - 1, i + 1, i + 7));
					fileNameMap.put(i, ".png");
					break;
			}
			connectedRoomsMap.put(i, connectedRoomsList);
		}
		setFileNameMap(fileNameMap);
		setConnectedRoomsMap(connectedRoomsMap);
	}

	private boolean isNotInGauntlet()
	{
		for (int region : client.getMapRegions())
		{
			if (region == CORRUPTED_GAUNTLET_REGION_ID || region == GAUNTLET_REGION_ID)
			{
				return false;
			}
		}
		return true;
	}

	@Provides
	GauntletMapConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(GauntletMapConfig.class);
	}
}
