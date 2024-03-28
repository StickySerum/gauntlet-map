package net.runelite.client.plugins.gauntletmap;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.GameObject;
import net.runelite.api.ItemID;
import net.runelite.api.ObjectID;
import net.runelite.api.Perspective;
import net.runelite.api.SkullIcon;
import net.runelite.api.TileObject;
import net.runelite.api.coords.LocalPoint;
import net.runelite.client.game.ItemManager;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayUtil;
import net.runelite.client.ui.overlay.components.ComponentOrientation;
import net.runelite.client.ui.overlay.components.ImageComponent;
import net.runelite.client.ui.overlay.components.PanelComponent;
import net.runelite.client.ui.overlay.outline.ModelOutlineRenderer;


public class GauntletMapOverlay extends OverlayPanel
{
	private final GauntletMapSession session;

	private final GauntletMapConfig config;

	private final Client client;

	private final GauntletMapPlugin plugin;

	private final ModelOutlineRenderer modelOutlineRenderer;

	@Inject
	private GauntletMapOverlay(GauntletMapPlugin plugin, GauntletMapSession session, GauntletMapConfig config, Client client, ModelOutlineRenderer modelOutlineRenderer)
	{
		super(plugin);
		this.session = session;
		this.config = config;
		this.client = client;
		this.plugin = plugin;
		this.modelOutlineRenderer = modelOutlineRenderer;

		setPosition(OverlayPosition.TOP_LEFT);
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
		panelComponent.setPreferredSize(new Dimension(size, size));

		for (int i = 1; i <= 49; i++)
		{
			panelComponent.getChildren().add(new ImageComponent(session.scaleImage(config.overlayTileSize(), session.getGauntletMap().get(i))));
		}

		float opacity = (float) config.overlayOpacityPercentage()/100;
		graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity));

		highlightDemiBoss(graphics);

		return super.render(graphics);
	}

	private void highlightDemiBoss(Graphics2D graphics)
	{
		if (session.getHighlightNodeMap() == null) {
			return;
		}

		session.getHighlightNodeMap().forEach((room, nodeObjectList) ->
		{
			nodeObjectList.forEach(nodeGameObject ->
			{
				switch (nodeGameObject.getGameObject().getOrientation())
				{
					case 0:
						modelOutlineRenderer.drawOutline(nodeGameObject.getGameObject(), 1, Color.BLUE, 1);
						break;
					case 512:
						modelOutlineRenderer.drawOutline(nodeGameObject.getGameObject(), 1, Color.GREEN, 1);
						break;
					case 1024:
						modelOutlineRenderer.drawOutline(nodeGameObject.getGameObject(), 1, Color.RED, 1);
						break;
					case 1536:
						modelOutlineRenderer.drawOutline(nodeGameObject.getGameObject(), 1, Color.ORANGE, 1);
						break;
				}
//				modelOutlineRenderer.drawOutline(gameObject, 1, Color.YELLOW, 1);
			});
		});
	}
}
