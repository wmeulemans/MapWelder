/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.tue.mapwelder.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import nl.tue.geometrycore.geometry.Vector;
import nl.tue.geometrycore.geometry.linear.Polygon;
import nl.tue.geometrycore.geometry.linear.Rectangle;
import nl.tue.geometrycore.gui.sidepanel.SideTab;
import nl.tue.mapwelder.data.PlaneMap;
import nl.tue.mapwelder.data.Region;
import nl.tue.mapwelder.gui.Data;

/**
 *
 * @author Wouter Meulemans (w.meulemans@tue.nl)
 */
public class GeojsonFormat extends Format {

    private String label = "label";

    public GeojsonFormat(Data data) {
        super(data, "GEOJSON");
    }

    @Override
    public void createGUI(SideTab tab) {
        super.createGUI(tab);
        tab.addLabel("ID label");
        tab.addTextField(label, (e, v) -> label = v);

    }
    @Override
    protected PlaneMap load(File file) throws IOException {
        return readFile(file, label);
    }


    @Override
    protected void save(File file, PlaneMap map) throws IOException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected boolean canLoad() {
        return true;
    }

    @Override
    protected boolean canSave() {
        return false;
    }


    public static PlaneMap readFile(File file, String label) throws IOException {
        try (BufferedReader read = new BufferedReader(new FileReader(file))) {
            return read(read, label);
        }
    }

    private static PlaneMap read(BufferedReader read, String label) throws IOException {

        PlaneMap map = new PlaneMap(new Rectangle());

        String line = read.readLine();
        String LI = "\"" + label + "\":";
        String CI = "\"coordinates\":";
        while (line != null) {
            if (line.contains("\"type\":\"Feature\"")) {

                int cs = line.indexOf(CI) + CI.length();
                int ce = line.indexOf("}", cs);
                String coords = line.substring(cs, ce);
                if (!coords.startsWith("[[[[")) {
                    coords = "[" + coords + "]";
                }

                int ls = line.indexOf(LI) + LI.length() + 1;
                int le = line.indexOf("\"", ls + 1);
                Region R = new Region(line.substring(ls, le));
                map.getRegions().add(R);
                parseMultiPolygon(R, coords);
                R.updateBox();
            }

            line = read.readLine();
        }
        map.getBox().includeGeometry(map.getRegions());

        return map;
    }

    private static void parseMultiPolygon(Region R, String coords) {
        String[] polies = coords.split("\\]\\]\\],\\[\\[\\[");
        for (String poly : polies) {
            String[] xys = poly.split(",");
            Polygon P = new Polygon();
            R.getParts().add(P);
            for (int i = 0; i < xys.length; i++) {
                xys[i] = xys[i].replaceAll("\\]", "").replaceAll("\\[", "");
            }
            for (int i = 0; i < xys.length; i += 2) {
                double x = Double.parseDouble(xys[i]);
                double y = Double.parseDouble(xys[i + 1]);
                Vector v = new Vector(x, y);
                if (P == null) {
                    P = new Polygon();
                    R.getParts().add(P);
                    P.addVertex(v);
                } else if (P.vertexCount() > 0 && v.isApproximately(P.vertex(0))) {
                    // start a new poly?
                    P = null;
                } else {
                    P.addVertex(v);
                }
            }
        }
//        if (polies.length > 1 || R.getParts().size() > 1) {
//            System.out.println("coords: " + coords);
//            for (Polygon P : R.getParts()) {
//                System.out.println("> " + P);
//            }
//        }
    }
}
