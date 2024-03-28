package net.runelite.client.plugins.gauntletmap;

import com.google.common.collect.Lists;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
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

@Singleton
public class GauntletMapSession
{
	private final GauntletMapPlugin plugin;

	private final GauntletMapConfig config;

	private static final int TILE_DISTANCE = 16;

	private static final int BOSS_ROOM = 25;

	public static final List<Integer> DEMI_ROOM_LIST = List.of(3, 4, 5, 15, 21, 22, 28, 29, 35, 45, 46, 47);

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
		FISHING_SPOT_GRYM_ROOT
	}

	private Map<Integer, MapIcons> demiBossLocationsMap = new TreeMap<>();

	@Getter
	private Map<Integer, List<NodeGameObject>> highlightNodeMap = new TreeMap<>();

	private Map<Integer, List<Integer>> demiBossNodeLocationsMap = new TreeMap<>();

	private Map<Integer, List<Integer>> roomResourcesMap = new TreeMap<>();

	private List<Integer> activeRoomsList = new ArrayList<>();

	@Getter
	private Map<Integer, BufferedImage> gauntletMap;
	
	private Map<Integer, WorldPoint> centerTileMap;

	private Map<Integer, List<WorldPoint>> roomTilesMap;
	
	private Integer startLocation;
	
	private Integer currentRoom;

	@Setter
	private boolean corrupted = false;

	@Getter
	private boolean newSession = true;

	@Inject
	GauntletMapSession(GauntletMapPlugin plugin, GauntletMapConfig config)
	{
		this.plugin = plugin;
		this.config = config;
	}

	public void createInstanceMaps(WorldPoint playerLocation)
	{
		WorldPoint northWestCornerRoom = null;
		Map<Integer, WorldPoint> centerTileMap = new TreeMap<>();
		Map<Integer, List<WorldPoint>> roomTilesMap = new TreeMap<>();
		Map<Integer, BufferedImage> gauntletMap = new TreeMap<>();

		switch (startLocation)
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

		for (int gauntletMapY = 0; gauntletMapY <= 6; gauntletMapY++)
		{
			for (int gauntletMapX = 0; gauntletMapX <= 6; gauntletMapX++)
			{
				WorldPoint centerTile = calculateNewPoint(northWestCornerRoom, gauntletMapX, -gauntletMapY, 0, 0);
				WorldPoint northWestCornerTile = calculateNewPoint(centerTile, 0, 0, -6, 5);
				List<WorldPoint> roomTiles = new ArrayList<>();
				int room = (gauntletMapY * 7 + gauntletMapX + 1);

				String path = "inactive" + plugin.getFileNameMap().get(room);
				gauntletMap.put(room, ImageUtil.loadImageResource(GauntletMapPlugin.class, path));

				if (!plugin.getConnectedRoomsMap().get(room).contains(startLocation))
				{
					plugin.getConnectedRoomsMap().get(room).add(startLocation);
				}

				for (int roomY = 0; roomY <= 11; roomY++)
				{
					for (int roomX = 0; roomX <= 11; roomX++)
					{
						roomTiles.add(calculateNewPoint(northWestCornerTile, 0, 0, roomX, -roomY));
					}
				}

				for (int roomEntranceY = 0; roomEntranceY <= 1; roomEntranceY++)
				{
					for (int roomEntranceX = 0; roomEntranceX <= 1; roomEntranceX++)
					{
						roomTiles.add(calculateNewPoint(centerTile, 0, 0, -roomEntranceX, 7 - roomEntranceY));
						roomTiles.add(calculateNewPoint(centerTile, 0, 0, 7 - roomEntranceX, -roomEntranceY));
						roomTiles.add(calculateNewPoint(centerTile, 0, 0, -roomEntranceX, -7 - roomEntranceY));
						roomTiles.add(calculateNewPoint(centerTile, 0, 0, -7 - roomEntranceX, -roomEntranceY));
					}
				}

				for (int nodeTiles = 0; nodeTiles <= 1; nodeTiles++)
				{
					//North nodes
					roomTiles.add(calculateNewPoint(centerTile, 0, 0, -2, 7 - nodeTiles));
					roomTiles.add(calculateNewPoint(centerTile, 0, 0, 1, 7 - nodeTiles));

					//South nodes
					roomTiles.add(calculateNewPoint(centerTile, 0, 0, -2, -7 - nodeTiles));
					roomTiles.add(calculateNewPoint(centerTile, 0, 0, 1, -7 - nodeTiles));

					//East nodes
					roomTiles.add(calculateNewPoint(centerTile, 0, 0, 7 - nodeTiles, 1));
					roomTiles.add(calculateNewPoint(centerTile, 0, 0, 7 - nodeTiles, -2));

					//West nodes
					roomTiles.add(calculateNewPoint(centerTile, 0, 0, -7 - nodeTiles, 1));
					roomTiles.add(calculateNewPoint(centerTile, 0, 0, -7 - nodeTiles, -2));
				}

				centerTileMap.put(room, centerTile);
				roomTilesMap.put(room, roomTiles);

			}
		}

		this.gauntletMap = gauntletMap;
		this.centerTileMap = centerTileMap;
		this.roomTilesMap = roomTilesMap;
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
		if (currentRoom == null)
		{
			currentRoom = 25;
		}

		return (currentRoom + calculateOrientation(player, target));
	}

	public Integer calculateOrientation(WorldPoint target, WorldPoint referencePoint)
	{
		if (Math.abs(target.getY() - referencePoint.getY()) > Math.abs(target.getX() - referencePoint.getX()))
		{
			if (target.getY() > referencePoint.getY())
			{
				//North
				return -7;
			}
			else
			{
				//South
				return 7;
			}
		}
		else
		{
			if (target.getX() > referencePoint.getX())
			{
				//East
				return 1;
			}
			else
			{
				//West
				return -1;
			}
		}
	}

	public void updateCurrentRoom(WorldPoint playerLocation)
	{
		//If player hasn't left the room
		if (roomTilesMap.get(currentRoom).contains(playerLocation))
		{
			return;
		}

		//Next room can only be connected to previous room -- Check connected rooms
		plugin.getConnectedRoomsMap().get(currentRoom).forEach(connectedRoom ->
		{
			if (roomTilesMap.get(connectedRoom).contains(playerLocation))
			{
				int previousRoom = currentRoom;
				currentRoom = connectedRoom;

				if (demiBossLocationsMap.containsKey(previousRoom))
				{
					updateGauntletMap(previousRoom, demiBossLocationsMap.get(previousRoom));
				}
				else if (roomResourcesMap.containsKey(previousRoom))
				{
					updateRoomResources(previousRoom);
				}
				else
				{
					updateGauntletMap(previousRoom, MapIcons.ACTIVE_TILE);
				}

				updateGauntletMap(currentRoom, MapIcons.PLAYER);
			}
		});
	}

	public void hunllefSpawned(WorldPoint player, WorldPoint hunllef)
	{
		startLocation = calculateActivatedRoom(player, hunllef);
		currentRoom = startLocation;
		createInstanceMaps(player);

		updateGauntletMap(currentRoom, MapIcons.PLAYER);
		updateGauntletMap(BOSS_ROOM, MapIcons.BOSS);

		if (this.config.showDemiBosses())
		{
			for (int i = 0; i <= DEMI_ROOM_LIST.size() - 1; i++)
			{
				updateGauntletMap(DEMI_ROOM_LIST.get(i), MapIcons.DEMIBOSS_UNKNOWN);
			}
		}

		plugin.getPanel().firstLoad();
		newSession = false;
	}

	public void updateDemiBossLocations(WorldPoint player, NPC demiBoss)
	{
		int room = calculateActivatedRoom(player, centerTileMap.get(currentRoom));

		if (demiBoss.isDead())
		{
			for (Map.Entry<Integer, MapIcons> entry : demiBossLocationsMap.entrySet())
			{
				if (roomTilesMap.get(entry.getKey()).contains(demiBoss.getWorldLocation()))
				{
					room = entry.getKey();
				}
			}

			demiBossLocationsMap.remove(room);

			if (!roomTilesMap.get(room).contains(player))
			{
				updateGauntletMap(room, MapIcons.ACTIVE_TILE);
			}

			return;
		}

		switch (demiBoss.getId())
		{
			case NpcID.CRYSTALLINE_BEAR:
			case NpcID.CORRUPTED_BEAR:
				demiBossLocationsMap.put(room, MapIcons.DEMIBOSS_MELEE);
				updateGauntletMap(room, MapIcons.DEMIBOSS_MELEE);
				break;

			case NpcID.CRYSTALLINE_DRAGON:
			case NpcID.CORRUPTED_DRAGON:
				demiBossLocationsMap.put(room, MapIcons.DEMIBOSS_MAGIC);
				updateGauntletMap(room, MapIcons.DEMIBOSS_MAGIC);
				break;

			case NpcID.CRYSTALLINE_DARK_BEAST:
			case NpcID.CORRUPTED_DARK_BEAST:
				demiBossLocationsMap.put(room, MapIcons.DEMIBOSS_RANGED);
				updateGauntletMap(room, MapIcons.DEMIBOSS_RANGED);
				break;
		}
	}

	public void gameObjectSpawned(GameObject gameObject)
	{
		WorldPoint player = plugin.getClient().getLocalPlayer().getWorldLocation();
		int room = calculateActivatedRoom(player, centerTileMap.get(currentRoom));

		switch (gameObject.getId())
		{
			case ObjectID.FISHING_SPOT_36068:
			case ObjectID.CORRUPT_FISHING_SPOT:
			case ObjectID.GRYM_ROOT:
			case ObjectID.CORRUPT_GRYM_ROOT:
				if (roomTilesMap.get(room).contains(gameObject.getWorldLocation()))
				{
					if (roomResourcesMap.containsKey(room))
					{
						roomResourcesMap.get(room).add(gameObject.getId());
					}
					else
					{
						roomResourcesMap.put(room, Lists.newArrayList(gameObject.getId()));
					}
					updateRoomResources(room);
				}
				break;

			case ObjectID.NODE_35998:
			case ObjectID.NODE_35999:
			case ObjectID.NODE_36101:
			case ObjectID.NODE_36102:
				plugin.getConnectedRoomsMap().get(room).forEach(connectedRoom ->
				{
					if (activeRoomsList.contains(connectedRoom))
					{
						return;
					}

					if (DEMI_ROOM_LIST.contains(connectedRoom))
					{
						if (gameObject.getWorldLocation().distanceTo(player) == 3 || gameObject.getWorldLocation().distanceTo(player) == 4)
						{
							return;
						}

						if (connectedRoom - room == calculateOrientation(gameObject.getWorldLocation(), centerTileMap.get(room)))
						{
							if (highlightNodeMap.containsKey(room))
							{
								highlightNodeMap.get(room).add(new NodeGameObject(gameObject));
							}
							else
							{
								highlightNodeMap.put(room, Lists.newArrayList(new NodeGameObject(gameObject)));
							}
						}
					}
				});
				break;
		}
	}

	public void gameObjectDespawned(GameObject gameObject)
	{
		switch (gameObject.getId())
		{
			case ObjectID.FISHING_SPOT_36068:
			case ObjectID.CORRUPT_FISHING_SPOT:
			case ObjectID.GRYM_ROOT:
			case ObjectID.CORRUPT_GRYM_ROOT:
				if (roomTilesMap.get(currentRoom).contains(gameObject.getWorldLocation()))
				{
					for (Integer resource : roomResourcesMap.get(currentRoom))
					{
						if (gameObject.getId() == resource)
						{
							roomResourcesMap.get(currentRoom).remove(resource);
							return;
						}
					}
				}
				break;

			case ObjectID.NODE_35998:
			case ObjectID.NODE_35999:
			case ObjectID.NODE_36101:
			case ObjectID.NODE_36102:
				if (roomTilesMap.get(currentRoom).contains(gameObject.getWorldLocation()))
				{
					if (highlightNodeMap.containsKey(currentRoom))
					{
						highlightNodeMap.get(currentRoom).removeIf(o -> o.getWorldLocation().equals(gameObject.getWorldLocation()));
					}
				}
				break;
		}
	}

	private void updateRoomResources(Integer room)
	{
		if (demiBossLocationsMap.containsKey(room))
		{
			return;
		}

		int fishingSpots = 0;
		int grymRoots = 0;

		for (int resource : roomResourcesMap.get(room))
		{
			switch (resource)
			{
				case ObjectID.FISHING_SPOT_36068:
				case ObjectID.CORRUPT_FISHING_SPOT:
					if (config.showFishingSpots())
					{
						fishingSpots++;
					}
					break;

				case ObjectID.GRYM_ROOT:
				case ObjectID.CORRUPT_GRYM_ROOT:
					if (config.showGrymLeaves())
					{
						grymRoots++;
					}
					break;
			}
		}

		if (fishingSpots > 0 && grymRoots > 0)
		{
			updateGauntletMap(room, MapIcons.FISHING_SPOT_GRYM_ROOT);
		}
		else if (fishingSpots > 0)
		{
			updateGauntletMap(room, MapIcons.FISHING_SPOT);
		}
		else if (grymRoots > 0)
		{
			updateGauntletMap(room, MapIcons.GRYM_ROOT);
		}
		else
		{
			updateGauntletMap(room, MapIcons.ACTIVE_TILE);
		}
	}

	public void gameStateChanged(GameStateChanged gameStateChanged, Client client)
	{
		switch (gameStateChanged.getGameState())
		{
			case LOADING:
				//Clear when room is activated to prevent duplicate loading
				highlightNodeMap.clear();

				//Reset session variables when leaving the instance
				if (!client.isInInstancedRegion() && !newSession)
				{
					newSession = true;
					corrupted = false;
					currentRoom = null;
					highlightNodeMap.clear();
					demiBossLocationsMap.clear();
					activeRoomsList.clear();
					plugin.getPanel().clearPanel();
					return;
				}

				//Update session while inside the instance
				if (client.isInInstancedRegion() && !newSession)
				{
					int activatedRoom = calculateActivatedRoom(client.getLocalPlayer().getWorldLocation(), centerTileMap.get(currentRoom));
					activeRoomsList.add(activatedRoom);
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

		if (corrupted)
		{
			type = "corrupted/";
		}

		switch (icon)
		{
			case PLAYER:
				path = type + "player" + plugin.getFileNameMap().get(room);
				if (room == startLocation)
				{
					path = type + "player_start.png";
				}
				gauntletMap.put(room, ImageUtil.loadImageResource(GauntletMapPlugin.class, path));
				break;

			case BOSS:
				path = type + "hunllef" + plugin.getFileNameMap().get(room);
				gauntletMap.put(room, ImageUtil.loadImageResource(GauntletMapPlugin.class, path));
				break;

			case ACTIVE_TILE:
				path = "active" + plugin.getFileNameMap().get(room);
				if (room == startLocation)
				{
					path = type + "start_room.png";
				}
				gauntletMap.put(room, ImageUtil.loadImageResource(GauntletMapPlugin.class, path));
				break;

			case DEMIBOSS_UNKNOWN:
				path = type + "demi" + plugin.getFileNameMap().get(room);
				gauntletMap.put(room, ImageUtil.loadImageResource(GauntletMapPlugin.class, path));
				break;

			case DEMIBOSS_MAGIC:
				path = "demiboss/magic" + plugin.getFileNameMap().get(room);
				gauntletMap.put(room, ImageUtil.loadImageResource(GauntletMapPlugin.class, path));
				break;

			case DEMIBOSS_MELEE:
				path = "demiboss/melee" + plugin.getFileNameMap().get(room);
				gauntletMap.put(room, ImageUtil.loadImageResource(GauntletMapPlugin.class, path));
				break;

			case DEMIBOSS_RANGED:
				path = "demiboss/ranged" + plugin.getFileNameMap().get(room);
				gauntletMap.put(room, ImageUtil.loadImageResource(GauntletMapPlugin.class, path));
				break;

			case GRYM_ROOT:
				type = "resources/";
				path = type + "grym" + plugin.getFileNameMap().get(room);
				gauntletMap.put(room, ImageUtil.loadImageResource(GauntletMapPlugin.class, path));
				break;

			case FISHING_SPOT:
				type = "resources/";
				path = type + "fish" + plugin.getFileNameMap().get(room);
				gauntletMap.put(room, ImageUtil.loadImageResource(GauntletMapPlugin.class, path));
				break;

			case FISHING_SPOT_GRYM_ROOT:
				type = "resources/";
				path = type + "grym_fish" + plugin.getFileNameMap().get(room);
				gauntletMap.put(room, ImageUtil.loadImageResource(GauntletMapPlugin.class, path));
				break;
		}

		if (!newSession)
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
