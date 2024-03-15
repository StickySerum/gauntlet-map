package com.gauntletmap;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.Getter;
import lombok.Setter;
import net.runelite.api.Client;
import net.runelite.api.GameObject;
import net.runelite.api.NPC;
import net.runelite.api.NpcID;
import net.runelite.api.ObjectID;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.GameStateChanged;
import net.runelite.client.util.ImageUtil;

@Getter
@Singleton
public class GauntletMapSession
{
	private final static int TILE_DISTANCE = 16;
	private final static int BOSS_ROOM = 25;

	private enum MapIcons
	{
		PLAYER,
		BOSS,
		ACTIVE_TILE,
		DEMIBOSS_UNKNOWN,
		DEMIBOSS_MAGIC,
		DEMIBOSS_MELEE,
		DEMIBOSS_RANGED,
		FISHING_SPOT,
		GRYM_ROOT,
		DEPOSIT,
		LINUM_TIRINUM,
		PHREN_ROOTS
	}

	@Setter
	private GauntletMapPlugin plugin;

	@Setter
	private GauntletMapConfig config;

	@Setter
	private boolean corrupted = false;

	@Setter
	private boolean newSession = true;

	@Setter
	private Map<Integer, BufferedImage> gauntletMap;

	@Setter
	private Map<Integer, WorldPoint> centerTileMap;

	@Setter
	private Map<Integer, List<WorldPoint>> roomTilesMap;

	@Setter
	private Map<Integer, MapIcons> demiBossLocationsMap = new TreeMap<>();

	@Setter
	private Map<Integer, List<GameObject>> highlightNodeMap = new TreeMap<>();

	@Setter
	private Integer startLocation;

	@Setter
	private Integer currentRoom;

	@Inject
	GauntletMapSession(GauntletMapPlugin plugin, GauntletMapConfig config)
	{
		setPlugin(plugin);
		setConfig(config);
	}

	public void createInstanceMaps(WorldPoint playerLocation)
	{
		WorldPoint northWestCornerRoom = null;
		Map<Integer, WorldPoint> centerTileMap = new TreeMap<>();
		Map<Integer, List<WorldPoint>> roomTilesMap = new TreeMap<>();
		Map<Integer, BufferedImage> gauntletMap = new TreeMap<>();


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

		for (int i = 0; i <= 6; i++)
		{
			for (int j = 0; j <= 6; j++)
			{
				WorldPoint centerTile = calculateNewPoint(northWestCornerRoom, j, -i, 0, 0);
				WorldPoint northWestCornerTile = calculateNewPoint(centerTile, 0, 0, -6, 5);
				List<WorldPoint> roomTiles = new ArrayList<>();
				int room = (i * 7 + j + 1);

				String path = "inactive" + plugin.getFileNameMap().get(room);
				gauntletMap.put(room, ImageUtil.loadImageResource(GauntletMapPlugin.class, path));

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

				centerTileMap.put(room, centerTile);
				roomTilesMap.put(room, roomTiles);

			}
		}

		setGauntletMap(gauntletMap);
		setCenterTileMap(centerTileMap);
		setRoomTilesMap(roomTilesMap);
	}

	private WorldPoint calculateNewPoint(WorldPoint startPoint, Integer roomsX, Integer roomsY, Integer tilesX, Integer tilesY)
	{
		return new WorldPoint
			(
				startPoint.getX() + (roomsX * TILE_DISTANCE) + tilesX,
				startPoint.getY() + (roomsY * TILE_DISTANCE) + tilesY,
				startPoint.getPlane()
			);
	}

	public Integer calculateActivatedRoom(WorldPoint player, WorldPoint target)
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

	public void updateCurrentRoom(WorldPoint playerLocation)
	{
		//If player hasn't left the room
		if (getRoomTilesMap().get(getCurrentRoom()).contains(playerLocation))
		{
			return;
		}

		//If player teleports to starting room
		if (getRoomTilesMap().get(getStartLocation()).contains(playerLocation))
		{
			int previousRoom = getCurrentRoom();
			setCurrentRoom(getStartLocation());

			updateGauntletMap(previousRoom, MapIcons.ACTIVE_TILE);
			updateGauntletMap(getCurrentRoom(), MapIcons.PLAYER);
			return;
		}

		//Next room can only be connected to previous room -- Check connected rooms
		for (Integer connectedRoom : plugin.getConnectedRoomsMap().get(getCurrentRoom()))
		{
			if (getRoomTilesMap().get(connectedRoom).contains(playerLocation))
			{
				int previousRoom = getCurrentRoom();
				setCurrentRoom(connectedRoom);

				updateGauntletMap(previousRoom, getDemiBossLocationsMap().getOrDefault(previousRoom, MapIcons.ACTIVE_TILE));
				updateGauntletMap(getCurrentRoom(), MapIcons.PLAYER);
			}
		}
	}

	public void hunllefSpawned(WorldPoint player, WorldPoint hunllef)
	{
		setStartLocation(calculateActivatedRoom(player, hunllef));
		setCurrentRoom(getStartLocation());
		createInstanceMaps(player);

		updateGauntletMap(getCurrentRoom(), MapIcons.PLAYER);
		updateGauntletMap(BOSS_ROOM, MapIcons.BOSS);

		if (this.config.showDemiBosses())
		{
			List<Integer> demiRoomList = Arrays.asList(3, 4, 5, 15, 21, 22, 28, 29, 35, 45, 46, 47);
			for (int i = 0; i <= demiRoomList.size() - 1; i++)
			{
				updateGauntletMap(demiRoomList.get(i), MapIcons.DEMIBOSS_UNKNOWN);
			}
		}

		plugin.getPanel().firstLoad();
		setNewSession(false);
	}

	public void updateDemiBossLocations(WorldPoint player, NPC demiBoss)
	{
		int room = calculateActivatedRoom(player, getCenterTileMap().get(getCurrentRoom()));

		if (demiBoss.isDead())
		{
			for (Map.Entry<Integer, MapIcons> entry : getDemiBossLocationsMap().entrySet())
			{
				if (getRoomTilesMap().get(entry.getKey()).contains(demiBoss.getWorldLocation()))
				{
					room = entry.getKey();
				}
			}

			getDemiBossLocationsMap().remove(room);

			if (!getRoomTilesMap().get(room).contains(player))
			{
				updateGauntletMap(room, MapIcons.ACTIVE_TILE);
			}

			return;
		}

		switch (demiBoss.getId())
		{
			case NpcID.CRYSTALLINE_BEAR:
			case NpcID.CORRUPTED_BEAR:
				getDemiBossLocationsMap().put(room, MapIcons.DEMIBOSS_MELEE);
				updateGauntletMap(room, MapIcons.DEMIBOSS_MELEE);
				break;
			case NpcID.CRYSTALLINE_DRAGON:
			case NpcID.CORRUPTED_DRAGON:
				getDemiBossLocationsMap().put(room, MapIcons.DEMIBOSS_MAGIC);
				updateGauntletMap(room, MapIcons.DEMIBOSS_MAGIC);
				break;
			case NpcID.CRYSTALLINE_DARK_BEAST:
			case NpcID.CORRUPTED_DARK_BEAST:
				getDemiBossLocationsMap().put(room, MapIcons.DEMIBOSS_RANGED);
				updateGauntletMap(room, MapIcons.DEMIBOSS_RANGED);
				break;
		}
	}

	public void gameObjectSpawned(Integer id, GameObject gameObject)
	{
		WorldPoint player = plugin.getClient().getLocalPlayer().getWorldLocation();
		int room = calculateActivatedRoom(player, getCenterTileMap().get(getCurrentRoom()));
		List<GameObject> gameObjectList = new ArrayList<>();
		//CHECK FOR DEMI BOSS ROOMS AND CONFIG SETTINGS

		switch (id)
		{
			case ObjectID.FISHING_SPOT_36068:
			case ObjectID.CORRUPT_FISHING_SPOT:
				updateGauntletMap(room, MapIcons.FISHING_SPOT);
				break;

			case ObjectID.GRYM_ROOT:
			case ObjectID.CORRUPT_GRYM_ROOT:
				updateGauntletMap(room, MapIcons.GRYM_ROOT);
				break;

			case ObjectID.CRYSTAL_DEPOSIT:
			case ObjectID.CORRUPT_DEPOSIT:
				updateGauntletMap(room, MapIcons.DEPOSIT);
				break;

			case ObjectID.LINUM_TIRINUM:
			case ObjectID.CORRUPT_LINUM_TIRINUM:
				updateGauntletMap(room, MapIcons.LINUM_TIRINUM);
				break;

			case ObjectID.PHREN_ROOTS:
			case ObjectID.CORRUPT_PHREN_ROOTS:
				updateGauntletMap(room, MapIcons.PHREN_ROOTS);
				break;

			case ObjectID.NODE_35998:
			case ObjectID.NODE_35999:
			case ObjectID.NODE_36101:
			case ObjectID.NODE_36102:
//				if (getRoomTilesMap().get(room).contains(gameObject.getWorldLocation()))
//				{
//					if (getHighlightNodeMap().containsKey(room))
//					{
//						getHighlightNodeMap().get(room).add(gameObject);
//						break;
//					}
//					gameObjectList.add(gameObject);
//					getHighlightNodeMap().put(room, gameObjectList);
//				}
				break;
		}
	}

	public void gameStateChanged(GameStateChanged gameStateChanged, Client client)
	{
		switch (gameStateChanged.getGameState())
		{
			case LOADING:
				//Reset session variables when leaving the instance
				if (!client.isInInstancedRegion() && !isNewSession())
				{
					setNewSession(true);
					setCorrupted(false);
					setCurrentRoom(null);
					plugin.getPanel().clearPanel();
					return;
				}

				//Update session while inside the instance
				if (client.isInInstancedRegion() && !isNewSession())
				{
					int activatedRoom = calculateActivatedRoom(client.getLocalPlayer().getWorldLocation(), getCenterTileMap().get(getCurrentRoom()));
					updateGauntletMap(activatedRoom, MapIcons.ACTIVE_TILE);
				}

				break;
			case LOGIN_SCREEN:
				plugin.getPanel().clearPanel();
				break;
		}
	}

	private void updateGauntletMap(Integer room, MapIcons icon)
	{
		String path;
		String type = "regular/";

		if (isCorrupted())
		{
			type = "corrupted/";
		}

		switch (icon)
		{
			case PLAYER:
				path = type + "player" + plugin.getFileNameMap().get(room);
				if (room == getStartLocation())
				{
					path = type + "player_start.png";
				}
				getGauntletMap().put(room, ImageUtil.loadImageResource(GauntletMapPlugin.class, path));
				break;

			case BOSS:
				path = type + "hunllef" + plugin.getFileNameMap().get(room);
				getGauntletMap().put(room, ImageUtil.loadImageResource(GauntletMapPlugin.class, path));
				break;

			case ACTIVE_TILE:
				path = "active" + plugin.getFileNameMap().get(room);
				if (room == getStartLocation())
				{
					path = type + "start_room.png";
				}
				getGauntletMap().put(room, ImageUtil.loadImageResource(GauntletMapPlugin.class, path));
				break;

			case DEMIBOSS_UNKNOWN:
				path = type + "demi" + plugin.getFileNameMap().get(room);
				getGauntletMap().put(room, ImageUtil.loadImageResource(GauntletMapPlugin.class, path));
				break;

			case DEMIBOSS_MAGIC:
				path = "demiboss/magic" + plugin.getFileNameMap().get(room);
				getGauntletMap().put(room, ImageUtil.loadImageResource(GauntletMapPlugin.class, path));
				break;

			case DEMIBOSS_MELEE:
				path = "demiboss/melee" + plugin.getFileNameMap().get(room);
				getGauntletMap().put(room, ImageUtil.loadImageResource(GauntletMapPlugin.class, path));
				break;

			case DEMIBOSS_RANGED:
				path = "demiboss/ranged" + plugin.getFileNameMap().get(room);
				getGauntletMap().put(room, ImageUtil.loadImageResource(GauntletMapPlugin.class, path));
				break;
		}

		if (!isNewSession())
		{
			plugin.getPanel().updatePanel(room);
		}
	}

	public BufferedImage scaleImage(Integer size, BufferedImage image)
	{
		if (size == 34)
		{
			return image;
		}

		Image scaledImage = image.getScaledInstance(size, size, Image.SCALE_DEFAULT);
		BufferedImage bufferedImage = new BufferedImage(scaledImage.getWidth(null), scaledImage.getHeight(null), BufferedImage.TYPE_INT_ARGB);
		bufferedImage.getGraphics().drawImage(scaledImage, 0, 0, null);
		return bufferedImage;
	}
}
