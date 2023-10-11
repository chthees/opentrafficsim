package org.opentrafficsim.editor.extensions.map;

import java.rmi.RemoteException;

import org.djunits.value.vdouble.scalar.Direction;
import org.djunits.value.vdouble.scalar.Length;
import org.djutils.draw.bounds.Bounds;
import org.djutils.draw.line.PolyLine2d;
import org.djutils.draw.point.OrientedPoint2d;
import org.djutils.draw.point.Point2d;
import org.djutils.event.Event;
import org.djutils.event.EventListener;
import org.opentrafficsim.core.geometry.OtsGeometryUtil;
import org.opentrafficsim.draw.ClickableBounds;
import org.opentrafficsim.draw.network.LinkAnimation.LinkData;
import org.opentrafficsim.editor.OtsEditor;
import org.opentrafficsim.editor.XsdTreeNode;
import org.opentrafficsim.road.network.factory.xml.parser.ScenarioParser;
import org.opentrafficsim.xml.bindings.DirectionAdapter;
import org.opentrafficsim.xml.bindings.LengthAdapter;
import org.opentrafficsim.xml.bindings.Point2dAdapter;

/**
 * LinkData for the editor Map.
 * <p>
 * Copyright (c) 2023-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
public class MapLinkData extends MapData implements LinkData, EventListener
{

    /** */
    private static final long serialVersionUID = 20231003L;

    /** Point adapter. */
    private final static Point2dAdapter POINT_ADAPTER = new Point2dAdapter();

    /** Direction adapter. */
    private final static DirectionAdapter DIRECTION_ADAPTER = new DirectionAdapter();

    /** Length adapter. */
    private final static LengthAdapter LENGTH_ADAPTER = new LengthAdapter();

    /** String attribute. */
    private String id = "";

    /** Tree node of start. */
    private XsdTreeNode nodeStart;

    /** Tree node of end. */
    private XsdTreeNode nodeEnd;

    /** Start direction. */
    private Direction directionStart = Direction.ZERO;

    /** End direction. */
    private Direction directionEnd = Direction.ZERO;

    /** Start offset. */
    private Length offsetStart;

    /** End offset. */
    private Length offsetEnd;

    /** From point. */
    private Point2d from;

    /** To point. */
    private Point2d to;

    /** Design line. */
    private PolyLine2d designLine = null;

    /** Location. */
    private OrientedPoint2d location = new OrientedPoint2d(0.0, 0.0);

    /**
     * Constructor.
     * @param map Map; map.
     * @param linkNode XsdTreeNode; node Ots.Network.Link.
     * @param editor OtsEditor; editor.
     */
    public MapLinkData(final Map map, final XsdTreeNode linkNode, final OtsEditor editor)
    {
        super(map, linkNode, editor);
        linkNode.addListener(this, XsdTreeNode.ATTRIBUTE_CHANGED);
        // for when node is duplicated, set data immediately
        try
        {
            if (getNode().isActive())
            {
                notify(new Event(XsdTreeNode.ATTRIBUTE_CHANGED, new Object[] {getNode(), "Id", null}));
                this.nodeStart = replaceNode(this.nodeStart, linkNode.getCoupledKeyrefNode("NodeStart"));
                this.nodeEnd = replaceNode(this.nodeEnd, linkNode.getCoupledKeyrefNode("NodeEnd"));
            }
        }
        catch (RemoteException e)
        {
            throw new RuntimeException(e);
        }
    }

    /** {@inheritDoc} */
    @Override
    public Bounds<?, ?, ?> getBounds() throws RemoteException
    {
        return ClickableBounds.get(this.designLine.getBounds());
    }

    /** {@inheritDoc} */
    @Override
    public String getId()
    {
        return this.id;
    }

    /** {@inheritDoc} */
    @Override
    public OrientedPoint2d getLocation()
    {
        return this.location;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isConnector()
    {
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public PolyLine2d getDesignLine()
    {
        return this.designLine;
    }

    /** {@inheritDoc} */
    @Override
    public void notify(final Event event) throws RemoteException
    {
        Object[] content = (Object[]) event.getContent();
        XsdTreeNode node = (XsdTreeNode) content[0];
        String attribute = (String) content[1];
        String value = node.getAttributeValue(attribute);

        if ("Id".equals(attribute))
        {
            this.id = value == null ? "" : value;
            return;
        }
        else if ("NodeStart".equals(attribute))
        {
            this.nodeStart = replaceNode(this.nodeStart, getNode().getCoupledKeyrefNode("NodeStart"));
        }
        else if ("NodeEnd".equals(attribute))
        {
            this.nodeEnd = replaceNode(this.nodeEnd, getNode().getCoupledKeyrefNode("NodeEnd"));
        }
        else if ("OffsetStart".equals(attribute))
        {
            setValue((v) -> this.offsetStart = v, LENGTH_ADAPTER, getNode(), attribute);
        }
        else if ("OffsetEnd".equals(attribute))
        {
            setValue((v) -> this.offsetEnd = v, LENGTH_ADAPTER, getNode(), attribute);
        }
        else if ("Coordinate".equals(attribute))
        {
            // this pertains to either of the nodes, to which this class also listens
            buildDesignLine();
        }
        else if ("Direction".equals(attribute))
        {
            // this pertains to either of the nodes, to which this class also listens
            buildDesignLine();
        }
        else
        {
            // other attribute, not important
            return;
        }
        buildDesignLine();
    }

    /**
     * Replaces the old node with the new node, adding and removing this as a listener as required. If a node refers to a
     * default input parameter node, the node that the input parameter id refers to is found instead.
     * @param oldNode XsdTreeNode; former node.
     * @param newNode XsdTreeNode; new node.
     * @return XsdTreeNode; the actual new node (Ots.Network.Node).
     */
    private XsdTreeNode replaceNode(final XsdTreeNode oldNode, final XsdTreeNode newNode)
    {
        if (oldNode != null)
        {
            oldNode.removeListener(this, XsdTreeNode.ATTRIBUTE_CHANGED);
            if (oldNode.getPathString().equals("Ots.Scenarios.DefaultInputParameters.String"))
            {
                XsdTreeNode node = getInputNode(newNode);
                if (node != null)
                {
                    node.removeListener(this, XsdTreeNode.ATTRIBUTE_CHANGED);
                }
            }
        }
        if (newNode != null)
        {
            newNode.addListener(this, XsdTreeNode.ATTRIBUTE_CHANGED);
            if (newNode.getPathString().equals("Ots.Scenarios.DefaultInputParameters.String"))
            {
                XsdTreeNode node = getInputNode(newNode);
                if (node != null)
                {
                    node.addListener(this, XsdTreeNode.ATTRIBUTE_CHANGED);
                }
                return node;
            }
        }
        return newNode;
    }

    /**
     * Finds the network node that the value of an input parameter node refers to.
     * @param inputParameter XsdTreeNode; input parameter node (default).
     * @return XsdTreeNode; the actual node (Ots.Network.Node).
     */
    private XsdTreeNode getInputNode(final XsdTreeNode inputParameter)
    {
        String inputId = inputParameter.getId();
        String nodeId;
        try
        {
            nodeId = (String) getEval().evaluate(inputId.substring(1, inputId.length() - 1));
        }
        catch (RuntimeException ex)
        {
            // TODO: dirty trick to obtain a value that was given to Eval, which not yet supports non Boolean/DoubleScalar.
            nodeId = (String) ScenarioParser.lastLookedUp;
        }
        XsdTreeNode ots = inputParameter.getPath().get(0);
        for (XsdTreeNode child : ots.getChildren())
        {
            if (child.getPathString().equals("Ots.Network"))
            {
                for (XsdTreeNode networkElement : child.getChildren())
                {
                    if (networkElement.isType("Node") && networkElement.getId().equals(nodeId))
                    {
                        return networkElement;
                    }
                }
            }
        }
        return null;
    }

    /**
     * Builds the design line.
     */
    private void buildDesignLine()
    {
        if (this.nodeStart == null || this.nodeEnd == null || this.nodeStart.equals(this.nodeEnd))
        {
            setInvalid();
            return;
        }
        setValue((v) -> this.from = v, POINT_ADAPTER, this.nodeStart, "Coordinate");
        setValue((v) -> this.to = v, POINT_ADAPTER, this.nodeEnd, "Coordinate");
        if (this.from == null || this.to == null)
        {
            setInvalid();
            return;
        }
        Point2d from = this.from;
        Point2d to = this.to;
        if (this.offsetStart != null)
        {
            setValue((v) -> this.directionStart = v, DIRECTION_ADAPTER, this.nodeStart, "Direction");
            double dir = this.directionStart == null ? 0.0 : this.directionStart.si;
            from = OtsGeometryUtil.offsetPoint(new OrientedPoint2d(from, dir), this.offsetStart.si);
        }
        if (this.offsetEnd != null)
        {
            setValue((v) -> this.directionEnd = v, DIRECTION_ADAPTER, this.nodeEnd, "Direction");
            double dir = this.directionEnd == null ? 0.0 : this.directionEnd.si;
            to = OtsGeometryUtil.offsetPoint(new OrientedPoint2d(to, dir), this.offsetEnd.si);
        }
        this.designLine = new PolyLine2d(from, to);
        setValid();
    }

    /** {@inheritDoc} */
    @Override
    public void destroy()
    {
        super.destroy();
        this.getNode().removeListener(this, XsdTreeNode.ATTRIBUTE_CHANGED);
    }

    /** {@inheritDoc} */
    @Override
    public void evalChanged()
    {
        this.id = getNode().getId() == null ? "" : getNode().getId();
        this.nodeStart = replaceNode(this.nodeStart, getNode().getCoupledKeyrefNode("NodeStart"));
        this.nodeEnd = replaceNode(this.nodeEnd, getNode().getCoupledKeyrefNode("NodeEnd"));
        buildDesignLine();
    }

    /**
     * Notification from the Map that a node (Ots.Network.Node) id was changed.
     * @param node XsdTreeNode; node.
     */
    public void notifyNodeIdChanged(final XsdTreeNode node)
    {
        evalChanged(); // same effect
    }

}
