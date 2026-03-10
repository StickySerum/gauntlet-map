package net.runelite.client.plugins.gauntletmap;

import com.google.common.collect.Lists;
import lombok.Getter;
import lombok.Setter;
import net.runelite.api.GameObject;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.gameval.ObjectID;
import net.runelite.client.util.ImageUtil;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Singleton
public class GauntletMapSession
{
	private final GauntletMapPlugin plugin;

	private final GauntletMapConfig config;

	private static final int TILE_DISTANCE = 16;

	static final int BOSS_ROOM = 25;

	static final List<Integer> DEMI_ROOM_LIST = List.of(3, 4, 5, 15, 21, 22, 28, 29, 35, 45, 46, 47);

    @Getter
	private Map<Integer, GauntletMapEnums.MapIcons> demiBossLocationsMap = new TreeMap<>();

	@Getter
	private Map<Integer, List<GameObject>> highlightNodeMap = new TreeMap<>();

    @Getter
	private Map<Integer, List<WorldPoint>> demiBossNodeLocationsMap = new TreeMap<>();

    @Getter
	private Map<Integer, List<Integer>> roomResourcesMap = new TreeMap<>();

	@Getter
	private Map<Integer, BufferedImage> gauntletMap;

    @Getter
	private Map<Integer, List<WorldPoint>> roomTilesMap;

    @Getter
	@Setter
    private Integer startLocation;


    @Setter
	private boolean corrupted = false;

    @Getter
    @Setter
    private boolean newSession = true;

    @Getter
    @Setter
    private Integer currentRoom;

    @Getter
    @Setter
    private Integer activatedRoom;

	@Inject
	GauntletMapSession(GauntletMapPlugin plugin, GauntletMapConfig config)
	{
		this.plugin = plugin;
		this.config = config;
	}

	public void clear()
	{
		corrupted = false;
		currentRoom = null;
        newSession = true;
		highlightNodeMap.clear();
		demiBossLocationsMap.clear();
		roomResourcesMap.clear();
        gauntletMap.clear();
		plugin.getPanel().clearPanel();
	}

	public void createInstanceMaps(WorldPoint playerLocation)
	{
		WorldPoint northWestCornerRoom = null;
		Map<Integer, List<WorldPoint>> roomTilesMap = new TreeMap<>();
		Map<Integer, BufferedImage> gauntletMap = new TreeMap<>();

		switch (startLocation)
		{
			//North start
			case 32:
				northWestCornerRoom = calculateNewPoint(playerLocation, -3, 2, -3, -2);
				break;

			//East start
			case 26:
				northWestCornerRoom = calculateNewPoint(playerLocation, -4, 3, -3, -2);
				break;

			//South start
			case 18:
				northWestCornerRoom = calculateNewPoint(playerLocation, -3, 4, -3, -2);
				break;

			//West start
			case 24:
				northWestCornerRoom = calculateNewPoint(playerLocation, -2, 3, -3, -2);
				break;
		}

		for (int gauntletMapYAxis = 6; gauntletMapYAxis >= 0; gauntletMapYAxis--)
		{
			for (int gauntletMapXAxis = 0; gauntletMapXAxis <= 6; gauntletMapXAxis++)
			{
				WorldPoint middleOfRoom = calculateNewPoint(northWestCornerRoom, gauntletMapXAxis, gauntletMapYAxis - 6, 0, 0);
				WorldPoint northWestCornerTile = calculateNewPoint(middleOfRoom, 0, 0, -6, 5);

                List<WorldPoint> roomTiles = new ArrayList<>();
				int room = (gauntletMapYAxis * 7 + gauntletMapXAxis + 1);

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
						roomTiles.add(calculateNewPoint(middleOfRoom, 0, 0, -roomEntranceX, 7 - roomEntranceY));
						roomTiles.add(calculateNewPoint(middleOfRoom, 0, 0, 7 - roomEntranceX, -roomEntranceY));
						roomTiles.add(calculateNewPoint(middleOfRoom, 0, 0, -roomEntranceX, -7 - roomEntranceY));
						roomTiles.add(calculateNewPoint(middleOfRoom, 0, 0, -7 - roomEntranceX, -roomEntranceY));
					}
				}

				for (Integer connectedRoom : plugin.getConnectedRoomsMap().get(room))
				{

					if (DEMI_ROOM_LIST.contains(connectedRoom))
					{
						switch (connectedRoom - room)
						{
							case 7:
								//North
								if (demiBossNodeLocationsMap.containsKey(room))
								{
									demiBossNodeLocationsMap.get(room).addAll(Lists.newArrayList(
											calculateNewPoint(middleOfRoom, 0, 0, -2, 7),
											calculateNewPoint(middleOfRoom, 0, 0, -2, 6),
											calculateNewPoint(middleOfRoom, 0, 0, 1, 7),
											calculateNewPoint(middleOfRoom, 0, 0, 1, 6)
									));
								}
								else
								{
									demiBossNodeLocationsMap.put(
											room,
											Lists.newArrayList(
													calculateNewPoint(middleOfRoom, 0, 0, -2, 7),
													calculateNewPoint(middleOfRoom, 0, 0, -2, 6),
													calculateNewPoint(middleOfRoom, 0, 0, 1, 7),
													calculateNewPoint(middleOfRoom, 0, 0, 1, 6)
											));
								}
								break;

							case -7:
								//South
								if (demiBossNodeLocationsMap.containsKey(room))
								{
									demiBossNodeLocationsMap.get(room).addAll(Lists.newArrayList(
											calculateNewPoint(middleOfRoom, 0, 0, -2, -7),
											calculateNewPoint(middleOfRoom, 0, 0, -2, -8),
											calculateNewPoint(middleOfRoom, 0, 0, 1, -7),
											calculateNewPoint(middleOfRoom, 0, 0, 1, -8)
									));
								}
								else
								{
									demiBossNodeLocationsMap.put(
											room,
											Lists.newArrayList(
													calculateNewPoint(middleOfRoom, 0, 0, -2, -7),
													calculateNewPoint(middleOfRoom, 0, 0, -2, -8),
													calculateNewPoint(middleOfRoom, 0, 0, 1, -7),
													calculateNewPoint(middleOfRoom, 0, 0, 1, -8)
											));
								}
								break;

							case 1:
								//East
								if (demiBossNodeLocationsMap.containsKey(room))
								{
									demiBossNodeLocationsMap.get(room).addAll(Lists.newArrayList(
											calculateNewPoint(middleOfRoom, 0, 0, 7, 1),
											calculateNewPoint(middleOfRoom, 0, 0, 6, 1),
											calculateNewPoint(middleOfRoom, 0, 0, 7, -2),
											calculateNewPoint(middleOfRoom, 0, 0, 6, -2)
									));
								}
								else
								{
									demiBossNodeLocationsMap.put(
											room,
											Lists.newArrayList(
													calculateNewPoint(middleOfRoom, 0, 0, 7, 1),
													calculateNewPoint(middleOfRoom, 0, 0, 6, 1),
													calculateNewPoint(middleOfRoom, 0, 0, 7, -2),
													calculateNewPoint(middleOfRoom, 0, 0, 6, -2)
											));
								}
								break;

							case -1:
								//West
								if (demiBossNodeLocationsMap.containsKey(room))
								{
									demiBossNodeLocationsMap.get(room).addAll(Lists.newArrayList(
											calculateNewPoint(middleOfRoom, 0, 0, -7, 1),
											calculateNewPoint(middleOfRoom, 0, 0, -8, 1),
											calculateNewPoint(middleOfRoom, 0, 0, -7, -2),
											calculateNewPoint(middleOfRoom, 0, 0, -8, -2)
									));
								}
								else
								{
									demiBossNodeLocationsMap.put(
											room,
											Lists.newArrayList(
													calculateNewPoint(middleOfRoom, 0, 0, -7, 1),
													calculateNewPoint(middleOfRoom, 0, 0, -8, 1),
													calculateNewPoint(middleOfRoom, 0, 0, -7, -2),
													calculateNewPoint(middleOfRoom, 0, 0, -8, -2)
											));
								}
								break;
						}
					}
				}

				roomTilesMap.put(room, roomTiles);

			}
		}

		this.gauntletMap = gauntletMap;
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
				setCurrentRoom(connectedRoom);

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
					updateGauntletMap(previousRoom, GauntletMapEnums.MapIcons.ACTIVE_TILE);
				}

				updateGauntletMap(currentRoom, GauntletMapEnums.MapIcons.PLAYER);
			}

		});

	}

    void updateRoomResources(Integer room)
	{

		if (demiBossLocationsMap.containsKey(room))
		{
			return;
		}

        int pond = ObjectID.GAUNTLET_POND;
        int pondCG = ObjectID.GAUNTLET_POND_HM;
        int herb = ObjectID.GAUNTLET_HERB;
        int herbCG = ObjectID.GAUNTLET_HERB_HM;

        if ((roomResourcesMap.get(room).contains(pond) || roomResourcesMap.get(room).contains(pondCG)) &&
            (roomResourcesMap.get(room).contains(herb) || roomResourcesMap.get(room).contains(herbCG)) &&
            config.showFishingSpots() &&
            config.showGrymLeaves())
        {
            updateGauntletMap(room, GauntletMapEnums.MapIcons.FISHING_SPOT_GRYM_ROOT);
        }

        else if (roomResourcesMap.get(room).contains(pond) || roomResourcesMap.get(room).contains(pondCG) && config.showFishingSpots())
        {
            updateGauntletMap(room, GauntletMapEnums.MapIcons.FISHING_SPOT);
        }

        else if (roomResourcesMap.get(room).contains(herb) ||  roomResourcesMap.get(room).contains(herbCG) && config.showGrymLeaves())
        {
            updateGauntletMap(room, GauntletMapEnums.MapIcons.GRYM_ROOT);
        }

        else
        {
            updateGauntletMap(room, GauntletMapEnums.MapIcons.ACTIVE_TILE);
        }

	}

	public void updateGauntletMap(Integer room, GauntletMapEnums.MapIcons icon)
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
				path = type + icon + plugin.getFileNameMap().get(room);
				if (room == startLocation)
				{
					path = type + "player_start.png";
				}
				gauntletMap.put(room, ImageUtil.loadImageResource(GauntletMapPlugin.class, path));
				break;

            case ACTIVE_TILE:
                path = icon + plugin.getFileNameMap().get(room);
                if (room == startLocation)
                {
                    path = type + "start_room.png";
                }
                gauntletMap.put(room, ImageUtil.loadImageResource(GauntletMapPlugin.class, path));
                break;

			case BOSS:
            case DEMIBOSS_UNKNOWN:
				path = type + icon + plugin.getFileNameMap().get(room);
				gauntletMap.put(room, ImageUtil.loadImageResource(GauntletMapPlugin.class, path));
				break;

            case DEMIBOSS_MELEE:
			case DEMIBOSS_MAGIC:
            case DEMIBOSS_RANGED:
            case FISHING_SPOT:
            case GRYM_ROOT:
            case FISHING_SPOT_GRYM_ROOT:
                path = icon + plugin.getFileNameMap().get(room);
				gauntletMap.put(room, ImageUtil.loadImageResource(GauntletMapPlugin.class, path));
				break;

        }

        plugin.getPanel().updatePanel(room);

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
