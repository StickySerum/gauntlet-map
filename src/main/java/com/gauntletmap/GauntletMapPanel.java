package com.gauntletmap;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import javax.inject.Inject;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import lombok.Getter;
import net.runelite.client.ui.PluginPanel;

@Getter
public class GauntletMapPanel extends PluginPanel
{

	private final GauntletMapPlugin plugin;

	private final GauntletMapSession session;

	@Inject
	GauntletMapPanel(GauntletMapPlugin plugin, GauntletMapSession session)
	{
		this.plugin = plugin;
		this.session = session;
	}

	public void clearPanel()
	{
		removeAll();
		revalidate();
		repaint();
	}

	public void firstLoad()
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

					add(new JLabel(new ImageIcon(session.getGauntletMap().get(room))), c);
				}
			}

			revalidate();
			repaint();
		});
	}

	public void updatePanel(Integer room)
	{
		SwingUtilities.invokeLater(() ->
		{
			GridBagLayout layout = (GridBagLayout) getLayout();
			Component toRemove = getComponent(room - 1);
			GridBagConstraints c = layout.getConstraints(toRemove);
			remove(toRemove);

			add(new JLabel(new ImageIcon(session.getGauntletMap().get(room))), c, room - 1);

			revalidate();
			repaint();
		});
	}
}
