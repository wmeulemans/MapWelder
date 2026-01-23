package nl.tue.mapwelder.gui;

import java.util.ArrayList;
import java.util.List;
import nl.tue.geometrycore.geometry.linear.Rectangle;
import nl.tue.mapwelder.data.Graph;
import nl.tue.mapwelder.data.PlaneMap;
import nl.tue.mapwelder.data.Region;
import nl.tue.mapwelder.algorithms.Algorithm;
import nl.tue.mapwelder.algorithms.Coarsen;
import nl.tue.mapwelder.algorithms.Decimate;
import nl.tue.mapwelder.algorithms.ShortEdgeRemoval;
import nl.tue.mapwelder.algorithms.SmallAngleRemoval;
import nl.tue.mapwelder.algorithms.SmallAreaRemoval;
import nl.tue.mapwelder.algorithms.SnapToGraph;
import nl.tue.mapwelder.algorithms.VisvalingamWhyatt;
import nl.tue.mapwelder.algorithms.WeldAlgorithm;
import nl.tue.mapwelder.analyses.Analysis;
import nl.tue.mapwelder.analyses.Analysis.Problem;
import nl.tue.mapwelder.analyses.Intersections;
import nl.tue.mapwelder.analyses.SmallAngles;
import nl.tue.mapwelder.tools.AutoWeldTool;
import nl.tue.mapwelder.tools.NoTool;
import nl.tue.mapwelder.tools.Tool;
import nl.tue.mapwelder.tools.VertexTool;
import nl.tue.mapwelder.tools.WeldTool;
import nl.tue.mapwelder.io.DecomposedJsonFormat;
import nl.tue.mapwelder.io.Format;
import nl.tue.mapwelder.io.GeojsonFormat;
import nl.tue.mapwelder.io.IpeFormat;
import nl.tue.mapwelder.io.SvgFormat;

/**
 *
 * @author Wouter Meulemans (w.meulemans@tue.nl)
 */
public class Data {

    public Tool[] tools = {
        new NoTool(this),
        new WeldTool(this),
        new VertexTool(this),
        new AutoWeldTool(this)};
    public Tool activeTool = tools[3];

    public Algorithm[] algorithms = {
        new SmallAngleRemoval(this),
        new ShortEdgeRemoval(this),
        new SmallAreaRemoval(this),
        new Coarsen(this),
        new Decimate(this),
        new VisvalingamWhyatt(this),
        new SnapToGraph(this),
        new WeldAlgorithm(this)
    };
    public Algorithm activeAlgorithm = algorithms[0];

    public Analysis[] analyses = {
        new SmallAngles(this),
        new Intersections(this)
    };
    public List<Problem> problems = new ArrayList();

    public Format[] formats = {
        new IpeFormat(this),
        new SvgFormat(this),
        new GeojsonFormat(this),
        new DecomposedJsonFormat(this)};
    public Format activeFormat = formats[2];

    public PlaneMap map = new PlaneMap(new Rectangle());
    public Region hover = null;

    public Graph graph = null;
    public boolean autoregraph = true;

    // polygon settings
    public boolean render_polygons = true;
    // vertex
    public int polygon_vertex_radius = 30;
    public int polygon_vertex_alpha = 100;
    public boolean polygon_vertex_hover = true;
    // edges    
    public int polygon_edge_width = 0;
    public int polygon_edge_alpha = 100;
    public boolean polygon_edge_hover = false;
    // interior
    public int polygon_fill_alpha = 50;
    public boolean polygon_fill_hover = false;
    // labels
    public int label_size = 100;
    public boolean label_hover = true;
    
    // graph settings
    public boolean render_graph = true;
    public int graph_vertex_radius = 30;
    public int graph_edge_width = 10;

    public DrawPanel draw;
    public SidePanel side;

    public void updateAnalysis(Analysis analysis) {
        if (analysis == null) {
            problems.clear();
            for (Analysis a : analyses) {
                if (a.isEnabled()) {
                    a.run(problems);
                }
            }
        } else {
            int i = 0;
            int s = problems.size();
            while (i < s) {
                if (problems.get(i).source == analysis) {
                    s--;
                    problems.set(i, problems.get(s));
                    problems.remove(s);
                } else {
                    i++;
                }
            }

            if (analysis.isEnabled()) {
                analysis.run(problems);
            }
        }
        repaint();
    }

    public void mapChanged(boolean clearGraph) {
        hover = null;
        if (clearGraph) {
            graph = null;
        }
        if (autoregraph) {
            ensureGraph();
        }
        updateAnalysis(null);
        repaint();
    }

    public void runAlgorithm(Algorithm alg) {
        if (alg.run()) {
            mapChanged(!alg.maintainsGraph());
        }
    }

    public void ensureGraph() {
        if (graph == null) {
            graph = new Graph(map);
            repaint();
        }
    }

    public void repaint() {
        if (draw != null) {
            draw.repaint();
        }
    }

    public void clearGraph() {
        graph = null;
        repaint();
    }

    public void zoomToRegion(int offset) {
        if (offset != 0) {
            if (hover == null) {
                if (offset > 0) {
                    hover = map.getRegions().get(0);
                } else if (offset < 0) {
                    hover = map.getRegions().get(map.getRegions().size() - 1);
                }
            } else {
                int i = map.getRegions().indexOf(hover) + offset;
                if (0 <= i && i < map.getRegions().size()) {
                    hover = map.getRegions().get(i);
                } else {
                    hover = null;
                }
            }
        }

        if (hover == null) {
            draw.zoomToFit();
        } else {
            Rectangle R = hover.getBox().clone();
            R.scale(1.02, R.center());
            draw.zoomToBox(R);
        }
    }
}
