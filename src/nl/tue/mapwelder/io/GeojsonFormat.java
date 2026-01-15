/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.tue.mapwelder.io;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import nl.tue.geometrycore.geometry.linear.Polygon;
import nl.tue.geometrycore.geometry.linear.Rectangle;
import nl.tue.geometrycore.geometry.mix.GeometryGroup;
import nl.tue.geometrycore.gui.sidepanel.SideTab;
import nl.tue.geometrycore.io.ReadItem;
import nl.tue.geometrycore.io.json.GeoJSONReader;
import nl.tue.geometrycore.io.json.GeoJSONWriter;
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
        super(data, "GeoJSON", "geojson");
    }

    @Override
    public void createGUI(SideTab tab) {
        super.createGUI(tab);
        tab.addLabel("ID label");
        tab.addTextField(label, (e, v) -> label = v);

    }

    @Override
    protected PlaneMap load(File file) throws IOException {
        try (GeoJSONReader read = GeoJSONReader.fileReader(file)) {
            read.setTrimQuotes(false);
            List<ReadItem> items = read.read();

            PlaneMap map = new PlaneMap(new Rectangle());

            for (ReadItem item : items) {

                // reading only polygons/multipolygons
                // other features are ignored                
                try {
                    String lbl = item.getAuxiliary().get(label);
                    if (lbl == null) {
                        lbl = "r" + (map.getRegions().size() + 1);
                    }
                    Region R = new Region(lbl);

                    switch (item.getGeometry().getGeometryType()) {
                        case POLYGON:
                            R.getParts().add((Polygon) item.toGeometry());
                            break;
                        case GEOMETRYGROUP:
                            GeometryGroup<Polygon> grp = (GeometryGroup<Polygon>) item.getGeometry();
                            for (Polygon p : grp.getParts()) {
                                R.getParts().add(p);
                            }
                            break;
                    }

                    
                    R.setAux(item.getAuxiliary());
                    R.updateBox();
                    map.getRegions().add(R);

                } catch (Exception ex) {
                    System.out.println("Warning: unsupported geometry type -- " + item.getGeometry());
                }
            }

            map.getBox().includeGeometry(map.getRegions());

            return map;
        }
    }

    @Override
    protected void save(File file, PlaneMap map) throws IOException {
        try (GeoJSONWriter write = GeoJSONWriter.fileWriter(file, true)) {

            write.initialize();

            for (Region r : map.getRegions()) {

                String[][] props = getPropertyArray(r);
                Polygon[][] polies = getMultiPolygon(r);

                if (polies.length == 1) {
                    write.write(polies[0], props);
                } else {
                    write.write(polies, props);
                }
            }
        }
    }

    @Override
    protected boolean canLoad() {
        return true;
    }

    @Override
    protected boolean canSave() {
        return true;
    }

    private class Ring {
        Polygon polygon;
        double area;
        List<Ring> inner;
    }
    
    private Polygon[][] getMultiPolygon(Region region) {
        
        // NB: we assume that all polygons of a single region do not boundary-intersect, not even at a common vertex
        
        List<Ring> rings = new ArrayList();
        for (Polygon p : region.getParts()) {
            Ring ring = new Ring();
            ring.polygon = p.clone();
            ring.area = p.areaSigned();
            ring.inner = null;
            rings.add(ring);
        }
        
        rings.sort((a,b) -> Double.compare(Math.abs(b.area), Math.abs(a.area))); // large to small
                
        List<Ring> outer = new ArrayList();
        nextring:
        for (Ring r : rings) {
            // see if its nested in an outerring
            for (Ring o : outer) {
                if (o.polygon.contains(r.polygon.vertex(0))) {
                    o.inner.add(r);
                    // ensure CW
                    if (r.area > 0) {
                        r.polygon.reverse();
                    }
                    continue nextring;
                }
            }
            // no outerring, so this must be a new one
            r.inner = new ArrayList();
            outer.add(r);
            // ensure CCW
            if (r.area < 0) {
                r.polygon.reverse();
            }
        }
        
        Polygon[][] mp = new Polygon[outer.size()][];
        for (int out_i = 0; out_i < outer.size(); out_i++) {
            Ring o = outer.get(out_i);
            Polygon[] p = mp[out_i] = new Polygon[o.inner.size()+1];
            p[0] = o.polygon;
            for (int in_i = 0; in_i < o.inner.size(); in_i++) {
                p[in_i+1] = o.inner.get(in_i).polygon;
            }
        }
        
        return mp;
    }

    private String[][] getPropertyArray(Region r) {
        if (r.getAux() == null) {
            return null;
        }

        String[][] props = new String[r.getAux().size()][2];
        int i = 0;
        for (Entry<String, String> e : r.getAux().entrySet()) {
            props[i][0] = e.getKey();
            props[i][1] = e.getValue();
            i++;
        }
        return props;
    }
}
