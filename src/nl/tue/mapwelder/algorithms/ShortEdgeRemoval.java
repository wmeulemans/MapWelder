/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package nl.tue.mapwelder.algorithms;

import nl.tue.geometrycore.geometry.Vector;
import nl.tue.geometrycore.geometry.linear.Polygon;
import nl.tue.geometrycore.gui.sidepanel.SideTab;
import nl.tue.geometrycore.util.DoubleUtil;
import nl.tue.geometrycore.util.LexicographicOrder;
import nl.tue.mapwelder.data.Region;
import nl.tue.mapwelder.gui.Data;

/**
 *
 * @author wmeulema
 */
public class ShortEdgeRemoval extends Algorithm {

    private double threshold = 0;

    public ShortEdgeRemoval(Data data) {
        super(data, "Short Edge Remover", "Traverses all polygons, dropping edges below or equal"
                + " to the specified length threshold. Threshold is specified as a percentage"
                + " of the bounding box width, divided by 1000.");
    }

    @Override
    public void createGUI(SideTab tab) {
        super.createGUI(tab);

        tab.addButton("Scan", (e) -> scan());

        tab.addLabel("Threshold:");
        tab.addDoubleSpinner(threshold, 0, 10000000, 0.01, (e, v) -> threshold = v);
    }

    public void scan() {
        int rem = 0;
        double T = threshold * 0.01 * data.map.getBox().width() / 1000.0 + DoubleUtil.EPS;
        double min_l = Double.POSITIVE_INFINITY;
        for (Region r : data.map.getRegions()) {
            for (Polygon p : r.getParts()) {
                int i = 0;
                while (i < p.vertexCount()) {
                    Vector u = p.vertex(i - 1);
                    Vector v = p.vertex(i);
                    if (u.distanceTo(v) < T) {
                        rem++;
                    }
                    min_l = Math.min(min_l, u.distanceTo(v));
                    i++;
                }
            }
        }

        System.out.println("Minimum edge length: " + (min_l / (0.01 * data.map.getBox().width() / 1000.0)));
        System.out.println("Number of of edges below threshold: " + rem);
    }

    @Override
    public boolean run() {
        int rem = 0;
        double T = threshold * 0.01 * data.map.getBox().width() / 1000.0 + DoubleUtil.EPS;
        LexicographicOrder order = new LexicographicOrder();
        for (Region r : data.map.getRegions()) {
            for (Polygon p : r.getParts()) {
                int i = 0;
                while (i < p.vertexCount()) {
                    Vector u = p.vertex(i - 1);
                    Vector v = p.vertex(i);
                    if (u.distanceTo(v) < T) {
                        if (order.compare(u, v) < 0) {
                            p.removeVertex(i - 1);
                        } else {
                            p.removeVertex(i);
                        }
                        rem++;
                    } else {
                        i++;
                    }
                }
            }
        }
        System.out.println("Number of vertices removed: " + rem);
        return rem > 0;
    }
}
