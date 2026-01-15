/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.tue.mapwelder.data;

import java.util.ArrayList;
import java.util.List;
import nl.tue.geometrycore.datastructures.quadtree.PointQuadTree;
import nl.tue.geometrycore.geometry.Vector;
import nl.tue.geometrycore.geometry.linear.LineSegment;
import nl.tue.geometrycore.geometry.linear.Polygon;
import nl.tue.geometrycore.geometry.linear.Rectangle;
import nl.tue.geometrycore.graphs.simple.SimpleEdge;
import nl.tue.geometrycore.graphs.simple.SimpleGraph;
import nl.tue.geometrycore.graphs.simple.SimpleVertex;
import nl.tue.geometrycore.util.DoubleUtil;
import nl.tue.mapwelder.data.Graph.Edge;
import nl.tue.mapwelder.data.Graph.Vertex;

/**
 *
 * @author Wouter
 */
public class Graph extends SimpleGraph<LineSegment, Vertex, Edge> {

    private final PointQuadTree<Vertex> quadtree;

    public Graph(PlaneMap map) {
        quadtree = new PointQuadTree(map.getBox(), 18);

        for (Region r : map.getRegions()) {
            for (Polygon p : r.getParts()) {
                Vertex pp = getOrAdd(p.vertex(-1));
                for (int i = 0; i < p.vertexCount(); i++) {
                    Vertex pi = getOrAdd(p.vertex(i));
                    pi.map.add(new MapOccurrance(p, p.vertex(i)));
                    if (pp != pi && !pp.isNeighborOf(pi)) {
                        addEdge(pp, pi, new LineSegment(pp.clone(), pi.clone()));
                    }
                    pp = pi;
                }
            }
        }
    }

    public Vertex getOrAdd(Vector v) {
        Vertex r = quadtree.find(v, DoubleUtil.EPS);
        if (r != null) {
            return r;
        }
        r = addVertex(v);
        quadtree.insert(r);
        return r;
    }

    public List<Vertex> find(Rectangle R) {
        return quadtree.findContained(R, 10 * DoubleUtil.EPS);
    }

    public void removeWithBacktracing(Vertex v) {
        assert v.getDegree() == 2;

        Vertex u = v.getEdges().get(0).getOtherVertex(v);
        Vertex w = v.getEdges().get(1).getOtherVertex(v);

        quadtree.remove(v);
        removeVertex(v);
        addEdge(u, w, new LineSegment(u.clone(), w.clone()));

        for (MapOccurrance mo : v.map) {
            mo.P.removeVertex(mo.v);
        }
    }

    @Override
    public Vertex createVertex() {
        return new Vertex();
    }

    @Override
    public Edge createEdge() {
        return new Edge();
    }

    public static class Vertex extends SimpleVertex<LineSegment, Vertex, Edge> {

        private List<MapOccurrance> map = new ArrayList();
    }

    public static class Edge extends SimpleEdge<LineSegment, Vertex, Edge> {
    }

    public static class MapOccurrance {

        final Polygon P;
        final Vector v;

        public MapOccurrance(Polygon P, Vector v) {
            this.P = P;
            this.v = v;
        }

    }
}
