package nl.tue.mapwelder.algorithms;

import java.util.ArrayList;
import java.util.List;
import nl.tue.geometrycore.datastructures.priorityqueue.BasicIndexable;
import nl.tue.geometrycore.datastructures.priorityqueue.IndexedPriorityQueue;
import nl.tue.geometrycore.geometry.linear.Polygon;
import nl.tue.geometrycore.geometry.linear.Rectangle;
import nl.tue.geometrycore.gui.sidepanel.SideTab;
import nl.tue.mapwelder.data.Graph.Vertex;
import nl.tue.mapwelder.gui.Data;

/**
 *
 * @author Wouter Meulemans (w.meulemans@tue.nl)
 */
public class VisvalingamWhyatt extends Algorithm {

    private boolean toposafe = true;
    private double threshold = 1;

    public VisvalingamWhyatt(Data data) {
        super(data, "Visvalingam-Whyatt",
                "Executes the Visvalingam-Whyatt simplification algorithm. The threshold is"
                + " specified as a percentage of the bounding box area,"
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
        Queue queue = new Queue(data.graph.getVertices().size());

        double T = threshold * 0.01 * data.map.getBox().areaUnsigned() / 1000.0;

        Elt[] eltmap = new Elt[data.graph.getVertices().size()];

        for (Vertex v : data.graph.getVertices()) {
            if (v.getDegree() == 2) {
                eltmap[v.getGraphIndex()] = new Elt(v, -1);
            }
        }

        for (Vertex v : data.graph.getVertices()) {
            updateElt(v, T, queue, eltmap);
        }

        int rem = 0;
        while (queue.size() > 0) {
            Elt elt = queue.poll();

            Vertex v = elt.vertex;

            Vertex u = v.getEdges().get(0).getOtherVertex(v);
            Vertex w = v.getEdges().get(1).getOtherVertex(v);

            // the last is going to move to the graph index, when we remove v...
            // we could test whether v is last, but that doesnt really help much
            eltmap[v.getGraphIndex()] = eltmap[data.graph.getVertices().size() - 1];
            eltmap[data.graph.getVertices().size() - 1] = null;

            data.graph.mergeVertexWithBacktracing(v);

            for (Vertex b : elt.blocks) {
                Elt belt = eltmap[b.getGraphIndex()];
                belt.blocked_by.remove(v);
                if (belt.blocked_by.isEmpty()) {
                    // unblocked an operation!
                    queue.add(belt);
                }
            }
            
            updateElt(u, T, queue, eltmap);
            updateElt(w, T, queue, eltmap);

            // special case: if u,w have a common neighbor, then this needs to be disabled
            for (Vertex nbr : u.getNeighbors()) {
                if (nbr.isNeighborOf(w)) {
                    // triangle
                    updateElt(nbr, T, queue, eltmap);
                }
            }

            rem++;
        }
        System.out.println("  done!");

        System.out.println("Vertices removed: " + rem);

        return rem > 0;
    }

    private void updateElt(Vertex v, double T, Queue queue, Elt[] eltmap) {
        Elt elt = eltmap[v.getGraphIndex()];
        if (elt == null) {
            // degree != 2
            return;
        }

        Vertex u = v.getEdges().get(0).getOtherVertex(v);
        Vertex w = v.getEdges().get(1).getOtherVertex(v);

        if (u.isNeighborOf(w)) {
            // triangle, cannot collapse
            for (Vertex b : elt.blocked_by) {
                if (eltmap[b.getGraphIndex()] != null) {
                    eltmap[b.getGraphIndex()].blocks.remove(v);
                }
            }
            elt.blocked_by.clear();
            queue.remove(elt);
            return;
        }

        Polygon p = new Polygon(u, v, w);
        double area = p.areaUnsigned();

        if (area <= T) {
            // small enough
            elt.cost = area;

            // is it safe?
            if (toposafe) {
                for (Vertex b : elt.blocked_by) {
                    if (eltmap[b.getGraphIndex()] != null) {
                        // otherwise, it is a degree 3 vertex, and it isnt going to be removed
                        eltmap[b.getGraphIndex()].blocks.remove(v);
                    }
                }
                elt.blocked_by.clear();

                List<Vertex> vts = data.graph.find(Rectangle.byBoundingBox(p));
                for (Vertex b : vts) {
                    if (b != u && b != v && b != w && p.convexContainsPoint(b)) {
                        elt.blocked_by.add(b);
                        if (eltmap[b.getGraphIndex()] != null) {
                            // otherwise, it is a degree 3 vertex, and it isnt going to be removed
                            eltmap[b.getGraphIndex()].blocks.add(v);
                        }
                    }
                }
            }

            if (!elt.blocked_by.isEmpty()) {
                // not safe to perform, ignore for now
                queue.remove(elt);
            } else if (queue.contains(elt)) {
                // update
                queue.priorityChanged(elt);
            } else {
                // insert
                queue.add(elt);
            }
        } else {
            // too big, must wait until its own geometry changes
            for (Vertex b : elt.blocked_by) {
                if (eltmap[b.getGraphIndex()] != null) {
                    eltmap[b.getGraphIndex()].blocks.remove(v);
                }
            }
            elt.blocked_by.clear();
            queue.remove(elt);
        }
    }

    @Override
    public boolean maintainsGraph() {
        return true;
    }

    private class Queue extends IndexedPriorityQueue<Elt> {

        public Queue(int initialCapacity) {
            super(initialCapacity, (e, f) -> Double.compare(e.cost, f.cost));
        }
    }

    private class Elt extends BasicIndexable {

        Vertex vertex;
        double cost;
        List<Vertex> blocked_by = new ArrayList();
        List<Vertex> blocks = new ArrayList();

        public Elt(Vertex vertex, double cost) {
            this.vertex = vertex;
            this.cost = cost;
        }

    }
}
