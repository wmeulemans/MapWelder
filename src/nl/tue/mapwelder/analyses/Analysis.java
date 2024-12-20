/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package nl.tue.mapwelder.analyses;

import java.awt.Color;
import java.util.List;
import nl.tue.geometrycore.geometry.BaseGeometry;
import nl.tue.geometrycore.geometryrendering.GeometryRenderer;
import nl.tue.geometrycore.gui.sidepanel.ComboTabItem;
import nl.tue.geometrycore.gui.sidepanel.SideTab;
import nl.tue.mapwelder.gui.Data;

/**
 *
 * @author wmeulema
 */
public abstract class Analysis implements ComboTabItem {

    protected final Data data;
    protected final String name;
    protected final String description;
    protected boolean enabled = false;

    public Analysis(Data data, String name, String description) {
        this.data = data;
        this.name = name;
        this.description = description;
    }

    @Override
    public void createGUI(SideTab tab) {
        if (description != null) {
            tab.addStaticText(description, 10);
        }

        tab.addCheckbox("Enabled", enabled, (e, v) -> {
            enabled = v;
            data.updateAnalysis(this);
        });
    }

    public boolean isEnabled() {
        return enabled;
    }

    public abstract void run(List<Problem> problems);

    public abstract class Problem {

        public final Analysis source;

        public Problem(Analysis source) {
            this.source = source;
        }

        public abstract void render(GeometryRenderer draw);

    }

    @Override
    public String toString() {
        return name;
    }
}
