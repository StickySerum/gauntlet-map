package com.gauntletmap;

import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.outline.ModelOutlineRenderer;

import javax.inject.Inject;
import java.awt.*;

public class DemiBossRoomOutline extends OverlayPanel {
    private final GauntletMapConfig config;

    private final GauntletMapSession session;

    private final ModelOutlineRenderer modelOutlineRenderer;

    @Inject
    private DemiBossRoomOutline(GauntletMapPlugin plugin, GauntletMapSession session, GauntletMapConfig config, ModelOutlineRenderer modelOutlineRenderer)
    {
        super(plugin);
        this.session = session;
        this.config = config;
        this.modelOutlineRenderer = modelOutlineRenderer;
    }

    @Override
    public Dimension render(Graphics2D graphics)
    {
        if (!config.showDemiBosses())
        {
            return null;
        }

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
//            if (GauntletMapConstants.GUARANTEED_DEMI_ROOM_LIST.contains(room))
//            {
//                nodeObjectList.forEach(nodeGameObject ->
//                {
//                    modelOutlineRenderer.drawOutline(nodeGameObject, config.guaranteedDemiBossOutlineSize(), config.guaranteedDemiBossOutlineColor(), 1);
//                });
//            }
//            else
//            {
//                nodeObjectList.forEach(nodeGameObject ->
//                {
//                    modelOutlineRenderer.drawOutline(nodeGameObject, config.possibleDemiBossOutlineSize(), config.possibleDemiBossOutlineColor(), 1);
//                });
//            }

            nodeObjectList.forEach(nodeGameObject ->
            {
                modelOutlineRenderer.drawOutline(nodeGameObject, config.demiBossOutlineSize(), config.demiBossOutlineColor(), 1);
            });

        });
    }
}
