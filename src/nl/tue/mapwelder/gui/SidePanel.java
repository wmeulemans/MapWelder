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

        tab.addLabel("POLYGON RENDERING");

        tab.addCheckbox("Enabled", data.render_polygons, (e, v) -> {
            data.render_polygons = v;
            data.repaint();
        });

        tab.addSpace();

        tab.addLabel("Vertices");
        tab.addCheckbox("Hover only", data.polygon_vertex_hover, (e, v) -> {
            data.polygon_vertex_hover = v;
            data.repaint();
        });
        tab.makeCustomSplit(2, 0.3, 0.7);
        tab.addLabel("- Radius");
        tab.addIntegerSlider(data.polygon_vertex_radius, 0, 100, (e, v) -> {
            data.polygon_vertex_radius = v;
            data.repaint();
        });
        tab.makeCustomSplit(2, 0.3, 0.7);
        tab.addLabel("- Alpha");
        tab.addIntegerSlider(data.polygon_vertex_alpha, 0, 100, (e, v) -> {
            data.polygon_vertex_alpha = v;
            data.repaint();
        });

        tab.addSpace();

        tab.addLabel("Boundary");
        tab.addCheckbox("Hover only", data.polygon_edge_hover, (e, v) -> {
            data.polygon_edge_hover = v;
            data.repaint();
        });
        tab.makeCustomSplit(2, 0.3, 0.7);
        tab.addLabel("- Width");
        tab.addIntegerSlider(data.polygon_edge_width, 0, 100, (e, v) -> {
            data.polygon_edge_width = v;
            data.repaint();
        });
        tab.makeCustomSplit(2, 0.3, 0.7);
        tab.addLabel("- Alpha");
        tab.addIntegerSlider(data.polygon_edge_alpha, 0, 100, (e, v) -> {
            data.polygon_edge_alpha = v;
            data.repaint();
        });

        tab.addSpace();

        tab.addLabel("Fill");
        tab.addCheckbox("Hover only", data.polygon_fill_hover, (e, v) -> {
            data.polygon_fill_hover = v;
            data.repaint();
        });
        tab.makeCustomSplit(2, 0.3, 0.7);
        tab.addLabel("- Alpha");
        tab.addIntegerSlider(data.polygon_fill_alpha, 0, 100, (e, v) -> {
            data.polygon_fill_alpha = v;
            data.repaint();
        });

        tab.addSpace();

        tab.addLabel("Label");
        tab.addCheckbox("Hover only", data.label_hover, (e, v) -> {
            data.label_hover = v;
            data.repaint();
        });
        tab.makeCustomSplit(2, 0.3, 0.7);
        tab.addLabel("- Size");
        tab.addIntegerSlider(data.label_size, 0, 500, (e, v) -> {
            data.label_size = v;
            data.repaint();
        });

        tab.addSpace(4);

        tab.addLabel("GRAPH RENDERING");

        tab.addCheckbox("Enabled", data.render_graph, (e, v) -> {
            data.render_graph = v;
            data.repaint();
        });

        tab.addSpace();

        tab.addLabel("Vertices");
        tab.makeCustomSplit(2, 0.3, 0.7);
        tab.addLabel("- Radius");
        tab.addIntegerSlider(data.graph_vertex_radius, 0, 100, (e, v) -> {
            data.graph_vertex_radius = v;
            data.repaint();
        });

        tab.addSpace();

        tab.addLabel("Edges");
        tab.makeCustomSplit(2, 0.3, 0.7);
        tab.addLabel("- Width");
        tab.addIntegerSlider(data.graph_edge_width, 0, 100, (e, v) -> {
            data.graph_edge_width = v;
            data.repaint();
        });
    }
}
