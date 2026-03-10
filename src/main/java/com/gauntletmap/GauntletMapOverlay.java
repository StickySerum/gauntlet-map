package com.gauntletmap;

import net.runelite.api.Client;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.ComponentOrientation;
import net.runelite.client.ui.overlay.components.ImageComponent;
import net.runelite.client.ui.overlay.outline.ModelOutlineRenderer;

import javax.inject.Inject;
import java.awt.*;


public class GauntletMapOverlay extends OverlayPanel
{
	private final GauntletMapSession session;

	private final GauntletMapConfig config;

    private final GauntletMapPlugin plugin;

	@Inject
	private GauntletMapOverlay(GauntletMapPlugin plugin, GauntletMapSession session, GauntletMapConfig config, Client client, ModelOutlineRenderer modelOutlineRenderer)
	{

        super(plugin);
		this.session = session;
		this.config = config;
        this.plugin = plugin;

		setPosition(OverlayPosition.TOP_CENTER);
		setLayer(OverlayLayer.ABOVE_SCENE);

		panelComponent.setWrap(true);
		panelComponent.setOrientation(ComponentOrientation.HORIZONTAL);

	}

	@Override
	public Dimension render(Graphics2D graphics)
	{

		if (!plugin.isPlayerInGauntlet())
		{
			return null;
		}

		int size = config.overlayTileSize() * 7;
		panelComponent.setPreferredSize(new Dimension(size, size));

        for (int roomYAxis = 6; roomYAxis >= 0; roomYAxis--)
        {
            for (int roomXAxis = 0; roomXAxis <= 6; roomXAxis++)
            {
                int room = (roomYAxis * 7 + roomXAxis + 1);
                panelComponent.getChildren().add(new ImageComponent(session.scaleImage(config.overlayTileSize(), session.getGauntletMap().get(room))));
            }
        }

		float opacity = (float) config.overlayOpacityPercentage()/100;
		graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity));

		return super.render(graphics);

	}
}
