package nl.tue.mapwelder.tools;

import nl.tue.geometrycore.geometry.Vector;
import nl.tue.mapwelder.gui.Data;

/**
 *
 * @author Wouter Meulemans (w.meulemans@tue.nl)
 */
public class NoTool extends Tool {

    public NoTool(Data data) {
        super(data, "No tool");
    }

    @Override
    public String getDescription() {
        return "Prevents interacting with the map.";
    }
    
    @Override
    public void render() {
        
    }

    @Override
    public void mouseWheelMove(Vector loc, int numup, boolean ctrl, boolean shift, boolean alt) {
        
    }

    @Override
    public void mousePress(Vector loc, int button, boolean ctrl, boolean shift, boolean alt) {
        
    }

    @Override
    public void mouseMove(Vector loc, int button, boolean ctrl, boolean shift, boolean alt) {
        
    }

    @Override
    public void keyPress(int keycode, boolean ctrl, boolean shift, boolean alt) {
        
    }

    @Override
    public void mouseDrag(Vector loc, Vector prevloc, int button, boolean ctrl, boolean shift, boolean alt) {
       
    }

    @Override
    public void mouseRelease(Vector loc, int button, boolean ctrl, boolean shift, boolean alt) {
        
    }

    @Override
    public void onSelect() {
        
    }

    @Override
    public void onDeselect() {
       
    }
    
}
