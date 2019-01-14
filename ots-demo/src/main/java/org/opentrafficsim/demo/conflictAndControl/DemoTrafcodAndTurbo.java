package org.opentrafficsim.demo.conflictAndControl;

import static org.opentrafficsim.core.gtu.GTUType.VEHICLE;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.naming.NamingException;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.djunits.unit.AccelerationUnit;
import org.djunits.unit.DurationUnit;
import org.djunits.unit.LengthUnit;
import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.djunits.value.vdouble.scalar.Time;
import org.djutils.io.URLResource;
import org.opentrafficsim.base.parameters.Parameters;
import org.opentrafficsim.core.animation.gtu.colorer.DefaultSwitchableGTUColorer;
import org.opentrafficsim.core.animation.gtu.colorer.GTUColorer;
import org.opentrafficsim.core.compatibility.Compatible;
import org.opentrafficsim.core.dsol.AbstractOTSModel;
import org.opentrafficsim.core.dsol.OTSAnimator;
import org.opentrafficsim.core.dsol.OTSSimulatorInterface;
import org.opentrafficsim.core.geometry.OTSGeometryException;
import org.opentrafficsim.core.gtu.GTUDirectionality;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.gtu.RelativePosition;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.OTSNetwork;
import org.opentrafficsim.demo.DefaultsFactory;
import org.opentrafficsim.draw.core.OTSDrawingException;
import org.opentrafficsim.draw.road.TrafficLightAnimation;
import org.opentrafficsim.road.gtu.lane.LaneBasedIndividualGTU;
import org.opentrafficsim.road.gtu.lane.tactical.LaneBasedGTUFollowingTacticalPlanner;
import org.opentrafficsim.road.gtu.lane.tactical.following.IDMPlusOld;
import org.opentrafficsim.road.gtu.strategical.LaneBasedStrategicalPlanner;
import org.opentrafficsim.road.gtu.strategical.route.LaneBasedStrategicalRoutePlanner;
import org.opentrafficsim.road.network.factory.xml.XmlNetworkLaneParser;
import org.opentrafficsim.road.network.lane.CrossSectionLink;
import org.opentrafficsim.road.network.lane.CrossSectionLink.Priority;
import org.opentrafficsim.road.network.lane.DirectedLanePosition;
import org.opentrafficsim.road.network.lane.Lane;
import org.opentrafficsim.road.network.lane.conflict.ConflictBuilder;
import org.opentrafficsim.road.network.lane.object.sensor.TrafficLightSensor;
import org.opentrafficsim.road.network.lane.object.trafficlight.SimpleTrafficLight;
import org.opentrafficsim.road.network.lane.object.trafficlight.TrafficLight;
import org.opentrafficsim.swing.gui.AbstractOTSSwingApplication;
import org.opentrafficsim.swing.gui.AnimationToggles;
import org.opentrafficsim.swing.gui.OTSAnimationPanel;
import org.opentrafficsim.trafficcontrol.TrafficController;
import org.opentrafficsim.trafficcontrol.trafcod.TrafCOD;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.event.EventInterface;
import nl.tudelft.simulation.event.EventListenerInterface;
import nl.tudelft.simulation.event.EventType;

/**
 * <p>
 * Copyright (c) 2013-2019 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$,
 *          initial version Dec 06, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class DemoTrafcodAndTurbo extends AbstractOTSSwingApplication
{
    /** */
    private static final long serialVersionUID = 20161118L;

    /** the panel. */
    private OTSAnimationPanel animationPanel;

    /**
     * Create a T-Junction demo.
     * @param title the title of the Frame
     * @param panel the tabbed panel to display
     * @param model the model
     * @throws OTSDrawingException on animation error
     */
    public DemoTrafcodAndTurbo(final String title, final OTSAnimationPanel panel, final TrafCODModel model)
            throws OTSDrawingException
    {
        super(model, panel);
        this.animationPanel = panel;
        // DefaultAnimationFactory.animateNetwork(model.getNetwork(), model.getSimulator());
        AnimationToggles.setTextAnimationTogglesStandard(panel);
        addTabs();
    }

    /**
     * Main program.
     * @param args String[]; the command line arguments (not used)
     */
    public static void main(final String[] args)
    {
        demo(true);
    }

    /**
     * Start the demo.
     * @param exitOnClose boolean; when running stand-alone: true; when running as part of a demo: false
     */
    public static void demo(final boolean exitOnClose)
    {
        try
        {
            OTSAnimator simulator = new OTSAnimator();
            final TrafCODModel junctionModel = new TrafCODModel(simulator);
            simulator.initialize(Time.ZERO, Duration.ZERO, Duration.createSI(3600.0), junctionModel);
            OTSAnimationPanel animationPanel =
                    new OTSAnimationPanel(junctionModel.getNetwork().getExtent(), new Dimension(800, 600), simulator,
                            junctionModel, new DefaultSwitchableGTUColorer(), junctionModel.getNetwork());
            DemoTrafcodAndTurbo app = new DemoTrafcodAndTurbo("TrafCOD Turbo demo", animationPanel, junctionModel);
            app.setExitOnClose(exitOnClose);
        }
        catch (SimRuntimeException | NamingException | RemoteException | OTSDrawingException exception)
        {
            exception.printStackTrace();
        }
    }

    /**
     * Add tab with trafCOD status.
     */
    private void addTabs()
    {
        TrafCODModel model = (TrafCODModel) getModel();
        JScrollPane scrollPane = new JScrollPane(model.getControllerDisplayPanel());
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.add(scrollPane);
        this.animationPanel.getTabbedPane().addTab(this.animationPanel.getTabbedPane().getTabCount() - 1,
                model.getTrafCOD().getId(), wrapper);
    }

    /**
     * The simulation model.
     */
    static class TrafCODModel extends AbstractOTSModel implements EventListenerInterface
    {
        /** */
        private static final long serialVersionUID = 20161020L;

        /** The network. */
        private OTSNetwork network;

        /** The TrafCOD controller. */
        private TrafCOD trafCOD;

        /** TrafCOD controller display. */
        private JPanel controllerDisplayPanel = new JPanel(new BorderLayout());

        /**
         * @param simulator the simulator for this model
         */
        TrafCODModel(final OTSSimulatorInterface simulator)
        {
            super(simulator);
        }

        /** {@inheritDoc} */
        @Override
        public void constructModel() throws SimRuntimeException
        {
            try
            {
                URL url = URLResource.getResource("/conflictAndControl/TurboRoundaboutAndSignal.xml");
                XmlNetworkLaneParser nlp = new XmlNetworkLaneParser(this.simulator);
                this.network = nlp.build(url, true);
                // add conflicts
                ((CrossSectionLink) this.network.getLink("EBNA")).setPriority(Priority.PRIORITY);
                ((CrossSectionLink) this.network.getLink("NBWA")).setPriority(Priority.PRIORITY);
                ((CrossSectionLink) this.network.getLink("WBSA")).setPriority(Priority.PRIORITY);
                ((CrossSectionLink) this.network.getLink("SBEA")).setPriority(Priority.PRIORITY);
                ConflictBuilder.buildConflicts(this.network, VEHICLE, this.simulator,
                        new ConflictBuilder.FixedWidthGenerator(new Length(2.0, LengthUnit.SI)));

                // CrossSectionLink csLink = ((CrossSectionLink)
                // this.network.getLink("WWW"));
                // Lane lane = (Lane) csLink.getCrossSectionElement("RIGHT");
                // GTUColorer gtuColorer = null;
                // setupBlock(lane, (DEVSSimulatorInterface.TimeDoubleUnit) this.simulator,
                // gtuColorer );

                String[] directions = { "E", "S", "W", "N" };
                // Add the traffic lights and the detectors
                Set<TrafficLight> trafficLights = new HashSet<>();
                Set<TrafficLightSensor> sensors = new HashSet<>();
                Length stopLineMargin = new Length(0.1, LengthUnit.METER);
                Length headDetectorLength = new Length(1, LengthUnit.METER);
                Length headDetectorMargin = stopLineMargin.plus(headDetectorLength).plus(new Length(3, LengthUnit.METER));
                Length longDetectorLength = new Length(30, LengthUnit.METER);
                Length longDetectorMargin = stopLineMargin.plus(longDetectorLength).plus(new Length(10, LengthUnit.METER));
                int stream = 1;
                for (String direction : directions)
                {
                    for (int laneNumber = 3; laneNumber >= 1; laneNumber--)
                    {
                        Lane lane = (Lane) ((CrossSectionLink) this.network.getLink(direction + "S", direction + "C"))
                                .getCrossSectionElement("FORWARD" + laneNumber);
                        if (lane != null)
                        {
                            if (stream != 7)
                            {
                                TrafficLight tl = new SimpleTrafficLight(String.format("TL%02d", stream), lane,
                                        lane.getLength().minus(stopLineMargin), this.simulator);
                                trafficLights.add(tl);

                                try
                                {
                                    new TrafficLightAnimation(tl, this.simulator);
                                }
                                catch (RemoteException | NamingException exception)
                                {
                                    throw new NetworkException(exception);
                                }

                                sensors.add(new TrafficLightSensor(String.format("D%02d1", stream), lane,
                                        lane.getLength().minus(headDetectorMargin), lane,
                                        lane.getLength().minus(headDetectorMargin).plus(headDetectorLength), null,
                                        RelativePosition.FRONT, RelativePosition.REAR, this.simulator, Compatible.EVERYTHING));
                                sensors.add(new TrafficLightSensor(String.format("D%02d2", stream), lane,
                                        lane.getLength().minus(longDetectorMargin), lane,
                                        lane.getLength().minus(longDetectorMargin).plus(longDetectorLength), null,
                                        RelativePosition.FRONT, RelativePosition.REAR, this.simulator, Compatible.EVERYTHING));
                            }
                            else
                            {
                                lane = (Lane) ((CrossSectionLink) this.network.getLink("ESS1", "ESS"))
                                        .getCrossSectionElement("FORWARD");
                                TrafficLight tl = new SimpleTrafficLight(String.format("TL%02d", stream), lane,
                                        lane.getLength().minus(stopLineMargin), this.simulator);
                                trafficLights.add(tl);

                                try
                                {
                                    new TrafficLightAnimation(tl, this.simulator);
                                }
                                catch (RemoteException | NamingException exception)
                                {
                                    throw new NetworkException(exception);
                                }

                                sensors.add(new TrafficLightSensor(String.format("D%02d1", stream), lane,
                                        lane.getLength().minus(headDetectorMargin), lane,
                                        lane.getLength().minus(headDetectorMargin).plus(headDetectorLength), null,
                                        RelativePosition.FRONT, RelativePosition.REAR, this.simulator, Compatible.EVERYTHING));
                                sensors.add(new TrafficLightSensor(String.format("D%02d2", stream), lane,
                                        lane.getLength().minus(longDetectorMargin), lane,
                                        lane.getLength().minus(longDetectorMargin).plus(longDetectorLength), null,
                                        RelativePosition.FRONT, RelativePosition.REAR, this.simulator, Compatible.EVERYTHING));

                            }

                        }
                        stream++;
                    }
                }
                String controllerName = "Not so simple TrafCOD controller";
                this.trafCOD = new TrafCOD(controllerName, URLResource.getResource("/conflictAndControl/Intersection12Dir.tfc"),
                        trafficLights, sensors, this.simulator, this.controllerDisplayPanel);
                this.trafCOD.addListener(this, TrafficController.TRAFFICCONTROL_CONTROLLER_EVALUATING);
                this.trafCOD.addListener(this, TrafficController.TRAFFICCONTROL_CONTROLLER_WARNING);
                this.trafCOD.addListener(this, TrafficController.TRAFFICCONTROL_CONFLICT_GROUP_CHANGED);
                this.trafCOD.addListener(this, TrafficController.TRAFFICCONTROL_STATE_CHANGED);
                this.trafCOD.addListener(this, TrafficController.TRAFFICCONTROL_VARIABLE_CREATED);
                this.trafCOD.addListener(this, TrafficController.TRAFFICCONTROL_TRACED_VARIABLE_UPDATED);
                // Subscribe the TrafCOD machine to trace command events that we
                // emit
                addListener(this.trafCOD, TrafficController.TRAFFICCONTROL_SET_TRACING);
                // fireEvent(TrafficController.TRAFFICCONTROL_SET_TRACING, new
                // Object[] {controllerName, "TGX", 8, true});
                // fireEvent(TrafficController.TRAFFICCONTROL_SET_TRACING, new
                // Object[] {controllerName, "XR1", 11, true});
                // fireEvent(TrafficController.TRAFFICCONTROL_SET_TRACING, new
                // Object[] {controllerName, "TD1", 11, true});
                // fireEvent(TrafficController.TRAFFICCONTROL_SET_TRACING, new
                // Object[] {controllerName, "TGX", 11, true});
                // fireEvent(TrafficController.TRAFFICCONTROL_SET_TRACING, new
                // Object[] {controllerName, "TL", 11, true});
                // System.out.println("demo: emitting a SET TRACING event for
                // all variables related to stream 11");
                // fireEvent(TrafficController.TRAFFICCONTROL_SET_TRACING, new
                // Object[] { controllerName, "", 11, true });

                // TrafCODDemo2.this.trafCOD.traceVariablesOfStream(TrafficController.NO_STREAM,
                // true);
                // TrafCODDemo2.this.trafCOD.traceVariablesOfStream(11, true);
                // TrafCODDemo2.this.trafCOD.traceVariable("MRV", 11, true);
            }
            catch (Exception exception)
            {
                exception.printStackTrace();
            }
        }

        /** {@inheritDoc} */
        @Override
        public final OTSNetwork getNetwork()
        {
            return this.network;
        }

        /**
         * @return trafCOD
         */
        public final TrafCOD getTrafCOD()
        {
            return this.trafCOD;
        }

        /**
         * @return controllerDisplayPanel
         */
        public final JPanel getControllerDisplayPanel()
        {
            return this.controllerDisplayPanel;
        }

        /** {@inheritDoc} */
        @Override
        public void notify(final EventInterface event) throws RemoteException
        {
            EventType type = event.getType();
            Object[] payload = (Object[]) event.getContent();
            if (TrafficController.TRAFFICCONTROL_CONTROLLER_EVALUATING.equals(type))
            {
                // System.out.println("Evaluation starts at " +
                // getSimulator().getSimulatorTime());
                return;
            }
            else if (TrafficController.TRAFFICCONTROL_CONFLICT_GROUP_CHANGED.equals(type))
            {
                System.out.println("Conflict group changed from " + ((String) payload[1]) + " to " + ((String) payload[2]));
            }
            else if (TrafficController.TRAFFICCONTROL_TRACED_VARIABLE_UPDATED.equals(type))
            {
                System.out.println(String.format("Variable changed %s <- %d   %s", payload[1], payload[4], payload[5]));
            }
            else if (TrafficController.TRAFFICCONTROL_CONTROLLER_WARNING.equals(type))
            {
                System.out.println("Warning " + payload[1]);
            }
            else
            {
                System.out.print("TrafCODDemo received event of type " + event.getType() + ", payload [");
                String separator = "";
                for (Object o : payload)
                {
                    System.out.print(separator + o);
                    separator = ",";
                }
                System.out.println("]");
            }
        }

        /**
         * Put a block at the end of a Lane.
         * @param lane Lane; the lane on which the block is placed
         * @param simulator OTSSimulatorInterface; the simulator
         * @param gtuColorer GTUColorer; the gtu colorer to use
         * @return Lane; the lane
         * @throws NamingException on ???
         * @throws NetworkException on network inconsistency
         * @throws SimRuntimeException on ???
         * @throws GTUException when construction of the GTU (the block is a GTU) fails
         * @throws OTSGeometryException when the initial path is wrong
         */
        private Lane setupBlock(final Lane lane, final OTSSimulatorInterface simulator, final GTUColorer gtuColorer)
                throws NamingException, NetworkException, SimRuntimeException, GTUException, OTSGeometryException
        {
            Length initialPosition = lane.getLength();
            Duration tSafe = new Duration(0, DurationUnit.SECOND);
            Acceleration ac1 = new Acceleration(1.0, AccelerationUnit.METER_PER_SECOND_2);
            Length l = new Length(1.0, LengthUnit.METER);
            IDMPlusOld carFollowingModelCars = new IDMPlusOld(ac1, ac1, l, tSafe, 1.0);
            Set<DirectedLanePosition> initialPositions = new LinkedHashSet<>(1);
            initialPositions.add(new DirectedLanePosition(lane, initialPosition, GTUDirectionality.DIR_PLUS));
            GTUType gtuType = GTUType.CAR;
            Parameters parameters = DefaultsFactory.getDefaultParameters();
            LaneBasedIndividualGTU block = new LaneBasedIndividualGTU("999999", gtuType, new Length(1, LengthUnit.METER),
                    lane.getWidth(1), Speed.ZERO, Length.createSI(0.5), this.simulator, this.network);
            LaneBasedStrategicalPlanner strategicalPlanner = new LaneBasedStrategicalRoutePlanner(
                    new LaneBasedGTUFollowingTacticalPlanner(carFollowingModelCars, block), block);
            block.setParameters(parameters);
            block.init(strategicalPlanner, initialPositions, Speed.ZERO);
            return lane;
        }

    }

}
