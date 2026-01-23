package nl.tue.mapwelder.algorithms;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import nl.tue.geometrycore.geometry.Vector;
import nl.tue.geometrycore.geometry.linear.Polygon;
import nl.tue.geometrycore.gui.sidepanel.SideTab;
import nl.tue.geometrycore.util.DoubleUtil;
import nl.tue.mapwelder.data.Graph.DefiningMapVertex;
import nl.tue.mapwelder.data.Graph.Edge;
import nl.tue.mapwelder.data.Graph.Vertex;
import nl.tue.mapwelder.gui.Data;

/**
 *
 * @author Wouter Meulemans (w.meulemans@tue.nl)
 */
public class WeldAlgorithm extends Algorithm {

    private double threshold = 0;
    private double threshold_squared = 0;

    public WeldAlgorithm(Data data) {
        super(data, "Algorithmic welding", "Detects and automatically welds simple cases in the graph and trims degree-1 paths."
                + "<br/><br/>"
                + "The provided threshold is indicated the allowed discrete Fréchet distance between the weld paths. This distance is expressed in the coordinate space of the input.");
    }

    @Override
    public void createGUI(SideTab tab) {
        super.createGUI(tab);

        tab.addLabel("Threshold:");
        tab.addDoubleSpinner(threshold, 0, 10000000, 0.01, (e, v) -> {
            threshold = v;
            threshold_squared = v * v;
        });

        tab.addButton("Recommend threshold", (e) -> scan());
    }

    private void scan() {
        double minweld = Double.POSITIVE_INFINITY;
        data.ensureGraph();
        for (Vertex v : data.graph.getVertices()) {
            minweld = scanVertex(v, minweld);
        }
        System.out.println("Minimal DFD for weld path: " + minweld);
    }

    @Override
    public boolean run() {

        System.out.println("Building graph");
        data.ensureGraph();

        System.out.println("Trimming degree-1");
        List<Vertex> deg1 = new ArrayList();
        for (Vertex v : data.graph.getVertices()) {
            if (v.getDegree() <= 1) {
                deg1.add(v);
            }
        }
        for (Vertex v : deg1) {
            trimDegreeOne(v);
        }
        System.out.println("  Number of paths trimmed: " + deg1.size());

        System.out.println("Detecting weld paths");
        boolean[] handled = new boolean[data.graph.getVertices().size()];
        Arrays.fill(handled, false);

        int welds = 0;
        for (Vertex v : data.graph.getVertices()) {
            welds += tryVertex(v, handled);
        }
        System.out.println("  Number of weld paths found and resolved: " + welds);

        return deg1.size() > 0 || welds > 0;
    }

    private int tryVertex(Vertex v, boolean[] handled) {

        if (handled[v.getGraphIndex()]) {
            return 0;
        }

        int welds = 0;
        mainloop:
        while (true) {
            int d = v.getDegree();
            if (d <= 2) {
                return welds;
            }

            for (int i = 0; i < d; i++) {
                for (int j = i + 1; j < d; j++) {
                    if (tryWelding(v, v.getEdges().get(i), v.getEdges().get(j), handled)) {
                        welds++;
                        continue mainloop;
                    }
                }
            }

            // nothing happened
            return welds;
        }
    }

    private double scanVertex(Vertex v, double minweld) {
        int d = v.getDegree();
        if (d <= 2) {
            return minweld;
        }

        for (int i = 0; i < d; i++) {
            for (int j = i + 1; j < d; j++) {
                minweld = scanWelding(v, v.getEdges().get(i), v.getEdges().get(j), minweld);
            }
        }

        return minweld;

    }

    private double scanWelding(Vertex v, Edge a, Edge b, double minweld) {

        if (a.getMap().size() != 1) {
            return minweld;
        }
        if (b.getMap().size() != 1) {
            return minweld;
        }
        Polygon a_pol = a.getMap().iterator().next().polygon;
        Polygon b_pol = b.getMap().iterator().next().polygon;
        if (a_pol == b_pol) {
            // from the same polygon...
            return minweld;
        }

        Vertex a_first = a.getOtherVertex(v);
        Vertex b_first = b.getOtherVertex(v);

        if (a_first.squaredDistanceTo(b_first) >= minweld
                && a_first.squaredDistanceTo(v) >= minweld
                && b_first.squaredDistanceTo(v) >= minweld) {
            // DFD cannot be reduced
            return minweld;
        }

        List<Vertex> a_chain = createChain(v, a);
        List<Vertex> b_chain = createChain(v, b);

        int a_n = a_chain.size();
        int b_n = b_chain.size();

        if (a_chain.get(a_n - 1) != b_chain.get(b_n - 1)) {
            return minweld;
        }

        double[][] dfd = new double[a_n][b_n];
        int[][] prev = new int[a_n][b_n];

        dfd[0][0] = a_chain.get(0).squaredDistanceTo(b_chain.get(0));
        for (int i = 1; i < a_n; i++) {
            dfd[i][0] = Math.max(dfd[i - 1][0], a_chain.get(i).squaredDistanceTo(b_chain.get(0)));
            prev[i][0] = -1;
        }
        for (int j = 1; j < b_n; j++) {
            dfd[0][j] = Math.max(dfd[0][j - 1], a_chain.get(0).squaredDistanceTo(b_chain.get(j)));
            prev[0][j] = 1;
        }

        for (int i = 1; i < a_n; i++) {
            for (int j = 1; j < b_n; j++) {
                double d = a_chain.get(i).squaredDistanceTo(b_chain.get(j));
                if (dfd[i][j - 1] <= dfd[i - 1][j - 1] - DoubleUtil.EPS && dfd[i][j - 1] <= dfd[i - 1][j]) {
                    dfd[i][j] = Math.max(dfd[i][j - 1], d);
                    prev[i][j] = 1;
                } else if (dfd[i - 1][j] <= dfd[i - 1][j - 1] - DoubleUtil.EPS && dfd[i - 1][j] <= dfd[i][j - 1]) {
                    dfd[i][j] = Math.max(dfd[i - 1][j], d);
                    prev[i][j] = -1;
                } else {
                    dfd[i][j] = Math.max(dfd[i - 1][j - 1], d);
                    prev[i][j] = 0;
                }
            }
        }

        return Math.min(minweld, dfd[a_n - 1][b_n - 1]);
    }

    private boolean tryWelding(Vertex v, Edge a, Edge b, boolean[] handled) {

        if (a.getMap().size() != 1) {
            return false;
        }
        if (b.getMap().size() != 1) {
            return false;
        }
        Polygon a_pol = a.getMap().iterator().next().polygon;
        Polygon b_pol = b.getMap().iterator().next().polygon;
        if (a_pol == b_pol) {
            // from the same polygon...
            return false;
        }

        Vertex a_first = a.getOtherVertex(v);
        Vertex b_first = b.getOtherVertex(v);

        if (handled[a_first.getGraphIndex()] || handled[b_first.getGraphIndex()]) {
            return false;
        }

        if (a_first.squaredDistanceTo(b_first) > threshold_squared
                && a_first.squaredDistanceTo(v) > threshold_squared
                && b_first.squaredDistanceTo(v) > threshold_squared) {
            // DFD cannot be satisfied
            return false;
        }

        List<Vertex> a_chain = createChain(v, a);
        List<Vertex> b_chain = createChain(v, b);

        int a_n = a_chain.size();
        int b_n = b_chain.size();

        if (a_chain.get(a_n - 1) != b_chain.get(b_n - 1)) {
            return false;
        }

        double[][] dfd = new double[a_n][b_n];
        int[][] prev = new int[a_n][b_n];

        dfd[0][0] = a_chain.get(0).squaredDistanceTo(b_chain.get(0));
        for (int i = 1; i < a_n; i++) {
            dfd[i][0] = Math.max(dfd[i - 1][0], a_chain.get(i).squaredDistanceTo(b_chain.get(0)));
            prev[i][0] = -1;
        }
        for (int j = 1; j < b_n; j++) {
            dfd[0][j] = Math.max(dfd[0][j - 1], a_chain.get(0).squaredDistanceTo(b_chain.get(j)));
            prev[0][j] = 1;
        }

        for (int i = 1; i < a_n; i++) {
            for (int j = 1; j < b_n; j++) {
                double d = a_chain.get(i).squaredDistanceTo(b_chain.get(j));
                if (dfd[i][j - 1] <= dfd[i - 1][j - 1] - DoubleUtil.EPS && dfd[i][j - 1] <= dfd[i - 1][j]) {
                    dfd[i][j] = Math.max(dfd[i][j - 1], d);
                    prev[i][j] = 1;
                } else if (dfd[i - 1][j] <= dfd[i - 1][j - 1] - DoubleUtil.EPS && dfd[i - 1][j] <= dfd[i][j - 1]) {
                    dfd[i][j] = Math.max(dfd[i - 1][j], d);
                    prev[i][j] = -1;
                } else {
                    dfd[i][j] = Math.max(dfd[i - 1][j - 1], d);
                    prev[i][j] = 0;
                }
            }
        }

        if (dfd[a_n - 1][b_n - 1] > threshold_squared) {
            return false;
        }

        int w1 = a_n - 1, w2 = b_n - 1;
        Set<Vertex> v1s = new HashSet();
        Set<Vertex> v2s = new HashSet();
        while (w1 > 0 || w2 > 0) {
            v1s.add(a_chain.get(w1));
            v2s.add(b_chain.get(w2));
            switch (prev[w1][w2]) {
                case 0:
                    match(v1s, v2s, handled);
                    w1--;
                    w2--;
                    break;
                case -1:
                    w1--;
                    break;
                default:
                    w2--;
                    break;
            }
        }
        if (!v1s.isEmpty()) {
            match(v1s, v2s, handled);
        }

        Common.deduplicate(a_pol);
        Common.deduplicate(b_pol);

        return true;
    }

    private void match(Set<Vertex> v1s, Set<Vertex> v2s, boolean[] handled) {
        if (v1s.size() > 1 && v2s.size() > 1) {
            System.err.println("No diagonal?");
        }
        Vector avg = Vector.origin();
        for (Vertex v : v1s) {
            avg.translate(v);
        }
        for (Vertex v : v2s) {
            avg.translate(v);
        }
        avg.scale(1.0 / (v1s.size() + v2s.size()));

        for (Vertex v : v1s) {
            handled[v.getGraphIndex()] = true;
            for (DefiningMapVertex dmv : v.getDefiningMapVertices()) {
                dmv.vertex.set(avg);
            }
        }
        for (Vertex v : v2s) {
            handled[v.getGraphIndex()] = true;
            for (DefiningMapVertex dmv : v.getDefiningMapVertices()) {
                dmv.vertex.set(avg);
            }
        }
        v1s.clear();
        v2s.clear();
    }

    private List<Vertex> createChain(Vertex v, Edge e) {
        List<Vertex> chain = new ArrayList();
        chain.add(v);

        v = e.getOtherVertex(v);
        while (v.getDegree() == 2) {
            chain.add(v);
            e = next(v, e);
            v = e.getOtherVertex(v);
        }

        chain.add(v);
        return chain;
    }

    private Edge next(Vertex v, Edge prev) {
        if (v.getEdges().get(0) == prev) {
            return v.getEdges().get(1);
        } else {
            return v.getEdges().get(0);
        }
    }

    private void trimDegreeOne(Vertex v) {
        if (v.getGraphIndex() < 0) {
            // there was a floating boundary somehow (path ending and starting with degree-1 vertex)
            return;
        }
        while (v != null && v.getDegree() <= 1) {
            Vertex next = v.getDegree() == 0 ? null : v.getEdges().get(0).getOtherVertex(v);
            data.graph.removeVertexWithBacktracing(v);
            v = next;
        }
    }

}
