
package nl.tue.mapwelder.algorithms;

import java.util.Random;
import nl.tue.geometrycore.gui.sidepanel.SideTab;
import nl.tue.mapwelder.data.Graph.Vertex;
import nl.tue.mapwelder.gui.Data;

/**
 *
 * @author Wouter Meulemans (w.meulemans@tue.nl)
 */
public class Decimate extends Algorithm {

    private double pct = 1;

    public Decimate(Data data) {
        super(data, "Decimate", "Keeps roughly the given percentage of degree-2"
                + " vertices, randomly removing others.");
    }

    @Override
    public void createGUI(SideTab tab) {
        super.createGUI(tab);

        tab.addLabel("Percentage:");
        tab.addDoubleSpinner(pct, 0, 100, 0.1, (e, v) -> pct = v);
    }

    @Override
    public boolean maintainsGraph() {
        return true;
    }

    @Override
    public boolean run() {

        System.out.println("Obtaining graph");
        data.ensureGraph();

        int initsize = data.graph.getVertices().size();
        System.out.println("  vertex count: " + initsize);

        System.out.println("Starting removal process");
        int rem = 0;
        int i = 0;
        double df = 0.2;
        double f = df;
        Random R = new Random();
        while (i < data.graph.getVertices().size()) {
            if (i + rem > f * initsize) {
                System.out.println("  " + (int) Math.round(100 * f) + "%");
                f += df;
            }
            Vertex v = data.graph.getVertices().get(i);
            if (v.getDegree() == 2 && R.nextDouble() * 100 > pct) {
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
}
