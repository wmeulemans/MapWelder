package nl.tue.mapwelder.algorithms;

import java.util.List;
import nl.tue.geometrycore.geometry.linear.Polygon;
import nl.tue.mapwelder.data.PlaneMap;
import nl.tue.mapwelder.data.Region;

/**
 *
 * @author Wouter Meulemans (w.meulemans@tue.nl)
 */
public class Common {

    public static void deduplicate(PlaneMap map) {
        deduplicate(map.getRegions());
    }

    public static void deduplicate(List<Region> regions) {
        for (Region r : regions) {
            deduplicate(r);
        }
    }

    public static void deduplicate(Region r) {
        for (Polygon p : r.getParts()) {
            deduplicate(p);
        }
    }

    public static void deduplicate(Polygon p) {
        int i = p.vertexCount() - 1;
        while (i >= 0) {
            if (p.vertex(i).isApproximately(p.vertex(i - 1))) {
                p.removeVertex(i);
            } else {
                i--;
            }
        }
    }
}
