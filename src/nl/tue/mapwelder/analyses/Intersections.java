/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package nl.tue.mapwelder.analyses;

import java.util.ArrayList;
import java.util.List;
import nl.tue.geometrycore.geometry.BaseGeometry;
import nl.tue.geometrycore.geometry.linear.LineSegment;
import nl.tue.geometrycore.geometry.linear.Polygon;
import nl.tue.geometrycore.geometryrendering.GeometryRenderer;
import nl.tue.geometrycore.gui.sidepanel.SideTab;
import nl.tue.geometrycore.util.DoubleUtil;
import nl.tue.mapwelder.data.Region;
import nl.tue.mapwelder.gui.Data;

/**
 *
 * @author wmeulema
 */
public class Intersections extends Analysis {

    private boolean between = true;
    private boolean within = true;

    public Intersections(Data data) {
        super(data, "Intersections", "Finds intersections between and within regions");
    }

    @Override
    public void createGUI(SideTab tab) {
        super.createGUI(tab);
        
        tab.addCheckbox("Within regions", within, (e, v) -> {
            within = v;
            if (enabled) {
                data.updateAnalysis(this);
            }
        });
        tab.addCheckbox("Between regions", between, (e, v) -> {
            between = v;
            if (enabled) {
                data.updateAnalysis(this);
            }
        });
    }

    @Override
    public void run(List<Analysis.Problem> problems) {

        List<RegionEdge> edges = new ArrayList();

        for (Region r : data.map.getRegions()) {
            for (Polygon p : r.getParts()) {
                for (int i = 0; i < p.vertexCount(); i++) {
                    edges.add(new RegionEdge(r, p, i));
                }
            }
        }

        edges.sort((e, f) -> Double.compare(e.start(), f.start()));
        List<RegionEdge> active = new ArrayList();
        for (RegionEdge e : edges) {
            int i = 0;
            while (i < active.size()) {
                RegionEdge a = active.get(i);

                if (e.start() > a.end() + DoubleUtil.EPS) {
                    // remove a
                    if (i == active.size() - 1) {
                        active.remove(i);
                    } else {
                        active.set(i, active.remove(active.size() - 1));
                    }
                } else if (e.r == a.r) {
                    // same region
                    if (e.p == a.p
                            && (Math.abs(e.i - a.i) <= 1
                            || Math.abs(e.i - a.i) >= e.p.vertexCount() - 1)) {
                        // adjacent
                    } else {
                        // not adjacent
                        if (within) {
                            for (BaseGeometry bg : e.ls().intersect(a.ls())) {
                                problems.add(new Intersection(this, bg));
                            }
                        }
                    }
                    i++;
                } else {
                    // different region
                    if (between) {
                        for (BaseGeometry bg : e.ls().intersect(a.ls())) {
                            problems.add(new Intersection(this, bg));
                        }
                    }
                    i++;
                }
            }
            active.add(e);
        }
    }

    private class RegionEdge {

        Region r;
        Polygon p;
        int i;

        public RegionEdge(Region r, Polygon p, int i) {
            this.r = r;
            this.p = p;
            this.i = i;
        }

        LineSegment ls() {
            return p.edge(i);
        }

        double start() {
            return Math.min(ls().getStart().getX(), ls().getEnd().getX());
        }

        double end() {
            return Math.max(ls().getStart().getX(), ls().getEnd().getX());
        }
    }

    public class Intersection extends Problem {

        BaseGeometry bg;

        public Intersection(Analysis source, BaseGeometry bg) {
            super(source);
            this.bg = bg;
        }

        @Override
        public void render(GeometryRenderer draw) {
            draw.draw(bg);
        }

    }
}
