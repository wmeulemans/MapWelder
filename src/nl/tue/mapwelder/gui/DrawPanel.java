package nl.tue.mapwelder.gui;

import java.awt.Color;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import nl.tue.geometrycore.geometry.Vector;
import nl.tue.geometrycore.geometry.linear.Polygon;
import nl.tue.geometrycore.geometry.linear.Rectangle;
import nl.tue.geometrycore.geometryrendering.GeometryPanel;
import nl.tue.geometrycore.geometryrendering.glyphs.PointStyle;
import nl.tue.geometrycore.geometryrendering.styling.Dashing;
import nl.tue.geometrycore.geometryrendering.styling.ExtendedColors;
import nl.tue.geometrycore.geometryrendering.styling.Hashures;
import nl.tue.geometrycore.geometryrendering.styling.SizeMode;
import nl.tue.geometrycore.geometryrendering.styling.TextAnchor;
import nl.tue.mapwelder.data.Graph.Vertex;
import nl.tue.mapwelder.data.Region;
import nl.tue.mapwelder.analyses.Analysis.Problem;
import nl.tue.mapwelder.data.Graph.Edge;
import nl.tue.mapwelder.io.IpeFormat;

/**
 *
 * @author Wouter Meulemans (w.meulemans@tue.nl)
 */
public class DrawPanel extends GeometryPanel {

    private final Data data;

    public DrawPanel(Data data) {
        this.data = data;
    }

    @Override
    protected void drawScene() {
        setSizeMode(SizeMode.VIEW);
        setPointStyle(PointStyle.CIRCLE_WHITE, data.vertex);

        setStroke(Color.BLACK, 1, Dashing.SOLID);
        setAlpha(1);
        setFill(null, Hashures.SOLID);
        draw(data.map.getBox());

        for (Region r : data.map.getRegions()) {
            Color color;
            if (r == data.hover) {
                color = Color.red;
            } else if (r.getColor() != null) {
                color = r.getColor();
            } else {
                color = Color.black;
            }

            if (data.fillalpha > 0 && (!data.fillalphahover || r == data.hover)) {
                setStroke(null, 1, Dashing.SOLID);
                setAlpha(data.fillalpha / 100.0);
                setFill(color, Hashures.SOLID);
                draw(r);
            }

            if (data.boundary > 0 && (!data.boundaryhover || r == data.hover)) {
                setStroke(color, data.boundary, Dashing.SOLID);
                setAlpha(1);
                setFill(null, Hashures.SOLID);
                draw(r.getParts());
            }

            if (data.vertex > 0 && (!data.vertexhover || r == data.hover)) {
                setStroke(color, data.boundary, Dashing.SOLID);
                setAlpha(1);
                setFill(null, Hashures.SOLID);
                for (Polygon p : r.getParts()) {
                    draw(p.vertices());
                }
            }

            if (data.labelsize > 0 && (!data.labelhover || r == data.hover)) {
                if (r.getLabel() != null && r.getLabel().length() > 0) {
                    setStroke(color, 1, Dashing.SOLID);
                    setTextStyle(TextAnchor.CENTER, data.labelsize);
                    setAlpha(1);
                    setFill(color, Hashures.SOLID);
                    draw(r.getBox().center(), r.getLabel());
                }
            }
        }

        if (data.graph != null) {
            setFill(null, Hashures.SOLID);
            setAlpha(1);
            
            for (Edge e : data.graph.getEdges()) {
                switch (e.getMap().size()) {
                    case 1:
                        setStroke(Color.orange, 1, Dashing.SOLID);
                        break;
                    case 2:                        
                        setStroke(Color.green, 1, Dashing.SOLID);
                        break;
                    default:
                        setStroke(Color.red, 1, Dashing.SOLID);
                        break;
                }
                draw(e);
            }

            setPointStyle(PointStyle.CIRCLE_SOLID, data.vertex);

            for (Vertex v : data.graph.getVertices()) {
                switch (v.getDegree()) {
                    case 0:
                        setStroke(Color.black, 2, Dashing.SOLID);
                        draw(v);
                        break;
                    case 1:
                        setStroke(Color.red, 2, Dashing.SOLID);
                        draw(v);
                        break;
                    case 2:
                        //setStroke(Color.green, 2, Dashing.SOLID);   
                        break;
                    case 3:
                        setStroke(Color.blue, 2, Dashing.SOLID);
                        draw(v);
                        break;
                    default:
                    case 4:
                        setStroke(Color.yellow, 2, Dashing.SOLID);
                        draw(v);
                        break;
                }
            }
        }

        setStroke(Color.red, data.boundary + 2, Dashing.SOLID);
        setFill(null, Hashures.SOLID);
        setAlpha(1);
        setPointStyle(PointStyle.SQUARE_SOLID, data.vertex + 2);

        for (Problem p : data.problems) {
            p.render(this);
        }
        data.activeTool.render();
    }

    @Override
    public Rectangle getBoundingRectangle() {
        return data.map.getBox();
    }

    @Override
    protected void mouseWheelMove(Vector loc, int numup, boolean ctrl, boolean shift, boolean alt) {
        if (ctrl || shift || alt) {
            data.activeTool.mouseWheelMove(loc, numup, ctrl, shift, alt);
        }
    }

    @Override
    protected void mousePress(Vector loc, int button, boolean ctrl, boolean shift, boolean alt) {
        if (button != MouseEvent.BUTTON2 || ctrl || shift || alt) {
            data.activeTool.mousePress(loc, button, ctrl, shift, alt);
        }
    }

    @Override
    protected void mouseMove(Vector loc, int button, boolean ctrl, boolean shift, boolean alt) {
        Region newhover = null;
        for (Region r : data.map.getRegions()) {
            if (r.getBox().contains(loc)) {
                for (Polygon p : r.getParts()) {
                    if (p.contains(loc)) {
                        newhover = r;
                        break;
                    }
                }
                if (newhover != null) {
                    break;
                }
            }
        }

        if (newhover != data.hover) {
            data.hover = newhover;
            data.repaint();
        }

        data.activeTool.mouseMove(loc, button, ctrl, shift, alt);
    }

    @Override
    protected void keyPress(int keycode, boolean ctrl, boolean shift, boolean alt) {
        switch (keycode) {
            case KeyEvent.VK_V: {
                if (ctrl) {
                    try {
                        data.map = IpeFormat.fromClipboard();
                        data.mapChanged(true);
                        data.draw.zoomToFit();
                    } catch (IOException ex) {
                        Logger.getLogger(DrawPanel.class.getName()).log(Level.SEVERE, null, ex);
                    }
                } else {
                    data.activeTool.keyPress(keycode, ctrl, shift, alt);
                }
                break;
            }
            case KeyEvent.VK_C: {
                if (ctrl) {
                    try {
                        IpeFormat.toClipboard(data.map);
                    } catch (IOException ex) {
                        Logger.getLogger(DrawPanel.class.getName()).log(Level.SEVERE, null, ex);
                    }
                } else {
                    data.activeTool.keyPress(keycode, ctrl, shift, alt);
                }
                break;
            }
            case KeyEvent.VK_SPACE:
                break;
            case KeyEvent.VK_R:
                data.zoomToRegion(shift ? 1 : ctrl ? -1 : 0);
                break;
            default:
                data.activeTool.keyPress(keycode, ctrl, shift, alt);
        }
    }

    @Override
    protected void mouseDrag(Vector loc, Vector prevloc, int button, boolean ctrl, boolean shift, boolean alt) {
        if (button != MouseEvent.BUTTON2 || ctrl || shift || alt) {
            data.activeTool.mouseDrag(loc, prevloc, button, ctrl, shift, alt);
        }
    }

    @Override
    protected void mouseRelease(Vector loc, int button, boolean ctrl, boolean shift, boolean alt) {
        if (button != MouseEvent.BUTTON2 || ctrl || shift || alt) {
            data.activeTool.mouseRelease(loc, button, ctrl, shift, alt);
        }
    }

}
