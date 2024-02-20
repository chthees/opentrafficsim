package org.opentrafficsim.editor.extensions.map;

import java.awt.Color;

import org.opentrafficsim.draw.road.TrafficLightAnimation.TrafficLightData;
import org.opentrafficsim.editor.OtsEditor;
import org.opentrafficsim.editor.XsdTreeNode;

/**
 * TrafficLightData for the editor Map.
 * <p>
 * Copyright (c) 2023-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
public class MapTrafficLightData extends MapLineData implements TrafficLightData
{

    /** */
    private static final long serialVersionUID = 20240212L;

    /**
     * Constructor.
     * @param map Map; map.
     * @param node XsdTreeNode; node Ots.Network.Link.TrafficLight.
     * @param editor OtsEditor; editor.
     */
    public MapTrafficLightData(final EditorMap map, final XsdTreeNode node, final OtsEditor editor)
    {
        super(map, node, editor);
    }

    /** {@inheritDoc} */
    @Override
    protected XsdTreeNode getLinkNode()
    {
        return getNode().getParent();
    }

    /** {@inheritDoc} */
    @Override
    public Color getColor()
    {
        return Color.RED;
    }

    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        return "TrafficLight " + getId();
    }

}
