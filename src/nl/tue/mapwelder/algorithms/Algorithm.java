package nl.tue.mapwelder.algorithms;

import nl.tue.geometrycore.gui.sidepanel.ComboTabItem;
import nl.tue.geometrycore.gui.sidepanel.SideTab;
import nl.tue.mapwelder.gui.Data;

/**
 *
 * @author Wouter Meulemans (w.meulemans@tue.nl)
 */
public abstract class Algorithm implements ComboTabItem {

    protected final Data data;
    protected final String name;
    protected final String description;

    public Algorithm(Data data, String name, String description) {
        this.data = data;
        this.name = name;
        this.description = description;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public void createGUI(SideTab tab) {
        if (description != null) {
            tab.addStaticText(description, 10);
        }

        tab.addButton("Run", (e) -> data.runAlgorithm(this));
    }
    
    public boolean maintainsGraph() {
        return false;
    }
    
    public abstract boolean run();
}
