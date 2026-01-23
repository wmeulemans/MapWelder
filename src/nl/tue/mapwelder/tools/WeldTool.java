package nl.tue.mapwelder.tools;

import java.awt.Color;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import nl.tue.geometrycore.geometry.CyclicGeometry;
import nl.tue.geometrycore.geometry.Vector;
import nl.tue.geometrycore.geometry.linear.LineSegment;
import nl.tue.geometrycore.geometry.linear.Polygon;
import nl.tue.geometrycore.geometryrendering.styling.Dashing;
import nl.tue.geometrycore.geometryrendering.styling.Hashures;
import nl.tue.geometrycore.util.Pair;
import nl.tue.mapwelder.data.Region;
import nl.tue.mapwelder.gui.Data;

/**
 *
 * @author Wouter Meulemans (w.meulemans@tue.nl)
 */
public class WeldTool extends BrushTool {

    public WeldTool(Data data) {
        super(data, "Welding tool");
    }

    @Override
    public void onSelect() {
        loc = null;
    }

    @Override
    public void onDeselect() {
        loc = null;
    }

    @Override
    public String getDescription() {
        return "All vertices are set to the location that is the mean position of the vertices within the brush. When holding Shift, the clicked location is used instead."
                + "<br/><br/>"
                + "Any edge fully crossing the brush receives a new vertex at the above-mentioned location as well. When holding Control, such edges are not changed."
                + "<br/><br/>"
                + "Press Delete to remove all vertices within the brush.";
    }

    private void weldClick(boolean useclickloc, boolean pulledges) {
        CyclicGeometry placedbrush = getBrush(loc);

        List<Vector> merging = new ArrayList();
        List<Pair<Polygon, Vector>> removes = new ArrayList();
        List<Pair<Polygon, Integer>> pulls = new ArrayList();
        for (Region r : data.map.getRegions()) {
            if (isRegionTouchingBrush(r, placedbrush)) {
                boolean foundsomething = false;
                for (Polygon p : r.getParts()) {
                    for (Vector v : p.vertices()) {
                        if (placedbrush.contains(v)) {
                            if (foundsomething) {
                                removes.add(new Pair(p, v));
                            } else {
                                foundsomething = true;
                            }
                            merging.add(v);
                        }
                    }
                }

                if (!foundsomething && !pulledges) {
                    for (Polygon p : r.getParts()) {
                        for (int i = 0; i < p.edgeCount(); i++) {
                            LineSegment ls = p.edge(i);
                            if (ls.intersect(placedbrush).size() == 2) {
                                pulls.add(new Pair(p, i));
                            }
                        }
                    }
                }
            }
        }

        if (merging.isEmpty()) {
            return;
        }

        Vector newloc;
        if (useclickloc) {
            newloc = loc;
        } else {
            newloc = new Vector(0, 0);
            for (Vector v : merging) {
                newloc.translate(v);
            }
            newloc.scale(1.0 / merging.size());
        }

        for (Vector v : merging) {
            v.set(newloc);
        }

        for (Pair<Polygon, Vector> rem : removes) {
            rem.getFirst().vertices().remove(rem.getSecond());
        }

        for (Pair<Polygon, Integer> pull : pulls) {
            pull.getFirst().addVertex(pull.getSecond() + 1, newloc.clone());
        }

        data.mapChanged(true);
    }

    private void deletePress() {
        CyclicGeometry placedbrush = getBrush(loc);

        for (Region r : data.map.getRegions()) {
            if (isRegionTouchingBrush(r, placedbrush)) {

                Iterator<Polygon> itp = r.getParts().iterator();
                while (itp.hasNext()) {
                    Polygon p = itp.next();
                    Iterator<Vector> it = p.vertices().iterator();
                    while (it.hasNext()) {
                        Vector v = it.next();
                        if (placedbrush.contains(v)) {
                            it.remove();
                        }
                    }

                    if (p.vertexCount() <= 2) {
                        itp.remove();
                    }
                }
            }
        }

        data.mapChanged(true);
    }

    private boolean isRegionTouchingBrush(Region r, CyclicGeometry placedbrush) {
        return r.getBox().contains(loc) || placedbrush.contains(r.getBox().leftBottom()) || placedbrush.intersect(r.getBox()).size() >= 1;
    }

    @Override
    public void render() {
        CyclicGeometry placedbrush = getBrush(loc);

        if (placedbrush != null) {
            data.draw.setStroke(null, 1, Dashing.SOLID);
            data.draw.setAlpha(0.5);
            data.draw.setFill(Color.blue, Hashures.SOLID);
            data.draw.draw(placedbrush);
        }
    }

    @Override
    public void mousePress(Vector loc, int button, boolean ctrl, boolean shift, boolean alt) {
        super.mousePress(loc, button, ctrl, shift, alt);
        if (button == MouseEvent.BUTTON1) {
            weldClick(shift, ctrl);
        }
    }

    @Override
    public void keyPress(int keycode, boolean ctrl, boolean shift, boolean alt) {
        switch (keycode) {
            case KeyEvent.VK_DELETE: {
                deletePress();
                break;
            }
        }
    }

}
