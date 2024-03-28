package net.runelite.client.plugins.gauntletmap;

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

//		session.getHighlightNodeMap().forEach((room, gameObjectList) ->
//		{
//			gameObjectList.forEach(gameObject -> modelOutlineRenderer.drawOutline(gameObject, 5, Color.YELLOW, 1));
//		});

	}


}
