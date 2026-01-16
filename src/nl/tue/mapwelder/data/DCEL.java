package nl.tue.mapwelder.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import nl.tue.geometrycore.geometry.OrientedGeometry;
import nl.tue.geometrycore.geometry.linear.LineSegment;
import nl.tue.geometrycore.geometry.linear.Polygon;
import nl.tue.geometrycore.graphs.GraphConstruction;
import nl.tue.geometrycore.graphs.dcel.DCELDart;
import nl.tue.geometrycore.graphs.dcel.DCELFace;
import nl.tue.geometrycore.graphs.dcel.DCELGraph;
import nl.tue.geometrycore.graphs.dcel.DCELVertex;
import nl.tue.geometrycore.util.DoubleUtil;
import nl.tue.mapwelder.data.DCEL.Dart;
import nl.tue.mapwelder.data.DCEL.Face;
import nl.tue.mapwelder.data.DCEL.Node;

/**
 *
 * @author Wouter Meulemans (w.meulemans@tue.nl)
 */
public class DCEL extends DCELGraph<LineSegment, Node, Dart, Face> {

    public Map<Region, List<Face>> regionToFaces = new HashMap();

    public DCEL(PlaneMap map) {

        Map<Dart, List<Region>> backmap = new HashMap();
        GraphConstruction.convertGeometriesToDCEL(this, map.getRegions(), DoubleUtil.EPS,
                (OrientedGeometry geometry) -> new LineSegment(geometry.getStart().clone(), geometry.getEnd().clone()),
                null, null, null, (Map) backmap);

        sortToContainmentOrder();

        for (Region r : map.getRegions()) {
            regionToFaces.put(r, new ArrayList());
        }
        for (Face f : getFaces()) {
            if (f.isProperFace() && !f.isOuterFace()) {
                Set<Region> regions = new HashSet(map.getRegions());
                for (Dart d : f.iterateDarts()) {
                    regions.retainAll(backmap.containsKey(d) ? backmap.get(d) : backmap.get(d.getTwin()));
                }
                for (Face ff : f.getFloatingComponents()) {
                    for (Dart d : ff.iterateDarts()) {
                        regions.retainAll(backmap.containsKey(d) ? backmap.get(d) : backmap.get(d.getTwin()));
                    }
                }
                // get smallest one out of regions
                if (regions.size() > 0) {
                    // TODO: not very robust
                    double area = Double.POSITIVE_INFINITY;
                    Region minr = null;
                    for (Region r : regions) {
                        double a = 0;
                        for (Polygon P : r.getParts()) {
                            a += P.areaUnsigned();
                        }
                        if (a < area) {
                            area = a;
                            minr = r;
                        }
                    }
                    f.region = minr;
                    regionToFaces.get(minr).add(f);
                } else {
                    System.err.println("cannot identify region: " + regions.size());
                }
            }
        }
    }

    @Override
    public Node createVertex() {
        return new Node();
    }

    @Override
    public Dart createDart() {
        return new Dart();
    }

    @Override
    public Face createFace() {
        return new Face();
    }

    public static class Node extends DCELVertex<LineSegment, Node, Dart, Face> {
    }

    public static class Dart extends DCELDart<LineSegment, Node, Dart, Face> {
    }

    public static class Face extends DCELFace<LineSegment, Node, Dart, Face> {

        public Region region;
    }
}
