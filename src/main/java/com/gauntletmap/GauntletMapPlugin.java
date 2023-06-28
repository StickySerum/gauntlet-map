package com.gauntletmap;

import com.google.inject.Provides;

import javax.inject.Inject;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.*;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.util.ImageUtil;

import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;

@Slf4j
@PluginDescriptor(
	name = "Gauntlet Map"
)
public class GauntletMapPlugin extends Plugin
{
	@Inject
	private Client client;
	@Inject
	private GauntletMapConfig config;
	@Inject
	private ClientToolbar clientToolbar;
	private NavigationButton navButton;
	private GauntletMapPanel panel;

	@Getter
	@Setter
	private boolean firstLoad = true;

	@Getter
	@Setter
	private boolean corrupted;

	@Getter
	@Setter
	private boolean demiBoss;

	@Getter
	@Setter
	private String attackStyle;

	@Getter
	@Setter
	private Integer demiBossRoom;

	@Getter
	@Setter
	private Map<Integer, WorldPoint> centerTileMap;

	@Getter
	@Setter
	private Map<Integer, List<WorldPoint>> roomTilesMap;

	@Getter
	@Setter
	private Map<Integer, List<Integer>> connectedRoomsMap;

	@Getter
	@Setter
	private Integer startLocation;

	@Getter
	@Setter
	private Integer currentRoom;
	private final static int TILE_DISTANCE = 16;

	@Override
	protected void startUp() throws Exception
	{
		panel = injector.getInstance(GauntletMapPanel.class);

		BufferedImage icon = ImageUtil.loadImageResource(GauntletMapPlugin.class, "hunllef_icon.png");

		navButton = NavigationButton.builder()
			.tooltip("Gauntlet Map")
			.icon(icon)
			.priority(99)
			.panel(panel)
			.build();

		clientToolbar.addNavigation(navButton);
	}

	@Override
	protected void shutDown() throws Exception
	{
		clientToolbar.removeNavigation(navButton);
	}

	@Subscribe
	public void onNpcSpawned(NpcSpawned npcSpawned)
	{
		if (!client.isInInstancedRegion())
		{
			return;
		}

		if (npcSpawned.getNpc().getId() == NpcID.CORRUPTED_HUNLLEF || npcSpawned.getNpc().getId() == NpcID.CRYSTALLINE_HUNLLEF)
		{
			if (npcSpawned.getNpc().getId() == NpcID.CORRUPTED_HUNLLEF)
			{
				setCorrupted(true);
			}

			WorldPoint hunllef = npcSpawned.getActor().getWorldLocation();
			WorldPoint player = client.getLocalPlayer().getWorldLocation();

			setStartLocation(calculateActivatedRoom(player, hunllef));
			setCurrentRoom(getStartLocation());

			createInstanceMaps(player);

			panel.updateStartingTiles();
		}

		if (npcSpawned.getNpc().getId() == NpcID.CORRUPTED_DRAGON ||
			npcSpawned.getNpc().getId() == NpcID.CRYSTALLINE_DRAGON ||
			npcSpawned.getNpc().getId() == NpcID.CORRUPTED_DARK_BEAST ||
			npcSpawned.getNpc().getId() == NpcID.CRYSTALLINE_DARK_BEAST ||
			npcSpawned.getNpc().getId() == NpcID.CORRUPTED_BEAR ||
			npcSpawned.getNpc().getId() == NpcID.CRYSTALLINE_BEAR)
		{
			WorldPoint player = client.getLocalPlayer().getWorldLocation();
			setDemiBoss(true);
			setDemiBossRoom(calculateActivatedRoom(player, getCenterTileMap().get(getCurrentRoom())));

			switch (npcSpawned.getNpc().getId())
			{
				case NpcID.CORRUPTED_DRAGON:
				case NpcID.CRYSTALLINE_DRAGON:
					setAttackStyle("magic");
					break;

				case NpcID.CORRUPTED_DARK_BEAST:
				case NpcID.CRYSTALLINE_DARK_BEAST:
					setAttackStyle("ranged");
					break;

				case NpcID.CORRUPTED_BEAR:
				case NpcID.CRYSTALLINE_BEAR:
					setAttackStyle("melee");
					break;
			}

			panel.addNewActiveTile(calculateActivatedRoom(player, getCenterTileMap().get(getCurrentRoom())));
		}
	}

	@Subscribe
	public void onNpcDespawned(NpcDespawned npcDespawned)
	{
		if (npcDespawned.getNpc().getId() == NpcID.CORRUPTED_DRAGON ||
			npcDespawned.getNpc().getId() == NpcID.CRYSTALLINE_DRAGON ||
			npcDespawned.getNpc().getId() == NpcID.CORRUPTED_DARK_BEAST ||
			npcDespawned.getNpc().getId() == NpcID.CRYSTALLINE_DARK_BEAST ||
			npcDespawned.getNpc().getId() == NpcID.CORRUPTED_BEAR ||
			npcDespawned.getNpc().getId() == NpcID.CRYSTALLINE_BEAR)
		{
			setDemiBoss(false);

			WorldPoint npc = npcDespawned.getNpc().getWorldLocation();
			int[] rooms = {3, 4, 5, 15, 22, 29, 21, 28, 35, 45, 46, 47};

			for (int i = 0; i <= rooms.length; i++)
			{
				if (getRoomTilesMap().get(rooms[i]).contains(npc))
				{
					panel.addNewActiveTile(rooms[i]);
				}
			}

		}
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged gameStateChanged)
	{
		//Reset variables
		if (gameStateChanged.getGameState() == GameState.LOADING && !client.isInInstancedRegion() && !firstLoad)
		{
			setFirstLoad(true);
			setCorrupted(false);
			setStartLocation(null);
			setCurrentRoom(null);
			setCenterTileMap(null);
			setConnectedRoomsMap(null);
			setRoomTilesMap(null);
			setDemiBoss(false);
			setAttackStyle("");
			panel.clearMapTiles();
			return;
		}

		//Check for first load && update flag
		if (gameStateChanged.getGameState() == GameState.LOADING && client.isInInstancedRegion() && firstLoad)
		{
			setFirstLoad(false);
			return;
		}

		//Confirm not first load && update map panel
		if (gameStateChanged.getGameState() == GameState.LOADING && client.isInInstancedRegion() && !firstLoad)
		{
			panel.addNewActiveTile(calculateActivatedRoom(client.getLocalPlayer().getWorldLocation(), getCenterTileMap().get(getCurrentRoom())));
		}

		if (gameStateChanged.getGameState() == GameState.LOGIN_SCREEN)
		{
			panel.clearMapTiles();
		}
	}

	@Subscribe
	public void onGameTick(GameTick gameTick)
	{
		if (!client.isInInstancedRegion())
		{
			return;
		}

		updateCurrentRoom(client.getLocalPlayer().getWorldLocation());
	}

	private Integer calculateActivatedRoom(WorldPoint player, WorldPoint target)
	{
		Integer difference;

		if (Math.abs(player.getY() - target.getY()) > Math.abs(player.getX() - target.getX()))
		{
			if (player.getY() > target.getY())
			{
				//North
				difference = -7;
			}
			else
			{
				//South
				difference = 7;
			}
		}
		else
		{
			if (player.getX() > target.getX())
			{
				//East
				difference = 1;
			}
			else
			{
				//West
				difference = -1;
			}
		}

		//If start of instance use boss room as current room
		if (getCurrentRoom() == null)
		{
			setCurrentRoom(25);
		}

		return (getCurrentRoom() + difference);
	}

	private void createInstanceMaps(WorldPoint playerLocation)
	{
		WorldPoint northWestCornerRoom = null;
		Map<Integer, WorldPoint> centerTileMap = new TreeMap<>();
		Map<Integer, List<WorldPoint>> roomTilesMap = new TreeMap<>();
		Map<Integer, List<Integer>> connectedRoomsMap = new TreeMap<>();

		switch (getStartLocation())
		{
			//North start
			case 18:
				northWestCornerRoom = calculateNewPoint(playerLocation, -3, 2, -3, -2);
				break;

			//East start
			case 26:
				northWestCornerRoom = calculateNewPoint(playerLocation, -4, 3, -3, -2);
				break;

			//South start
			case 32:
				northWestCornerRoom = calculateNewPoint(playerLocation, -3, 4, -3, -2);
				break;

			//West start
			case 24:
				northWestCornerRoom = calculateNewPoint(playerLocation, -2, 3, -3, -2);
				break;
		}

		//Loop through 7x7 grid and calculate center points
		//Loop through 12x12 grid and calculate all coordinates in a room
		//Loop through 2x2 grid to calculate coordinates outside 12x12 room
		//Create list of connected rooms for the current room

		for (int i = 0; i <= 6; i++)
		{
			for (int j = 0; j <= 6; j++)
			{
				WorldPoint centerTile = calculateNewPoint(northWestCornerRoom, j, -i, 0, 0);
				WorldPoint northWestCornerTile = calculateNewPoint(centerTile, 0, 0, -6, 5);
				List<WorldPoint> roomTiles = new ArrayList<>();
				List<Integer> connectedRoomsList = new ArrayList<>();
				int room = (i * 7 + j + 1);

				for (int k = 0; k <= 11; k++)
				{
					for (int l = 0; l <= 11; l++)
					{
						roomTiles.add(calculateNewPoint(northWestCornerTile, 0, 0, l, -k));
					}
				}

				for (int m = 0; m <= 1; m++)
				{
					for (int n = 0; n <= 1; n++)
					{
						roomTiles.add(calculateNewPoint(centerTile, 0, 0, -n, 7 - m));
						roomTiles.add(calculateNewPoint(centerTile, 0, 0, 7 - n, -m));
						roomTiles.add(calculateNewPoint(centerTile, 0, 0, -n, -7 - m));
						roomTiles.add(calculateNewPoint(centerTile, 0, 0, -7 - n, -m));
					}
				}

				if (room == 1 || room == 7 || room == 43 || room == 49)
				{
					switch (room)
					{
						case 1:
							connectedRoomsList.addAll(Arrays.asList(2, 8));
							break;
						case 7:
							connectedRoomsList.addAll(Arrays.asList(6, 14));
							break;
						case 43:
							connectedRoomsList.addAll(Arrays.asList(36, 44));
							break;
						case 49:
							connectedRoomsList.addAll(Arrays.asList(42, 48));
							break;
					}
				}
				else if (room % 7 == 1)
				{
					connectedRoomsList.addAll(Arrays.asList(room - 7, room + 1, room + 7));
				}
				else if (room % 7 == 0)
				{
					connectedRoomsList.addAll(Arrays.asList(room - 7, room - 1, room + 7));
				}
				else if (room > 1 && room < 7)
				{
					connectedRoomsList.addAll(Arrays.asList(room - 1, room + 1, room + 7));
				}
				else if (room > 43 && room < 49)
				{
					connectedRoomsList.addAll(Arrays.asList(room - 7, room - 1, room + 1));
				}
				else
				{
					connectedRoomsList.addAll(Arrays.asList(room - 7, room - 1, room + 1, room + 7));
				}

				centerTileMap.put(room, centerTile);
				roomTilesMap.put(room, roomTiles);
				connectedRoomsMap.put(room, connectedRoomsList);
			}
		}

		setCenterTileMap(centerTileMap);
		setRoomTilesMap(roomTilesMap);
		setConnectedRoomsMap(connectedRoomsMap);
	}

	private WorldPoint calculateNewPoint(WorldPoint startPoint, Integer roomsX, Integer roomsY, Integer tilesX, Integer tilesY)
	{
		WorldPoint newPoint = new WorldPoint
			(
				startPoint.getX() + (roomsX * TILE_DISTANCE) + tilesX,
				startPoint.getY() + (roomsY * TILE_DISTANCE) + tilesY,
				startPoint.getPlane()
			);
		return newPoint;
	}

	private void updateCurrentRoom(WorldPoint playerLocation)
	{
		if (getRoomTilesMap().get(getCurrentRoom()).contains(playerLocation))
		{
			return;
		}

		if (getRoomTilesMap().get(getStartLocation()).contains(playerLocation))
		{
			int previousRoom = getCurrentRoom();
			setCurrentRoom(getStartLocation());
			panel.updatePlayerIcon(previousRoom);
			return;
		}

		for (Integer connectedRoom : getConnectedRoomsMap().get(getCurrentRoom()))
		{
			if (getRoomTilesMap().get(connectedRoom).contains(playerLocation))
			{
				int previousRoom = getCurrentRoom();
				setCurrentRoom(connectedRoom);
				panel.updatePlayerIcon(previousRoom);
			}
		}
	}

	@Provides
	GauntletMapConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(GauntletMapConfig.class);
	}
}
