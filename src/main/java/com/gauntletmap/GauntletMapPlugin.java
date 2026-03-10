package com.gauntletmap;

import com.google.common.collect.Lists;
import com.google.inject.Provides;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameObject;
import net.runelite.api.NPC;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.*;
import net.runelite.api.gameval.ObjectID;
import net.runelite.api.gameval.VarbitID;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.ImageUtil;

import javax.inject.Inject;
import java.awt.image.BufferedImage;
import java.util.*;

@Slf4j
@PluginDescriptor(
	name = "Gauntlet Map"
)
@Getter
public class GauntletMapPlugin extends Plugin
{
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

	private Map<Integer, String> fileNameMap = new HashMap<>();

	private Map<Integer, List<Integer>> connectedRoomsMap =  new HashMap<>();

    private Map<Integer, Integer> roomFoundVarbitMap = new HashMap<>();

	private NavigationButton navButton;

	private boolean isPanelDisplayed;

    @Getter
    private boolean playerInGauntlet = false;

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

		updatePanelDisplay();
		createRequiredMapsAndLists();

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

        switch (Objects.requireNonNull(npcSpawned.getNpc().getName()).toLowerCase())
        {

            case "crystalline hunllef":
            case "corrupted hunllef":

                if (session.isNewSession())
                {
                    gauntletStarted();
                    playerInGauntlet = true;
                    session.setNewSession(false);
                }

                break;

            case "crystalline bear":
            case "corrupted bear":
            case "crystalline dark beast":
            case "corrupted dark beast":
            case "crystalline dragon":
            case "corrupted dragon":

                //Check if the npc has spawned before

                GauntletMapSession.DEMI_ROOM_LIST.forEach(room ->
                {

                    if (session.getRoomTilesMap().get(room).contains(npcSpawned.getNpc().getWorldLocation()))
                    {

                        if (!session.getDemiBossLocationsMap().containsKey(room))
                        {

                            switch (Objects.requireNonNull(npcSpawned.getNpc().getName()).toLowerCase()) {

                                case "crystalline bear":
                                case "corrupted bear":

                                    session.getDemiBossLocationsMap().put(session.getActivatedRoom(), GauntletMapEnums.MapIcons.DEMIBOSS_MELEE);
                                    session.updateGauntletMap(session.getActivatedRoom(), GauntletMapEnums.MapIcons.DEMIBOSS_MELEE);

                                    break;

                                case "crystalline dark beast":
                                case "corrupted dark beast":

                                    session.getDemiBossLocationsMap().put(session.getActivatedRoom(), GauntletMapEnums.MapIcons.DEMIBOSS_RANGED);
                                    session.updateGauntletMap(session.getActivatedRoom(), GauntletMapEnums.MapIcons.DEMIBOSS_RANGED);

                                    break;

                                case "crystalline dragon":
                                case "corrupted dragon":

                                    session.getDemiBossLocationsMap().put(session.getActivatedRoom(), GauntletMapEnums.MapIcons.DEMIBOSS_MAGIC);
                                    session.updateGauntletMap(session.getActivatedRoom(), GauntletMapEnums.MapIcons.DEMIBOSS_MAGIC);

                                    break;

                            }
                        }
                    }
                });

                break;

        }

	}

	@Subscribe
	public void onNpcDespawned(NpcDespawned npcDespawned)
	{
		if (!playerInGauntlet)
        {
            return;
        }

        switch (Objects.requireNonNull(npcDespawned.getNpc().getName()).toLowerCase())
        {

            case "crystalline bear":
            case "corrupted bear":
            case "crystalline dark beast":
            case "corrupted dark beast":
            case "crystalline dragon":
            case "corrupted dragon":

                NPC demiBoss = npcDespawned.getNpc();
                WorldPoint player = client.getLocalPlayer().getWorldLocation();
                int room = 0;

                if (demiBoss.isDead())
                {

                    for (Map.Entry<Integer, GauntletMapEnums.MapIcons> demiLocation : session.getDemiBossLocationsMap().entrySet())
                    {

                        if (session.getRoomTilesMap().get(demiLocation.getKey()).contains(demiBoss.getWorldLocation()))
                        {
                            room = demiLocation.getKey();
                            session.getDemiBossLocationsMap().remove(room);
                        }

                    }

                    if (session.getRoomTilesMap().get(room).contains(player))
                    {
                        session.updateGauntletMap(room, GauntletMapEnums.MapIcons.PLAYER);
                    }

                    else if (session.getRoomResourcesMap().get(room) != null)
                    {
                        session.updateRoomResources(room);
                    }

                    else
                    {
                        session.updateGauntletMap(room, GauntletMapEnums.MapIcons.ACTIVE_TILE);
                    }

                }

                break;
        }

	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged gameStateChanged)
    {

        switch (gameStateChanged.getGameState())
        {

            case LOADING:

                if (!playerInGauntlet)
                {
                    return;
                }

                //Clear when room is activated to prevent duplicate loading
                session.getHighlightNodeMap().clear();

                session.updateGauntletMap(session.getActivatedRoom(), GauntletMapEnums.MapIcons.ACTIVE_TILE);

                break;

            case LOGIN_SCREEN:
                panel.clearPanel();
                break;

        }

    }

	@Subscribe
	public void onConfigChanged(ConfigChanged event)
	{

		if (!event.getGroup().equals("Gauntlet Map"))
		{
			return;
		}

        switch (event.getKey())
        {
            case "panelVisibility":

                updatePanelDisplay();
                break;

            case "showGrymLeaves":
            case "showFishingSpots":

                for (Map.Entry<Integer, List<Integer>> resourceList : session.getRoomResourcesMap().entrySet())
                {
                    session.updateRoomResources(resourceList.getKey());
                }

                break;
        }

		if (event.getKey().equals("panelVisibility"))
		{
			updatePanelDisplay();
		}

	}

	@Subscribe
	public void onGameTick(GameTick gameTick)
	{

		updatePanelDisplay();

        if (!playerInGauntlet)
        {
            return;
        }

		session.updateCurrentRoom(client.getLocalPlayer().getWorldLocation());

	}

	@Subscribe
	public void onGameObjectSpawned(GameObjectSpawned gameObjectSpawned)
	{

		if (!playerInGauntlet)
        {
            return;
        }

        WorldPoint player = client.getLocalPlayer().getWorldLocation();
        GameObject gameObject = gameObjectSpawned.getGameObject();

        switch (gameObject.getId())
        {

            case ObjectID.GAUNTLET_POND:
            case ObjectID.GAUNTLET_POND_HM:
            case ObjectID.GAUNTLET_HERB:
            case ObjectID.GAUNTLET_HERB_HM:

                if (session.getRoomTilesMap().get(session.getActivatedRoom()).contains(gameObject.getWorldLocation()))
                {
                    if (session.getRoomResourcesMap().containsKey(session.getActivatedRoom()))
                    {
                        session.getRoomResourcesMap().get(session.getActivatedRoom()).add(gameObject.getId());
                    }
                    else
                    {
                        session.getRoomResourcesMap().put(session.getActivatedRoom(), Lists.newArrayList(gameObject.getId()));
                    }
                    session.updateRoomResources(session.getActivatedRoom());
                }

                break;

            case ObjectID.PRIF_GAUNTLET_DOOR_WALL_UNLIT_01_HM:
            case ObjectID.PRIF_GAUNTLET_DOOR_WALL_UNLIT_02_HM:
            case ObjectID.PRIF_GAUNTLET_DOOR_WALL_UNLIT_01:
            case ObjectID.PRIF_GAUNTLET_DOOR_WALL_UNLIT_02:

                session.getDemiBossNodeLocationsMap().forEach((roomKey, worldPoints) ->
                {

                    if (worldPoints.contains(gameObject.getWorldLocation()))
                    {
                        if (session.getHighlightNodeMap().containsKey(roomKey))
                        {
                            session.getHighlightNodeMap().get(roomKey).add(gameObject);
                        }
                        else
                        {
                            session.getHighlightNodeMap().put(roomKey, Lists.newArrayList(gameObject));
                        }
                    }

                });
                break;
        }

	}

	@Subscribe
	public void onGameObjectDespawned(GameObjectDespawned gameObjectDespawned)
	{
		if (!playerInGauntlet)
        {
            return;
        }

        GameObject gameObject = gameObjectDespawned.getGameObject();

        switch (gameObject.getId())
        {

            case ObjectID.GAUNTLET_POND:
            case ObjectID.GAUNTLET_POND_HM:
            case ObjectID.GAUNTLET_HERB:
            case ObjectID.GAUNTLET_HERB_HM:

                if (session.getRoomTilesMap().get(session.getCurrentRoom()).contains(gameObject.getWorldLocation()))
                {

                    for (Integer resource : session.getRoomResourcesMap().get(session.getCurrentRoom()))
                    {

                        if (gameObject.getId() == resource)
                        {
                            session.getRoomResourcesMap().get(session.getCurrentRoom()).remove(resource);
                            return;
                        }

                    }

                }

                break;

            case ObjectID.PRIF_GAUNTLET_DOOR_WALL_UNLIT_01_HM:
            case ObjectID.PRIF_GAUNTLET_DOOR_WALL_UNLIT_02_HM:
            case ObjectID.PRIF_GAUNTLET_DOOR_WALL_UNLIT_01:
            case ObjectID.PRIF_GAUNTLET_DOOR_WALL_UNLIT_02:

                session.getDemiBossNodeLocationsMap().forEach((roomKey, worldPoints) ->
                {

                    worldPoints.removeIf(wp -> wp.equals(gameObject.getWorldLocation()));

                    if (session.getHighlightNodeMap().containsKey(roomKey))
                    {
                        session.getHighlightNodeMap().get(roomKey).removeIf(wp -> wp.getWorldLocation().equals(gameObject.getWorldLocation()));
                    }

                });

                break;

        }

	}

	@Subscribe
	public void onVarbitChanged(VarbitChanged event)
	{

        switch (event.getVarbitId())
        {

            case VarbitID.PLAYER_IN_GAUNTLET:
                if (event.getValue() == 0)
                {
                    playerInGauntlet = false;
                    session.clear();
                }
                break;

            case VarbitID.GAUNTLET_START:
                if (event.getValue() != 0)
                {

                    session.setStartLocation(event.getValue() + 1);
                    session.setCurrentRoom(session.getStartLocation());

                }
                break;

            case VarbitID.GAUNTLET_CORRUPTED:
                session.setCorrupted(event.getValue() == 1);
                break;

                //@@@@@@@@@@@@@@@@@@@@UPDATE THIS PROBABLY@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
            case VarbitID.GAUNTLET_BOSS_STARTED:
                if (event.getValue() == 1)
                {
                    session.clear();
                }
                break;

        }

        if (playerInGauntlet && roomFoundVarbitMap.get(event.getVarbitId()) != null)
        {
            session.setActivatedRoom(roomFoundVarbitMap.get(event.getVarbitId()));
        }

	}

	private void createRequiredMapsAndLists()
	{

		for (int room = 1; room <= 49; room++)
		{

            try
            {
                int roomFoundVarbitId = VarbitID.class.getDeclaredField("GAUNTLET_ROOM_" + (room - 1) + "_FOUND").getInt(VarbitID.class);
                roomFoundVarbitMap.put(roomFoundVarbitId, (room));
            }
            catch (Exception e)
            {
                System.out.println("ERROR: " + e);
            }

            List<Integer> connectedRoomsList = new ArrayList<>();

			switch (room)
			{

				case 1:
					connectedRoomsList.addAll(Arrays.asList(2, 8));
					fileNameMap.put(room, "_bottom_left.png");
					break;

				case 7:
					connectedRoomsList.addAll(Arrays.asList(6, 14));
					fileNameMap.put(room, "_bottom_right.png");
					break;

				case 43:
					connectedRoomsList.addAll(Arrays.asList(36, 44));
					fileNameMap.put(room, "_top_left.png");
					break;

				case 49:
					connectedRoomsList.addAll(Arrays.asList(42, 48));
					fileNameMap.put(room, "_top_right.png");
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
					fileNameMap.put(room, "_bottom.png");
					break;

				case 44:
				case 45:
				case 46:
				case 47:
				case 48:
					connectedRoomsList.addAll(Arrays.asList(room - 7, room - 1, room + 1));
					fileNameMap.put(room, "_top.png");
					break;

				default:
					connectedRoomsList.addAll(Arrays.asList(room - 7, room - 1, room + 1, room + 7));
					fileNameMap.put(room, ".png");
					break;

			}

			connectedRoomsMap.put(room, connectedRoomsList);

		}
	}

    private void gauntletStarted()
    {

        session.createInstanceMaps(client.getLocalPlayer().getWorldLocation());

        session.updateGauntletMap(session.getStartLocation(), GauntletMapEnums.MapIcons.PLAYER);
        session.updateGauntletMap(GauntletMapSession.BOSS_ROOM, GauntletMapEnums.MapIcons.BOSS);

        if (config.showDemiBosses())
        {
            for (int i = 0; i <= GauntletMapSession.DEMI_ROOM_LIST.size() - 1; i++)
            {
                session.updateGauntletMap(GauntletMapSession.DEMI_ROOM_LIST.get(i), GauntletMapEnums.MapIcons.DEMIBOSS_UNKNOWN);
            }
        }

        //Start the map in the sidebar
        panel.firstLoad();

    }

	private boolean shouldDisplayPanel()
    {

		switch (config.panelVisibility())
		{

			case NEVER:
				return false;

			case IN_GAUNTLET:
				return playerInGauntlet;

			case ALWAYS:
			default:
				return true;

		}

	}

	private void updatePanelDisplay()
	{
		boolean shouldDisplayPanel = shouldDisplayPanel();
		if (shouldDisplayPanel == isPanelDisplayed)
		{
			return;
		}

		if (shouldDisplayPanel)
		{
			isPanelDisplayed = true;
			clientToolbar.addNavigation(navButton);
		}
		else
		{
			isPanelDisplayed = false;
			clientToolbar.removeNavigation(navButton);
		}
	}

	@Provides
    GauntletMapConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(GauntletMapConfig.class);
	}
}
