package com.gauntletmap;

import com.google.inject.Inject;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.util.List;
import java.util.Map;
import net.runelite.api.GameObject;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.outline.ModelOutlineRenderer;

public class GauntletMapGuide extends Overlay
{
	private final GauntletMapSession session;

	private final ModelOutlineRenderer modelOutlineRenderer;

	@Inject
	private GauntletMapGuide(GauntletMapSession session, ModelOutlineRenderer modelOutlineRenderer)
	{
		this.session = session;
		this.modelOutlineRenderer = modelOutlineRenderer;
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		//CALL HIGHLIGHT METHOD
		highlightNode();
		return super.render(graphics);
	}

	private void highlightNode()
	{
		//SAVE GAME OBJECT AFTER SPAWNING, CALL IT HERE

		for (Map.Entry<Integer, List<GameObject>> entry : session.getHighlightNodeMap().entrySet())
		{
			for (GameObject gameObject : entry.getValue())
			{
				modelOutlineRenderer.drawOutline(gameObject, 1, Color.YELLOW, 1);
			}
		}
	}


}
