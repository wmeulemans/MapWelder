/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.tue.mapwelder.data;

import nl.tue.geometrycore.geometry.Vector;
import nl.tue.geometrycore.geometry.linear.Polygon;
import nl.tue.geometrycore.geometry.linear.Rectangle;
import nl.tue.geometrycore.geometry.mix.GeometryGroup;

/**
 *
 * @author Wouter
 */
public class Region extends GeometryGroup<Polygon> {

    private String label;
    private Rectangle box = new Rectangle();

    public Region(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public Rectangle getBox() {
        return box;
    }

    public void updateBox() {
        box.setWidth(-1);
        box.setHeight(-1);
        box.includeGeometry(getParts());
    }

    public Vector centroid() {
        Vector pos = Vector.origin();
        double A = 0;
        for (Polygon p : getParts()) {
            A += p.areaUnsigned();
        }
        for (Polygon p : getParts()) {
            double f = p.areaUnsigned() / A;
            pos.translate(Vector.multiply(f, p.centroid()));
        }
        return pos;
    }

}
