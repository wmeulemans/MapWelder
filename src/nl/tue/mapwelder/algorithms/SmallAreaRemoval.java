/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package nl.tue.mapwelder.algorithms;

import java.util.Iterator;
import nl.tue.geometrycore.geometry.linear.Polygon;
import nl.tue.geometrycore.gui.sidepanel.SideTab;
import nl.tue.geometrycore.util.DoubleUtil;
import nl.tue.mapwelder.data.Region;
import nl.tue.mapwelder.gui.Data;

/**
 *
 * @author wmeulema
 */
public class SmallAreaRemoval extends Algorithm {

    private double threshold = 0;

    public SmallAreaRemoval(Data data) {
        super(data, "Small Area Remover",
                "Traverses all polygons, dropping those below or"
                + " equal to the specified area threshold. Threshold is specified as"
                + " a percentage of the bounding box area.");
    }

    @Override
    public void createGUI(SideTab tab) {
        super.createGUI(tab);

        tab.addButton("Scan", (e) -> scan());

        tab.addLabel("Threshold:");
        tab.addDoubleSpinner(threshold, 0, 100, 0.01, (e, v) -> threshold = v);
    }

    public void scan() {

        double min_a = Double.POSITIVE_INFINITY;
        int pcnt = 0;
        for (Region r : data.map.getRegions()) {
            double a = 0;
            for (Polygon P : r.getParts()) {
                a = Math.max(a, P.areaUnsigned());
                pcnt++;
            }
            min_a = Math.min(a, min_a);
        }
        double frac = min_a / data.map.getBox().areaUnsigned();
        double pct = frac * 100;

        System.out.println("Number of regions: " + data.map.getRegions().size());
        System.out.println("Number of polygons: " + pcnt);

        System.out.println("Smallest area of the largest polygon in each region:");
        System.out.println("  " + pct + "% of bounding box");
    }

    @Override
    public boolean run() {
        int rem = 0;
        double T = threshold * 0.01 * data.map.getBox().areaUnsigned() + DoubleUtil.EPS;
        for (Region r : data.map.getRegions()) {
            Iterator<Polygon> it = r.getParts().iterator();
            while (it.hasNext()) {
                Polygon p = it.next();

                if (p.areaUnsigned() < T) {
                    it.remove();
                    rem++;
                }
            }
        }
        System.out.println("Number of polygons removed: " + rem);
        return rem > 0;
    }
}
