package org.opentrafficsim.demo.ntm;

import java.awt.Dimension;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;
import java.net.URL;

import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import nl.tudelft.simulation.dsol.animation.D2.AnimationPanel;
import nl.tudelft.simulation.dsol.experiment.ReplicationMode;
import nl.tudelft.simulation.dsol.gui.swing.DSOLApplication;
import nl.tudelft.simulation.dsol.gui.swing.DSOLPanel;
import nl.tudelft.simulation.dsol.gui.swing.HTMLPanel;
import nl.tudelft.simulation.dsol.simulators.SimulatorInterface;
import nl.tudelft.simulation.event.Event;

import org.djunits.unit.TimeUnit;
import org.djunits.value.vdouble.scalar.DoubleScalar;
import org.opentrafficsim.core.dsol.OTSDEVSAnimator;
import org.opentrafficsim.core.dsol.OTSReplication;
import org.opentrafficsim.core.dsol.OTSSimTimeDouble;
import org.opentrafficsim.demo.ntm.IO.ProjectConfigurations;

/**
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate$, @version $Revision$, by $Author$,
 * initial version Aug 15, 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public class NTMTestApplication extends DSOLApplication
{
    /**
     * @param title
     * @param panel
     */
    public NTMTestApplication(String title,
        DSOLPanel<DoubleScalar.Abs<TimeUnit>, DoubleScalar.Rel<TimeUnit>, OTSSimTimeDouble> panel)
    {
        super(title, panel);
    }

    /** */
    private static final long serialVersionUID = 20140819L;

    /** */
    public static DSOLPanel<DoubleScalar.Abs<TimeUnit>, DoubleScalar.Rel<TimeUnit>, OTSSimTimeDouble> panel;

    public static JTextArea textArea;

    /**
     * @param args
     * @throws Exception
     */
    public static void main(final String[] args) throws Exception
    {
        NTMModel model = new NTMModel();
        InputNTM inputNTM = new InputNTM();
        model.setInputNTM(inputNTM);
        // String startMap = "D:/gtamminga/workspace/ots-ntm/src/main/resources/gis/TheHague/";
        String startMap = "D:/java/opentrafficsim/workspace/ots-ntm/src/main/resources/gis/";
        // String startMap = System.getProperty("user.dir");
        ProjectConfigurations.readConfigurations(startMap, model);
        if (!new File(model.getInputNTM().getInputMap()).canRead())
        {
            model.getInputNTM().setInputMap(System.getProperty("user.dir"));
        }
        OTSDEVSAnimator simulator = new OTSDEVSAnimator();
        // model.getSettingsNTM().getStartTimeSinceMidnight().getInUnit(TimeUnit.SECOND)
        OTSSimTimeDouble startTime = new OTSSimTimeDouble(new DoubleScalar.Abs<TimeUnit>(0.0, TimeUnit.SECOND));
        OTSReplication replication =
            new OTSReplication("rep1", startTime, new DoubleScalar.Rel<TimeUnit>(0.0, TimeUnit.SECOND),
                new DoubleScalar.Rel<TimeUnit>(10800.0, TimeUnit.SECOND), model);
        // simulator.initialize(replication, ReplicationMode.TERMINATING);

        panel = new DSOLPanel<DoubleScalar.Abs<TimeUnit>, DoubleScalar.Rel<TimeUnit>, OTSSimTimeDouble>(model, simulator);
        addInfoTab(panel);

        Rectangle2D extent = new Rectangle2D.Double(65000.0, 440000.0, 55000.0, 30000.0);
        Dimension size = new Dimension(1024, 768);
        AnimationPanel animationPanel = new AnimationPanel(extent, size, simulator);
        panel.getTabbedPane().addTab(0, "animation", animationPanel);
        String content =
            "The simulation starts with the import of alle data, and initializes the NTM model\n"
                + "Wait untill this process has finished...\n" + " \n";
        int index = panel.getTabbedPane().getSelectedIndex();
        textArea = new JTextArea(content);
        panel.getTabbedPane().setComponentAt(index, textArea);
        // tell the animation panel to update its statistics
        // TODO should be done automatically in DSOL!
        animationPanel.notify(new Event(SimulatorInterface.START_REPLICATION_EVENT, simulator, null));
        // infoBox("Start initialization", "NTM");
        new NTMTestApplication("Network Transmission Model", panel);

        simulator.initialize(replication, ReplicationMode.TERMINATING);
        // infoBox("Ended initialization", "NTM");
        textArea.append("Finished the initialization,\n" + "Push the Start button now! \n" + " \n");

    }

    public static void infoBox(String infoMessage, String titleBar)
    {
        JOptionPane.showMessageDialog(null, infoMessage, "InfoBox: " + titleBar, JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * @param panel
     */
    private static void addInfoTab(
        final DSOLPanel<DoubleScalar.Abs<TimeUnit>, DoubleScalar.Rel<TimeUnit>, OTSSimTimeDouble> panel)
    {
        // Let's find some content for our infoscreen and add it to our tabbedPane
        String helpSource = "/" + NTMModel.class.getPackage().getName().replace('.', '/') + "/html/ntm.html";
        URL page = NTMModel.class.getResource(helpSource);
        if (page != null)
        {
            HTMLPanel htmlPanel = null;
            try
            {
                htmlPanel = new HTMLPanel(page);
            }
            catch (IOException exception)
            {
                exception.printStackTrace();
            }
            panel.getTabbedPane().addTab("info", new JScrollPane(htmlPanel));
        }
        else
        {
            System.err.println("Information page " + helpSource + " not found.");
        }
    }
}
