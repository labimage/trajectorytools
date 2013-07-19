package tt.euclidtime3i.discretization;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.jgrapht.DirectedGraph;
import org.jgrapht.EdgeFactory;

import tt.euclid2i.Line;
import tt.euclidtime3i.Point;
import tt.euclidtime3i.Region;
import tt.util.NotImplementedException;

public class ConstantSpeedTimeExtension implements DirectedGraph<Point, Straight> {

    private DirectedGraph<tt.euclid2i.Point, tt.euclid2i.Line> spatialGraph;
    private int maxTime;
    private int[] speeds;
    private Collection<Region> dynamicObstacles;

    public ConstantSpeedTimeExtension(
            DirectedGraph<tt.euclid2i.Point, Line> spatialGraph, int maxTime,
            int[] speeds, Collection<Region> dynamicObstacles) {
        super();
        this.spatialGraph = spatialGraph;
        this.maxTime = maxTime;
        this.speeds = speeds;
        this.dynamicObstacles = dynamicObstacles;
    }

    @Override
    public Straight addEdge(Point arg0, Point arg1) {
       throw new NotImplementedException();
    }

    @Override
    public boolean addEdge(Point arg0, Point arg1, Straight arg2) {
        throw new NotImplementedException();
    }

    @Override
    public boolean addVertex(Point arg0) {
        throw new NotImplementedException();
    }

    @Override
    public boolean containsEdge(Straight arg0) {
        throw new NotImplementedException();
    }

    @Override
    public boolean containsEdge(Point arg0, Point arg1) {
        throw new NotImplementedException();
    }

    @Override
    public boolean containsVertex(Point p) {
        return spatialGraph.containsVertex(p.getPosition()) &&
                p.getTime() <= maxTime;
    }

    @Override
    public Set<Straight> edgeSet() {
        throw new NotImplementedException();
    }

    @Override
    public Set<Straight> edgesOf(Point vertex) {
        Set<Straight> edges = new HashSet<Straight>();
        edges.addAll(incomingEdgesOf(vertex));
        edges.addAll(outgoingEdgesOf(vertex));
        return edges;
    }

    @Override
    public Set<Straight> getAllEdges(Point start, Point end) {
        Set<Straight> edges = new HashSet<Straight>();
        edges.add(new Straight(start, end) );
        edges.add(new Straight(end, start) );
        return edges;
    }

    @Override
    public Straight getEdge(Point start, Point end) {
        return new Straight(start, end);
    }

    @Override
    public EdgeFactory<Point, Straight> getEdgeFactory() {
        return null;
    }

    @Override
    public Point getEdgeSource(Straight edge) {
        return edge.getStart();
    }

    @Override
    public Point getEdgeTarget(Straight edge) {
        return edge.getEnd();
    }

    @Override
    public double getEdgeWeight(Straight edge) {
        return edge.getEnd().getTime() - edge.getStart().getTime();
    }


    @Override
    public Set<Straight> removeAllEdges(Point arg0, Point arg1) {
        throw new NotImplementedException();
    }

    @Override
    public boolean removeEdge(Straight arg0) {
        throw new NotImplementedException();
    }

    @Override
    public Straight removeEdge(Point arg0, Point arg1) {
        throw new NotImplementedException();
    }

    @Override
    public boolean removeVertex(Point arg0) {
        throw new NotImplementedException();
    }

    @Override
    public Set<Point> vertexSet() {
        throw new NotImplementedException();
    }

    @Override
    public int inDegreeOf(Point vertex) {
         throw new NotImplementedException();
    }

    @Override
    public Set<Straight> incomingEdgesOf(Point vertex) {
         throw new NotImplementedException();
    }

    @Override
    public int outDegreeOf(Point vertex) {
        return outgoingEdgesOf(vertex).size();
    }

    @Override
    public Set<Straight> outgoingEdgesOf(Point vertex) {
        Set<Point> children = new HashSet<Point>();

        Set<Line> spatialEdges = spatialGraph.outgoingEdgesOf(new tt.euclid2i.Point(vertex.x, vertex.y));
        for (Line spatialEdge : spatialEdges) {
            for (int speed : speeds) {
                Point child = new Point(spatialEdge.getEnd().x, spatialEdge.getEnd().y, vertex.getTime() + (int) Math.round(spatialEdge.getDistance()/speed));
                if (child.getTime() < maxTime && isVisible(vertex, child, dynamicObstacles)) {
                    children.add(child);
                }
            }
        }

        Set<Straight> edges = new HashSet<Straight>();
        for (Point child : children) {
            edges.add(new Straight(vertex, child));
        }

        return edges;
    }

    private boolean isVisible(Point start, Point end, Collection<Region> obstacles) {
        for (Region obstacle : obstacles) {
            if (obstacle.intersectsLine(start, end)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean removeAllEdges(Collection<? extends Straight> arg0) {
        throw new NotImplementedException();
    }

    @Override
    public boolean removeAllVertices(Collection<? extends Point> arg0) {
        throw new NotImplementedException();
    }
}
