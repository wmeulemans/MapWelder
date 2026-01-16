package nl.tue.mapwelder.analyses;

import java.util.List;
import nl.tue.geometrycore.geometry.Vector;
import nl.tue.geometrycore.geometry.linear.Polygon;
import nl.tue.geometrycore.geometryrendering.GeometryRenderer;
import nl.tue.geometrycore.gui.sidepanel.SideTab;
import nl.tue.mapwelder.data.Region;
import nl.tue.mapwelder.gui.Data;

/**
 *
 * @author Wouter Meulemans (w.meulemans@tue.nl)
 */
public class SmallAngles extends Analysis {

    private double threshold_degrees = 2;

    public SmallAngles(Data data) {
        super(data, "Small angles", "Detects polygon vertices with an angle below the specified threshold.");
    }

    @Override
    public void createGUI(SideTab tab) {
        super.createGUI(tab);

        tab.addLabel("Angle (degrees):");
        tab.addDoubleSpinner(threshold_degrees, 0, 360, 0.25, (e, v) -> {
            threshold_degrees = v;
            if (enabled) {
                data.updateAnalysis(this);
            }
        });
    }

    @Override
    public void run(List<Problem> problems) {
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
                        problems.add(new SmallAngle(this, v));
                    }
                }
            }
        }
    }

    public class SmallAngle extends Problem {

        Vector pt;

        public SmallAngle(Analysis source, Vector pt) {
            super(source);
            this.pt = pt;
        }

        @Override
        public void render(GeometryRenderer draw) {
            draw.draw(pt);
        }

    }

}
