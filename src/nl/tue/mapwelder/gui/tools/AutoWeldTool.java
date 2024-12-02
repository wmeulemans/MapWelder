/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.tue.mapwelder.gui.tools;

import java.awt.Color;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import nl.tue.geometrycore.geometry.CyclicGeometry;
import nl.tue.geometrycore.geometry.Vector;
import nl.tue.geometrycore.geometry.curved.Circle;
import nl.tue.geometrycore.geometry.linear.LineSegment;
import nl.tue.geometrycore.geometry.linear.Polygon;
import nl.tue.geometrycore.geometry.linear.Rectangle;
import nl.tue.geometrycore.geometryrendering.styling.Dashing;
import nl.tue.geometrycore.geometryrendering.styling.Hashures;
import nl.tue.geometrycore.util.DoubleUtil;
import nl.tue.geometrycore.util.Pair;
import nl.tue.mapwelder.data.Region;
import nl.tue.mapwelder.gui.Data;

/**
 *
 * @author Wouter Meulemans (w.meulemans@tue.nl)
 */
public class AutoWeldTool extends BrushTool {

    List<CyclicGeometry> positions = new ArrayList();
    boolean autoapplyOn3 = true;
    boolean autoWeldEndpoints = true;

    public AutoWeldTool(Data data) {
        super(data, "Auto-Welding tool");
    }

    @Override
    public String getDescription() {
        return "Click three points in order along a single shared boundary between two polygons to fuse them together."
                + " Make sure each click contains vertices from both polygons and that the circles do not overlap."
                + "<br/><br/>"
                + "The normal Weld operation is automatically applied to the first and last click as well.";
    }

    @Override
    public void onSelect() {
        loc = null;
        positions.clear();
    }

    @Override
    public void onDeselect() {
        loc = null;
        positions.clear();
    }

    private void weldClick() {
        CyclicGeometry placedbrush = getBrush(loc);
        positions.add(placedbrush);
        if (autoapplyOn3 && positions.size() > 2) {
            enterPress();
        } else {
            data.repaint();
        }
    }

    private void deletePress() {
        positions.remove(positions.size() - 1);
        data.repaint();
    }

    private void escapePress() {
        positions.clear();
        data.repaint();
    }

    private void enterPress() {
        if (positions.size() > 2) {
            Polygon P1 = null, P2 = null;
            List<Vector> r1 = new ArrayList();
            List<Vector> r2 = null;
            mainloop:
            for (Region r : data.map.getRegions()) {
                for (CyclicGeometry cg : positions) {
                    if (!isRegionTouchingBrush(r, cg)) {
                        continue mainloop;
                    }
                }
                nextpolygon:
                for (Polygon part : r.getParts()) {
                    List<List<Integer>> indices = new ArrayList();
                    for (CyclicGeometry cg : positions) {
                        List<Integer> ids = new ArrayList();
                        indices.add(ids);
                        for (int i = 0; i < part.vertexCount(); i++) {
                            if (cg.contains(part.vertex(i))) {
                                ids.add(i);
                            }
                        }
                        if (ids.isEmpty()) {
                            continue nextpolygon;
                        }
                    }

                    int s = indices.get(0).get(0);
                    int m = indices.get(1).get(0);
                    int f = indices.get(2).get(0);
                    // s>m>f or f>m>s? (NB: cyclic...
                    boolean fwd = (s < m && m < f) || (f < s && s < m) || (m < f && f < s);
                    if (fwd) {
                        for (int i = s; i != f; i = (i + 1) % part.vertexCount()) {
                            r1.add(part.vertex(i));
                        }
                    } else {
                        for (int i = s; i != f; i = (i - 1 + part.vertexCount()) % part.vertexCount()) {
                            r1.add(part.vertex(i));
                        }
                    }

                    if (r2 == null) {
                        P2 = part;
                        r2 = r1;
                        r1 = new ArrayList();
                        continue mainloop;
                    } else {
                        P1 = part;
                        break mainloop;
                    }
                }
            }

            if (r1.size() > 0 && r2.size() > 0) {
                // sqr-dtw
                int n = r1.size();
                int m = r2.size();
                double[][] dtw = new double[n][m];
                int[][] prev = new int[n][m];

                dtw[0][0] = r1.get(0).squaredDistanceTo(r2.get(0));
                for (int i = 1; i < n; i++) {
                    dtw[i][0] = dtw[i - 1][0] + r1.get(i).squaredDistanceTo(r2.get(0));
                    prev[i][0] = -1;
                }
                for (int j = 1; j < m; j++) {
                    dtw[0][j] = dtw[0][j - 1] + r1.get(0).squaredDistanceTo(r2.get(j));
                    prev[0][j] = 1;
                }

                for (int i = 1; i < n; i++) {
                    for (int j = 1; j < m; j++) {
                        double d = r1.get(i).squaredDistanceTo(r2.get(j));
                        if (dtw[i][j - 1] <= dtw[i - 1][j - 1] - DoubleUtil.EPS && dtw[i][j - 1] <= dtw[i - 1][j]) {
                            dtw[i][j] = dtw[i][j - 1] + d;
                            prev[i][j] = 1;
                        } else if (dtw[i - 1][j] <= dtw[i - 1][j - 1] - DoubleUtil.EPS && dtw[i - 1][j] <= dtw[i][j - 1]) {
                            dtw[i][j] = dtw[i - 1][j] + d;
                            prev[i][j] = -1;
                        } else {
                            dtw[i][j] = dtw[i - 1][j - 1] + d;
                            prev[i][j] = 0;
                        }
                    }
                }

                int w1 = n - 1, w2 = m - 1;
                Set<Vector> v1s = new HashSet();
                Set<Vector> v2s = new HashSet();
                while (w1 > 0 || w2 > 0) {
                    v1s.add(r1.get(w1));
                    v2s.add(r2.get(w2));
                    switch (prev[w1][w2]) {
                        case 0:
                            match(v1s, v2s);
                            w1--;
                            w2--;
                            break;
                        case -1:
                            w1--;
                            break;
                        default:
                            w2--;
                            break;
                    }
                }
                if (!v1s.isEmpty()) {
                    match(v1s, v2s);
                }

                int i = 0;
                while (i < P1.vertexCount()) {
                    if (P1.vertex(i).isApproximately(P1.vertex(i - 1))) {
                        P1.vertices().remove(i);
                    } else {
                        i++;
                    }
                }
                int j = 0;
                while (j < P2.vertexCount()) {
                    if (P2.vertex(j).isApproximately(P2.vertex(j - 1))) {
                        P2.vertices().remove(j);
                    } else {
                        j++;
                    }
                }

                if (autoWeldEndpoints) {
                    weldClick(positions.get(0), true);
                    weldClick(positions.get(positions.size() - 1), true);
                }

                data.mapChanged();
            }
        }
        positions.clear();
        data.repaint();
    }

    private void weldClick(CyclicGeometry placedbrush, boolean pulledges) {

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
        newloc = new Vector(0, 0);
        for (Vector v : merging) {
            newloc.translate(v);
        }
        newloc.scale(1.0 / merging.size());

        for (Vector v : merging) {
            v.set(newloc);
        }

        for (Pair<Polygon, Vector> rem : removes) {
            rem.getFirst().vertices().remove(rem.getSecond());
        }

        for (Pair<Polygon, Integer> pull : pulls) {
            pull.getFirst().addVertex(pull.getSecond() + 1, newloc.clone());
        }
    }

    private void match(Set<Vector> v1s, Set<Vector> v2s) {
        if (v1s.size() > 1 && v2s.size() > 1) {
            System.err.println("No diagonal?");
        }
        Vector avg = Vector.origin();
        for (Vector v : v1s) {
            avg.translate(v);
        }
        for (Vector v : v2s) {
            avg.translate(v);
        }
        avg.scale(1.0 / (v1s.size() + v2s.size()));

        for (Vector v : v1s) {
            v.set(avg);
        }
        for (Vector v : v2s) {
            v.set(avg);
        }
        v1s.clear();
        v2s.clear();
    }

    private boolean isRegionTouchingBrush(Region r, CyclicGeometry placedbrush) {
        return // box contains the center of the brush
                r.getBox().contains(Rectangle.byBoundingBox(placedbrush).center())
                // brush fully contains box
                || placedbrush.contains(r.getBox().leftBottom())
                // boundaries intersect
                || placedbrush.intersect(r.getBox()).size() >= 1;
    }

    @Override
    public void render() {

        for (CyclicGeometry cg : positions) {
            data.draw.setStroke(null, 1, Dashing.SOLID);
            data.draw.setAlpha(0.5);
            data.draw.setFill(Color.red, Hashures.SOLID);
            data.draw.draw(cg);
        }

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
            weldClick();
        }
    }

    @Override
    public void mouseMove(Vector loc, int button, boolean ctrl, boolean shift, boolean alt) {
        super.mouseMove(loc, button, ctrl, shift, alt);
    }

    @Override
    public void keyPress(int keycode, boolean ctrl, boolean shift, boolean alt) {
        switch (keycode) {
            case KeyEvent.VK_DELETE: {
                deletePress();
                break;
            }
            case KeyEvent.VK_ESCAPE: {
                escapePress();
                break;
            }
            case KeyEvent.VK_ENTER: {
                enterPress();
                break;
            }
        }
    }

}
