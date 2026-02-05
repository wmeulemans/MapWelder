package nl.tue.mapwelder.tools;

import java.awt.Color;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import nl.tue.geometrycore.geometry.Vector;
import nl.tue.geometrycore.geometry.linear.LineSegment;
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

    Region focus = null;

    double snap_distance = 10;

    public VertexTool(Data data) {
        super(data, "Vertex manipulation");
    }

    @Override
    public String getDescription() {
        return "Click to select a single vertex, and drag to move it."
                + "<br/><br/>"
                + "Clicking near an edge will introduce a vertex along that edge."
                + "<br/><br/>"
                + "Press Delete to remove the vertex completely."
                + "<br/><br/>"
                + "Pressing F will set the hovered region as the focus region. Selections and edits will only be done on the focus region. Press Shift+F to clear the focus region.";
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
                if (region.getParts().isEmpty()) {
                    data.map.getRegions().remove(region);
                }
            }

            region = null;
            point = null;
            polygon = null;

            data.mapChanged(true);
        }
    }

    private void setFocus() {
        focus = data.hover;
        data.repaint();
    }

    private void clearFocus() {
        focus = null;
        data.repaint();
    }

    @Override
    public void render() {
        if (focus != null) {
            data.draw.setStroke(Color.blue, data.polygon_edge_width / 10 + 1, Dashing.SOLID);
            data.draw.draw(focus);
        }

        if (point != null) {
            data.draw.setStroke(Color.blue, 2, Dashing.SOLID);
            data.draw.setPointStyle(PointStyle.SQUARE_SOLID, data.polygon_vertex_radius / 10 + 2);
            data.draw.draw(point);
        }
    }

    @Override
    public void mousePress(Vector loc, int button, boolean ctrl, boolean shift, boolean alt) {
        if (button != MouseEvent.BUTTON1) {
            point = null;
            return;
        }

        point = null;
        double nearest = data.draw.convertViewToWorld(snap_distance);

        if (focus == null) {
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

            if (point == null) {
                int edge = -1;
                for (Region r : data.map.getRegions()) {
                    if (r.getBox().contains(loc) || r.getBox().distanceTo(loc) < nearest) {
                        for (Polygon p : r.getParts()) {
                            for (int ei = 0; ei < p.edgeCount(); ei++) {
                                LineSegment e = p.edge(ei);
                                if (e.distanceTo(loc) < nearest) {
                                    region = r;
                                    polygon = p;
                                    edge = ei;
                                    nearest = e.distanceTo(loc);
                                }
                            }
                        }
                    }
                }

                if (edge >= 0) {
                    point = loc.clone();
                    polygon.addVertex(edge + 1, point);
                    region.updateBox();
                }
            }
        } else if (focus.getBox().contains(loc) || focus.getBox().distanceTo(loc) < nearest) {
            for (Polygon p : focus.getParts()) {
                for (Vector v : p.vertices()) {
                    if (v.distanceTo(loc) < nearest) {
                        region = focus;
                        polygon = p;
                        point = v;
                        nearest = v.distanceTo(loc);
                    }
                }
            }

            if (point == null) {
                int edge = -1;
                for (Polygon p : focus.getParts()) {
                    for (int ei = 0; ei < p.edgeCount(); ei++) {
                        LineSegment e = p.edge(ei);
                        if (e.distanceTo(loc) < nearest) {
                            region = focus;
                            polygon = p;
                            edge = ei;
                            nearest = e.distanceTo(loc);
                        }
                    }
                }

                if (edge >= 0) {
                    point = loc.clone();
                    polygon.addVertex(edge + 1, point);
                    region.updateBox();
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
            case KeyEvent.VK_F:
                if (shift) {
                    clearFocus();
                } else {
                    setFocus();
                }
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
            region.updateBox();
            data.mapChanged(true);
        }
    }

}
