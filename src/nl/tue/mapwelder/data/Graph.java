/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.tue.mapwelder.data;

import nl.tue.geometrycore.datastructures.quadtree.QuadTree;
import nl.tue.geometrycore.geometry.OrientedGeometry;
import nl.tue.geometrycore.geometry.linear.LineSegment;
import nl.tue.geometrycore.graphs.GraphConstruction;
import nl.tue.geometrycore.graphs.simple.SimpleEdge;
import nl.tue.geometrycore.graphs.simple.SimpleGraph;
import nl.tue.geometrycore.graphs.simple.SimpleVertex;
import nl.tue.mapwelder.data.Graph.Edge;
import nl.tue.mapwelder.data.Graph.Vertex;

/**
 *
 * @author Wouter
 */
public class Graph extends SimpleGraph<LineSegment, Vertex, Edge> {

    public Graph(PlaneMap map) {
        QuadTree<Vertex> quadtree = new QuadTree(map.getBox(), 10);
        GraphConstruction.convertGeometriesToGraph(this, map.getRegions(),
                (OrientedGeometry geometry) -> new LineSegment(geometry.getStart().clone(), geometry.getEnd().clone()),
                quadtree);
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
    }

    public static class Edge extends SimpleEdge<LineSegment, Vertex, Edge> {
    }
}
