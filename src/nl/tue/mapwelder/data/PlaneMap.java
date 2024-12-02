/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.tue.mapwelder.data;

import java.util.ArrayList;
import java.util.List;
import nl.tue.geometrycore.geometry.linear.Rectangle;

/**
 *
 * @author Wouter
 */
public class PlaneMap {

    private Rectangle box;
    private List<Region> regions = new ArrayList();

    public PlaneMap(Rectangle box) {
        this.box = box;
    }

    public List<Region> getRegions() {
        return regions;
    }

    public Rectangle getBox() {
        return box;
    }

    public void updateAllBoxes() {
        for (Region r : regions) {
            r.updateBox();
        }
        updateBox();
    }

    public void updateBox() {
        box = new Rectangle();
        for (Region r : regions) {
            box.includeGeometry(r.getBox());
        }
    }
}
