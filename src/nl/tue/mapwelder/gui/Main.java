/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.tue.mapwelder.gui;

import nl.tue.geometrycore.gui.GUIUtil;

/**
 *
 * @author Wouter
 */
public class Main {
    
    public static void main(String[] args) {
                
        Data data = new Data();
        data.draw = new DrawPanel(data);
        data.side = new SidePanel(data);
        
        GUIUtil.makeMainFrame("MapWelder", data.draw, data.side);
        
    }
}
