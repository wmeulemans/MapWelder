package nl.tue.mapwelder.gui;

import nl.tue.geometrycore.gui.GUIUtil;

/**
 *
 * @author Wouter Meulemans (w.meulemans@tue.nl)
 */
public class Main {
    
    public static void main(String[] args) {
                
        Data data = new Data();
        data.draw = new DrawPanel(data);
        data.side = new SidePanel(data);
        
        GUIUtil.makeMainFrame("MapWelder", data.draw, data.side);
        
    }
}
