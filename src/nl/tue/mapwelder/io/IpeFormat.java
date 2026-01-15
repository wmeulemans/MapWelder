/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.tue.mapwelder.io;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import nl.tue.geometrycore.geometry.BaseGeometry;
import nl.tue.geometrycore.geometry.GeometryType;
import nl.tue.geometrycore.geometry.Vector;
import nl.tue.geometrycore.geometry.linear.Polygon;
import nl.tue.geometrycore.geometry.linear.Rectangle;
import nl.tue.geometrycore.geometry.mix.GeometryGroup;
import nl.tue.geometrycore.geometryrendering.styling.Dashing;
import nl.tue.geometrycore.geometryrendering.styling.Hashures;
import nl.tue.geometrycore.geometryrendering.styling.SizeMode;
import nl.tue.geometrycore.gui.sidepanel.SideTab;
import nl.tue.geometrycore.io.ReadItem;
import nl.tue.geometrycore.io.ipe.IPEReader;
import nl.tue.geometrycore.io.ipe.IPEWriter;
import nl.tue.mapwelder.data.PlaneMap;
import nl.tue.mapwelder.data.Region;
import nl.tue.mapwelder.gui.Data;

/**
 *
 * @author Wouter Meulemans (w.meulemans@tue.nl)
 */
public class IpeFormat extends Format {

    public IpeFormat(Data data) {
        super(data, "IPE");
    }

    @Override
    protected PlaneMap load(File file) throws IOException {
        return fromFile(file);
    }

    @Override
    protected void save(File file, PlaneMap map) throws IOException {
        toFile(file, map);
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
    }

    public static PlaneMap fromClipboard() throws IOException {
        return read(IPEReader.clipboardReader());
    }

    public static PlaneMap fromFile(File file) throws IOException {
        return read(IPEReader.fileReader(file));
    }

    private static boolean handle(Region r, BaseGeometry geom) {
        switch (geom.getGeometryType()) {
            case POLYGON:
                Polygon p = (Polygon) geom;
                r.getParts().add(p);

                return false;
            case GEOMETRYGROUP:
                boolean err = false;
                for (BaseGeometry part : ((GeometryGroup<? extends BaseGeometry>) geom).getParts()) {
                    if (handle(r, part)) {
                        err = true;
                    }
                }
                return err;
            default:
                System.err.println("  Unexpected geometry type: " + geom.getGeometryType());
                return true;
        }
    }

    private static PlaneMap read(IPEReader read) throws IOException {

        read.setBezierSampling(5);

        List<ReadItem> items = read.read();
        Rectangle box = IPEWriter.getA4Size();
        read.close();

        PlaneMap map = new PlaneMap(box);

        List<ReadItem> labels = new ArrayList();
        Iterator<ReadItem> rit = items.iterator();
        while (rit.hasNext()) {
            ReadItem ri = rit.next();
            if (ri.getGeometry().getGeometryType() == GeometryType.VECTOR) {
                rit.remove();
                labels.add(ri);
            }
        }

        for (ReadItem item : items) {

            Region r = new Region("");
            r.setColor(item.getFill());
            map.getRegions().add(r);
            boolean err = handle(r, item.getGeometry());
            r.updateBox();

            labelloop:
            for (ReadItem ri : labels) {
                Vector v = (Vector) ri.getGeometry();
                if (!r.getBox().contains(v)) {
                    continue;
                }
                for (Polygon p : r.getParts()) {
                    if (p.contains(v)) {
                        r.setLabel(ri.getString());
                        break labelloop;
                    }
                }
            }

            if (err) {
                System.err.println("DONE: " + r.getLabel());
            }
        }

        map.updateBox();

        return map;
    }

    public static void toClipboard(PlaneMap map) throws IOException {
        try (IPEWriter write = IPEWriter.clipboardWriter()) {
            write(write, map);
        }
    }

    public static void toFile(File file, PlaneMap map) throws IOException {
        try (IPEWriter write = IPEWriter.fileWriter(file)) {
            write(write, map);
        }
    }

    private static void write(IPEWriter write, PlaneMap map) throws IOException {

        write.initialize();
        write.setTextSerifs(true);

        write.setSizeMode(SizeMode.WORLD);
        write.setStroke(Color.black, 1, Dashing.SOLID);
        write.setFill(Color.lightGray, Hashures.SOLID);

        for (Region r : map.getRegions()) {
            if (r.getColor() != null) {
                write.setFill(r.getColor(), Hashures.SOLID);
            } else {
                write.setFill(Color.lightGray, Hashures.SOLID);
            }
            write.draw(r);
            write.draw(r.centroid(), r.getLabel());
        }
    }
}
