/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.tue.mapwelder.gui.tools;

import javax.swing.ButtonGroup;
import nl.tue.geometrycore.geometry.CyclicGeometry;
import nl.tue.geometrycore.geometry.Vector;
import nl.tue.geometrycore.geometry.curved.Circle;
import nl.tue.geometrycore.geometry.linear.Polygon;
import nl.tue.geometrycore.geometry.linear.Rectangle;
import nl.tue.geometrycore.gui.sidepanel.SideTab;
import nl.tue.mapwelder.gui.Data;

/**
 *
 * @author Wouter Meulemans (w.meulemans@tue.nl)
 */
public abstract class BrushTool extends Tool {

    private static class BrushType {

        final String name;
        final CyclicGeometry geometry;

        public BrushType(String name, CyclicGeometry geometry) {
            this.name = name;
            this.geometry = geometry;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    private static BrushType[] brushes = {
        new BrushType("Circle", new Circle(Vector.origin(), 1)),
        new BrushType("Square", new Rectangle(-1, 1, -1, 1)),
        new BrushType("Diamond", new Polygon(Vector.left(), Vector.down(), Vector.right(), Vector.up()))
    };

    private BrushType brush;
    private double size = 25;

    public BrushTool(Data data, String name) {
        super(data, name);
        brush = brushes[0];
    }

    @Override
    public void createGUI(SideTab tab) {
        super.createGUI(tab);

        tab.addRadioButtonList(brushes, brush, (e, v) -> {
            brush = v;
            data.repaint();
        });
    }

    public CyclicGeometry getBrush(Vector loc) {
        if (brush == null || loc == null) {
            return null;
        }

        CyclicGeometry placed = (CyclicGeometry) brush.geometry.clone();
        placed.scale(size / data.draw.convertWorldToView(1));
        placed.translate(loc);
        return placed;
    }

    @Override
    public void mouseWheelMove(Vector loc, int numup, boolean ctrl, boolean shift, boolean alt) {
        super.mouseWheelMove(loc, numup, ctrl, shift, alt);
        if (shift) {
            size = Math.max(size + numup, 1);
            data.repaint();
        }
    }

}
