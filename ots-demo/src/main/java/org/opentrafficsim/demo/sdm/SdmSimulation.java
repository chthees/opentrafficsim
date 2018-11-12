package org.opentrafficsim.demo.sdm;

import java.awt.Color;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

import org.djunits.unit.FrequencyUnit;
import org.djunits.unit.SpeedUnit;
import org.djunits.unit.TimeUnit;
import org.djunits.value.StorageType;
import org.djunits.value.ValueException;
import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.djunits.value.vdouble.scalar.Time;
import org.djunits.value.vdouble.vector.FrequencyVector;
import org.djunits.value.vdouble.vector.TimeVector;
import org.djunits.value.vfloat.scalar.FloatDuration;
import org.opentrafficsim.base.compressedfiles.CompressionType;
import org.opentrafficsim.base.compressedfiles.Writer;
import org.opentrafficsim.core.geometry.OTSPoint3D;
import org.opentrafficsim.core.graphs.ContourDataSource;
import org.opentrafficsim.core.graphs.ContourPlotSpeed;
import org.opentrafficsim.core.graphs.GraphPath;
import org.opentrafficsim.core.gtu.GTUDirectionality;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.gtu.animation.AccelerationGTUColorer;
import org.opentrafficsim.core.gtu.animation.SpeedGTUColorer;
import org.opentrafficsim.core.gtu.animation.SwitchableGTUColorer;
import org.opentrafficsim.core.network.LinkType;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.OTSNetwork;
import org.opentrafficsim.core.network.OTSNode;
import org.opentrafficsim.core.perception.HistoryManagerDEVS;
import org.opentrafficsim.graphs.GraphLaneUtil;
import org.opentrafficsim.kpi.sampling.KpiGtuDirectionality;
import org.opentrafficsim.kpi.sampling.KpiLaneDirection;
import org.opentrafficsim.kpi.sampling.SamplingException;
import org.opentrafficsim.kpi.sampling.SpaceTimeRegion;
import org.opentrafficsim.kpi.sampling.Trajectory;
import org.opentrafficsim.kpi.sampling.TrajectoryGroup;
import org.opentrafficsim.road.animation.AbstractSimulationScript;
import org.opentrafficsim.road.gtu.animation.DesiredHeadwayColorer;
import org.opentrafficsim.road.gtu.animation.DistractionColorer;
import org.opentrafficsim.road.gtu.animation.FixedColor;
import org.opentrafficsim.road.gtu.animation.TaskSaturationColorer;
import org.opentrafficsim.road.gtu.generator.od.DefaultGTUCharacteristicsGeneratorOD;
import org.opentrafficsim.road.gtu.generator.od.ODApplier;
import org.opentrafficsim.road.gtu.generator.od.ODOptions;
import org.opentrafficsim.road.gtu.lane.LaneBasedGTU;
import org.opentrafficsim.road.gtu.lane.perception.mental.AdaptationSituationalAwareness;
import org.opentrafficsim.road.gtu.lane.perception.mental.Fuller.Task;
import org.opentrafficsim.road.gtu.lane.perception.mental.sdm.DefaultDistraction;
import org.opentrafficsim.road.gtu.lane.perception.mental.sdm.DistractionFactory;
import org.opentrafficsim.road.gtu.lane.perception.mental.sdm.StochasticDistractionModel;
import org.opentrafficsim.road.gtu.lane.perception.mental.sdm.TaskSupplier;
import org.opentrafficsim.road.gtu.strategical.od.Categorization;
import org.opentrafficsim.road.gtu.strategical.od.Category;
import org.opentrafficsim.road.gtu.strategical.od.Interpolation;
import org.opentrafficsim.road.gtu.strategical.od.ODMatrix;
import org.opentrafficsim.road.network.factory.LaneFactory;
import org.opentrafficsim.road.network.lane.CrossSectionLink;
import org.opentrafficsim.road.network.lane.Lane;
import org.opentrafficsim.road.network.lane.LaneDirection;
import org.opentrafficsim.road.network.lane.LaneType;
import org.opentrafficsim.road.network.lane.Stripe.Permeable;
import org.opentrafficsim.road.network.lane.changing.LaneKeepingPolicy;
import org.opentrafficsim.road.network.lane.object.sensor.SinkSensor;
import org.opentrafficsim.road.network.sampling.LaneData;
import org.opentrafficsim.road.network.sampling.RoadSampler;
import org.opentrafficsim.road.network.sampling.data.TimeToCollision;
import org.opentrafficsim.simulationengine.AbstractWrappableAnimation;
import org.opentrafficsim.simulationengine.OTSSimulatorInterface;

import nl.tudelft.simulation.dsol.gui.swing.TablePanel;
import nl.tudelft.simulation.jstats.streams.StreamInterface;

/**
 * Script to run a simulation with the Stochastic Distraction Model.
 * <p>
 * Copyright (c) 2013-2018 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 5 nov. 2018 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class SdmSimulation extends AbstractSimulationScript
{

    /** Network. */
    private OTSNetwork network;

    /** Sampler for statistics. */
    private RoadSampler sampler;

    /** Time to collision data type. */
    private final TimeToCollision ttc = new TimeToCollision();

    /** Space time regions to sample traffic on. */
    private final List<SpaceTimeRegion> regions = new ArrayList<>();

    /**
     * Constructor.
     * @param properties String[]; command line arguments
     */
    protected SdmSimulation(final String[] properties)
    {
        super("SDM simulation", "Simulations using the Stochastic Distraction Model", properties);
        // set GTU colorers to use
        setGtuColorer(SwitchableGTUColorer.builder().addActiveColorer(new FixedColor(Color.BLUE, "Blue"))
                .addColorer(new DistractionColorer(DefaultDistraction.ANSWERING_CELL_PHONE, DefaultDistraction.CONVERSING,
                        DefaultDistraction.MANIPULATING_AUDIO_CONTROLS, DefaultDistraction.EXTERNAL_DISTRACTION))
                .addColorer(new SpeedGTUColorer(new Speed(150, SpeedUnit.KM_PER_HOUR)))
                .addColorer(new AccelerationGTUColorer(Acceleration.createSI(-6.0), Acceleration.createSI(2)))
                .addColorer(new DesiredHeadwayColorer(Duration.createSI(0.56), Duration.createSI(2.4)))
                .addColorer(new TaskSaturationColorer()).build());
    }

    /**
     * Start a simulation.
     * @param args String...; command line arguments
     */
    public static void main(final String... args)
    {
        new SdmSimulation(args).start();
    }

    /** {@inheritDoc} */
    @Override
    protected void setDefaultProperties()
    {
        setProperty("allowMultiTasking", "false");
        // 1=TALKING_CELL_PHONE,12=CONVERSING,5=MANIPULATING_AUDIO_CONTROLS,16=EXTERNAL_DISTRACTION
        setProperty("distractions", "1,12,5,16");
        setProperty("phoneInit", 1.0);
        setProperty("phoneFinal", 0.3);
        setProperty("phoneTau", 10.0);
        setProperty("conversing", 0.3);
        setProperty("audio", 0.3);
        setProperty("externalBase", 0.2);
        setProperty("externalVar", 0.3);
        setProperty("truckFraction", 0.05);
        setProperty("outputFile", "output.txt");
        setProperty("output", true);
        setProperty("plots", true);
    }

    /** {@inheritDoc} */
    @Override
    protected OTSNetwork setupSimulation(final OTSSimulatorInterface sim) throws Exception
    {
        // manager of historic information to allow a reaction time
        sim.getReplication().setHistoryManager(
                new HistoryManagerDEVS(sim, AdaptationSituationalAwareness.TR_MAX.getDefaultValue(), Duration.createSI(10.0)));

        // Network
        this.network = new OTSNetwork("SDM");
        OTSPoint3D pointA = new OTSPoint3D(0.0, 0.0);
        OTSPoint3D pointB = new OTSPoint3D(0.0, -20.0);
        OTSPoint3D pointC = new OTSPoint3D(1600.0, -20.0);
        OTSPoint3D pointD = new OTSPoint3D(2000.0, 0.0);
        OTSPoint3D pointE = new OTSPoint3D(2500.0, 0.0);
        OTSPoint3D pointF = new OTSPoint3D(3500.0, 0.0);
        OTSNode nodeA = new OTSNode(this.network, "A", pointA);
        OTSNode nodeB = new OTSNode(this.network, "B", pointB);
        OTSNode nodeC = new OTSNode(this.network, "C", pointC);
        OTSNode nodeD = new OTSNode(this.network, "D", pointD);
        OTSNode nodeE = new OTSNode(this.network, "E", pointE);
        OTSNode nodeF = new OTSNode(this.network, "F", pointF);
        LinkType type = LinkType.FREEWAY;
        LaneKeepingPolicy policy = LaneKeepingPolicy.KEEP_RIGHT;
        Length laneWidth = Length.createSI(3.5);
        LaneType laneType = LaneType.FREEWAY;
        Speed speedLimit = new Speed(120.0, SpeedUnit.KM_PER_HOUR);
        List<Lane> allLanes = new ArrayList<>();
        allLanes.addAll(new LaneFactory(this.network, nodeA, nodeD, type, sim, policy)
                .leftToRight(2.0, laneWidth, laneType, speedLimit).addLanes(Permeable.BOTH).getLanes());
        allLanes.addAll(new LaneFactory(this.network, nodeB, nodeC, type, sim, policy)
                .leftToRight(0.0, laneWidth, laneType, speedLimit).addLanes(Permeable.BOTH).getLanes());
        allLanes.addAll(new LaneFactory(this.network, nodeC, nodeD, type, sim, policy)
                .leftToRight(0.0, laneWidth, laneType, speedLimit).addLanes(Permeable.BOTH).getLanes());
        allLanes.addAll(
                new LaneFactory(this.network, nodeD, nodeE, type, sim, policy).leftToRight(2.0, laneWidth, laneType, speedLimit)
                        .addLanes(Permeable.BOTH, Permeable.BOTH, Permeable.BOTH).getLanes());
        List<Lane> lanesEF = new LaneFactory(this.network, nodeE, nodeF, type, sim, policy)
                .leftToRight(1.0, laneWidth, laneType, speedLimit).addLanes(Permeable.BOTH, Permeable.BOTH).getLanes();
        allLanes.addAll(lanesEF);
        for (Lane lane : lanesEF)
        {
            new SinkSensor(lane, lane.getLength().minus(Length.createSI(50)), sim);
        }

        // OD
        List<OTSNode> origins = new ArrayList<>();
        origins.add(nodeA);
        origins.add(nodeB);
        List<OTSNode> destinations = new ArrayList<>();
        destinations.add(nodeF);
        TimeVector timeVector = new TimeVector(new double[] { 0.0, 0.05, 0.55, 1.05 }, TimeUnit.BASE_HOUR, StorageType.DENSE);
        Interpolation interpolation = Interpolation.LINEAR;
        Categorization categorization = new Categorization("GTU categorization", GTUType.class);
        ODMatrix odMatrix = new ODMatrix("OD", origins, destinations, categorization, timeVector, interpolation);
        Category carCategory = new Category(categorization, GTUType.CAR);
        Category truCategory = new Category(categorization, GTUType.TRUCK);
        double f1 = getDoubleProperty("truckFraction");
        double f2 = 1.0 - f1;
        odMatrix.putDemandVector(nodeA, nodeF, carCategory, freq(new double[] { f2 * 1500.0, f2 * 1500.0, f2 * 3400.0, 0.0 }));
        odMatrix.putDemandVector(nodeA, nodeF, truCategory, freq(new double[] { f1 * 1500.0, f1 * 1500.0, f1 * 3400.0, 0.0 }));
        odMatrix.putDemandVector(nodeB, nodeF, carCategory, freq(new double[] { f2 * 1500.0, f2 * 1500.0, f2 * 3400.0, 0.0 }));
        odMatrix.putDemandVector(nodeB, nodeF, truCategory, freq(new double[] { f1 * 1500.0, f1 * 1500.0, f1 * 3400.0, 0.0 }));
        ODOptions odOptions = new ODOptions().set(ODOptions.GTU_COLORER, getGtuColorer()).set(ODOptions.ANIMATION, true)
                .set(ODOptions.NO_LC_DIST, Length.createSI(200))
                .set(ODOptions.GTU_TYPE, new DefaultGTUCharacteristicsGeneratorOD(
                        new SdmStrategicalPlannerFactory(sim.getReplication().getStream("generation"))));
        ODApplier.applyOD(this.network, odMatrix, sim, odOptions);

        // animation
        this.animateNetwork(this.network);

        // setup the SDM
        DistractionFactory distFactory = new DistractionFactory(sim.getReplication().getStream("default"));
        for (String distraction : getProperty("distractions").split(","))
        {
            DefaultDistraction dist = DefaultDistraction.values()[Integer.parseInt(distraction) - 1];
            distFactory.addDistraction(dist, getTaskSupplier(dist, sim.getReplication().getStream("default")));
        }
        new StochasticDistractionModel(getBooleanProperty("allowMultiTasking"), distFactory.build(), sim, this.network);

        // sampler
        if (getBooleanProperty("output"))
        {
            this.sampler = new RoadSampler(sim);
            Time start = new Time(0.05, TimeUnit.BASE_HOUR);
            Time end = new Time(1.05, TimeUnit.BASE_HOUR);
            for (Lane lane : allLanes)
            {
                KpiLaneDirection kpiLane = new KpiLaneDirection(new LaneData(lane), KpiGtuDirectionality.DIR_PLUS);
                SpaceTimeRegion region = new SpaceTimeRegion(kpiLane, Length.ZERO, lane.getLength(), start, end);
                this.regions.add(region);
                this.sampler.registerSpaceTimeRegion(region);
            }
            this.sampler.registerExtendedDataType(this.ttc);
        }

        // return network
        return this.network;
    }

    /** {@inheritDoc} */
    @Override
    protected void addTabs(final OTSSimulatorInterface sim, final AbstractWrappableAnimation animation)
    {
        if (!getBooleanProperty("output") || !getBooleanProperty("plots"))
        {
            return;
        }
        try
        {
            TablePanel charts = new TablePanel(2, 2);
            GraphPath<KpiLaneDirection> path1 = GraphLaneUtil.createPath("Left road, left lane",
                    new LaneDirection((Lane) ((CrossSectionLink) this.network.getLink("AD")).getCrossSectionElement("Lane 1"),
                            GTUDirectionality.DIR_PLUS));
            GraphPath<KpiLaneDirection> path2 = GraphLaneUtil.createPath("Left road, right lane",
                    new LaneDirection((Lane) ((CrossSectionLink) this.network.getLink("AD")).getCrossSectionElement("Lane 2"),
                            GTUDirectionality.DIR_PLUS));
            GraphPath<KpiLaneDirection> path3 = GraphLaneUtil.createPath("Right road, left lane",
                    new LaneDirection((Lane) ((CrossSectionLink) this.network.getLink("BC")).getCrossSectionElement("Lane 1"),
                            GTUDirectionality.DIR_PLUS));
            GraphPath<KpiLaneDirection> path4 = GraphLaneUtil.createPath("Right road, right lane",
                    new LaneDirection((Lane) ((CrossSectionLink) this.network.getLink("BC")).getCrossSectionElement("Lane 2"),
                            GTUDirectionality.DIR_PLUS));
            charts.setCell(new ContourPlotSpeed("Left road, left lane", sim, new ContourDataSource<>(this.sampler, path1))
                    .getContentPane(), 0, 0);
            charts.setCell(new ContourPlotSpeed("Left road, right lane", sim, new ContourDataSource<>(this.sampler, path2))
                    .getContentPane(), 1, 0);
            charts.setCell(new ContourPlotSpeed("Right road, left lane", sim, new ContourDataSource<>(this.sampler, path3))
                    .getContentPane(), 0, 1);
            charts.setCell(new ContourPlotSpeed("Right road, right lane", sim, new ContourDataSource<>(this.sampler, path4))
                    .getContentPane(), 1, 1);
            animation.addTab(animation.getTabCount(), "contour plots", charts);
        }
        catch (NetworkException exception)
        {
            throw new RuntimeException(exception);
        }
    }

    /**
     * Creates a frequency vector.
     * @param array double[]; array in veh/h
     * @return FrequencyVector; frequency vector
     * @throws ValueException on problem
     */
    private FrequencyVector freq(final double[] array) throws ValueException
    {
        return new FrequencyVector(array, FrequencyUnit.PER_HOUR, StorageType.DENSE);
    }

    /**
     * Returns a task supplier for a distraction. These are specific to the SDM simulations.
     * @param distraction DefaultDistraction; distraction
     * @param stream StreamInterface; random number stream for randomized aspects of the distraction
     * @return TaskSupplier; task supplier
     */
    private TaskSupplier getTaskSupplier(final DefaultDistraction distraction, final StreamInterface stream)
    {
        switch (distraction)
        {
            case TALKING_CELL_PHONE:
            {
                return new TaskSupplier()
                {
                    /** {@inheritDoc} */
                    @Override
                    public Task getTask(final LaneBasedGTU gtu)
                    {
                        return new Task.Exponential(distraction.getId(), getDoubleProperty("phoneInit"),
                                getDoubleProperty("phoneFinal"), getDoubleProperty("phoneTau"), gtu.getSimulator());
                    }
                };
            }
            case CONVERSING:
            {
                return new TaskSupplier.Constant(distraction.getId(), getDoubleProperty("conversing"));
            }
            case MANIPULATING_AUDIO_CONTROLS:
            {
                return new TaskSupplier.Constant(distraction.getId(), getDoubleProperty("audio"));
            }
            case EXTERNAL_DISTRACTION:
            {
                return new TaskSupplier.Constant(distraction.getId(),
                        getDoubleProperty("externalBase") + getDoubleProperty("externalVar") * stream.nextDouble());
            }
            default:
                throw new IllegalArgumentException("Distraction " + distraction + " is not recognized.");
        }
    }

    /** {@inheritDoc} */
    @Override
    protected void onSimulationEnd()
    {
        if (getBooleanProperty("output"))
        {
            Length detectorPosition = Length.createSI(100.0);
            double tts = 0.0;
            List<Float> ttcList = new ArrayList<>();
            List<Float> decList = new ArrayList<>();
            int[] counts = new int[60];
            for (SpaceTimeRegion region : this.regions)
            {
                TrajectoryGroup<?> trajectoryGroup =
                        this.sampler.getTrajectoryGroup(region.getLaneDirection()).getTrajectoryGroup(region.getStartPosition(),
                                region.getEndPosition(), region.getStartTime(), region.getEndTime());
                for (Trajectory<?> trajectory : trajectoryGroup)
                {
                    try
                    {
                        tts += trajectory.getTotalDuration().si;
                        for (FloatDuration ttcVal : trajectory.getExtendedData(this.ttc))
                        {
                            if (!Float.isNaN(ttcVal.si) && ttcVal.si < 20)
                            {
                                ttcList.add(ttcVal.si);
                            }
                        }
                        for (float decVal : trajectory.getA())
                        {
                            if (decVal < -2.0)
                            {
                                decList.add(decVal);
                            }
                        }
                        if (region.getLaneDirection().getLaneData().getLinkData().getId().equals("EF") && trajectory.size() > 1
                                && trajectory.getX(0) < detectorPosition.si
                                && trajectory.getX(trajectory.size() - 1) > detectorPosition.si)
                        {
                            double t = trajectory.getTimeAtPosition(detectorPosition).si - region.getStartTime().si;
                            counts[(int) (t / 60.0)]++;
                        }
                    }
                    catch (SamplingException exception)
                    {
                        throw new RuntimeException(
                                "Unexpected exception: TimeToCollission not available or index out of bounds.", exception);
                    }
                }
            }
            int qMax = 0;
            for (int i = 0; i < counts.length - 4; i++)
            {
                int q = 0;
                for (int j = i; j < i + 5; j++)
                {
                    q += counts[j];
                }
                qMax = qMax > q ? qMax : q;
            }
            BufferedWriter bw;
            try
            {
                bw = new BufferedWriter(
                        new OutputStreamWriter(Writer.createOutputStream(getProperty("outputFile"), CompressionType.ZIP)));
                bw.write(String.format("total time spent [s]: %.0f", tts));
                bw.newLine();
                bw.write(String.format("maximum flow [veh/5min]: %d", qMax));
                bw.newLine();
                bw.write(String.format("time to collision [s]: %s", ttcList));
                bw.newLine();
                bw.write(String.format("strong decelerations [m/s^2]: %s", decList));
                bw.close();
            }
            catch (IOException exception)
            {
                throw new RuntimeException(exception);
            }
        }
    }

}
