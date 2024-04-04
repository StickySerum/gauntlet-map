package net.runelite.client.plugins.gauntletmap;

import java.awt.AlphaComposite;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.ComponentOrientation;
import net.runelite.client.ui.overlay.components.ImageComponent;
import net.runelite.client.ui.overlay.outline.ModelOutlineRenderer;


public class GauntletMapOverlay extends OverlayPanel
{
	private final GauntletMapSession session;

	private final GauntletMapConfig config;

	@Inject
	private GauntletMapOverlay(GauntletMapPlugin plugin, GauntletMapSession session, GauntletMapConfig config, Client client, ModelOutlineRenderer modelOutlineRenderer)
	{
		super(plugin);
		this.session = session;
		this.config = config;

		setPosition(OverlayPosition.TOP_CENTER);
		setLayer(OverlayLayer.ABOVE_SCENE);

		panelComponent.setWrap(true);
		panelComponent.setOrientation(ComponentOrientation.HORIZONTAL);
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		if (session.isNewSession() || !config.showOverlay())
		{
			return null;
		}

		int size = config.overlayTileSize() * 7;
		setBounds(new Rectangle(size, size));
		panelComponent.setPreferredSize(new Dimension(size, size));

		for (int i = 1; i <= 49; i++)
		{
			panelComponent.getChildren().add(new ImageComponent(session.scaleImage(config.overlayTileSize(), session.getGauntletMap().get(i))));
		}

		float opacity = (float) config.overlayOpacityPercentage()/100;
		graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity));

		return super.render(graphics);
	}
}
