/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.tue.mapwelder.io;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import nl.tue.geometrycore.geometry.Vector;
import nl.tue.geometrycore.geometry.linear.Polygon;
import nl.tue.geometrycore.geometry.linear.Rectangle;
import nl.tue.geometrycore.gui.sidepanel.SideTab;
import nl.tue.geometrycore.util.ClipboardUtil;
import nl.tue.mapwelder.data.DCEL;
import nl.tue.mapwelder.data.PlaneMap;
import nl.tue.mapwelder.data.Region;
import nl.tue.mapwelder.gui.Data;

/**
 *
 * @author Wouter Meulemans (w.meulemans@tue.nl)
 */
public class DecomposedJsonFormat extends Format {

    private boolean polygonmode = false;
    private boolean fittobox = true;

    public DecomposedJsonFormat(Data data) {
        super(data, "Decomposed JSON", "json");
    }

    @Override
    protected PlaneMap load(File file) throws IOException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected void save(File file, PlaneMap map) throws IOException {
        toFile(file, map, polygonmode, fittobox);
    }

    @Override
    protected boolean canLoad() {
        return false;
    }

    @Override
    protected boolean canSave() {
        return true;
    }

    @Override
    public void createGUI(SideTab tab) {
        super.createGUI(tab);

        tab.addCheckbox("Polygon mode", polygonmode, (e, v) -> polygonmode = v);
        tab.addCheckbox("Fit to Box", fittobox, (e, v) -> fittobox = v);
    }

    public static void toClipboard(PlaneMap map, boolean polygonmode, boolean fittobox) throws IOException {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final String utf8 = StandardCharsets.UTF_8.name();
        try (PrintStream ps = new PrintStream(baos, true, utf8)) {
            write(ps, map, polygonmode, fittobox);
            ClipboardUtil.setClipboardContents(baos.toString(utf8));
        }
    }

    public static void toFile(File file, PlaneMap map, boolean polygonmode, boolean fittobox)throws IOException  {
        try (PrintStream write = new PrintStream(file)) {
            write(write, map, polygonmode, fittobox);
        } 
    }

    private static void write(PrintStream out, PlaneMap map, boolean polygonmode, boolean fittobox) throws IOException {

        boolean flipvertical = true;
        Rectangle fitToBox = new Rectangle(10, 490, 10, 490);

        if (fittobox) {
            Rectangle bb = Rectangle.byBoundingBox(map.getRegions());
            bb.growToAspectRatio(fitToBox);

            Vector bbcenter = bb.center();
            double scale = fitToBox.width() / bb.width();
            Vector delta = Vector.subtract(fitToBox.center(), bbcenter);

            Rectangle mb = map.getBox().clone();
            mb.scale(scale, flipvertical ? -scale : scale, bbcenter);
            mb.translate(delta);
            PlaneMap map2 = new PlaneMap(mb);

            for (Region r : map.getRegions()) {
                Region r2 = new Region(r.getLabel());
                r2.updateBox();
                for (Polygon p : r.getParts()) {
                    Polygon p2 = p.clone();
                    p2.scale(scale, flipvertical ? -scale : scale, bbcenter);
                    p2.translate(delta);
                    r2.getParts().add(p2);
                }
                map2.getRegions().add(r2);
            }

            map = map2;
        }

        if (polygonmode) {
            out.println("{");
            out.println("\"polylines\": [],");

            int polynum = 0;
            out.println("\"polygons\": [");
            Map<Region, List<Integer>> nums = new HashMap();
            for (Region r : map.getRegions()) {
                List<Integer> num = new ArrayList();
                nums.put(r, num);
                for (Polygon p : r.getParts()) {
                    if (polynum > 0) {
                        out.println(",");
                    }
                    out.print("[");

                    boolean first = true;
                    for (Vector v : p.vertices()) {
                        if (!first) {
                            out.print(",");
                        }
                        first = false;
                        out.print("[" + v.getX() + "," + v.getY() + "]");
                    }
                    out.print("]");
                    num.add(polynum);
                    polynum++;
                }
            }
            if (polynum > 0) {
                out.println("");
            }
            out.println("],");

            out.println("\"regions\": [");

            for (Region r : map.getRegions()) {
                out.print("{ \"name\": \"" + r.getLabel() + "\", \"parts\": [");
                boolean first = true;

                for (Integer num : nums.get(r)) {
                    if (!first) {
                        out.print(",");
                    }
                    first = false;
                    out.print("[{ \"neighbor\": " + null
                            + ", \"polygon\": " + num
                            + ", \"reversed\": false }]");
                }
                out.println("");

                if (r != map.getRegions().get(map.getRegions().size() - 1)) {
                    out.println("]},");
                } else {
                    out.println("]}");
                }
            }
            out.println("]");
            out.println("}");
            return;
        }

        DCEL dcel = new DCEL(map);

        out.println("{");
        Map<DCEL.Dart, Integer> chainback = new HashMap();
        Map<DCEL.Dart, Integer> chainfwd = new HashMap();
        int chainnum = 0;
        out.println("\"polylines\": [");
        for (DCEL.Dart d : dcel.getDarts()) {
            if (d.isMarked()) {
                // skip
            } else {
                DCEL.Dart walk = d;
                while (walk.getOrigin().getDegree() == 2) {
                    walk = walk.getPrevious();
                    assert !walk.isMarked();
                    if (walk == d) {
                        break;
                    }
                }

                if (walk.getOrigin().getDegree() == 2) {
                    // island...
                    continue;
                }

                if (chainnum > 0) {
                    out.println(",");
                }

                List<Vector> tmp = new ArrayList();
                tmp.add(walk.getOrigin());
                out.print("[[" + walk.getOrigin().getX() + "," + walk.getOrigin().getY() + "]");

                chainback.put(walk.getTwin(), chainnum);
                chainfwd.put(walk, chainnum);

                tmp.add(walk.getDestination());
                out.print(",[" + walk.getDestination().getX() + "," + walk.getDestination().getY() + "]");

                walk.setMarked(true);
                walk.getTwin().setMarked(true);

                DCEL.Dart startwalk = walk;

                while (walk.getDestination().getDegree() == 2 && walk != startwalk.getPrevious()) {
                    walk = walk.getNext();

                    assert !walk.isMarked();

                    tmp.add(walk.getDestination());
                    out.print(",[" + walk.getDestination().getX() + "," + walk.getDestination().getY() + "]");

                    walk.setMarked(true);
                    walk.getTwin().setMarked(true);
                }

                out.print("]");
                chainnum++;
            }
        }
        if (chainnum > 0) {
            out.println("");
        }
        out.println("],");

        Map<DCEL.Dart, Integer> polyback = new HashMap();
        Map<DCEL.Dart, Integer> polyfwd = new HashMap();
        int polynum = 0;
        out.println("\"polygons\": [");
        for (DCEL.Dart d : dcel.getDarts()) {
            if (d.isMarked()) {
                // skip
            } else {

                DCEL.Dart walk = d;
                while (walk.getOrigin().getDegree() == 2) {
                    walk = walk.getPrevious();
                    if (walk == d) {
                        break;
                    }
                }

                if (walk.getOrigin().getDegree() != 2) {
                    // not an island
                    continue;
                }

                // walk == d...
                if (polynum > 0) {
                    out.println(",");
                }

                List<Vector> tmp = new ArrayList();
                tmp.add(walk.getOrigin());

                out.print("[[" + walk.getOrigin().getX() + "," + walk.getOrigin().getY() + "]");

                polyback.put(walk.getTwin(), polynum);
                polyfwd.put(walk, polynum);

                walk.setMarked(true);
                walk.getTwin().setMarked(true);

                walk = walk.getNext();
                do {
                    tmp.add(walk.getOrigin());
                    out.print(",[" + walk.getOrigin().getX() + "," + walk.getOrigin().getY() + "]");

                    walk.setMarked(true);
                    walk.getTwin().setMarked(true);

                    walk = walk.getNext();
                } while (walk != d);

                out.print("]");
                polynum++;
            }
        }
        if (polynum > 0) {
            out.println("");
        }
        out.println("],");

        out.println("\"regions\": [");

        for (Region region : map.getRegions()) {
            String name = region.getLabel();
            out.print("{ \"name\": \"" + name + "\", \"parts\": [");
            boolean firstface = true;

            List<DCEL.Face> faces = dcel.regionToFaces.get(region);
            if (faces.isEmpty()) {
                System.err.println("Empty geometry for " + name);
            }

            for (DCEL.Face f : faces) {
                if (firstface) {
                    out.print("[");
                    firstface = false;
                } else {
                    out.print(",[");
                }
                DCEL.Dart walk = f.getDart();
                boolean first = true;
                do {
                    String neighbor;
                    DCEL.Face twinface = walk.getTwin().getFace();
                    Region nbr = twinface.region; // TODO: handle enclaves properly
                    if (nbr == null) {
                        neighbor = "null";
                    } else {
                        int neighborindex = map.getRegions().indexOf(nbr);
                        int partindex = dcel.regionToFaces.get(nbr).indexOf(twinface);
                        neighbor = "[" + neighborindex + "," + partindex + "]";
                    }

                    boolean reversed;
                    String geom;

                    if (chainback.containsKey(walk)) {
                        geom = "\"polyline\": " + chainback.get(walk);
                        reversed = true;
                    } else if (chainfwd.containsKey(walk)) {
                        geom = "\"polyline\": " + chainfwd.get(walk);
                        reversed = false;
                    } else if (polyback.containsKey(walk)) {
                        geom = "\"polygon\": " + polyback.get(walk);
                        reversed = true;
                    } else if (polyfwd.containsKey(walk)) {
                        geom = "\"polygon\": " + polyfwd.get(walk);
                        reversed = false;
                    } else {
                        geom = null;
                        reversed = false;
                    }

                    if (geom != null) {
                        out.print((first ? "" : ",")
                                + "{ \"neighbor\": " + neighbor
                                + ", " + geom
                                + ", \"reversed\": " + reversed + "}");
                        first = false;
                    }

                    walk = walk.getNext();
                } while (walk != f.getDart());

                out.print("]");
            }
            if (map.getRegions().indexOf(region) < map.getRegions().size() - 1) {
                out.println("]},");
            } else {
                out.println("]}");
            }
        }
        out.println("]");
        out.println("}");
    }
}
