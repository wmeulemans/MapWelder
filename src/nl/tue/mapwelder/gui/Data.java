/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.tue.mapwelder.gui;

import java.util.ArrayList;
import java.util.List;
import nl.tue.geometrycore.geometry.BaseGeometry;
import nl.tue.geometrycore.geometry.Vector;
import nl.tue.geometrycore.geometry.linear.LineSegment;
import nl.tue.geometrycore.geometry.linear.Polygon;
import nl.tue.geometrycore.geometry.linear.Rectangle;
import nl.tue.geometrycore.util.DoubleUtil;
import nl.tue.mapwelder.data.Graph;
import nl.tue.mapwelder.data.Graph.Edge;
import nl.tue.mapwelder.data.Graph.Vertex;
import nl.tue.mapwelder.data.PlaneMap;
import nl.tue.mapwelder.data.Region;
import nl.tue.mapwelder.gui.tools.AutoWeldTool;
import nl.tue.mapwelder.gui.tools.NoTool;
import nl.tue.mapwelder.gui.tools.Tool;
import nl.tue.mapwelder.gui.tools.VertexTool;
import nl.tue.mapwelder.gui.tools.WeldTool;
import nl.tue.mapwelder.io.DecomposedJsonFormat;
import nl.tue.mapwelder.io.Format;
import nl.tue.mapwelder.io.GeojsonFormat;
import nl.tue.mapwelder.io.IpeFormat;
import nl.tue.mapwelder.io.SvgFormat;

/**
 *
 * @author Wouter
 */
public class Data {

    public Tool[] tools = {
        new NoTool(this),
        new WeldTool(this),
        new VertexTool(this),
        new AutoWeldTool(this)};
    public Tool activeTool = tools[3];

    public Format[] formats = {
        new IpeFormat(this),
        new SvgFormat(this),
        new GeojsonFormat(this),
        new DecomposedJsonFormat(this)};
    public Format activeFormat = formats[0];

    public PlaneMap map = new PlaneMap(new Rectangle());
    public Region hover = null;

    public Graph graph = null;
    public boolean autoregraph = false;

    public List<Vector> smallangles = new ArrayList();
    public List<BaseGeometry> withinRegionIntersections = new ArrayList();
    public List<BaseGeometry> betweenRegionIntersections = new ArrayList();

    public int boundary = 0;
    public boolean boundaryhover = false;

    public int fillalpha = 50;
    public boolean fillalphahover = false;

    public int vertex = 3;
    public boolean vertexhover = true;

    public int labelsize = 10;
    public boolean labelhover = true;

    public DrawPanel draw;
    public SidePanel side;

    public boolean drawSmallAngles = true;
    public boolean drawBetweenRegionIssues = true;
    public boolean drawWithinRegionIssues = true;
    public boolean autoanalyze = false;

    public void mapChanged() {
        hover = null;
        cleargraph();
        clearanalysis();
        if (autoanalyze) {
            analyzeMapProblems();
        }
        if (autoregraph) {
            computeGraph();
        }
        draw.repaint();
    }

    public void computeGraph() {
        cleargraph();
        graph = new Graph(map);
        repaint();
    }
    
    public void clearanalysis() {
        smallangles.clear();
        withinRegionIntersections.clear();
        betweenRegionIntersections.clear();
    }

    public void analyzeMapProblems() {
        clearanalysis();

        List<RegionEdge> edges = new ArrayList();

        for (Region r : map.getRegions()) {
            for (Polygon p : r.getParts()) {
                for (int i = 0; i < p.vertexCount(); i++) {
                    edges.add(new RegionEdge(r, p, i));

                    Vector u = p.vertex(i - 1);
                    Vector v = p.vertex(i);
                    Vector w = p.vertex(i + 1);

                    Vector dir1 = Vector.subtract(u, v);
                    Vector dir2 = Vector.subtract(w, v);

                    double angle = Math.abs(dir1.computeSignedAngleTo(dir2, true, true));

                    if (angle < Math.toRadians(2)) {
                        smallangles.add(v);
                    }
                }
            }
        }

        edges.sort((e, f) -> Double.compare(e.start(), f.start()));
        List<RegionEdge> active = new ArrayList();
        for (RegionEdge e : edges) {
            int i = 0;
            while (i < active.size()) {
                RegionEdge a = active.get(i);

                if (e.start() > a.end() + DoubleUtil.EPS) {
                    // remove a
                    if (i == active.size() - 1) {
                        active.remove(i);
                    } else {
                        active.set(i, active.remove(active.size() - 1));
                    }
                } else if (e.r == a.r) {
                    // same region
                    if (e.p == a.p
                            && (Math.abs(e.i - a.i) <= 1
                            || Math.abs(e.i - a.i) >= e.p.vertexCount() - 1)) {
                        // adjacent
                    } else {
                        // not adjacent
                        e.ls().intersect(a.ls(), withinRegionIntersections);
                    }
                    i++;
                } else {
                    // different region
                    e.ls().intersect(a.ls(), betweenRegionIntersections);
                    i++;
                }
            }
            active.add(e);
        }
    }

    private class RegionEdge {

        Region r;
        Polygon p;
        int i;

        public RegionEdge(Region r, Polygon p, int i) {
            this.r = r;
            this.p = p;
            this.i = i;
        }

        LineSegment ls() {
            return p.edge(i);
        }

        double start() {
            return Math.min(ls().getStart().getX(), ls().getEnd().getX());
        }

        double end() {
            return Math.max(ls().getStart().getX(), ls().getEnd().getX());
        }
    }

    public void analyzeProblems() {
        if (graph == null) {
            computeGraph();
        }

        System.out.println("Analyzing");

        smallangles = new ArrayList();
        //intersections = new ArrayList();

        for (Vertex v : graph.getVertices()) {
            v.sortEdges(true);
            for (int i = 0; i < v.getDegree(); i++) {
                Edge ei = v.getEdges().get(i);
                Edge eip1 = v.getEdges().get((i + 1) % v.getDegree());

                Vector dir1 = Vector.subtract(ei.getOtherVertex(v), v);
                Vector dir2 = Vector.subtract(eip1.getOtherVertex(v), v);

                double angle = Math.abs(dir1.computeSignedAngleTo(dir2, true, true));

                if (angle < Math.toRadians(2)) {
                    smallangles.add(v);
                }
            }
        }

        List<Edge> edges = new ArrayList(graph.getEdges());
        edges.sort((e, f) -> Double.compare(
                Math.min(e.getStart().getX(), e.getEnd().getX()),
                Math.min(f.getStart().getX(), f.getEnd().getX())));
        List<Edge> active = new ArrayList();
        for (Edge e : edges) {
            int i = 0;
            double x = Math.min(e.getStart().getX(), e.getEnd().getX());
            while (i < active.size()) {
                Edge a = active.get(i);

                double xx = Math.max(a.getStart().getX(), a.getEnd().getX());
                if (x > xx + DoubleUtil.EPS) {
                    // remove a
                    if (i == active.size() - 1) {
                        active.remove(i);
                    } else {
                        active.set(i, active.remove(active.size() - 1));
                    }
                } else {
                    if (e.getCommonVertex(a) == null) {
                        // no shared vertex, may intersect
                        //     e.toGeometry().intersect(a.toGeometry(), intersections);
                    }
                    i++;
                }
            }
            active.add(e);
        }
//        for (int i = 0; i < graph.getEdges().size(); i++) {
//            Edge ei = graph.getEdges().get(i);
//            for (int j = i + 1; j < graph.getEdges().size(); j++) {
//                Edge ej = graph.getEdges().get(j);
//                if (ei.getCommonVertex(ej) != null) {
//                    continue;
//                }
//
//                ei.toGeometry().intersect(ej.toGeometry(), intersections);
//            }
//        }

        //  System.out.println("  Small angles  : " + smallangles.size());
        //  System.out.println("  Intersections : " + intersections.size());
        repaint();
    }

    public void repaint() {
        if (draw != null) {
            draw.repaint();
        }
    }

    public void cleargraph() {
        graph = null;
        draw.repaint();
    }
}
