package nl.tue.mapwelder.algorithms;

import nl.tue.mapwelder.data.Graph.DefiningMapVertex;
import nl.tue.mapwelder.data.Graph.Vertex;
import nl.tue.mapwelder.gui.Data;

/**
 *
 * @author Wouter Meulemans (w.meulemans@tue.nl)
 */
public class SnapToGraph extends Algorithm {

    public SnapToGraph(Data data) {
        super(data, "Graph Snap", "Sets all region vertices to their corresponding vertex in the derived graph, and erases consecutive points along polygons.");
    }

    @Override
    public boolean run() {

        data.ensureGraph();

        for (Vertex v : data.graph.getVertices()) {

            if (v.getDefiningMapVertices().size() > 1) {
                for (DefiningMapVertex dmv : v.getDefiningMapVertices()) {
                    dmv.vertex.set(v);
                }
            }
        }

        Common.deduplicate(data.map);

        return true;
    }
}
