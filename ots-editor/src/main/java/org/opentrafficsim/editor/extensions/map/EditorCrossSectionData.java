package org.opentrafficsim.editor.extensions.map;

import java.rmi.RemoteException;
import java.util.List;

import org.djunits.value.vdouble.scalar.Length;
import org.djutils.draw.bounds.Bounds;
import org.djutils.draw.bounds.Bounds2d;
import org.djutils.draw.line.PolyLine2d;
import org.djutils.draw.line.Polygon2d;
import org.djutils.draw.point.Point2d;
import org.opentrafficsim.draw.road.CrossSectionElementAnimation.CrossSectionElementData;
import org.opentrafficsim.editor.XsdTreeNode;
import org.opentrafficsim.road.network.lane.SliceInfo;

/**
 * Cross section element data for in the editor.
 * <p>
 * Copyright (c) 2023-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
public class EditorCrossSectionData implements CrossSectionElementData
{

    /** Node representing the element. */
    private final XsdTreeNode linkNode;

    /** Center line. */
    protected final PolyLine2d centerLine;

    /** Contour. */
    private final Polygon2d contour;

    /** Slice info. */
    private SliceInfo sliceInfo;

    /**
     * Constructor.
     * @param linkNode XsdTreeNode; node representing the element.
     * @param centerLine PolyLine2d; center line.
     * @param contour PolyLine2d; contour.
     * @param sliceInfo SliceInfo; slice info.
     */
    public EditorCrossSectionData(final XsdTreeNode linkNode, final PolyLine2d centerLine, final Polygon2d contour,
            final SliceInfo sliceInfo)
    {
        this.linkNode = linkNode;
        this.centerLine = centerLine;
        this.contour = contour;
        this.sliceInfo = sliceInfo;
    }

    /** {@inheritDoc} */
    @Override
    public Bounds<?, ?, ?> getBounds() throws RemoteException
    {
        return new Bounds2d(this.contour.getBounds().getDeltaX(), this.contour.getBounds().getDeltaY());
    }

    /** {@inheritDoc} */
    @Override
    public Point2d getLocation()
    {
        return this.contour.getBounds().midPoint();
    }

    /** {@inheritDoc} */
    @Override
    public List<Point2d> getContour()
    {
        return this.contour.getPointList();
    }

    /**
     * Returns the link id.
     * @return String; link id.
     */
    public String getLinkId()
    {
        return this.linkNode.getId();
    }

    /**
     * Returns the lane width at the give position.
     * @param position Length; position along the lane.
     * @return Length; lane width at the position.
     */
    public Length getWidth(final Length position)
    {
        return this.sliceInfo.getWidth(position.si / this.centerLine.getLength());
    }

    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        return "Cross section element of " + getLinkId();
    }

}
