package cz.agents.alite.trajectorytools.vis;import java.awt.BasicStroke;import java.awt.Color;import java.awt.Graphics2D;import org.jgrapht.Graph;import cz.agents.alite.trajectorytools.util.Waypoint;import cz.agents.alite.vis.Vis;import cz.agents.alite.vis.layer.AbstractLayer;import cz.agents.alite.vis.layer.VisLayer;import cz.agents.alite.vis.layer.toggle.KeyToggleLayer;public class NodeIdLayer extends AbstractLayer {    protected Graph<Waypoint, ?> waypoints;    protected int strokeWidth;    protected Color color;    NodeIdLayer(Graph<Waypoint, ?> waypoints, Color color, int strokeWidth) {        this.waypoints = waypoints;        this.color = color;        this.strokeWidth = strokeWidth;    }    @Override    public void paint(Graphics2D canvas) {        canvas.setStroke(new BasicStroke(strokeWidth));        canvas.setColor(color);        for (Waypoint wp: waypoints.vertexSet()) {            canvas.setStroke(new BasicStroke(strokeWidth));            canvas.setColor(color);            canvas.drawString(Integer.toString(wp.id), Vis.transX(wp.x), Vis.transY(wp.y));        }    }    @Override    public String getLayerDescription() {        String description = "Layer shows ids of the graph nodes.";        return buildLayersDescription(description);    }    public static VisLayer create(final Graph<Waypoint, ?> graph, Color color, int strokeWidth, String toggleKey) {        KeyToggleLayer toggle = KeyToggleLayer.create(toggleKey);        toggle.addSubLayer(new NodeIdLayer(graph, color, strokeWidth));        toggle.setEnabled(false);        return toggle;    }}