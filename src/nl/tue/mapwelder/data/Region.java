/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.tue.mapwelder.data;

import java.awt.Color;
import java.util.Map;
import nl.tue.geometrycore.geometry.Vector;
import nl.tue.geometrycore.geometry.linear.Polygon;
import nl.tue.geometrycore.geometry.linear.Rectangle;
import nl.tue.geometrycore.geometry.mix.GeometryGroup;

/**
 *
 * @author Wouter
 */
public class Region extends GeometryGroup<Polygon> {

    private Color color;
    private String label;
    private Rectangle box = new Rectangle();
    private Map<String, String> aux;

    public Region(String label) {
        this.label = label;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public Map<String, String> getAux() {
        return aux;
    }

    public void setAux(Map<String, String> aux) {
        this.aux = aux;
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
