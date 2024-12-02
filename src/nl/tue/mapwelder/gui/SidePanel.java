/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.tue.mapwelder.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;
import nl.tue.geometrycore.geometry.linear.Polygon;
import nl.tue.geometrycore.gui.sidepanel.SideTab;
import nl.tue.geometrycore.gui.sidepanel.TabbedSidePanel;
import nl.tue.geometrycore.util.DoubleUtil;
import nl.tue.mapwelder.data.Region;

/**
 *
 * @author Wouter
 */
public class SidePanel extends TabbedSidePanel {

    private final Data data;

    public SidePanel(Data data) {
        this.data = data;

        initIOTab();
        initOperationTab();
        initToolTab();
        initRenderTab();
    }

    private void initIOTab() {
        addComboTab("IO", (e, v) -> data.activeFormat = v, data.activeFormat, data.formats);
    }

    private void initOperationTab() {
        SideTab tab = addTab("Operations");

        tab.addButton("Trim no area", new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                for (Region r : data.map.getRegions()) {
                    Iterator<Polygon> it = r.getParts().iterator();
                    while (it.hasNext()) {
                        Polygon p = it.next();

                        if (p.areaUnsigned() < DoubleUtil.EPS) {
                            it.remove();
                        }
                    }
                }
                data.mapChanged();
            }
        });

        tab.addSpace();

        tab.addCheckbox("Auto refresh graph", data.autoanalyze, (e, v) -> data.autoanalyze = v);
        tab.addButton("Analyze problems", (ActionEvent e) -> {
            data.analyzeMapProblems();
        });
        tab.addCheckbox("Draw small angles", data.drawSmallAngles, (e, v) -> {
            data.drawSmallAngles = v;
            data.repaint();
        });
        tab.addCheckbox("Draw within-region issues", data.drawWithinRegionIssues, (e, v) -> {
            data.drawWithinRegionIssues = v;
            data.repaint();
        });
        tab.addCheckbox("Draw between-region issues", data.drawBetweenRegionIssues, (e, v) -> {
            data.drawBetweenRegionIssues = v;
            data.repaint();
        });

        tab.addCheckbox("Auto refresh graph", data.autoregraph, (e, v) -> data.autoregraph = v);
        tab.addButton("Create graph", (ActionEvent e) -> {
            data.computeGraph();
        });

        tab.addButton("Clear graph", (ActionEvent e) -> {
            data.cleargraph();
        });

        tab.addButton("Analyze graph", (ActionEvent e) -> {
            data.analyzeProblems();
        });
    }

    private void initToolTab() {
        addComboTab("Tools", (e, v) -> {
            data.activeTool = v;
            data.repaint();
        }, data.activeTool, data.tools);
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
