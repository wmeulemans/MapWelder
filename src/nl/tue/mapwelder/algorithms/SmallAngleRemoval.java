package nl.tue.mapwelder.algorithms;

import nl.tue.geometrycore.geometry.Vector;
import nl.tue.geometrycore.geometry.linear.Polygon;
import nl.tue.geometrycore.gui.sidepanel.SideTab;
import nl.tue.mapwelder.data.Region;
import nl.tue.mapwelder.gui.Data;

/**
 *
 * @author Wouter Meulemans (w.meulemans@tue.nl)
 */
public class SmallAngleRemoval extends Algorithm {
    private double threshold_degrees = 2;

    public SmallAngleRemoval(Data data) {
        super(data, "Small angles", "Removes polygon vertices with an angle below the specified threshold.");
    }

    @Override
    public void createGUI(SideTab tab) {
        super.createGUI(tab);

        tab.addLabel("Angle (degrees):");
        tab.addDoubleSpinner(threshold_degrees, 0, 360, 0.25, (e, v) -> {
            threshold_degrees = v;
        });
    }

    @Override
    public boolean run() {
        System.out.println("Removing small angles");
        int rem = 0;
        for (Region r : data.map.getRegions()) {
            for (Polygon p : r.getParts()) {
                for (int i = 0; i < p.vertexCount(); i++) {
                    Vector u = p.vertex(i - 1);
                    Vector v = p.vertex(i);
                    Vector w = p.vertex(i + 1);

                    Vector dir1 = Vector.subtract(u, v);
                    Vector dir2 = Vector.subtract(w, v);

                    double angle = Math.abs(dir1.computeSignedAngleTo(dir2, true, true));

                    if (angle < Math.toRadians(threshold_degrees)) {
                        p.removeVertex(i);
                        i--;
                        rem++;
                    }
                }
            }
        }
        System.out.println("Number of vertices removed: "+rem);
        return rem > 0;
    }
}
