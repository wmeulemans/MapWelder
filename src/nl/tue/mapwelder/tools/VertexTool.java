package nl.tue.mapwelder.tools;

import java.awt.Color;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import nl.tue.geometrycore.geometry.Vector;
import nl.tue.geometrycore.geometry.linear.Polygon;
import nl.tue.geometrycore.geometryrendering.glyphs.PointStyle;
import nl.tue.geometrycore.geometryrendering.styling.Dashing;
import nl.tue.mapwelder.data.Region;
import nl.tue.mapwelder.gui.Data;

/**
 *
 * @author Wouter Meulemans (w.meulemans@tue.nl)
 */
public class VertexTool extends Tool {

    Region region = null;
    Polygon polygon = null;
    Vector point = null;

    public VertexTool(Data data) {
        super(data, "Vertex manipulation");
    }

    @Override
    public String getDescription() {
        return "Click to select a single vertex, and drag to move it."
                + "<br/><br/>"
                + "Press Delete to remove the vertex completely.";
    }

    @Override
    public void onSelect() {
        region = null;
        point = null;
        polygon = null;
    }

    @Override
    public void onDeselect() {
        region = null;
        point = null;
        polygon = null;
    }

    private void deletePress() {
        if (point != null) {
            polygon.removeVertex(point);
            if (polygon.vertexCount() <= 2) {
                region.getParts().remove(polygon);
            }

            region = null;
            point = null;
            polygon = null;

            data.mapChanged(true);
        }
    }

    @Override
    public void render() {
        if (point != null) {
            data.draw.setStroke(Color.blue, 2, Dashing.SOLID);
            data.draw.setPointStyle(PointStyle.SQUARE_SOLID, data.vertex + 3);
            data.draw.draw(point);
        }
    }

    @Override
    public void mouseWheelMove(Vector loc, int numup, boolean ctrl, boolean shift, boolean alt) {

    }

    @Override
    public void mousePress(Vector loc, int button, boolean ctrl, boolean shift, boolean alt) {
        if (button != MouseEvent.BUTTON1) {
            point = null;
            return;
        }

        point = null;
        double nearest = data.draw.convertViewToWorld(50);

        for (Region r : data.map.getRegions()) {
            if (r.getBox().contains(loc) || r.getBox().distanceTo(loc) < nearest) {
                for (Polygon p : r.getParts()) {
                    for (Vector v : p.vertices()) {
                        if (v.distanceTo(loc) < nearest) {
                            region = r;
                            polygon = p;
                            point = v;
                            nearest = v.distanceTo(loc);
                        }
                    }
                }
            }
        }

        data.repaint();
    }

    @Override
    public void mouseMove(Vector loc, int button, boolean ctrl, boolean shift, boolean alt) {

    }

    @Override
    public void keyPress(int keycode, boolean ctrl, boolean shift, boolean alt) {
        switch (keycode) {
            case KeyEvent.VK_DELETE:
                deletePress();
                break;
        }
    }

    @Override
    public void mouseDrag(Vector loc, Vector prevloc, int button, boolean ctrl, boolean shift, boolean alt) {
        if (point != null && button == MouseEvent.BUTTON1) {
            point.set(loc);
            data.repaint();
        }
    }

    @Override
    public void mouseRelease(Vector loc, int button, boolean ctrl, boolean shift, boolean alt) {
        if (point != null && button == MouseEvent.BUTTON1) {
            point.set(loc);
            data.mapChanged(true);
        }
    }

}
