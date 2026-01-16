package nl.tue.mapwelder.algorithms;

import java.util.List;
import nl.tue.geometrycore.geometry.linear.LineSegment;
import nl.tue.geometrycore.geometry.linear.Polygon;
import nl.tue.geometrycore.geometry.linear.Rectangle;
import nl.tue.geometrycore.gui.sidepanel.SideTab;
import nl.tue.mapwelder.data.Graph.Vertex;
import nl.tue.mapwelder.gui.Data;

/**
 *
 * @author Wouter Meulemans (w.meulemans@tue.nl)
 */
public class Coarsen extends Algorithm {

    private boolean toposafe = false;
    private double threshold = 1;

    public Coarsen(Data data) {
        super(data, "Coarsen",
                "Traverses through all degree-2 vertices of a graph and removes"
                + " the vertex if it does not collapse a triangle and"
                + " the distance to the replacing segment does not"
                + " exceed a specified threshold. The threshold is"
                + " specified as a percentage of the bounding box width,"
                + " divided by 1000. That is, 1% threshold requires 1000"
                + " as a parameter.");
    }

    @Override
    public void createGUI(SideTab tab) {
        super.createGUI(tab);

        tab.addCheckbox("Avoid intersections", toposafe, (e, v) -> toposafe = v);

        tab.addLabel("Threshold / 1000:");
        tab.addDoubleSpinner(threshold, 0, Double.POSITIVE_INFINITY, 0.1, (e, v) -> threshold = v);
    }

    @Override
    public boolean run() {

        System.out.println("Obtaining graph");
        data.ensureGraph();

        int initsize = data.graph.getVertices().size();
        System.out.println("  vertex count: " + initsize);

        System.out.println("Starting removal process");
        double T = threshold * 0.01 * data.map.getBox().width() / 1000.0;
        int rem = 0;
        int i = 0;
        while (i < data.graph.getVertices().size()) {
            Vertex v = data.graph.getVertices().get(i);
            if (superfluous(v, T) && (!toposafe || safe(v))) {
                rem++;
                data.graph.removeWithBacktracing(v);
            } else {
                i++;
            }
        }
        System.out.println("  done!");

        System.out.println("Vertices removed: " + rem);

        return rem > 0;
    }

    @Override
    public boolean maintainsGraph() {
        return true;
    }

    private boolean superfluous(Vertex v, double T) {
        if (v.getDegree() != 2) {
            return false;
        }

        Vertex u = v.getEdges().get(0).getOtherVertex(v);
        Vertex w = v.getEdges().get(1).getOtherVertex(v);

        if (u.isNeighborOf(w)) {
            return false;
        }

        LineSegment ls = new LineSegment(u, w);
        if (ls.distanceTo(v) > T) {
            return false;
        }

        return true;
    }

    private boolean safe(Vertex v) {
        // only called for superfluous vertices

        Vertex u = v.getEdges().get(0).getOtherVertex(v);
        Vertex w = v.getEdges().get(1).getOtherVertex(v);

        Polygon p = new Polygon(u, v, w);

        List<Vertex> vts = data.graph.find(Rectangle.byBoundingBox(p));

        for (Vertex b : vts) {
            if (b != u && b != v && b != w && p.convexContainsPoint(b)) {
                return false;
            }
        }

        return true;
    }
}
