package net.runelite.client.plugins.gauntletmap;

import lombok.Getter;
import lombok.Setter;
import net.runelite.client.ui.PluginPanel;
import net.runelite.client.util.ImageUtil;

import javax.inject.Inject;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Map;
import java.util.TreeMap;

public class GauntletMapPanel extends PluginPanel
{
	@Inject
	private GauntletMapConfig config;

	@Getter
	@Setter
	private BufferedImage hunllef;

	@Getter
	@Setter
	private BufferedImage player;

	@Getter
	@Setter
	private BufferedImage startRoom;

	@Getter
	@Setter
	private Map<Integer, String> tileTypeMap;

	@Getter
	@Setter
	private GauntletMapPlugin plugin;

	@Inject
	GauntletMapPanel(GauntletMapPlugin plugin)
	{
		setPlugin(plugin);
		createTileTypeMap();
		clearMapTiles();
	}

	public void clearMapTiles()
	{
		SwingUtilities.invokeLater(() ->
		{
			removeAll();
			setLayout(new GridBagLayout());
			GridBagConstraints c = new GridBagConstraints();
			c.fill = GridBagConstraints.HORIZONTAL;

			for (int y = 0; y <= 6; y++)
			{
				for (int x = 0; x <= 6; x++)
				{
					c.gridx = x;
					c.gridy = y;
					int room = (y * 7 + x + 1);

					String path = "inactive" + getTileTypeMap().get(room);
					BufferedImage tile = ImageUtil.loadImageResource(GauntletMapPlugin.class, path);

					add(new JLabel(new ImageIcon(tile)), c);
				}
			}
			revalidate();
			repaint();
		});
	}

	public void updateStartingTiles()
	{
		SwingUtilities.invokeLater(() ->
		{
			if (config.showDemiBosses())
			{
				updatePanel(3, false, false, true);
				updatePanel(4, false, false, true);
				updatePanel(5, false, false, true);

				updatePanel(15, false, false, true);
				updatePanel(22, false, false, true);
				updatePanel(29, false, false, true);

				updatePanel(21, false, false, true);
				updatePanel(28, false, false, true);
				updatePanel(35, false, false, true);

				updatePanel(45, false, false, true);
				updatePanel(46, false, false, true);
				updatePanel(47, false, false, true);
			}

			updatePanel(25, false, true, false);
			updatePanel(plugin.getStartLocation(), true, false, false);

			revalidate();
			repaint();
		});
	}

	public void updatePlayerIcon(Integer previousRoom)
	{
		SwingUtilities.invokeLater(() ->
		{
			updatePanel(previousRoom, false, false, false);
			updatePanel(plugin.getCurrentRoom(), true, false, false);

			revalidate();
			repaint();
		});
	}

	public void addNewActiveTile(Integer newRoom)
	{
		SwingUtilities.invokeLater(() ->
		{
			updatePanel(newRoom, false, false, false);

			revalidate();
			repaint();
		});
	}

	private void updatePanel(Integer room, boolean playerIcon, boolean bossIcon, boolean demiIcon)
	{
		GridBagLayout layout = (GridBagLayout) getLayout();
		Component toRemove = getComponent(room - 1);
		GridBagConstraints c = layout.getConstraints(toRemove);
		remove(toRemove);
		String tile;
		String type = "regular/";

		if (plugin.isCorrupted())
		{
			type = "corrupted/";
		}

		if (playerIcon)
		{
			String path = type + "player" + tileTypeMap.get(room);

			if (room == plugin.getStartLocation())
			{
				path = type + "player_start.png";
			}

			BufferedImage player = ImageUtil.loadImageResource(GauntletMapPlugin.class, path);
			add(new JLabel(new ImageIcon(player)), c, room - 1);
			return;
		}

		if (bossIcon)
		{
			String path = type + "hunllef" + tileTypeMap.get(room);
			BufferedImage hunllef = ImageUtil.loadImageResource(GauntletMapPlugin.class, path);
			add(new JLabel(new ImageIcon(hunllef)), c, room - 1);
			return;
		}

		if (room == plugin.getStartLocation())
		{
			String path = type + "start_room.png";
			BufferedImage startRoom = ImageUtil.loadImageResource(GauntletMapPlugin.class, path);
			add(new JLabel(new ImageIcon(startRoom)), c, room - 1);
			return;
		}

		if (plugin.isDemiBoss() && room == plugin.getDemiBossRoom())
		{
			String path = "demiboss/" + plugin.getAttackStyle() + tileTypeMap.get(room);
			BufferedImage startRoom = ImageUtil.loadImageResource(GauntletMapPlugin.class, path);
			add(new JLabel(new ImageIcon(startRoom)), c, room - 1);
			return;
		}

		if (config.showDemiBosses() && demiIcon)
		{
			String path = type + "demi" + getTileTypeMap().get(room);
			System.out.println(path);
			BufferedImage demiRoom = ImageUtil.loadImageResource(GauntletMapPlugin.class, path);
			add(new JLabel(new ImageIcon(demiRoom)), c, room - 1);
			return;
		}

		if (plugin.isFirstLoad())
		{
			tile = "inactive";
		}
		else
		{
			tile = "active";
		}

		BufferedImage mapTile = ImageUtil.loadImageResource(GauntletMapPlugin.class, tile + getTileTypeMap().get(room));
		add(new JLabel(new ImageIcon(mapTile)), c, room - 1);
	}

	private void createTileTypeMap()
	{
		Map<Integer, String> tileTypeMap = new TreeMap<>();

		for (int i = 1; i <= 49; i++)
		{
			if (i == 1 || i == 7 || i == 43 || i == 49)
			{
				switch (i)
				{
					case 1:
						tileTypeMap.put(i, "_top_left.png");
						break;
					case 7:
						tileTypeMap.put(i, "_top_right.png");
						break;
					case 43:
						tileTypeMap.put(i, "_bottom_left.png");
						break;
					case 49:
						tileTypeMap.put(i, "_bottom_right.png");
						break;
				}
			}
			else if (i % 7 == 1)
			{
				tileTypeMap.put(i, "_left.png");
			}
			else if (i % 7 == 0)
			{
				tileTypeMap.put(i, "_right.png");
			}
			else if (i > 1 && i < 7)
			{
				tileTypeMap.put(i, "_top.png");
			}
			else if (i > 43 && i < 49)
			{
				tileTypeMap.put(i, "_bottom.png");
			}
			else
			{
				tileTypeMap.put(i, ".png");
			}
		}

		setTileTypeMap(tileTypeMap);
	}
}
