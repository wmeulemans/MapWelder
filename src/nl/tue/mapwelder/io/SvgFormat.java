/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.tue.mapwelder.io;

import java.awt.Color;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JTextField;
import nl.tue.geometrycore.geometry.BaseGeometry;
import nl.tue.geometrycore.geometry.GeometryType;
import nl.tue.geometrycore.geometry.linear.Polygon;
import nl.tue.geometrycore.geometry.linear.Rectangle;
import nl.tue.geometrycore.geometry.mix.GeometryGroup;
import nl.tue.geometrycore.geometryrendering.styling.Dashing;
import nl.tue.geometrycore.geometryrendering.styling.Hashures;
import nl.tue.geometrycore.geometryrendering.styling.SizeMode;
import nl.tue.geometrycore.gui.sidepanel.SideTab;
import nl.tue.geometrycore.io.ReadItem;
import nl.tue.geometrycore.io.svg.SVGReader;
import nl.tue.geometrycore.io.svg.SVGWriter;
import nl.tue.mapwelder.data.PlaneMap;
import nl.tue.mapwelder.data.Region;
import nl.tue.mapwelder.gui.Data;

/**
 *
 * @author Wouter Meulemans (w.meulemans@tue.nl)
 */
public class SvgFormat extends Format {

    private String label = "label";

    public SvgFormat(Data data) {
        super(data, "SVG");
    }

    @Override
    protected PlaneMap load(File file) throws IOException{
        return fromFile(file, label);
    }
    

    @Override
    protected void save(File file, PlaneMap map) throws IOException {
        toFile(file, map, label);
    }

    @Override
    protected boolean canLoad() {
        return true;
    }

    @Override
    protected boolean canSave() {
        return true;
    }

    @Override
    public void createGUI(SideTab tab) {
        super.createGUI(tab);
        tab.addLabel("ID label");
        tab.addTextField(label, (e, v) -> label = v);
    }

    public static PlaneMap fromClipboard(String label) throws IOException {
        return read(SVGReader.clipboardReader(), label);
    }

    public static PlaneMap fromFile(File file, String label) throws IOException {
        return read(SVGReader.fileReader(file), label);
    }

    private static PlaneMap read(SVGReader read, String label) throws IOException {
        List<ReadItem> items = read.read();
        Rectangle box = read.getViewBox();
        read.close();

        PlaneMap map = new PlaneMap(box);

        for (ReadItem item : items) {

            if (item.getAuxiliary() == null || !item.getAuxiliary().containsKey(label)) {
                continue;
            }

            Region r = new Region(item.getAuxiliary().get(label));
            map.getRegions().add(r);

            BaseGeometry geom = item.getGeometry();
            switch (geom.getGeometryType()) {
                case POLYGON:
                    Polygon p = (Polygon) geom;
                    r.getParts().add(p);
                    break;
                case GEOMETRYGROUP:
                    for (BaseGeometry part : ((GeometryGroup<? extends BaseGeometry>) geom).getParts()) {
                        if (part.getGeometryType() == GeometryType.POLYGON) {
                            r.getParts().add((Polygon) part);
                        }
                    }
                    break;
            }

            r.updateBox();
        }

        return map;
    }

    public static void toClipboard(PlaneMap map, String label) throws IOException {
        try (SVGWriter write = SVGWriter.clipboardWriter()) {
            write(write, map, label);
        }
    }

    public static void toFile(File file, PlaneMap map, String label) throws IOException {
        try (SVGWriter write = SVGWriter.fileWriter(file)) {
            write(write, map, label);
        }
    }

    private static void write(SVGWriter write, PlaneMap map, String label) throws IOException {

        write.setNoTransformationView(map.getBox());
        write.initialize();

        write.setWriteGroupAsCompositePath(true);

        write.setSizeMode(SizeMode.WORLD);
        write.setStroke(Color.black, 1, Dashing.SOLID);
        write.setFill(Color.lightGray, Hashures.SOLID);

        for (Region r : map.getRegions()) {
            write.setCustomAttributes(label + "=\"" + r.getLabel() + "\"");
            write.draw(r);
        }
    }
}
