package org.opentrafficsim.core.network;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.awt.geom.Rectangle2D;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.djutils.event.Event;
import org.djutils.event.EventListener;
import org.djutils.event.EventType;
import org.junit.Test;
import org.opentrafficsim.core.compatibility.GtuCompatibility;
import org.opentrafficsim.core.definitions.DefaultsNl;
import org.opentrafficsim.core.dsol.OtsSimulatorInterface;
import org.opentrafficsim.core.geometry.OtsGeometryException;
import org.opentrafficsim.core.geometry.OtsLine3d;
import org.opentrafficsim.core.geometry.OtsPoint3d;
import org.opentrafficsim.core.gtu.Gtu;
import org.opentrafficsim.core.gtu.GtuType;
import org.opentrafficsim.core.mock.MockGtu;
import org.opentrafficsim.core.mock.MockSimulator;
import org.opentrafficsim.core.network.route.Route;

/**
 * <p>
 * Copyright (c) 2013-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
public class NetworkTest implements EventListener
{
    /** ... */
    private static final long serialVersionUID = 1L;

    /** Count NODE_ADD events. */
    private int nodeAddedCount = 0;

    /** Count NODE_REMOVE events. */
    private int nodeRemovedCount = 0;

    /** Count LINK_ADD events. */
    private int linkAddedCount = 0;

    /** Count LINK_REMOVE events. */
    private int linkRemovedCount = 0;

    /** Count GTU_ADD events. */
    private int gtuAddedCount = 0;

    /** Count GTU_REMOVE events. */
    private int gtuRemovedCount = 0;

    /** Count other events. */
    private int otherEventCount = 0;

    /**
     * Test Network class.
     * @throws NetworkException if that happens; this test has failed
     * @throws OtsGeometryException if that happens; this test has failed
     */
    @Test
    public final void testNetwork() throws NetworkException, OtsGeometryException
    {
        String networkId = "testNetwork";
        OtsSimulatorInterface simulator = MockSimulator.createMock();
        Network network = new Network(networkId, simulator);
        assertTrue("Id must match", networkId.equals(network.getId()));
        network.addListener(this, Network.LINK_ADD_EVENT);
        network.addListener(this, Network.LINK_REMOVE_EVENT);
        network.addListener(this, Network.NODE_ADD_EVENT);
        network.addListener(this, Network.NODE_REMOVE_EVENT);
        network.addListener(this, Network.GTU_ADD_EVENT);
        network.addListener(this, Network.GTU_REMOVE_EVENT);
        assertEquals("link add event count is 0", 0, this.linkAddedCount);
        assertEquals("link removed event count is 0", 0, this.linkRemovedCount);
        assertEquals("node add event count is 0", 0, this.nodeAddedCount);
        assertEquals("node removed event count is 0", 0, this.nodeRemovedCount);
        assertEquals("GTU add event count is 0", 0, this.gtuAddedCount);
        assertEquals("GTU removed event count is 0", 0, this.gtuRemovedCount);
        assertEquals("other event count is 0", 0, this.otherEventCount);
        assertEquals("Node map is empty", 0, network.getNodeMap().size());
        Node node1 = new Node(network, "node1", new OtsPoint3d(10, 20, 30));
        assertEquals("link add event count is 0", 0, this.linkAddedCount);
        assertEquals("link removed event count is 0", 0, this.linkRemovedCount);
        assertEquals("node add event count is 1", 1, this.nodeAddedCount);
        assertEquals("node removed event count is 0", 0, this.nodeRemovedCount);
        assertEquals("GTU add event count is 0", 0, this.gtuAddedCount);
        assertEquals("GTU removed event count is 0", 0, this.gtuRemovedCount);
        assertEquals("other event count is 0", 0, this.otherEventCount);
        assertEquals("Node map now contains one node", 1, network.getNodeMap().size());
        assertEquals("Node is node1", node1, network.getNodeMap().values().iterator().next());
        assertEquals("Raw node map also contains one node", 1, network.getRawNodeMap().size());
        assertEquals("Raw node map also contains node1", node1, network.getRawNodeMap().values().iterator().next());
        assertEquals("Node can be retrieved by id", node1, network.getNode(node1.getId()));
        assertTrue("network contains a node with id node1", network.containsNode("node1"));
        // Create a node that is NOT in this network; to do that we must create another network
        Network otherNetwork = new Network("other network", simulator);
        Node node2 = new Node(otherNetwork, "node2", new OtsPoint3d(11, 12, 13));
        assertFalse("node2 is NOT in network", network.containsNode(node2));
        assertEquals("link add event count is 0", 0, this.linkAddedCount);
        assertEquals("link removed event count is 0", 0, this.linkRemovedCount);
        assertEquals("node add event count is 1", 1, this.nodeAddedCount);
        assertEquals("node removed event count is 0", 0, this.nodeRemovedCount);
        assertEquals("GTU add event count is 0", 0, this.gtuAddedCount);
        assertEquals("GTU removed event count is 0", 0, this.gtuRemovedCount);
        assertEquals("other event count is 0", 0, this.otherEventCount);
        try
        {
            new Node(network, "node1", new OtsPoint3d(110, 20, 30));
            fail("duplicate node id should have thrown a NetworkException");
        }
        catch (NetworkException ne)
        {
            // Ignore expected exception
        }
        try
        {
            network.addNode(node1);
            fail("duplicate node should have thrown a NetworkException");
        }
        catch (NetworkException ne)
        {
            // Ignore expected exception
        }
        network.removeNode(node1);
        assertEquals("link add event count is 0", 0, this.linkAddedCount);
        assertEquals("link removed event count is 0", 0, this.linkRemovedCount);
        assertEquals("node add event count is 1", 1, this.nodeAddedCount);
        assertEquals("node removed event count is 1", 1, this.nodeRemovedCount);
        assertEquals("GTU add event count is 0", 0, this.gtuAddedCount);
        assertEquals("GTU removed event count is 0", 0, this.gtuRemovedCount);
        assertEquals("other event count is 0", 0, this.otherEventCount);
        assertEquals("Node map is empty", 0, network.getNodeMap().size());
        assertEquals("network now had 0 nodes", 0, network.getNodeMap().size());
        try
        {
            network.removeNode(node1);
            fail("Attempt to remove an already removed node should have thrown a NetworkException");
        }
        catch (NetworkException ne)
        {
            // Ignore expected exception
        }
        network.addNode(node1);
        assertEquals("Node map now contains one node", 1, network.getNodeMap().size());
        assertEquals("Node is node1", node1, network.getNodeMap().values().iterator().next());
        assertEquals("Node can be retrieved by id", node1, network.getNode(node1.getId()));
        assertEquals("LinkMap is empty", 0, network.getLinkMap().size());
        assertEquals("link add event count is 0", 0, this.linkAddedCount);
        assertEquals("link removed event count is 0", 0, this.linkRemovedCount);
        assertEquals("node add event count is 2", 2, this.nodeAddedCount);
        assertEquals("node removed event count is 1", 1, this.nodeRemovedCount);
        assertEquals("GTU add event count is 0", 0, this.gtuAddedCount);
        assertEquals("GTU removed event count is 0", 0, this.gtuRemovedCount);
        assertEquals("other event count is 0", 0, this.otherEventCount);
        try
        {
            new Link(network, "link1", node1, node2, DefaultsNl.ROAD, new OtsLine3d(node1.getPoint(), node2.getPoint()));
            fail("new OTSLink should have thrown an exception because node2 is not in network");
        }
        catch (NetworkException ne)
        {
            // Ignore expected exception
        }
        try
        {
            new Link(network, "link1", node2, node1, DefaultsNl.ROAD, new OtsLine3d(node2.getPoint(), node1.getPoint()));
            fail("new OTSLink should have thrown an exception because node2 is not in network");
        }
        catch (NetworkException ne)
        {
            // Ignore expected exception
        }
        Node node3 = new Node(network, "node3", new OtsPoint3d(11, 12, 13));
        assertEquals("link add event count is 0", 0, this.linkAddedCount);
        assertEquals("link removed event count is 0", 0, this.linkRemovedCount);
        assertEquals("node add event count is 3", 3, this.nodeAddedCount);
        assertEquals("node removed event count is 1", 1, this.nodeRemovedCount);
        assertEquals("GTU add event count is 0", 0, this.gtuAddedCount);
        assertEquals("GTU removed event count is 0", 0, this.gtuRemovedCount);
        assertEquals("other event count is 0", 0, this.otherEventCount);
        Link link1 =
                new Link(network, "link1", node1, node3, DefaultsNl.ROAD, new OtsLine3d(node1.getPoint(), node3.getPoint()));
        assertEquals("LinkMap now contains 1 link", 1, network.getLinkMap().size());
        assertTrue("LinkMap contains link1", network.containsLink(link1));
        assertTrue("LinkMap.contain link with name link1", network.containsLink("link1"));
        assertEquals("link add event count is 1", 1, this.linkAddedCount);
        assertEquals("link removed event count is 0", 0, this.linkRemovedCount);
        assertEquals("node add event count is 3", 3, this.nodeAddedCount);
        assertEquals("node removed event count is 1", 1, this.nodeRemovedCount);
        assertEquals("GTU add event count is 0", 0, this.gtuAddedCount);
        assertEquals("GTU removed event count is 0", 0, this.gtuRemovedCount);
        assertEquals("other event count is 0", 0, this.otherEventCount);
        try
        {
            network.addLink(link1);
            fail("Adding link1 again should have thrown a NetworkException");
        }
        catch (NetworkException ne)
        {
            // Ignore expected exception
        }
        assertEquals("link1 is the link connecting node1 to node3", link1, network.getLink(node1, node3));
        assertEquals("link1 is the link connecting node named node1 to node named node3", link1,
                network.getLink("node1", "node3"));
        Node node4 = new Node(otherNetwork, "node4", new OtsPoint3d(-2, -3, -4));
        Link otherLink = new Link(otherNetwork, "otherLink", node2, node4, DefaultsNl.ROAD,
                new OtsLine3d(node2.getPoint(), node4.getPoint()));
        try
        {
            network.removeLink(otherLink);
            fail("Removing a link that is in another network should have thrown a NetworkException");
        }
        catch (NetworkException ne)
        {
            // Ignore expected exception
        }
        try
        {
            network.addLink(otherLink);
            fail("Adding a link that connects nodes not in the network should have thrown a NetworkException");
        }
        catch (NetworkException ne)
        {
            // Ignore expected exception
        }
        assertEquals("link add event count is 1", 1, this.linkAddedCount);
        assertEquals("link removed event count is 0", 0, this.linkRemovedCount);
        assertEquals("node add event count is 3", 3, this.nodeAddedCount);
        assertEquals("node removed event count is 1", 1, this.nodeRemovedCount);
        assertEquals("GTU add event count is 0", 0, this.gtuAddedCount);
        assertEquals("GTU removed event count is 0", 0, this.gtuRemovedCount);
        assertEquals("other event count is 0", 0, this.otherEventCount);
        Link secondLink = new Link(network, "reverseLink", node3, node1, DefaultsNl.ROAD,
                new OtsLine3d(node3.getPoint(), node1.getPoint()));
        assertEquals("link add event count is 2", 2, this.linkAddedCount);
        assertEquals("link removed event count is 0", 0, this.linkRemovedCount);
        assertEquals("node add event count is 3", 3, this.nodeAddedCount);
        assertEquals("node removed event count is 1", 1, this.nodeRemovedCount);
        assertEquals("GTU add event count is 0", 0, this.gtuAddedCount);
        assertEquals("GTU removed event count is 0", 0, this.gtuRemovedCount);
        assertEquals("other event count is 0", 0, this.otherEventCount);
        assertTrue("Network contains secondLink", network.containsLink(secondLink));
        assertTrue("Network contains link named reverseLink", network.containsLink("reverseLink"));
        assertFalse("Network does not contain link named junk", network.containsLink("junk"));
        try
        {
            network.getLink("junk", "node3");
            fail("looking up a link starting at nonexistent node should have thrown a NetworkException");
        }
        catch (NetworkException ne)
        {
            // Ignore expected exception
        }
        try
        {
            network.getLink("node1", "junk");
            fail("looking up a link ending at nonexistent node should have thrown a NetworkException");
        }
        catch (NetworkException ne)
        {
            // Ignore expected exception
        }
        assertEquals("lookup link from node node1 to node node3", link1, network.getLink("node1", "node3"));
        assertEquals("lookup link from node1 to node3", link1, network.getLink(node1, node3));
        assertEquals("lookup link from node node3 to node node1", secondLink, network.getLink("node3", "node1"));
        assertEquals("lookup link from node3 to node1", secondLink, network.getLink(node3, node1));
        assertNull("lookup link that does not exist but both nodes do exist", network.getLink(node1, node1));
        assertNull("lookup link that does not exist but both nodes do exist", network.getLink("node1", "node1"));
        assertEquals("lookup link by name", link1, network.getLink("link1"));
        assertEquals("lookup link by name", secondLink, network.getLink("reverseLink"));
        network.removeLink(link1);
        assertFalse("Network no longer contains link1", network.containsLink(link1));
        assertFalse("Network no longer contains link with name link1", network.containsLink("link1"));
        assertEquals("link add event count is 2", 2, this.linkAddedCount);
        assertEquals("link removed event count is 1", 1, this.linkRemovedCount);
        assertEquals("node add event count is 3", 3, this.nodeAddedCount);
        assertEquals("node removed event count is 1", 1, this.nodeRemovedCount);
        assertEquals("GTU add event count is 0", 0, this.gtuAddedCount);
        assertEquals("GTU removed event count is 0", 0, this.gtuRemovedCount);
        assertEquals("other event count is 0", 0, this.otherEventCount);
        assertEquals("network now contains one link", 1, network.getLinkMap().size());
        MockGtu mockGtu1 = new MockGtu("gtu1");
        Gtu gtu1 = mockGtu1.getMock();
        network.addGTU(gtu1);
        assertEquals("link add event count is 2", 2, this.linkAddedCount);
        assertEquals("link removed event count is 1", 1, this.linkRemovedCount);
        assertEquals("node add event count is 3", 3, this.nodeAddedCount);
        assertEquals("node removed event count is 1", 1, this.nodeRemovedCount);
        assertEquals("GTU add event count is 1", 1, this.gtuAddedCount);
        assertEquals("GTU removed event count is 0", 0, this.gtuRemovedCount);
        MockGtu mockGtu2 = new MockGtu("gtu2");
        Gtu gtu2 = mockGtu2.getMock();
        network.addGTU(gtu2);
        assertEquals("link add event count is 2", 2, this.linkAddedCount);
        assertEquals("link removed event count is 1", 1, this.linkRemovedCount);
        assertEquals("node add event count is 3", 3, this.nodeAddedCount);
        assertEquals("node removed event count is 1", 1, this.nodeRemovedCount);
        assertEquals("GTU add event count is 2", 2, this.gtuAddedCount);
        assertEquals("GTU removed event count is 0", 0, this.gtuRemovedCount);
        assertEquals("gtu1 can be retrieved", gtu1, network.getGTU("gtu1"));
        assertEquals("gtu2 can be retrieved", gtu2, network.getGTU("gtu2"));
        network.removeGTU(gtu1);
        assertEquals("link add event count is 2", 2, this.linkAddedCount);
        assertEquals("link removed event count is 1", 1, this.linkRemovedCount);
        assertEquals("node add event count is 3", 3, this.nodeAddedCount);
        assertEquals("node removed event count is 1", 1, this.nodeRemovedCount);
        assertEquals("GTU add event count is 2", 2, this.gtuAddedCount);
        assertEquals("GTU removed event count is 1", 1, this.gtuRemovedCount);
        network.removeGTU(gtu2);
        assertEquals("link add event count is 2", 2, this.linkAddedCount);
        assertEquals("link removed event count is 1", 1, this.linkRemovedCount);
        assertEquals("node add event count is 3", 3, this.nodeAddedCount);
        assertEquals("node removed event count is 1", 1, this.nodeRemovedCount);
        assertEquals("GTU add event count is 2", 2, this.gtuAddedCount);
        assertEquals("GTU removed event count is 2", 2, this.gtuRemovedCount);
        assertNull("gtu1 can no longer be retrieved", network.getGTU("gtu1"));
        assertNull("gtu2 can no longer be retrieved", network.getGTU("gtu2"));
        assertTrue("toString contains the name of the network", network.toString().contains(network.getId()));
    }

    /**
     * Test the getExtent method of Network.
     * @throws NetworkException if that happens uncaught, this test has failed
     */
    @Test
    public final void testExtent() throws NetworkException
    {
        Network network = new Network("test", MockSimulator.createMock());
        Rectangle2D extent = network.getExtent();
        assertEquals("extend left", -500, extent.getMinX(), 0);
        assertEquals("extend bottom", -500, extent.getMinY(), 0);
        assertEquals("extend right", 500, extent.getMaxX(), 0);
        assertEquals("extend top", 500, extent.getMaxY(), 0);

        // Add one node
        new Node(network, "node1", new OtsPoint3d(10, 20, 30));
        extent = network.getExtent();
        double margin = Node.BOUNDINGRADIUS * (1.0 + Network.EXTENT_MARGIN);
        assertEquals("extend left", 10 - margin, extent.getMinX(), 0.01);
        assertEquals("extend bottom", 20 - margin, extent.getMinY(), 0.01);
        assertEquals("extend right", 10 + margin, extent.getMaxX(), 0.01);
        assertEquals("extend top", 20 + margin, extent.getMaxY(), 0.01);
        // Add another node
        new Node(network, "node2", new OtsPoint3d(110, 220, 330));
        extent = network.getExtent();
        double xMargin = (100 + 2 * Node.BOUNDINGRADIUS) * Network.EXTENT_MARGIN / 2;
        double yMargin = (200 + 2 * Node.BOUNDINGRADIUS) * Network.EXTENT_MARGIN / 2;
        assertEquals("extend left", 10 - Node.BOUNDINGRADIUS - xMargin, extent.getMinX(), 0.01);
        assertEquals("extend bottom", 20 - Node.BOUNDINGRADIUS - yMargin, extent.getMinY(), 0.01);
        assertEquals("extend right", 110 + Node.BOUNDINGRADIUS + xMargin, extent.getMaxX(), 0.01);
        assertEquals("extend top", 220 + Node.BOUNDINGRADIUS + yMargin, extent.getMaxY(), 0.01);
    }

    /** {@inheritDoc} */
    @Override
    public final void notify(final Event event) throws RemoteException
    {
        EventType type = event.getType();
        if (type.equals(Network.NODE_ADD_EVENT))
        {
            this.nodeAddedCount++;
        }
        else if (type.equals(Network.NODE_REMOVE_EVENT))
        {
            this.nodeRemovedCount++;
        }
        else if (type.equals(Network.LINK_ADD_EVENT))
        {
            this.linkAddedCount++;
        }
        else if (type.equals(Network.LINK_REMOVE_EVENT))
        {
            this.linkRemovedCount++;
        }
        else if (type.equals(Network.GTU_ADD_EVENT))
        {
            this.gtuAddedCount++;
        }
        else if (type.equals(Network.GTU_REMOVE_EVENT))
        {
            this.gtuRemovedCount++;
        }
        else
        {
            this.otherEventCount++;
        }
    }

    /**
     * Test the route map stuff.
     * @throws NetworkException if that happens uncaught; this test has failed
     * @throws OtsGeometryException if that happens uncaught; this test has failed
     */
    @Test
    public final void testRouteMap() throws NetworkException, OtsGeometryException
    {
        Network network = new Network("Route map test network", MockSimulator.createMock());
        Node node1 = new Node(network, "node1", new OtsPoint3d(10, 20, 30));
        Node node2 = new Node(network, "node2", new OtsPoint3d(110, 20, 30));
        List<Node> nodeList = new ArrayList<>();
        nodeList.add(node1);
        nodeList.add(node2);
        GtuType carType = new GtuType("car", DefaultsNl.CAR);
        GtuType bicycleType = new GtuType("bicycle", DefaultsNl.BICYCLE);
        new Link(network, "Link12", node1, node2, DefaultsNl.ROAD, new OtsLine3d(node1.getPoint(), node2.getPoint()));
        Route route1 = new Route("route1", carType, nodeList);
        Route route2 = new Route("route2", carType);
        Route route3 = new Route("route3", bicycleType);
        // The next test makes little sense until the getters are changed to search up to the GtuType root.
        assertEquals("initially the network has 0 routes", 0, network.getDefinedRouteMap(DefaultsNl.VEHICLE).size());
        network.addRoute(carType, route1);
        assertEquals("list for carType contains one entry", 1, network.getDefinedRouteMap(carType).size());
        assertEquals("route for carType route1 is route1", route1, network.getRoute(carType, "route1"));
        assertNull("route for bycicleType route1 is null", network.getRoute(bicycleType, "route1"));
        assertEquals("list for bicycleType contains 0 routes", 0, network.getDefinedRouteMap(bicycleType).size());
        network.addRoute(carType, route2);
        network.addRoute(bicycleType, route3);
        assertEquals("list for carType contains two entries", 2, network.getDefinedRouteMap(carType).size());
        assertEquals("list for bicycleType contains one entry", 1, network.getDefinedRouteMap(bicycleType).size());
        assertEquals("route for carType route1 is route1", route1, network.getRoute(carType, "route1"));
        assertEquals("route for carType route2 is route2", route2, network.getRoute(carType, "route2"));
        assertEquals("route for bicycle route3 is route3", route3, network.getRoute(bicycleType, "route3"));
        assertNull("route for bicycle route1 is null", network.getRoute(bicycleType, "route1"));
        try
        {
            network.addRoute(carType, route2);
            fail("adding route again should have thrown a NetworkException");
        }
        catch (NetworkException ne)
        {
            // Ignore expected exception
        }
        Network otherNetwork = new Network("other Route map test network", MockSimulator.createMock());
        Node badNode = new Node(otherNetwork, "nodeInOtherNetwork", new OtsPoint3d(100, 200, 0));
        List<Node> badNodeList = new ArrayList<>();
        badNodeList.add(node1);
        badNodeList.add(node2);
        badNodeList.add(badNode);
        try
        {
            Route badRoute = new Route("badRoute", carType, badNodeList);
            fail("creating a route with a node that is not in the network should have thrown a NetworkException");
        }
        catch (NetworkException ne)
        {
            // Ignore expected exception
        }
        try
        {
            network.removeRoute(bicycleType, route1);
            fail("attempt to remove a route that is not defined for this GtuType should have thrown a NetworkException");
        }
        catch (NetworkException ne)
        {
            // Ignore expected exception
        }
        assertEquals("there is one route from node1 to node2 for carType", 1,
                network.getRoutesBetween(carType, node1, node2).size());
        assertEquals("the one route from node1 to node2 is route1", route1,
                network.getRoutesBetween(carType, node1, node2).iterator().next());
        assertEquals("there are no routes from node1 to node2 for bicycleType", 0,
                network.getRoutesBetween(bicycleType, node1, node2).size());
        assertEquals("there are no routes from node2 to node1 for carTypecleType", 0,
                network.getRoutesBetween(carType, node2, node1).size());
        assertEquals("there are no routes from node1 to node1 for carTypecleType", 0,
                network.getRoutesBetween(carType, node1, node1).size());
        GtuType junkType = new GtuType("junk", DefaultsNl.VEHICLE);
        assertEquals("there are no routes from node1 to node2 for badType", 0,
                network.getRoutesBetween(junkType, node1, node2).size());
        network.removeRoute(carType, route1);
        assertEquals("list for carType now contains one entry", 1, network.getDefinedRouteMap(carType).size());
        assertEquals("list for bicycleType contains one entry", 1, network.getDefinedRouteMap(bicycleType).size());
        assertNull("route for carType route1 is null", network.getRoute(carType, "route1"));
        assertEquals("route for carType route2 is route2", route2, network.getRoute(carType, "route2"));
        assertEquals("route for bicycle route3 is route3", route3, network.getRoute(bicycleType, "route3"));
        assertTrue("network contains route2 for carType", network.containsRoute(carType, route2));
        assertFalse("network does not contain route1 for carType", network.containsRoute(carType, route1));
        assertTrue("network contains route with name route2 for carType", network.containsRoute(carType, "route2"));
        assertFalse("network does not contain route with name route1 for carType", network.containsRoute(carType, "route1"));
        assertFalse("network does not contain route with name route1 for junkType", network.containsRoute(junkType, "route1"));
    }

    /**
     * Test the shortest path functionality.
     * @throws NetworkException if that happens uncaught; this test has failed
     * @throws OtsGeometryException if that happens uncaught; this test has failed
     */
    @Test
    public final void testShortestPathBiDirectional() throws NetworkException, OtsGeometryException
    {
        Network network = new Network("shortest path test network", MockSimulator.createMock());
        List<Node> nodes = createRingNodesAndLinks(network);
        int maxNode = nodes.size();
        for (int skip = 1; skip < maxNode / 2; skip++)
        {
            for (int fromNodeIndex = 0; fromNodeIndex < maxNode; fromNodeIndex++)
            {
                Node fromNode = nodes.get(fromNodeIndex);
                Node toNode = nodes.get((fromNodeIndex + skip) % maxNode);
                Route route = network.getShortestRouteBetween(DefaultsNl.VEHICLE, fromNode, toNode);
                assertEquals("route size is skip + 1", skip + 1, route.size());
                for (int i = 0; i < route.size(); i++)
                {
                    assertEquals("node in route at position i should match", nodes.get((fromNodeIndex + i) % maxNode),
                            route.getNode(i));
                }
                Route routeWithExplicitLengthWeight =
                        network.getShortestRouteBetween(DefaultsNl.VEHICLE, fromNode, toNode, LinkWeight.LENGTH);
                assertEquals("route with explicit link weight should be the same", route, routeWithExplicitLengthWeight);
                // reverse direction
                route = network.getShortestRouteBetween(DefaultsNl.VEHICLE, toNode, fromNode);
                // System.out.println("Shortest route from " + toNode + " to " + fromNode + " is " + route);
                assertEquals("route size is 10 - skip + 1", 10 - skip + 1, route.size());
            }
        }
    }

    /**
     * Test the shortest path functionality.
     * @throws NetworkException if that happens uncaught; this test has failed
     * @throws OtsGeometryException if that happens uncaught; this test has failed
     */
    @Test
    public final void testShortestPathClockWise() throws NetworkException, OtsGeometryException
    {
        Network network = new Network("shortest path test network", MockSimulator.createMock());
        List<Node> nodes = createRingNodesAndLinks(network);
        int maxNode = nodes.size();
        for (int skip = 1; skip < maxNode; skip++)
        {
            for (int fromNodeIndex = 0; fromNodeIndex < maxNode; fromNodeIndex++)
            {
                Node fromNode = nodes.get(fromNodeIndex);
                Node toNode = nodes.get((fromNodeIndex + skip) % maxNode);
                Route route = network.getShortestRouteBetween(DefaultsNl.VEHICLE, fromNode, toNode);
                assertEquals("route size is skip + 1", skip + 1, route.size());
                for (int i = 0; i < route.size(); i++)
                {
                    assertEquals("node in route at position i should match", nodes.get((fromNodeIndex + i) % maxNode),
                            route.getNode(i));
                }
                // reverse direction
                route = network.getShortestRouteBetween(DefaultsNl.VEHICLE, toNode, fromNode);
                // System.out.println("Shortest route from " + toNode + " to " + fromNode + " is " + route);
                assertEquals("route size is maxNode - skip + 1", maxNode - skip + 1, route.size());
                for (int i = 0; i < route.size(); i++)
                {
                    assertEquals("node in route at position i should match", nodes.get((fromNodeIndex + skip + i) % maxNode),
                            route.getNode(i));
                }
            }
        }
    }

    /**
     * Test the shortest path method that takes a list of intermediate nodes.
     * @throws OtsGeometryException if that happens uncaught; this test has failed
     * @throws NetworkException if that happens uncaught; this test has failed
     */
    @Test
    public final void testShortestPathWithIntermediateNodes() throws NetworkException, OtsGeometryException
    {
        Network network = new Network("shortest path test network", MockSimulator.createMock());
        List<Node> nodes = createRingNodesAndLinks(network, 5);
        int maxNode = nodes.size();
        for (int fromNodeIndex = 0; fromNodeIndex < maxNode; fromNodeIndex++)
        {
            Node fromNode = network.getNode("node" + fromNodeIndex);
            for (int intermediateNodes = 0; intermediateNodes <= 2; intermediateNodes++)
            {
                // Because the number of nodes is odd, and they are evenly spread out; there is never a tie
                int numPaths = (int) Math.pow(maxNode - 1, intermediateNodes);
                for (int path = 0; path < numPaths; path++)
                {
                    List<Node> viaNodes = new ArrayList<>();
                    int prevNodeIndex = fromNodeIndex;
                    int pathNumber = path;
                    for (int step = 0; step < intermediateNodes; step++)
                    {
                        int nextNodeIndex = pathNumber % (maxNode - 1);
                        if (nextNodeIndex >= prevNodeIndex)
                        {
                            nextNodeIndex = (nextNodeIndex + 1) % maxNode;
                        }
                        viaNodes.add(network.getNode("node" + nextNodeIndex));
                        prevNodeIndex = nextNodeIndex;
                        pathNumber /= (maxNode - 1);
                    }
                    for (int toNodeIndex = 0; toNodeIndex < maxNode; toNodeIndex++)
                    {
                        if (prevNodeIndex == toNodeIndex)
                        {
                            continue;
                        }
                        // System.out.print("Path " + path + " from " + fromNodeIndex + " to " + toNodeIndex + " visits");
                        // for (Node node : viaNodes)
                        // {
                        // System.out.print(" " + node.getId());
                        // }
                        // System.out.println("");
                        Node toNode = network.getNode("node" + toNodeIndex);
                        Route route = network.getShortestRouteBetween(DefaultsNl.VEHICLE, fromNode, toNode, viaNodes);
                        // Now compute the expected path using our knowledge about the structure
                        List<Node> expectedPath = new ArrayList<>();
                        expectedPath.add(fromNode);
                        viaNodes.add(network.getNode("node" + toNodeIndex));
                        int from = fromNodeIndex;
                        for (int positionInPlan = 0; positionInPlan < viaNodes.size(); positionInPlan++)
                        {
                            Node nextNode = viaNodes.get(positionInPlan);
                            int to = Integer.parseInt(nextNode.getId().substring(4));
                            int distance = (to + maxNode - from) % maxNode;
                            if (distance > maxNode / 2)
                            {
                                distance -= maxNode;
                            }
                            boolean clockWise = true;
                            while (from != to)
                            {
                                from = (from + (clockWise ? 1 : maxNode - 1)) % maxNode;
                                expectedPath.add(network.getNode("node" + from));
                            }
                        }
                        assertEquals("expected path should have same length as route", expectedPath.size(), route.size());
                        for (int i = 0; i < expectedPath.size(); i++)
                        {
                            assertEquals("node i should match", expectedPath.get(i), route.getNode(i));
                        }
                        route = network.getShortestRouteBetween(DefaultsNl.VEHICLE, fromNode, toNode, viaNodes);
                        Route routeWithExplicitLengthAsWeight = network.getShortestRouteBetween(DefaultsNl.VEHICLE, fromNode,
                                toNode, viaNodes, LinkWeight.LENGTH);
                        assertEquals("route with explicit weight should be same as route", route,
                                routeWithExplicitLengthAsWeight);
                    }
                }
            }
        }
    }

    /**
     * Construct a ring of 10 nodes with links in clockwise fashion.
     * @param network Network; the network that will contain the nodes
     * @return List&lt;Node&gt;; the constructed nodes (in clockwise order)
     * @throws NetworkException if that happens uncaught; this test has failed
     * @throws OtsGeometryException if that happens uncaught; this test has failed
     */
    private List<Node> createRingNodesAndLinks(final Network network) throws NetworkException, OtsGeometryException
    {
        return createRingNodesAndLinks(network, 10);
    }

    /**
     * Construct a ring of nodes with links in clockwise fashion.
     * @param network Network; the network that will contain the nodes
     * @param maxNode int; number of nodes on the ring
     * @return List&lt;Node&gt;; the constructed nodes (in clockwise order)
     * @throws NetworkException if that happens uncaught; this test has failed
     * @throws OtsGeometryException if that happens uncaught; this test has failed
     */
    private List<Node> createRingNodesAndLinks(final Network network, final int maxNode)
            throws NetworkException, OtsGeometryException
    {
        LinkType linkType = new LinkType("linkType", null);
        linkType.addCompatibleGtuType(DefaultsNl.ROAD_USER);
        List<Node> nodes = new ArrayList<>();
        double radius = 500;
        double centerX = 0;
        double centerY = 0;
        for (int i = 0; i < maxNode; i++)
        {
            double angle = i * Math.PI * 2 / maxNode;
            nodes.add(new Node(network, "node" + i,
                    new OtsPoint3d(centerX + radius * Math.cos(angle), centerY + radius * Math.sin(angle), 20)));
        }
        // Create bi-directional links between all adjacent nodes
        Node prevNode = nodes.get(maxNode - 1);
        for (Node node : nodes)
        {
            new Link(network, "from " + prevNode.getId() + " to " + node.getId(), prevNode, node, linkType,
                    new OtsLine3d(prevNode.getPoint(), node.getPoint()));
            prevNode = node;
        }
        return nodes;
    }

    /**
     * Tests whether the A* algorithm delivers the same shortest path as Dijkstra.
     * @throws OtsGeometryException on error
     * @throws NetworkException on error
     */
    @Test
    public void testAStar() throws NetworkException, OtsGeometryException
    {
        boolean showTime = false; // not part of formal test, set to true for benchmarking
        long totalTimeDijkstra = 0;
        long totalTimeAStar = 0;
        int gridSize = 20;
        double sigma = 1.0;
        double sigmaLim = 0.4;
        int trials = 100;
        for (int i = 0; i < trials; i++)
        {
            Network network = new Network("shortest path test network", MockSimulator.createMock());
            Node[] od = randomTestNetwork(network, gridSize, sigma, sigmaLim);
            // this first call triggers the graph to be constructed, which should not be part of timing an algorithm
            if (showTime)
            {
                network.getShortestRouteBetween(DefaultsNl.VEHICLE, od[0], od[1], LinkWeight.LENGTH_NO_CONNECTORS);
            }
            long t1 = System.currentTimeMillis();
            Route dijkstra = network.getShortestRouteBetween(DefaultsNl.VEHICLE, od[0], od[1], LinkWeight.LENGTH_NO_CONNECTORS);
            long t2 = System.currentTimeMillis();
            Route aStar =
                    network.getShortestRouteBetween(DefaultsNl.VEHICLE, od[0], od[1], LinkWeight.ASTAR_LENGTH_NO_CONNECTORS);
            long t3 = System.currentTimeMillis();
            totalTimeDijkstra += (t2 - t1);
            totalTimeAStar += (t3 - t2);
            assertEquals("A* gave different shortest path from Dijkstra.", routeLength(dijkstra), routeLength(aStar), 0.001);
        }
        double percentage = 100.0 * totalTimeAStar / totalTimeDijkstra;
        if (showTime)
        {
            System.out.println("Dijkstra took a total of " + totalTimeDijkstra + "ms.");
            System.out.println("A* took a total of " + totalTimeAStar + "ms, which is " + percentage + "%.");
        }
    }

    /**
     * Creates a random grid network, where each node is randomly located with a 'cell' surrounding it. These cells do not
     * overlap, guaranteeing a logical network, but with random lengths. The origin will be roughly in the middle, while the
     * destination will by roughly in the middle of the upper-right quadrant. The grid will have an overall spacing of 10m.
     * @param network Network; network.
     * @param gridSize int; the network will consist of gridSize x gridSize nodes.
     * @param sigma double; Gaussian standard deviation for node location, assuming a unit grid with a spacing of 1.
     * @param sigmaLim double; limits the random location between -sigmaLim and sigmaLim around its regular grid point.
     * @return Node[]; origin (at index 0) and destination (at index 1) to use.
     * @throws NetworkException
     * @throws OtsGeometryException
     */
    private Node[] randomTestNetwork(final Network network, final int gridSize, final double sigma, final double sigmaLim)
            throws NetworkException, OtsGeometryException
    {
        double originLocation = (gridSize - 1.0) * 0.5;
        double destinationLocation = (gridSize - 1.0) * 0.75;

        int nodeNumber = 1;
        Random r = new Random();
        Node[] returnOriginDestination = new Node[2];
        for (int i = 0; i < gridSize; i++)
        {
            double y = 10.0 * (Math.max(Math.min(sigma * r.nextGaussian(), sigmaLim), -sigmaLim) + i);
            for (int j = 0; j < gridSize; j++)
            {
                double x = 10.0 * (Math.max(Math.min(sigma * r.nextGaussian(), sigmaLim), -sigmaLim) + j);
                OtsPoint3d point = new OtsPoint3d(x, y);
                Node node = new Node(network, "Node " + nodeNumber, point);

                // origin-destination
                if (returnOriginDestination[0] == null && i > originLocation && j > originLocation)
                {
                    returnOriginDestination[0] = node;
                }
                if (returnOriginDestination[1] == null && i > destinationLocation && j > destinationLocation)
                {
                    returnOriginDestination[1] = node;
                }

                // create links
                if (j > 0)
                {
                    Node up = network.getNode("Node " + (nodeNumber - 1));
                    String id = "Link " + (nodeNumber - 1) + "-" + nodeNumber;
                    OtsLine3d designLine = new OtsLine3d(up.getPoint(), node.getPoint());
                    new Link(network, id, up, node, DefaultsNl.RURAL, designLine);
                    id = "Link " + nodeNumber + "-" + (nodeNumber - 1);
                    designLine = new OtsLine3d(node.getPoint(), up.getPoint());
                    new Link(network, id, node, up, DefaultsNl.RURAL, designLine);
                }
                if (i > 0)
                {
                    Node left = network.getNode("Node " + (nodeNumber - gridSize));
                    String id = "Link " + (nodeNumber - gridSize) + "-" + nodeNumber;
                    OtsLine3d designLine = new OtsLine3d(left.getPoint(), node.getPoint());
                    new Link(network, id, left, node, DefaultsNl.RURAL, designLine);
                    id = "Link " + nodeNumber + "-" + (nodeNumber - gridSize);
                    designLine = new OtsLine3d(node.getPoint(), left.getPoint());
                    new Link(network, id, node, left, DefaultsNl.RURAL, designLine);
                }

                nodeNumber++;
            }
        }
        return returnOriginDestination;
    }

    /**
     * Calculates length of the route.
     * @param route Route; route.
     * @return double; route length.
     * @throws NetworkException
     */
    private double routeLength(final Route route) throws NetworkException
    {
        double length = 0.0;
        for (int i = 0; i < route.size(); i++)
        {
            if (i > 0)
            {
                length += route.getNode(i - 1).getPoint().distanceSI(route.getNode(i).getPoint());
            }
        }
        return length;
    }

}