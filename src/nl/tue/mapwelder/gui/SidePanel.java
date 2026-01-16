package nl.tue.mapwelder.gui;

import nl.tue.geometrycore.gui.sidepanel.ComboTab;
import nl.tue.geometrycore.gui.sidepanel.SideTab;
import nl.tue.geometrycore.gui.sidepanel.TabbedSidePanel;

/**
 *
 * @author Wouter Meulemans (w.meulemans@tue.nl)
 */
public class SidePanel extends TabbedSidePanel {

    private final Data data;

    public SidePanel(Data data) {
        this.data = data;

        initIOTab();
        initAnalysisTab();
        initAlgorithmTab();
        initToolTab();
        initRenderTab();
    }

    private void initIOTab() {
        addComboTab("IO", (e, v) -> data.activeFormat = v, data.activeFormat, data.formats);
    }

    private void initAnalysisTab() {
        ComboTab tab = addComboTab("Analysis", (e, v) -> {
        }, data.analyses[0], data.analyses);
        
        tab.startCommonMode();

        tab.addCheckbox("Auto refresh graph", data.autoregraph, (e, v) -> {
            data.autoregraph = v;
            if (data.autoregraph) {
                data.ensureGraph();
            }
        });
        tab.addButton("Compute graph", (e) -> data.ensureGraph());

        tab.endCommonMode();

    }

    private void initToolTab() {
        addComboTab("Tools", (e, v) -> {
            data.activeTool = v;
            data.repaint();
        }, data.activeTool, data.tools);
    }

    private void initAlgorithmTab() {
        addComboTab("Algorithms", (e, v) -> {
            data.activeAlgorithm = v;
            data.repaint();
        }, data.activeAlgorithm, data.algorithms);
    }

    private void initRenderTab() {
        SideTab tab = addTab("Render");

        tab.addLabel("Fill alpha");
        tab.addCheckbox("Hover only", data.fillalphahover, (e, v) -> {
            data.fillalphahover = v;
            data.repaint();
        });
        tab.addIntegerSlider(data.fillalpha, 0, 100, (e, v) -> {
            data.fillalpha = v;
            data.repaint();
        });

        tab.addSpace();

        tab.addLabel("Boundary");
        tab.addCheckbox("Hover only", data.boundaryhover, (e, v) -> {
            data.boundaryhover = v;
            data.repaint();
        });
        tab.addIntegerSlider(data.boundary, 0, 10, (e, v) -> {
            data.boundary = v;
            data.repaint();
        });

        tab.addSpace();

        tab.addLabel("Vertices");
        tab.addCheckbox("Hover only", data.vertexhover, (e, v) -> {
            data.vertexhover = v;
            data.repaint();
        });
        tab.addIntegerSlider(data.vertex, 0, 10, (e, v) -> {
            data.vertex = v;
            data.repaint();
        });

        tab.addLabel("Label");
        tab.addCheckbox("Hover only", data.labelhover, (e, v) -> {
            data.labelhover = v;
            data.repaint();
        });
        tab.addIntegerSlider(data.labelsize, 0, 50, (e, v) -> {
            data.labelsize = v;
            data.repaint();
        });
    }
}
