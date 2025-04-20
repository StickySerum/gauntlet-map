package com.gauntletmap;

import com.google.inject.Provides;

import javax.inject.Inject;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.GameObjectDespawned;
import net.runelite.api.events.GameObjectSpawned;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.NpcDespawned;
import net.runelite.api.events.NpcSpawned;
import net.runelite.api.gameval.VarbitID;
import net.runelite.api.events.VarbitChanged;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.ImageUtil;

import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;

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
	private DemiBossOverlay demiBossOverlay;

	@Inject
	private ClientThread clientThread;

	private Map<Integer, String> fileNameMap;

	private Map<Integer, List<Integer>> connectedRoomsMap;

	private NavigationButton navButton;

	@Override
	protected void startUp() throws Exception
	{
		this.panel = injector.getInstance(GauntletMapPanel.class);

		BufferedImage icon = ImageUtil.loadImageResource(GauntletMapPlugin.class, "icon.png");
		
		navButton = NavigationButton.builder()
			.tooltip("Gauntlet Map")
			.icon(icon)
			.priority(99)
			.panel(panel)
			.build();
		
		clientToolbar.addNavigation(navButton);

		createStartingMaps();

		overlayManager.add(mapOverlay);

		overlayManager.add(demiBossOverlay);
	}

	@Override
	protected void shutDown() throws Exception
	{
		clientToolbar.removeNavigation(navButton);
		overlayManager.remove(demiBossOverlay);
		overlayManager.remove(mapOverlay);
	}

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
				session.setCorrupted(true);
			case NpcID.CRYSTALLINE_HUNLLEF:
				session.hunllefSpawned(player, npcSpawned.getActor().getWorldLocation());
				break;

			case NpcID.CORRUPTED_BEAR:
			case NpcID.CRYSTALLINE_BEAR:
			case NpcID.CORRUPTED_DARK_BEAST:
			case NpcID.CRYSTALLINE_DARK_BEAST:
			case NpcID.CORRUPTED_DRAGON:
			case NpcID.CRYSTALLINE_DRAGON:
				session.updateDemiBossLocations(player, npcSpawned.getNpc());
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
				session.updateDemiBossLocations(player, npcDespawned.getNpc());
				break;
		}
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged gameStateChanged)
	{
		session.gameStateChanged(gameStateChanged, client);
	}

	@Subscribe
	public void onGameTick(GameTick gameTick)
	{

		if (!client.isInInstancedRegion() || isNotInGauntlet())
		{
			return;
		}

		if (session.isNewSession())
		{
			return;
		}

		session.updateCurrentRoom(client.getLocalPlayer().getWorldLocation());
	}

	@Subscribe
	public void onGameObjectSpawned(GameObjectSpawned gameObjectSpawned)
	{
		if (!client.isInInstancedRegion() || isNotInGauntlet())
		{
			return;
		}

		session.gameObjectSpawned(gameObjectSpawned.getGameObject());
	}

	@Subscribe
	public void onGameObjectDespawned(GameObjectDespawned gameObjectDespawned)
	{
		if (!client.isInInstancedRegion() || isNotInGauntlet())
		{
			return;
		}

		session.gameObjectDespawned(gameObjectDespawned.getGameObject());
	}

	@Subscribe
	public void onVarbitChanged(VarbitChanged event)
	{
		if(event.getVarbitId() == VarbitID.GAUNTLET_BOSS_STARTED && event.getValue() == 1)
		{
			session.stop();
		}
	}

	private void createStartingMaps()
	{
		Map<Integer, String> fileNameMap = new TreeMap<>();
		Map<Integer, List<Integer>> connectedRoomsMap = new TreeMap<>();

		for (int room = 1; room <= 49; room++)
		{
			List<Integer> connectedRoomsList = new ArrayList<>();

			switch (room)
			{
				case 1:
					connectedRoomsList.addAll(Arrays.asList(2, 8));
					fileNameMap.put(room, "_top_left.png");
					break;
				case 7:
					connectedRoomsList.addAll(Arrays.asList(6, 14));
					fileNameMap.put(room, "_top_right.png");
					break;
				case 43:
					connectedRoomsList.addAll(Arrays.asList(36, 44));
					fileNameMap.put(room, "_bottom_left.png");
					break;
				case 49:
					connectedRoomsList.addAll(Arrays.asList(42, 48));
					fileNameMap.put(room, "_bottom_right.png");
					break;
				case 8:
				case 15:
				case 22:
				case 29:
				case 36:
					connectedRoomsList.addAll(Arrays.asList(room - 7, room + 1, room + 7));
					fileNameMap.put(room, "_left.png");
					break;
				case 14:
				case 21:
				case 28:
				case 35:
				case 42:
					connectedRoomsList.addAll(Arrays.asList(room - 7, room - 1, room + 7));
					fileNameMap.put(room, "_right.png");
					break;
				case 2:
				case 3:
				case 4:
				case 5:
				case 6:
					connectedRoomsList.addAll(Arrays.asList(room - 1, room + 1, room + 7));
					fileNameMap.put(room, "_top.png");
					break;
				case 44:
				case 45:
				case 46:
				case 47:
				case 48:
					connectedRoomsList.addAll(Arrays.asList(room - 7, room - 1, room + 1));
					fileNameMap.put(room, "_bottom.png");
					break;
				default:
					connectedRoomsList.addAll(Arrays.asList(room - 7, room - 1, room + 1, room + 7));
					fileNameMap.put(room, ".png");
					break;
			}
			connectedRoomsMap.put(room, connectedRoomsList);
		}
		this.fileNameMap = fileNameMap;
		this.connectedRoomsMap = connectedRoomsMap;
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
