package nl.tue.mapwelder.data;

import java.util.ArrayList;
import java.util.List;
import nl.tue.geometrycore.geometry.linear.Rectangle;

/**
 *
 * @author Wouter Meulemans (w.meulemans@tue.nl)
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
