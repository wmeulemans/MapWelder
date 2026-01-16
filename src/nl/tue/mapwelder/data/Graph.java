package nl.tue.mapwelder.data;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
 * @author Wouter Meulemans (w.meulemans@tue.nl)
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
                    pi.map.add(new DefiningMapVertex(r, p, p.vertex(i)));
                    if (pp != pi) {
                        Edge e = pp.getEdgeTo(pi);
                        if (e == null) {
                            e = addEdge(pp, pi, new LineSegment(pp.clone(), pi.clone()));
                        }
                        e.map.add(new DefiningMapPolygon(r, p));
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

    public void mergeVertexWithBacktracing(Vertex v) {
        // NB: maintains graph
        assert v.getDegree() == 2;
        
        Vertex u = v.getEdges().get(0).getOtherVertex(v);
        Vertex w = v.getEdges().get(1).getOtherVertex(v);

        Set<DefiningMapPolygon> polies = new HashSet();
        polies.addAll(v.getEdges().get(0).map);
        polies.addAll(v.getEdges().get(1).map);
        
        removeVertexWithBacktracing(v);
        
        Edge e = addEdge(u, w, new LineSegment(u.clone(), w.clone()));
        e.map.addAll(polies);

    }
    
    public void removeVertexWithBacktracing(Vertex v) {
        // NB: does not maintain graph
        
        quadtree.remove(v);
        removeVertex(v);
        for (DefiningMapVertex dmv : v.map) {
            dmv.polygon.removeVertex(dmv.vertex);
            if (dmv.polygon.vertexCount() <= 2) {                
                dmv.region.getParts().remove(dmv.polygon);
            }
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

        private List<DefiningMapVertex> map = new ArrayList();

        public List<DefiningMapVertex> getDefiningMapVertices() {
            return map;
        }
    }

    public static class Edge extends SimpleEdge<LineSegment, Vertex, Edge> {

        private Set<DefiningMapPolygon> map = new HashSet();

        public Set<DefiningMapPolygon> getMap() {
            return map;
        }

    }

    public static class DefiningMapVertex {

        final public Region region;
        final public Polygon polygon;
        final public Vector vertex;

        public DefiningMapVertex(Region r, Polygon P, Vector v) {
            this.region = r;
            this.polygon = P;
            this.vertex = v;
        }

    }

    public static class DefiningMapPolygon {

        final public Region region;
        final public Polygon polygon;

        public DefiningMapPolygon(Region r, Polygon polygon) {
            this.region = r;
            this.polygon = polygon;
        }
    }
}
