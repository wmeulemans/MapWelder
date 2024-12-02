/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.tue.mapwelder.gui.tools;

import nl.tue.geometrycore.geometry.Vector;
import nl.tue.geometrycore.gui.sidepanel.ComboTabItem;
import nl.tue.geometrycore.gui.sidepanel.SideTab;
import nl.tue.geometrycore.util.Pair;
import nl.tue.mapwelder.gui.Data;

/**
 *
 * @author Wouter
 */
public abstract class Tool implements ComboTabItem {

    protected Data data;
    protected String name;
    protected Vector loc;

    public Tool(Data data, String name) {
        this.data = data;
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public void createGUI(SideTab tab) {
        String s = getDescription();
        if (s != null) {
            tab.addStaticText(s, 10);
        }
    }

    public String getDescription() {
        return null;
    }

    public abstract void onSelect();

    public abstract void onDeselect();

    public abstract void render();

    public void mouseWheelMove(Vector loc, int numup, boolean ctrl, boolean shift, boolean alt) {
        this.loc = loc;
        data.repaint();
    }

    public void mousePress(Vector loc, int button, boolean ctrl, boolean shift, boolean alt) {
        this.loc = loc;
        data.repaint();
    }

    public void mouseMove(Vector loc, int button, boolean ctrl, boolean shift, boolean alt) {
        this.loc = loc;
        data.repaint();
    }

    public abstract void keyPress(int keycode, boolean ctrl, boolean shift, boolean alt);

    public void mouseDrag(Vector loc, Vector prevloc, int button, boolean ctrl, boolean shift, boolean alt) {
        this.loc = loc;
        data.repaint();
    }

    public void mouseRelease(Vector loc, int button, boolean ctrl, boolean shift, boolean alt) {
        this.loc = loc;
        data.repaint();
    }

}
