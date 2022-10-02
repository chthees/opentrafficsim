package org.opentrafficsim.road.gtu.lane;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.djunits.unit.DirectionUnit;
import org.djunits.unit.DurationUnit;
import org.djunits.unit.LengthUnit;
import org.djunits.unit.PositionUnit;
import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.djunits.value.vdouble.scalar.Time;
import org.djutils.draw.point.Point3d;
import org.djutils.exceptions.Throw;
import org.djutils.exceptions.Try;
import org.djutils.immutablecollections.ImmutableLinkedHashSet;
import org.djutils.immutablecollections.ImmutableSet;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.core.dsol.OtsSimulatorInterface;
import org.opentrafficsim.core.geometry.Bounds;
import org.opentrafficsim.core.geometry.DirectedPoint;
import org.opentrafficsim.core.geometry.OtsGeometryException;
import org.opentrafficsim.core.geometry.OtsLine3D;
import org.opentrafficsim.core.geometry.OtsLine3D.FractionalFallback;
import org.opentrafficsim.core.geometry.OtsPoint3D;
import org.opentrafficsim.core.gtu.AbstractGtu;
import org.opentrafficsim.core.gtu.Gtu;
import org.opentrafficsim.core.gtu.GtuException;
import org.opentrafficsim.core.gtu.GtuType;
import org.opentrafficsim.core.gtu.RelativePosition;
import org.opentrafficsim.core.gtu.TurnIndicatorStatus;
import org.opentrafficsim.core.gtu.perception.EgoPerception;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlan;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlanBuilder;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlanException;
import org.opentrafficsim.core.network.LateralDirectionality;
import org.opentrafficsim.core.network.Link;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.perception.Historical;
import org.opentrafficsim.core.perception.HistoricalValue;
import org.opentrafficsim.core.perception.HistoryManager;
import org.opentrafficsim.core.perception.collections.HistoricalLinkedHashMap;
import org.opentrafficsim.core.perception.collections.HistoricalLinkedHashSet;
import org.opentrafficsim.core.perception.collections.HistoricalMap;
import org.opentrafficsim.core.perception.collections.HistoricalSet;
import org.opentrafficsim.road.gtu.lane.perception.LanePerception;
import org.opentrafficsim.road.gtu.lane.perception.PerceptionCollectable;
import org.opentrafficsim.road.gtu.lane.perception.RelativeLane;
import org.opentrafficsim.road.gtu.lane.perception.categories.DefaultSimplePerception;
import org.opentrafficsim.road.gtu.lane.perception.categories.InfrastructurePerception;
import org.opentrafficsim.road.gtu.lane.perception.categories.neighbors.NeighborsPerception;
import org.opentrafficsim.road.gtu.lane.perception.headway.HeadwayGtu;
import org.opentrafficsim.road.gtu.lane.plan.operational.LaneBasedOperationalPlan;
import org.opentrafficsim.road.gtu.strategical.LaneBasedStrategicalPlanner;
import org.opentrafficsim.road.network.OtsRoadNetwork;
import org.opentrafficsim.road.network.RoadNetwork;
import org.opentrafficsim.road.network.lane.CrossSectionElement;
import org.opentrafficsim.road.network.lane.CrossSectionLink;
import org.opentrafficsim.road.network.lane.Lane;
import org.opentrafficsim.road.network.lane.LanePosition;
import org.opentrafficsim.road.network.speed.SpeedLimitInfo;
import org.opentrafficsim.road.network.speed.SpeedLimitTypes;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.formalisms.eventscheduling.SimEvent;
import nl.tudelft.simulation.dsol.formalisms.eventscheduling.SimEventInterface;

/**
 * This class contains most of the code that is needed to run a lane based GTU. <br>
 * The starting point of a LaneBasedTU is that it can be in <b>multiple lanes</b> at the same time. This can be due to a lane
 * change (lateral), or due to crossing a link (front of the GTU is on another Lane than rear of the GTU). If a Lane is shorter
 * than the length of the GTU (e.g. when we do node expansion on a crossing, this is very well possible), a GTU could occupy
 * dozens of Lanes at the same time.
 * <p>
 * When calculating a headway, the GTU has to look in successive lanes. When Lanes (or underlying CrossSectionLinks) diverge,
 * the headway algorithms have to look at multiple Lanes and return the minimum headway in each of the Lanes. When the Lanes (or
 * underlying CrossSectionLinks) converge, "parallel" traffic is not taken into account in the headway calculation. Instead, gap
 * acceptance algorithms or their equivalent should guide the merging behavior.
 * <p>
 * To decide its movement, an AbstractLaneBasedGtu applies its car following algorithm and lane change algorithm to set the
 * acceleration and any lane change operation to perform. It then schedules the triggers that will add it to subsequent lanes
 * and remove it from current lanes as needed during the time step that is has committed to. Finally, it re-schedules its next
 * movement evaluation with the simulator.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 */
public abstract class AbstractLaneBasedGtu extends AbstractGtu implements LaneBasedGtu
{
    /** */
    private static final long serialVersionUID = 20140822L;

    /**
     * Fractional longitudinal positions of the reference point of the GTU on one or more links at the start of the current
     * operational plan. Because the reference point of the GTU might not be on all the links the GTU is registered on, the
     * fractional longitudinal positions can be more than one, or less than zero.
     */
    private HistoricalMap<Link, Double> fractionalLinkPositions;

    /**
     * The lanes the GTU is registered on. Each lane has to have its link registered in the fractionalLinkPositions as well to
     * keep consistency. Each link from the fractionalLinkPositions can have one or more Lanes on which the vehicle is
     * registered. This is a list to improve reproducibility: The 'oldest' lanes on which the vehicle is registered are at the
     * front of the list, the later ones more to the back.
     */
    private final HistoricalSet<Lane> currentLanes;

    /** Maps that we enter when initiating a lane change, but we may not actually enter given a deviative plan. */
    private final Set<Lane> enteredLanes = new LinkedHashSet<>();

    /** Pending leave triggers for each lane. */
    private Map<Lane, List<SimEventInterface<Duration>>> pendingLeaveTriggers = new LinkedHashMap<>();

    /** Pending enter triggers for each lane. */
    private Map<Lane, List<SimEventInterface<Duration>>> pendingEnterTriggers = new LinkedHashMap<>();

    /** Event to finalize lane change. */
    private SimEventInterface<Duration> finalizeLaneChangeEvent = null;

    /** Cached desired speed. */
    private Speed cachedDesiredSpeed;

    /** Time desired speed was cached. */
    private Time desiredSpeedTime;

    /** Cached car-following acceleration. */
    private Acceleration cachedCarFollowingAcceleration;

    /** Time car-following acceleration was cached. */
    private Time carFollowingAccelerationTime;

    /** The object to lock to make the GTU thread safe. */
    private Object lock = new Object();

    /** The threshold distance for differences between initial locations of the GTU on different lanes. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    public static Length initialLocationThresholdDifference = new Length(1.0, LengthUnit.MILLIMETER);

    /** Turn indicator status. */
    private final Historical<TurnIndicatorStatus> turnIndicatorStatus;

    /** Caching on or off. */
    // TODO: should be indicated with a Parameter
    public static boolean CACHING = true;

    /** cached position count. */
    // TODO: can be removed after testing period
    public static int CACHED_POSITION = 0;

    /** cached position count. */
    // TODO: can be removed after testing period
    public static int NON_CACHED_POSITION = 0;

    /** Vehicle model. */
    private VehicleModel vehicleModel = VehicleModel.MINMAX;

    /**
     * Construct a Lane Based GTU.
     * @param id String; the id of the GTU
     * @param gtuType GtuType; the type of GTU, e.g. TruckType, CarType, BusType
     * @param network OTSRoadNetwork; the network that the GTU is initially registered in
     * @throws GtuException when initial values are not correct
     */
    public AbstractLaneBasedGtu(final String id, final GtuType gtuType, final OtsRoadNetwork network) throws GtuException
    {
        super(id, gtuType, network.getSimulator(), network);
        OtsSimulatorInterface simulator = network.getSimulator();
        HistoryManager historyManager = simulator.getReplication().getHistoryManager(simulator);
        this.fractionalLinkPositions = new HistoricalLinkedHashMap<>(historyManager);
        this.currentLanes = new HistoricalLinkedHashSet<>(historyManager);
        this.turnIndicatorStatus = new HistoricalValue<>(historyManager, TurnIndicatorStatus.NOTPRESENT);
    }

    /**
     * @param strategicalPlanner LaneBasedStrategicalPlanner; the strategical planner (e.g., route determination) to use
     * @param initialLongitudinalPositions Set&lt;DirectedLanePosition&gt;; the initial positions of the car on one or more
     *            lanes with their directions
     * @param initialSpeed Speed; the initial speed of the car on the lane
     * @throws NetworkException when the GTU cannot be placed on the given lane
     * @throws SimRuntimeException when the move method cannot be scheduled
     * @throws GtuException when initial values are not correct
     * @throws OtsGeometryException when the initial path is wrong
     */
    @SuppressWarnings("checkstyle:designforextension")
    public void init(final LaneBasedStrategicalPlanner strategicalPlanner, final Set<LanePosition> initialLongitudinalPositions,
            final Speed initialSpeed) throws NetworkException, SimRuntimeException, GtuException, OtsGeometryException
    {
        Throw.when(null == initialLongitudinalPositions, GtuException.class, "InitialLongitudinalPositions is null");
        Throw.when(0 == initialLongitudinalPositions.size(), GtuException.class, "InitialLongitudinalPositions is empty set");

        DirectedPoint lastPoint = null;
        for (LanePosition pos : initialLongitudinalPositions)
        {
            // Throw.when(lastPoint != null && pos.getLocation().distance(lastPoint) > initialLocationThresholdDifference.si,
            // GTUException.class, "initial locations for GTU have distance > " + initialLocationThresholdDifference);
            lastPoint = pos.getLocation();
        }
        DirectedPoint initialLocation = lastPoint;

        // Give the GTU a 1 micrometer long operational plan, or a stand-still plan, so the first move and events will work
        Time now = getSimulator().getSimulatorAbsTime();
        try
        {
            if (initialSpeed.si < OperationalPlan.DRIFTING_SPEED_SI)
            {
                this.operationalPlan
                        .set(new OperationalPlan(this, initialLocation, now, new Duration(1E-6, DurationUnit.SECOND)));
            }
            else
            {
                OtsPoint3D p2 = new OtsPoint3D(initialLocation.x + 1E-6 * Math.cos(initialLocation.getRotZ()),
                        initialLocation.y + 1E-6 * Math.sin(initialLocation.getRotZ()), initialLocation.z);
                OtsLine3D path = new OtsLine3D(new OtsPoint3D(initialLocation), p2);
                this.operationalPlan.set(OperationalPlanBuilder.buildConstantSpeedPlan(this, path, now, initialSpeed));
            }
        }
        catch (OperationalPlanException e)
        {
            throw new RuntimeException("Initial operational plan could not be created.", e);
        }

        // register the GTU on the lanes
        for (LanePosition directedLanePosition : initialLongitudinalPositions)
        {
            Lane lane = directedLanePosition.getLane();
            addLaneToGtu(lane, directedLanePosition.getPosition()); // enter lane part 1
        }

        // init event
        LanePosition referencePosition = getReferencePosition();
        fireTimedEvent(
                LaneBasedGtu.LANEBASED_INIT_EVENT, new Object[] {getId(), initialLocation, getLength(), getWidth(),
                        referencePosition.getLane(), referencePosition.getPosition(), getGtuType()},
                getSimulator().getSimulatorTime());

        // register the GTU on the lanes
        for (LanePosition directedLanePosition : initialLongitudinalPositions)
        {
            Lane lane = directedLanePosition.getLane();
            lane.addGTU(this, directedLanePosition.getPosition()); // enter lane part 2
        }

        // initiate the actual move
        super.init(strategicalPlanner, initialLocation, initialSpeed);

        this.referencePositionTime = Double.NaN; // remove cache, it may be invalid as the above init results in a lane change

    }

    /**
     * {@inheritDoc} All lanes the GTU is on will be left.
     */
    @Override
    public void setParent(final Gtu gtu) throws GtuException
    {
        for (Lane lane : new LinkedHashSet<>(this.currentLanes)) // copy for concurrency problems
        {
            leaveLane(lane);
        }
        super.setParent(gtu);
    }

    /**
     * Reinitializes the GTU on the network using the existing strategical planner and zero speed.
     * @param initialLongitudinalPositions Set&lt;DirectedLanePosition&gt;; initial position
     * @throws NetworkException when the GTU cannot be placed on the given lane
     * @throws SimRuntimeException when the move method cannot be scheduled
     * @throws GtuException when initial values are not correct
     * @throws OtsGeometryException when the initial path is wrong
     */
    public void reinit(final Set<LanePosition> initialLongitudinalPositions)
            throws NetworkException, SimRuntimeException, GtuException, OtsGeometryException
    {
        init(getStrategicalPlanner(), initialLongitudinalPositions, Speed.ZERO);
    }

    /**
     * Hack method. TODO remove and solve better
     * @return safe to change
     * @throws GtuException on error
     */
    public final boolean isSafeToChange() throws GtuException
    {
        return this.fractionalLinkPositions.get(getReferencePosition().getLane().getParentLink()) > 0.0;
    }

    /**
     * insert GTU at a certain position. This can happen at setup (first initialization), and after a lane change of the GTU.
     * The relative position that will be registered is the referencePosition (dx, dy, dz) = (0, 0, 0). Front and rear positions
     * are relative towards this position.
     * @param lane Lane; the lane to add to the list of lanes on which the GTU is registered.
     * @param position Length; the position on the lane.
     * @throws GtuException when positioning the GTU on the lane causes a problem
     */
    @SuppressWarnings("checkstyle:designforextension")
    public void enterLane(final Lane lane, final Length position) throws GtuException
    {
        if (lane == null || position == null)
        {
            throw new GtuException("enterLane - one of the arguments is null");
        }
        addLaneToGtu(lane, position);
        addGtuToLane(lane, position);
    }

    /**
     * Registers the lane at the GTU. Only works at the start of a operational plan.
     * @param lane Lane; the lane to add to the list of lanes on which the GTU is registered.
     * @param position Length; the position on the lane.
     * @throws GtuException when positioning the GTU on the lane causes a problem
     */
    private void addLaneToGtu(final Lane lane, final Length position) throws GtuException
    {
        if (this.currentLanes.contains(lane))
        {
            System.err.println(this + " is already registered on lane: " + lane + " at fractional position "
                    + this.fractionalPosition(lane, RelativePosition.REFERENCE_POSITION) + " intended position is " + position
                    + " length of lane is " + lane.getLength());
            return;
        }
        // if the GTU is already registered on a lane of the same link, do not change its fractional position, as
        // this might lead to a "jump".
        if (!this.fractionalLinkPositions.containsKey(lane.getParentLink()))
        {
            this.fractionalLinkPositions.put(lane.getParentLink(), lane.fraction(position));
        }
        this.currentLanes.add(lane);
    }

    /**
     * Part of 'enterLane' which registers the GTU with the lane so the lane can report its GTUs.
     * @param lane Lane; lane
     * @param position Length; position
     * @throws GtuException on exception
     */
    protected void addGtuToLane(final Lane lane, final Length position) throws GtuException
    {
        List<SimEventInterface<Duration>> pending = this.pendingEnterTriggers.get(lane);
        if (null != pending)
        {
            for (SimEventInterface<Duration> event : pending)
            {
                if (event.getAbsoluteExecutionTime().ge(getSimulator().getSimulatorTime()))
                {
                    boolean result = getSimulator().cancelEvent(event);
                    if (!result && event.getAbsoluteExecutionTime().ne(getSimulator().getSimulatorTime()))
                    {
                        System.err.println("addLaneToGtu, trying to remove event: NOTHING REMOVED -- result=" + result
                                + ", simTime=" + getSimulator().getSimulatorTime() + ", eventTime="
                                + event.getAbsoluteExecutionTime());
                    }
                }
            }
            this.pendingEnterTriggers.remove(lane);
        }
        lane.addGTU(this, position);
    }

    /**
     * Unregister the GTU from a lane.
     * @param lane Lane; the lane to remove from the list of lanes on which the GTU is registered.
     * @throws GtuException when leaveLane should not be called
     */
    @SuppressWarnings("checkstyle:designforextension")
    public void leaveLane(final Lane lane) throws GtuException
    {
        leaveLane(lane, false);
    }

    /**
     * Leave a lane but do not complain about having no lanes left when beingDestroyed is true.
     * @param lane Lane; the lane to leave
     * @param beingDestroyed boolean; if true, no complaints about having no lanes left
     * @throws GtuException in case leaveLane should not be called
     */
    @SuppressWarnings("checkstyle:designforextension")
    public void leaveLane(final Lane lane, final boolean beingDestroyed) throws GtuException
    {
        Length position = position(lane, getReference());
        this.currentLanes.remove(lane);
        removePendingEvents(lane, this.pendingLeaveTriggers);
        removePendingEvents(lane, this.pendingEnterTriggers);
        // check if there are any lanes for this link left. If not, remove the link.
        boolean found = false;
        for (Lane l : this.currentLanes)
        {
            if (l.getParentLink().equals(lane.getParentLink()))
            {
                found = true;
            }
        }
        if (!found)
        {
            this.fractionalLinkPositions.remove(lane.getParentLink());
        }
        lane.removeGTU(this, !found, position);
        if (this.currentLanes.size() == 0 && !beingDestroyed)
        {
            System.err.println("leaveLane: lanes.size() = 0 for GTU " + getId());
        }
    }

    /**
     * Removes and cancels events for the given lane.
     * @param lane Lane; lane
     * @param triggers Map&lt;Lane, List&lt;SimEventInterface&lt;Duration&gt;&gt;&gt;; map to use
     */
    private void removePendingEvents(final Lane lane, final Map<Lane, List<SimEventInterface<Duration>>> triggers)
    {
        List<SimEventInterface<Duration>> pending = triggers.get(lane);
        if (null != pending)
        {
            for (SimEventInterface<Duration> event : pending)
            {
                if (event.getAbsoluteExecutionTime().ge(getSimulator().getSimulatorTime()))
                {
                    boolean result = getSimulator().cancelEvent(event);
                    if (!result && event.getAbsoluteExecutionTime().ne(getSimulator().getSimulatorTime()))
                    {
                        System.err.println("leaveLane, trying to remove event: NOTHING REMOVED -- result=" + result
                                + ", simTime=" + getSimulator().getSimulatorTime() + ", eventTime="
                                + event.getAbsoluteExecutionTime());
                    }
                }
            }
            triggers.remove(lane);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void changeLaneInstantaneously(final LateralDirectionality laneChangeDirection) throws GtuException
    {

        // from info
        LanePosition from = getReferencePosition();

        // keep a copy of the lanes and directions (!)
        Set<Lane> lanesToBeRemoved = new LinkedHashSet<>(this.currentLanes);

        // store the new positions
        // start with current link position, these will be overwritten, except if from a lane no adjacent lane is found, i.e.
        // changing over a continuous line when probably the reference point is past the line
        Map<Link, Double> newLinkPositionsLC = new LinkedHashMap<>(this.fractionalLinkPositions);

        // obtain position on lane adjacent to reference lane and enter lanes upstream/downstream from there
        Set<Lane> adjLanes = from.getLane().accessibleAdjacentLanesPhysical(laneChangeDirection, getGtuType());
        Lane adjLane = adjLanes.iterator().next();
        Length position = adjLane.position(from.getLane().fraction(from.getPosition()));
        Length planLength = Try.assign(() -> getOperationalPlan().getTraveledDistance(getSimulator().getSimulatorAbsTime()),
                "Exception while determining plan length.");
        enterLaneRecursive(adjLane, position, newLinkPositionsLC, planLength, lanesToBeRemoved, 0);

        // update the positions on the lanes we are registered on
        this.fractionalLinkPositions.clear();
        this.fractionalLinkPositions.putAll(newLinkPositionsLC);

        // leave the from lanes
        for (Lane lane : lanesToBeRemoved)
        {
            leaveLane(lane);
        }

        // stored positions no longer valid
        this.referencePositionTime = Double.NaN;
        this.cachedPositions.clear();

        // fire event
        this.fireTimedEvent(LaneBasedGtu.LANE_CHANGE_EVENT, new Object[] {getId(), laneChangeDirection, from},
                getSimulator().getSimulatorTime());

    }

    /**
     * Enters lanes upstream and downstream of the new location after an instantaneous lane change.
     * @param lane Lane considered lane
     * @param position Length; position to add GTU at
     * @param newLinkPositionsLC Map&lt;Link, Double&gt;; new link fractions to store
     * @param planLength Length; length of plan, to consider fractions at start
     * @param lanesToBeRemoved Set&lt;Lane&gt;; lanes to leave, from which lanes are removed when entered (such that they arent
     *            then left)
     * @param dir int; below 0 for upstream, above 0 for downstream, 0 for both <br>
     *            TODO: the below 0 and above 0 is NOT what is tested
     * @throws GtuException on exception
     */
    private void enterLaneRecursive(final Lane lane, final Length position, final Map<Link, Double> newLinkPositionsLC,
            final Length planLength, final Set<Lane> lanesToBeRemoved, final int dir) throws GtuException
    {
        enterLane(lane, position);
        lanesToBeRemoved.remove(lane);
        Length adjusted = position.minus(planLength);
        newLinkPositionsLC.put(lane.getParentLink(), adjusted.si / lane.getLength().si);

        // upstream
        if (dir < 1)
        {
            Length rear = position.plus(getRear().getDx());
            Length before = null;
            if (rear.si < 0.0)
            {
                before = rear.neg();
            }
            if (before != null)
            {
                ImmutableSet<Lane> upstream = new ImmutableLinkedHashSet<>(lane.prevLanes(getGtuType()));
                if (!upstream.isEmpty())
                {
                    Lane upLane = null;
                    for (Lane nextUp : upstream)
                    {
                        if (newLinkPositionsLC.containsKey(nextUp.getParentLink()))
                        {
                            // multiple upstream lanes could belong to the same link, we pick an arbitrary lane
                            // (a conflict should solve this)
                            upLane = nextUp;
                            break;
                        }
                    }
                    if (upLane == null)
                    {
                        // the rear is on an upstream section we weren't before the lane change, due to curvature, we pick an
                        // arbitrary lane (a conflict should solve this)
                        upLane = upstream.iterator().next();
                    }
                    if (!this.currentLanes.contains(upLane))
                    {
                        Lane next = upLane;
                        Length nextPos = next.getLength().minus(before).minus(getRear().getDx());
                        enterLaneRecursive(next, nextPos, newLinkPositionsLC, planLength, lanesToBeRemoved, -1);
                    }
                }
            }
        }

        // downstream
        if (dir > -1)
        {
            Length front = position.plus(getFront().getDx());
            Length passed = null;
            if (front.si > lane.getLength().si)
            {
                passed = front.minus(lane.getLength());
            }
            if (passed != null)
            {
                Lane next = getNextLaneForRoute(lane);
                if (!this.currentLanes.contains(next))
                {
                    Length nextPos = passed.minus(getFront().getDx());
                    enterLaneRecursive(next, nextPos, newLinkPositionsLC, planLength, lanesToBeRemoved, 1);
                }
            }
        }
    }

    /**
     * Register on lanes in target lane.
     * @param laneChangeDirection LateralDirectionality; direction of lane change
     * @throws GtuException exception
     */
    @Override
    @SuppressWarnings("checkstyle:designforextension")
    public void initLaneChange(final LateralDirectionality laneChangeDirection) throws GtuException
    {
        Set<Lane> lanesCopy = new LinkedHashSet<>(this.currentLanes);
        Map<Lane, Double> fractionalLanePositions = new LinkedHashMap<>();
        for (Lane lane : lanesCopy)
        {
            fractionalLanePositions.put(lane, fractionalPosition(lane, getReference()));
        }
        int numRegistered = 0;
        for (Lane lane : lanesCopy)
        {
            Set<Lane> laneSet = lane.accessibleAdjacentLanesLegal(laneChangeDirection, getGtuType());
            if (laneSet.size() > 0)
            {
                numRegistered++;
                Lane adjacentLane = laneSet.iterator().next();
                Length position = adjacentLane.getLength().times(fractionalLanePositions.get(lane));
                if (position.lt(lane.getLength().minus(getRear().getDx())))
                {
                    this.enteredLanes.add(adjacentLane);
                    enterLane(adjacentLane, position);
                }
                else
                {
                    System.out.println("Skipping enterLane for GTU " + getId() + " on lane " + lane.getFullId() + " at "
                            + position + ", lane length = " + lane.getLength() + " rear = " + getRear().getDx() + " front = "
                            + getFront().getDx());
                }
            }
        }
        Throw.when(numRegistered == 0, GtuException.class, "Gtu %s starting %s lane change, but no adjacent lane found.",
                getId(), laneChangeDirection);
    }

    /**
     * Performs the finalization of a lane change by leaving the from lanes.
     * @param laneChangeDirection LateralDirectionality; direction of lane change
     */
    @SuppressWarnings("checkstyle:designforextension")
    protected void finalizeLaneChange(final LateralDirectionality laneChangeDirection)
    {
        Set<Lane> lanesCopy = new LinkedHashSet<>(this.currentLanes);
        Set<Lane> lanesToBeRemoved = new LinkedHashSet<>();
        Lane fromLane = null;
        Length fromPosition = null;
        try
        {
            // find lanes to leave as they have an adjacent lane the GTU is also on in the lane change direction
            for (Lane lane : lanesCopy)
            {
                Iterator<Lane> iterator = lane.accessibleAdjacentLanesPhysical(laneChangeDirection, getGtuType()).iterator();
                if (iterator.hasNext() && lanesCopy.contains(iterator.next()))
                {
                    lanesToBeRemoved.add(lane);
                }
            }
            // some lanes registered to the GTU may be downstream of a split and have no adjacent lane, find longitudinally
            boolean added = true;
            while (added)
            {
                added = false;
                Set<Lane> lanesToAlsoBeRemoved = new LinkedHashSet<>();
                for (Lane lane : lanesToBeRemoved)
                {
                    for (Lane nextLane : lane.nextLanes(getGtuType()))
                    {
                        if (lanesCopy.contains(nextLane) && !lanesToBeRemoved.contains(nextLane))
                        {
                            added = true;
                            lanesToAlsoBeRemoved.add(nextLane);
                        }
                    }
                }
                lanesToBeRemoved.addAll(lanesToAlsoBeRemoved);
            }
            double nearest = Double.POSITIVE_INFINITY;
            for (Lane lane : lanesToBeRemoved)
            {
                Length pos = position(lane, RelativePosition.REFERENCE_POSITION);
                if (0.0 <= pos.si && pos.si <= lane.getLength().si)
                {
                    fromLane = lane;
                    fromPosition = pos;
                }
                else if (fromLane == null && pos.si > lane.getLength().si)
                {
                    // if the reference point is in between two lanes, this recognizes the lane upstream of the gap
                    double distance = pos.si - lane.getLength().si;
                    if (distance < nearest)
                    {
                        nearest = distance;
                        fromLane = lane;
                        fromPosition = pos;
                    }
                }
                leaveLane(lane);
            }
            this.referencePositionTime = Double.NaN;
            this.finalizeLaneChangeEvent = null;
        }
        catch (GtuException exception)
        {
            // should not happen, lane was obtained from GTU
            throw new RuntimeException("position on lane not possible", exception);
        }
        Throw.when(fromLane == null, RuntimeException.class, "No from lane for lane change event.");
        LanePosition from = new LanePosition(fromLane, fromPosition);
        this.fireTimedEvent(LaneBasedGtu.LANE_CHANGE_EVENT, new Object[] {getId(), laneChangeDirection, from},
                getSimulator().getSimulatorTime());
    }

    /** {@inheritDoc} */
    @Override
    public void setFinalizeLaneChangeEvent(final SimEventInterface<Duration> event)
    {
        this.finalizeLaneChangeEvent = event;
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("checkstyle:designforextension")
    protected boolean move(final DirectedPoint fromLocation)
            throws SimRuntimeException, GtuException, OperationalPlanException, NetworkException, ParameterException
    {
        // DirectedPoint currentPoint = getLocation(); // used for "jump" detection that is also commented out
        // Only carry out move() if we still have lane(s) to drive on.
        // Note: a (Sink) trigger can have 'destroyed' us between the previous evaluation step and this one.
        if (this.currentLanes.isEmpty())
        {
            destroy();
            return false; // Done; do not re-schedule execution of this move method.
        }

        // remove enter events
        // WS: why?
        // for (Lane lane : this.pendingEnterTriggers.keySet())
        // {
        // System.out.println("GTU " + getId() + " is canceling event on lane " + lane.getFullId());
        // List<SimEventInterface<Duration>> events = this.pendingEnterTriggers.get(lane);
        // for (SimEventInterface<Duration> event : events)
        // {
        // // also unregister from lane
        // this.currentLanes.remove(lane);
        // getSimulator().cancelEvent(event);
        // }
        // }
        // this.pendingEnterTriggers.clear();

        // get distance covered in previous plan, to aid a shift in link fraction (from which a plan moves onwards)
        Length covered;
        if (getOperationalPlan() instanceof LaneBasedOperationalPlan
                && ((LaneBasedOperationalPlan) getOperationalPlan()).isDeviative())
        {
            // traveled distance as difference between start and current position on reference lane
            // note that for a deviative plan the traveled distance along the path is not valuable here
            LaneBasedOperationalPlan plan = (LaneBasedOperationalPlan) getOperationalPlan();
            LanePosition ref = getReferencePosition();
            covered =
                    position(ref.getLane(), getReference()).minus(position(ref.getLane(), getReference(), plan.getStartTime()));
            // Note that distance is valid as the reference lane can not change (and location of previous plan is start location
            // of current plan). Only instantaneous lane changes can do that, which do not result in deviative plans.
        }
        else
        {
            covered = getOperationalPlan().getTraveledDistance(getSimulator().getSimulatorAbsTime());
        }

        // generate the next operational plan and carry it out
        // in case of an instantaneous lane change, fractionalLinkPositions will be accordingly adjusted to the new lane
        super.move(fromLocation);

        // update the positions on the lanes we are registered on
        // WS: this was previously done using fractions calculated before super.move() based on the GTU position, but an
        // instantaneous lane change while e.g. the nose is on the next lane which is curved, results in a different fraction on
        // the next link (the GTU doesn't stretch or shrink)
        Map<Link, Double> newLinkFractions = new LinkedHashMap<>(this.fractionalLinkPositions);
        Set<Link> done = new LinkedHashSet<>();
        // WS: this used to be on all current lanes, skipping links already processed, but 'covered' regards the reference lane
        updateLinkFraction(getReferencePosition().getLane(), newLinkFractions, done, false, covered, true);
        updateLinkFraction(getReferencePosition().getLane(), newLinkFractions, done, true, covered, true);
        this.fractionalLinkPositions.clear();
        this.fractionalLinkPositions.putAll(newLinkFractions);

        LanePosition dlp = getReferencePosition();
        fireTimedEvent(LaneBasedGtu.LANEBASED_MOVE_EVENT,
                new Object[] {getId(), new OtsPoint3D(fromLocation).doubleVector(PositionUnit.METER),
                        OtsPoint3D.direction(fromLocation, DirectionUnit.EAST_RADIAN), getSpeed(), getAcceleration(),
                        getTurnIndicatorStatus(), getOdometer(), dlp.getLane().getParentLink().getId(), dlp.getLane().getId(),
                        dlp.getPosition()},
                getSimulator().getSimulatorTime());

        if (getOperationalPlan().getAcceleration(Duration.ZERO).si < -10
                && getOperationalPlan().getSpeed(Duration.ZERO).si > 2.5)
        {
            System.err.println("GTU: " + getId() + " - getOperationalPlan().getAcceleration(Duration.ZERO).si < -10)");
            System.err.println("Lanes in current plan: " + this.currentLanes);
            if (getTacticalPlanner().getPerception().contains(DefaultSimplePerception.class))
            {
                DefaultSimplePerception p =
                        getTacticalPlanner().getPerception().getPerceptionCategory(DefaultSimplePerception.class);
                System.err.println("HeadwayGtu: " + p.getForwardHeadwayGtu());
                System.err.println("HeadwayObject: " + p.getForwardHeadwayObject());
            }
        }
        // DirectedPoint currentPointAfterMove = getLocation();
        // if (currentPoint.distance(currentPointAfterMove) > 0.1)
        // {
        // System.err.println(this.getId() + " jumped");
        // }
        // schedule triggers and determine when to enter lanes with front and leave lanes with rear
        scheduleEnterLeaveTriggers();
        return false;
    }

    /**
     * Recursive update of link fractions based on a moved distance.
     * @param lane Lane; current lane, start with reference lane
     * @param newLinkFractions Map&lt;Link, Double&gt;; map to put new fractions in
     * @param done Set&lt;Link&gt;; links to skip as link are already done
     * @param prevs boolean; whether to loop to the previous or next lanes, regardless of driving direction
     * @param covered Length; covered distance along the reference lane
     * @param isReferenceLane boolean; whether this lane is the reference lane (to skip in second call)
     */
    private void updateLinkFraction(final Lane lane, final Map<Link, Double> newLinkFractions, final Set<Link> done,
            final boolean prevs, final Length covered, final boolean isReferenceLane)
    {
        if (!prevs || !isReferenceLane)
        {
            if (done.contains(lane.getParentLink()) || !this.currentLanes.contains(lane))
            {
                return;
            }
            newLinkFractions.put(lane.getParentLink(),
                    this.fractionalLinkPositions.get(lane.getParentLink()) + covered.si / lane.getLength().si);
            done.add(lane.getParentLink());
        }
        for (Lane nextLane : (prevs ? lane.prevLanes(getGtuType()) : lane.nextLanes(getGtuType())))
        {
            updateLinkFraction(nextLane, newLinkFractions, done, prevs, covered, false);
        }
    }

    /** {@inheritDoc} */
    @Override
    public final Lane getNextLaneForRoute(final Lane lane)
    {
        Set<Lane> next = lane.nextLanes(getGtuType());
        if (next.isEmpty())
        {
            return null;
        }
        // ask strategical planner
        Set<Lane> set = getNextLanesForRoute(lane);
        if (set.size() == 1)
        {
            return set.iterator().next();
        }
        // check if the GTU is registered on any
        for (Lane l : set)
        {
            if (l.getGtuList().contains(this))
            {
                return l;
            }
        }
        // ask tactical planner
        return Try.assign(() -> getTacticalPlanner().chooseLaneAtSplit(lane, set),
                "Could not find suitable lane at split after lane %s of link %s for GTU %s.", lane.getId(),
                lane.getParentLink().getId(), getId());
    }

    /** {@inheritDoc} */
    @Override
    public Set<Lane> getNextLanesForRoute(final Lane lane)
    {
        Set<Lane> next = lane.nextLanes(getGtuType());
        if (next.isEmpty())
        {
            return null;
        }
        Link link;
        try
        {
            link = getStrategicalPlanner().nextLink(lane.getParentLink(), getGtuType());
        }
        catch (NetworkException exception)
        {
            throw new RuntimeException("Strategical planner experiences exception on network.", exception);
        }
        Set<Lane> out = new LinkedHashSet<>();
        for (Lane l : next)
        {
            if (l.getParentLink().equals(link))
            {
                out.add(l);
            }
        }
        return out;
    }

    /** {@inheritDoc} */
    @Override
    public final Map<Lane, Length> positions(final RelativePosition relativePosition) throws GtuException
    {
        return positions(relativePosition, getSimulator().getSimulatorAbsTime());
    }

    /** {@inheritDoc} */
    @Override
    public final Map<Lane, Length> positions(final RelativePosition relativePosition, final Time when) throws GtuException
    {
        Map<Lane, Length> positions = new LinkedHashMap<>();
        for (Lane lane : this.currentLanes)
        {
            positions.put(lane, position(lane, relativePosition, when));
        }
        return positions;
    }

    /** {@inheritDoc} */
    @Override
    public final Length position(final Lane lane, final RelativePosition relativePosition) throws GtuException
    {
        return position(lane, relativePosition, getSimulator().getSimulatorAbsTime());
    }

    /**
     * Return the longitudinal position that the indicated relative position of this GTU would have if it were to change to
     * another Lane with a / the current CrossSectionLink. This point may be before the begin or after the end of the link of
     * the projection lane of the GTU. This preserves the length of the GTU.
     * @param projectionLane Lane; the lane onto which the position of this GTU must be projected
     * @param relativePosition RelativePosition; the point on this GTU that must be projected
     * @param when Time; the time for which to project the position of this GTU
     * @return Length; the position of this GTU in the projectionLane
     * @throws GtuException when projectionLane it not in any of the CrossSectionLink that the GTU is on
     */
    @SuppressWarnings("checkstyle:designforextension")
    public Length translatedPosition(final Lane projectionLane, final RelativePosition relativePosition, final Time when)
            throws GtuException
    {
        CrossSectionLink link = projectionLane.getParentLink();
        for (CrossSectionElement cse : link.getCrossSectionElementList())
        {
            if (cse instanceof Lane)
            {
                Lane cseLane = (Lane) cse;
                if (this.currentLanes.contains(cseLane))
                {
                    double fractionalPosition = fractionalPosition(cseLane, RelativePosition.REFERENCE_POSITION, when);
                    Length pos = new Length(projectionLane.getLength().getSI() * fractionalPosition, LengthUnit.SI);
                    return pos.plus(relativePosition.getDx());
                }
            }
        }
        throw new GtuException(this + " is not on any lane of Link " + link);
    }

    /**
     * Return the longitudinal position on the projection lane that has the same fractional position on one of the current lanes
     * of the indicated relative position. This preserves the fractional positions of all relative positions of the GTU.
     * @param projectionLane Lane; the lane onto which the position of this GTU must be projected
     * @param relativePosition RelativePosition; the point on this GTU that must be projected
     * @param when Time; the time for which to project the position of this GTU
     * @return Length; the position of this GTU in the projectionLane
     * @throws GtuException when projectionLane it not in any of the CrossSectionLink that the GTU is on
     */
    @SuppressWarnings("checkstyle:designforextension")
    public Length projectedPosition(final Lane projectionLane, final RelativePosition relativePosition, final Time when)
            throws GtuException
    {
        CrossSectionLink link = projectionLane.getParentLink();
        for (CrossSectionElement cse : link.getCrossSectionElementList())
        {
            if (cse instanceof Lane)
            {
                Lane cseLane = (Lane) cse;
                if (this.currentLanes.contains(cseLane))
                {
                    double fractionalPosition = fractionalPosition(cseLane, relativePosition, when);
                    return new Length(projectionLane.getLength().getSI() * fractionalPosition, LengthUnit.SI);
                }
            }
        }
        throw new GtuException(this + " is not on any lane of Link " + link);
    }

    /** caching of time field for last stored position(s). */
    private double cachePositionsTime = Double.NaN;

    /** caching of last stored position(s). */
    private Map<Integer, Length> cachedPositions = new LinkedHashMap<>();

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("checkstyle:designforextension")
    public Length position(final Lane lane, final RelativePosition relativePosition, final Time when) throws GtuException
    {
        int cacheIndex = 0;
        if (CACHING)
        {
            cacheIndex = 17 * lane.hashCode() + relativePosition.hashCode();
            Length l;
            if (when.si == this.cachePositionsTime && (l = this.cachedPositions.get(cacheIndex)) != null)
            {
                // PK verify the result; uncomment if you don't trust the cache
                // this.cachedPositions.clear();
                // Length difficultWay = position(lane, relativePosition, when);
                // if (Math.abs(l.si - difficultWay.si) > 0.00001)
                // {
                // System.err.println("Whoops: cache returns bad value for GTU " + getId());
                // }
                CACHED_POSITION++;
                return l;
            }
            if (when.si != this.cachePositionsTime)
            {
                this.cachedPositions.clear();
                this.cachePositionsTime = when.si;
            }
        }
        NON_CACHED_POSITION++;

        synchronized (this.lock)
        {
            double loc = Double.NaN;
            try
            {
                OperationalPlan plan = getOperationalPlan(when);
                if (!(plan instanceof LaneBasedOperationalPlan) || !((LaneBasedOperationalPlan) plan).isDeviative())
                {
                    double longitudinalPosition;
                    try
                    {
                        longitudinalPosition =
                                lane.positionSI(this.fractionalLinkPositions.get(when).get(lane.getParentLink()));
                    }
                    catch (NullPointerException exception)
                    {
                        throw exception;
                    }
                    loc = longitudinalPosition + plan.getTraveledDistanceSI(when) + relativePosition.getDx().si;
                }
                else
                {
                    // deviative LaneBasedOperationalPlan, i.e. the GTU is not on a center line
                    DirectedPoint p = plan.getLocation(when, relativePosition);
                    double f = lane.getCenterLine().projectFractional(null, null, p.x, p.y, FractionalFallback.NaN);
                    if (!Double.isNaN(f))
                    {
                        loc = f * lane.getLength().si;
                    }
                    else
                    {
                        // the point does not project fractionally to this lane, it has to be up- or downstream of the lane

                        // simple heuristic to decide if we first look upstream or downstream
                        // TODO: should we still look upstream for a position?
                        boolean upstream = this.fractionalLinkPositions.get(lane.getParentLink()) < 0.0;

                        // use loop up to 2 times (for loop creates 'loc not initialized' warning)
                        int i = 0;
                        while (true)
                        {
                            Set<Lane> otherLanesToConsider = new LinkedHashSet<>();
                            otherLanesToConsider.addAll(this.currentLanes);
                            double distance = getDistanceAtOtherLane(lane, when, upstream, 0.0, p, otherLanesToConsider);
                            // distance can be positive on an upstream lane due to a loop
                            if (!Double.isNaN(distance))
                            {
                                if (i == 1 && !Double.isNaN(loc))
                                {
                                    // loc was determined in both loops, this constitutes a lane-loop, select nearest
                                    double loc2 = upstream ? -distance : distance + lane.getLength().si;
                                    double d1 = loc < 0.0 ? -loc : loc - lane.getLength().si;
                                    double d2 = loc2 < 0.0 ? -loc2 : loc2 - lane.getLength().si;
                                    loc = d1 < d2 ? loc : loc2;
                                    break;
                                }
                                else
                                {
                                    // loc was determined in second loop
                                    loc = upstream ? -distance : distance + lane.getLength().si;
                                }
                            }
                            else if (!Double.isNaN(loc))
                            {
                                // loc was determined in first loop
                                break;
                            }
                            else if (i == 1)
                            {
                                // loc was determined in neither loop
                                // Lane change ended while moving to next link. The source lanes are left and for a leave-lane
                                // event the position is required. This may depend on upstream or downstream lanes as the
                                // reference position is projected to that lane. But if we already left that lane, we can't use
                                // it. We thus use ENDPOINT fallback instead.
                                loc = lane.getLength().si * lane.getCenterLine().projectFractional(null, null, p.x, p.y,
                                        FractionalFallback.ENDPOINT);
                                break;
                            }
                            // try other direction
                            i++;
                            upstream = !upstream;
                        }
                    }
                }
            }
            catch (NullPointerException e)
            {
                throw new GtuException("lanesCurrentOperationalPlan or fractionalLinkPositions is null", e);
            }
            catch (Exception e)
            {
                System.err.println(toString());
                System.err.println(this.currentLanes.get(when));
                System.err.println(this.fractionalLinkPositions.get(when));
                throw new GtuException(e);
            }
            if (Double.isNaN(loc))
            {
                System.out.println("loc is NaN");
            }
            Length length = Length.instantiateSI(loc);
            if (CACHING)
            {
                this.cachedPositions.put(cacheIndex, length);
            }
            return length;
        }
    }

    /** Set of lane to attempt when determining the location with a deviative lane change. */
    // private Set<Lane> otherLanesToConsider;

    /**
     * In case of a deviative operational plan (not on the center lines), positions are projected fractionally to the center
     * lines. For points upstream or downstream of a lane, fractional projection is not valid. In such cases we need to project
     * the position to an upstream or downstream lane instead, and adjust length along the center lines.
     * @param lane Lane; lane to determine the position on
     * @param when Time; time
     * @param upstream boolean; whether to check upstream (or downstream)
     * @param distance double; cumulative distance in recursive search, starts at 0.0
     * @param point DirectedPoint; absolute point of GTU to be projected to center line
     * @param otherLanesToConsider Set&lt;Lane&gt;; lanes to consider
     * @return Length; position on lane being &lt;0 or &gt;{@code lane.getLength()}
     * @throws GtuException if GTU is not on the lane
     */
    private double getDistanceAtOtherLane(final Lane lane, final Time when, final boolean upstream, final double distance,
            final DirectedPoint point, final Set<Lane> otherLanesToConsider) throws GtuException
    {
        Set<Lane> nextLanes = new LinkedHashSet<>(lane.prevLanes(getGtuType())); // safe copy
        nextLanes.retainAll(otherLanesToConsider); // as we delete here
        if (!upstream && nextLanes.size() > 1)
        {
            Lane nextLane = getNextLaneForRoute(lane);
            if (nextLanes.contains(nextLane))
            {
                nextLanes.clear();
                nextLanes.add(nextLane);
            }
            else
            {
                getSimulator().getLogger().always().error("Distance on downstream lane could not be determined.");
            }
        }
        // TODO When requesting the position at the end of the plan, which will be on a further lane, this lane is not yet
        // part of the lanes in the current operational plan. This can be upstream or downstream depending on the direction of
        // travel. We might check whether getDirection(lane)=DIR_PLUS and upstream=false, or getDirection(lane)=DIR_MINUS and
        // upstream=true, to then use LaneDirection.getNextLaneDirection(this) to obtain the next lane. This is only required if
        // nextLanes originally had more than 1 lane, otherwise we can simply use that one lane. Problem is that the search
        // might go on far or even eternally (on a circular network), as projection simply keeps failing because the GTU is
        // actually towards the other longitudinal direction. Hence, the heuristic used before this method is called should
        // change and first always search against the direction of travel, and only consider lanes in currentLanes, while the
        // consecutive search in the direction of travel should then always find a point. We could build in a counter to prevent
        // a hanging software.
        if (nextLanes.size() == 0)
        {
            return Double.NaN; // point must be in the other direction
        }
        Throw.when(nextLanes.size() > 1, IllegalStateException.class,
                "A position (%s) of GTU %s is not on any of the current registered lanes.", point, this.getId());
        Lane nextLane = nextLanes.iterator().next();
        otherLanesToConsider.remove(lane);
        double f = nextLane.getCenterLine().projectFractional(null, null, point.x, point.y, FractionalFallback.NaN);
        if (Double.isNaN(f))
        {
            return getDistanceAtOtherLane(nextLane, when, upstream, distance + nextLane.getLength().si, point,
                    otherLanesToConsider);
        }
        return distance + (upstream ? 1.0 - f : f) * nextLane.getLength().si;
    }

    /** Time of reference position cache. */
    private double referencePositionTime = Double.NaN;

    /** Cached reference position. */
    private LanePosition cachedReferencePosition = null;

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("checkstyle:designforextension")
    public LanePosition getReferencePosition() throws GtuException
    {
        if (this.referencePositionTime == getSimulator().getSimulatorAbsTime().si)
        {
            return this.cachedReferencePosition;
        }
        boolean anyOnLink = false;
        Lane refLane = null;
        double closest = Double.POSITIVE_INFINITY;
        double minEps = Double.POSITIVE_INFINITY;
        for (Lane lane : this.currentLanes)
        {
            double fraction = fractionalPosition(lane, getReference());
            if (fraction >= 0.0 && fraction <= 1.0)
            {
                // TODO widest lane in case we are registered on more than one lane with the reference point?
                // TODO lane that leads to our location or not if we are registered on parallel lanes?
                if (!anyOnLink)
                {
                    refLane = lane;
                }
                else
                {
                    DirectedPoint loc = getLocation();
                    double f = lane.getCenterLine().projectFractional(null, null, loc.x, loc.y, FractionalFallback.ENDPOINT);
                    double distance = loc.distance(lane.getCenterLine().getLocationFractionExtended(f));
                    if (refLane != null && Double.isInfinite(closest))
                    {
                        f = refLane.getCenterLine().projectFractional(null, null, loc.x, loc.y, FractionalFallback.ENDPOINT);
                        closest = loc.distance(refLane.getCenterLine().getLocationFractionExtended(f));
                    }
                    if (distance < closest)
                    {
                        refLane = lane;
                        closest = distance;
                    }
                }
                anyOnLink = true;
            }
            else if (!anyOnLink && Double.isInfinite(closest))// && getOperationalPlan() instanceof LaneBasedOperationalPlan
            // && ((LaneBasedOperationalPlan) getOperationalPlan()).isDeviative())
            {
                double eps = (fraction > 1.0 ? lane.getCenterLine().getLast() : lane.getCenterLine().getFirst())
                        .distanceSI(new OtsPoint3D(getLocation()));
                if (eps < minEps)
                {
                    minEps = eps;
                    refLane = lane;
                }
            }
        }
        if (refLane != null)
        {
            this.cachedReferencePosition = new LanePosition(refLane, position(refLane, getReference()));
            this.referencePositionTime = getSimulator().getSimulatorAbsTime().si;
            return this.cachedReferencePosition;
        }
        // for (Lane lane : this.currentLanes)
        // {
        // Length relativePosition = position(lane, RelativePosition.REFERENCE_POSITION);
        // System.err
        // .println(String.format("Lane %s of Link %s: absolute position %s, relative position %5.1f%%", lane.getId(),
        // lane.getParentLink().getId(), relativePosition, relativePosition.si * 100 / lane.getLength().si));
        // }
        throw new GtuException("The reference point of GTU " + this + " is not on any of the lanes on which it is registered");
    }

    /**
     * Schedule the triggers for this GTU that are going to happen until the next evaluation time. Also schedule the
     * registration and deregistration of lanes when the vehicle enters or leaves them, at the exact right time. <br>
     * Note: when the GTU makes a lane change, the vehicle will be registered for both lanes during the entire maneuver.
     * @throws NetworkException on network inconsistency
     * @throws SimRuntimeException should never happen
     * @throws GtuException when a branch is reached where the GTU does not know where to go next
     */
    @SuppressWarnings("checkstyle:designforextension")
    protected void scheduleEnterLeaveTriggers() throws NetworkException, SimRuntimeException, GtuException
    {

        LaneBasedOperationalPlan plan = null;
        double moveSI;
        if (getOperationalPlan() instanceof LaneBasedOperationalPlan)
        {
            plan = (LaneBasedOperationalPlan) getOperationalPlan();
            moveSI = plan.getTotalLengthAlongLane(this).si;
        }
        else
        {
            moveSI = getOperationalPlan().getTotalLength().si;
        }

        // Figure out which lanes this GTU will enter with its FRONT, and schedule the lane enter events
        Set<Lane> lanesCopy = new LinkedHashSet<>(this.currentLanes);
        Iterator<Lane> it = lanesCopy.iterator();
        Lane enteredLane = null;
        // LateralDirectionality forceSide = LateralDirectionality.NONE;
        while (it.hasNext() || enteredLane != null) // use a copy because this.currentLanes can change
        {
            // next lane from 'lanesCopy', or asses the lane we just entered as it may be very short and also passed fully
            Lane lane = (enteredLane == null) ? lane = it.next() : enteredLane;
            enteredLane = null;

            // skip if already on next lane
            if (!Collections.disjoint(this.currentLanes, lane.nextLanes(getGtuType())))
            {
                continue;
            }

            // schedule triggers on this lane
            double referenceStartSI = this.fractionalLinkPositions.get(lane.getParentLink()) * lane.getLength().getSI();
            // referenceStartSI is position of reference of GTU on current lane
            lane.scheduleSensorTriggers(this, referenceStartSI, moveSI);

            double nextFrontPosSI = referenceStartSI + moveSI + getFront().getDx().si;
            Lane nextLane = null;
            Length refPosAtLastTimestep = null;
            DirectedPoint end = null;
            if (nextFrontPosSI > lane.getLength().si)
            {
                Lane next = getNextLaneForRoute(lane);
                if (null == next)
                {
                    // A sink should delete the GTU, or a lane change should end, before reaching the end of the lane
                    continue;
                }
                nextLane = next;
                double endPos = lane.getLength().si - getFront().getDx().si;
                Lane endLane = lane;
                // TODO: should this indeed be < 0.0?
                while (endPos < 0.0)
                {
                    // GTU front is more than lane length, so end location can not be extracted from the lane, let's move then
                    Set<Lane> set = new LinkedHashSet<>(endLane.prevLanes(getGtuType()));
                    set.retainAll(this.currentLanes);
                    double remain = -endPos;
                    endLane = set.iterator().next();
                    endPos = endLane.getLength().si - remain;
                }
                end = endLane.getCenterLine().getLocationExtendedSI(endPos);
                refPosAtLastTimestep = Length.instantiateSI(referenceStartSI - lane.getLength().si);
            }

            if (end != null)
            {
                Time enterTime = getOperationalPlan().timeAtPoint(end, false);
                if (enterTime != null)
                {
                    if (Double.isNaN(enterTime.si))
                    {
                        // TODO: this escape was in timeAtPoint, where it was changed to return null for leave lane events
                        enterTime = Time.instantiateSI(getOperationalPlan().getEndTime().si - 1e-9);
                        // -1e-9 prevents that next move() reschedules enter
                    }
                    addLaneToGtu(nextLane, refPosAtLastTimestep);
                    enteredLane = nextLane;
                    Length coveredDistance;
                    if (plan == null || !plan.isDeviative())
                    {
                        try
                        {
                            coveredDistance = getOperationalPlan().getTraveledDistance(enterTime);
                        }
                        catch (OperationalPlanException exception)
                        {
                            throw new RuntimeException("Enter time of lane beyond plan.", exception);
                        }
                    }
                    else
                    {
                        coveredDistance = plan.getDistanceAlongLane(this, end);
                    }
                    SimEventInterface<Duration> event = getSimulator().scheduleEventAbsTime(enterTime, this, this,
                            "addGtuToLane", new Object[] {nextLane, refPosAtLastTimestep.plus(coveredDistance)});
                    addEnterTrigger(nextLane, event);
                }
            }
        }

        // Figure out which lanes this GTU will exit with its BACK, and schedule the lane exit events
        for (Lane lane : this.currentLanes)
        {

            double referenceStartSI = this.fractionalLinkPositions.get(lane.getParentLink()) * lane.getLength().getSI();
            Time exitTime = null;

            if (plan == null || !plan.isDeviative())
            {
                double nextRearPosSI = referenceStartSI + getRear().getDx().si + moveSI;
                if (nextRearPosSI > lane.getLength().si)
                {
                    exitTime =
                            getOperationalPlan().timeAtDistance(Length.instantiateSI((lane.getLength().si - referenceStartSI)));
                }
            }
            else
            {
                DirectedPoint end = null;
                double endPos = lane.getLength().si - getRear().getDx().si;
                Lane endLane = lane;
                while (endPos > endLane.getLength().si)
                {
                    Set<Lane> set = new LinkedHashSet<>(endLane.nextLanes(getGtuType()));
                    set.retainAll(this.currentLanes);
                    if (!set.isEmpty())
                    {
                        double remain = endPos - endLane.getLength().si;
                        endLane = set.iterator().next();
                        endPos = remain;
                    }
                    else
                    {
                        endPos = endLane.getLength().si - getRear().getDx().si;
                        break;
                    }
                }
                end = endLane.getCenterLine().getLocationExtendedSI(endPos);
                if (end != null)
                {
                    exitTime = getOperationalPlan().timeAtPoint(end, false);
                    if (Double.isNaN(exitTime.si))
                    {
                        // code below will leave entered lanes if exitTime is null, make this so if NaN results due to the lane
                        // end being beyond the plan (rather than the GTU never having been there, but being registered there
                        // upon lane change initiation)
                        double nextRearPosSI = referenceStartSI + getRear().getDx().si + moveSI;
                        if (nextRearPosSI < lane.getLength().si)
                        {
                            exitTime = null;
                        }
                    }
                }
            }

            if (exitTime != null && !Double.isNaN(exitTime.si))
            {
                SimEvent<Duration> event = new SimEvent<>(new Duration(exitTime.minus(getSimulator().getStartTimeAbs())), this,
                        this, "leaveLane", new Object[] {lane, Boolean.valueOf(false)});
                getSimulator().scheduleEvent(event);
                addTrigger(lane, event);
            }
            else if (exitTime != null && this.enteredLanes.contains(lane))
            {
                // This lane was entered when initiating the lane change due to a fractional calculation. Now, the deviative
                // plan indicates we will never reach this location.
                SimEvent<Duration> event = new SimEvent<>(getSimulator().getSimulatorTime(), this, this, "leaveLane",
                        new Object[] {lane, Boolean.valueOf(false)});
                getSimulator().scheduleEvent(event);
                addTrigger(lane, event);
            }
        }

        this.enteredLanes.clear();
    }

    /** {@inheritDoc} */
    @Override
    public final Map<Lane, Double> fractionalPositions(final RelativePosition relativePosition) throws GtuException
    {
        return fractionalPositions(relativePosition, getSimulator().getSimulatorAbsTime());
    }

    /** {@inheritDoc} */
    @Override
    public final Map<Lane, Double> fractionalPositions(final RelativePosition relativePosition, final Time when)
            throws GtuException
    {
        Map<Lane, Double> positions = new LinkedHashMap<>();
        for (Lane lane : this.currentLanes)
        {
            positions.put(lane, fractionalPosition(lane, relativePosition, when));
        }
        return positions;
    }

    /** {@inheritDoc} */
    @Override
    public final double fractionalPosition(final Lane lane, final RelativePosition relativePosition, final Time when)
            throws GtuException
    {
        return position(lane, relativePosition, when).getSI() / lane.getLength().getSI();
    }

    /** {@inheritDoc} */
    @Override
    public final double fractionalPosition(final Lane lane, final RelativePosition relativePosition) throws GtuException
    {
        return position(lane, relativePosition).getSI() / lane.getLength().getSI();
    }

    /** {@inheritDoc} */
    @Override
    public final void addTrigger(final Lane lane, final SimEventInterface<Duration> event)
    {
        List<SimEventInterface<Duration>> list = this.pendingLeaveTriggers.get(lane);
        if (null == list)
        {
            list = new ArrayList<>();
        }
        list.add(event);
        this.pendingLeaveTriggers.put(lane, list);
    }

    /**
     * Add enter trigger.
     * @param lane Lane; lane
     * @param event SimEventInterface&lt;Duration&gt;; event
     */
    private void addEnterTrigger(final Lane lane, final SimEventInterface<Duration> event)
    {
        List<SimEventInterface<Duration>> list = this.pendingEnterTriggers.get(lane);
        if (null == list)
        {
            list = new ArrayList<>();
        }
        list.add(event);
        this.pendingEnterTriggers.put(lane, list);
    }

    /**
     * Sets a vehicle model.
     * @param vehicleModel VehicleModel; vehicle model
     */
    public void setVehicleModel(final VehicleModel vehicleModel)
    {
        this.vehicleModel = vehicleModel;
    }

    /** {@inheritDoc} */
    @Override
    public VehicleModel getVehicleModel()
    {
        return this.vehicleModel;
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("checkstyle:designforextension")
    public void destroy()
    {
        LanePosition dlp = null;
        try
        {
            dlp = getReferencePosition();
        }
        catch (GtuException e)
        {
            // ignore. not important at destroy
        }
        DirectedPoint location = this.getOperationalPlan() == null ? new DirectedPoint(0.0, 0.0, 0.0) : getLocation();

        synchronized (this.lock)
        {
            Set<Lane> laneSet = new LinkedHashSet<>(this.currentLanes); // Operate on a copy of the key
                                                                        // set
            for (Lane lane : laneSet)
            {
                try
                {
                    leaveLane(lane, true);
                }
                catch (GtuException e)
                {
                    // ignore. not important at destroy
                }
            }
        }

        if (dlp != null && dlp.getLane() != null)
        {
            Lane referenceLane = dlp.getLane();
            fireTimedEvent(LaneBasedGtu.LANEBASED_DESTROY_EVENT,
                    new Object[] {getId(), location, getOdometer(), referenceLane, dlp.getPosition()},
                    getSimulator().getSimulatorTime());
        }
        else
        {
            fireTimedEvent(LaneBasedGtu.LANEBASED_DESTROY_EVENT,
                    new Object[] {getId(), location, getOdometer(), null, Length.ZERO, null},
                    getSimulator().getSimulatorTime());
        }
        if (this.finalizeLaneChangeEvent != null)
        {
            getSimulator().cancelEvent(this.finalizeLaneChangeEvent);
        }

        super.destroy();
    }

    /** {@inheritDoc} */
    @Override
    public final Bounds getBounds()
    {
        double dx = 0.5 * getLength().doubleValue();
        double dy = 0.5 * getWidth().doubleValue();
        return new Bounds(new Point3d(-dx, -dy, 0.0), new Point3d(dx, dy, 0.0));
    }

    /** {@inheritDoc} */
    @Override
    public final LaneBasedStrategicalPlanner getStrategicalPlanner()
    {
        return (LaneBasedStrategicalPlanner) super.getStrategicalPlanner();
    }

    /** {@inheritDoc} */
    @Override
    public final LaneBasedStrategicalPlanner getStrategicalPlanner(final Time time)
    {
        return (LaneBasedStrategicalPlanner) super.getStrategicalPlanner(time);
    }

    /** {@inheritDoc} */
    @Override
    public RoadNetwork getNetwork()
    {
        return (RoadNetwork) super.getPerceivableContext();
    }

    /** {@inheritDoc} */
    @Override
    public Speed getDesiredSpeed()
    {
        Time simTime = getSimulator().getSimulatorAbsTime();
        if (this.desiredSpeedTime == null || this.desiredSpeedTime.si < simTime.si)
        {
            InfrastructurePerception infra =
                    getTacticalPlanner().getPerception().getPerceptionCategoryOrNull(InfrastructurePerception.class);
            SpeedLimitInfo speedInfo;
            if (infra == null)
            {
                speedInfo = new SpeedLimitInfo();
                speedInfo.addSpeedInfo(SpeedLimitTypes.MAX_VEHICLE_SPEED, getMaximumSpeed());
            }
            else
            {
                // Throw.whenNull(infra, "InfrastructurePerception is required to determine the desired speed.");
                speedInfo = infra.getSpeedLimitProspect(RelativeLane.CURRENT).getSpeedLimitInfo(Length.ZERO);
            }
            this.cachedDesiredSpeed =
                    Try.assign(() -> getTacticalPlanner().getCarFollowingModel().desiredSpeed(getParameters(), speedInfo),
                            "Parameter exception while obtaining the desired speed.");
            this.desiredSpeedTime = simTime;
        }
        return this.cachedDesiredSpeed;
    }

    /** {@inheritDoc} */
    @Override
    public Acceleration getCarFollowingAcceleration()
    {
        Time simTime = getSimulator().getSimulatorAbsTime();
        if (this.carFollowingAccelerationTime == null || this.carFollowingAccelerationTime.si < simTime.si)
        {
            LanePerception perception = getTacticalPlanner().getPerception();
            // speed
            EgoPerception<?, ?> ego = perception.getPerceptionCategoryOrNull(EgoPerception.class);
            Throw.whenNull(ego, "EgoPerception is required to determine the speed.");
            Speed speed = ego.getSpeed();
            // speed limit info
            InfrastructurePerception infra = perception.getPerceptionCategoryOrNull(InfrastructurePerception.class);
            Throw.whenNull(infra, "InfrastructurePerception is required to determine the desired speed.");
            SpeedLimitInfo speedInfo = infra.getSpeedLimitProspect(RelativeLane.CURRENT).getSpeedLimitInfo(Length.ZERO);
            // leaders
            NeighborsPerception neighbors = perception.getPerceptionCategoryOrNull(NeighborsPerception.class);
            Throw.whenNull(neighbors, "NeighborsPerception is required to determine the car-following acceleration.");
            PerceptionCollectable<HeadwayGtu, LaneBasedGtu> leaders = neighbors.getLeaders(RelativeLane.CURRENT);
            // obtain
            this.cachedCarFollowingAcceleration =
                    Try.assign(() -> getTacticalPlanner().getCarFollowingModel().followingAcceleration(getParameters(), speed,
                            speedInfo, leaders), "Parameter exception while obtaining the desired speed.");
            this.carFollowingAccelerationTime = simTime;
        }
        return this.cachedCarFollowingAcceleration;
    }

    /** {@inheritDoc} */
    @Override
    public final TurnIndicatorStatus getTurnIndicatorStatus()
    {
        return this.turnIndicatorStatus.get();
    }

    /** {@inheritDoc} */
    @Override
    public final TurnIndicatorStatus getTurnIndicatorStatus(final Time time)
    {
        return this.turnIndicatorStatus.get(time);
    }

    /** {@inheritDoc} */
    @Override
    public final void setTurnIndicatorStatus(final TurnIndicatorStatus turnIndicatorStatus)
    {
        this.turnIndicatorStatus.set(turnIndicatorStatus);
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("checkstyle:designforextension")
    public String toString()
    {
        return String.format("GTU " + getId());
    }

}
