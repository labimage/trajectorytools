package org.jgrapht.alg;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import org.jgrapht.Graphs;
import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.util.Goal;
import org.jgrapht.util.Heuristic;
import org.jgrapht.util.PlanningHeapWrapper;
import org.teneighty.heap.FibonacciHeap;
import org.teneighty.heap.Heap;

public class ARAStarShortestPath<V, E> extends PlanningAlgorithm<V, E> {

    private Heuristic<V> heuristic;
    private Result result;
    private double suboptimalityScale;
    private double suboptimalityDecreaseStep;
    //
    private Set<V> closed;
    private Set<V> inconsistent;
    private PlanningHeapWrapper<Double, V> heap;

    public ARAStarShortestPath(Graph<V, E> graph, Heuristic<V> heuristic, V startVertex,
            final V endVertex, double suboptimalityScale, double suboptimalityDecreaseStep) {

        this(graph, heuristic, startVertex, new Goal<V>() {
            @Override
            public boolean isGoal(V current) {
                return endVertex.equals(current);
            }
        }, suboptimalityScale, suboptimalityDecreaseStep);
    }

    public ARAStarShortestPath(Graph<V, E> graph, Heuristic<V> heuristic,
            V startVertex, Goal<V> goal, double suboptimalityScale, double suboptimalityDecreaseStep) {
        super(graph, startVertex, goal);

        this.heuristic = heuristic;
        this.suboptimalityScale = suboptimalityScale;
        this.suboptimalityDecreaseStep = suboptimalityDecreaseStep;
        initialize();
    }

    private void initialize() {
        heap = new PlanningHeapWrapper<Double, V>(new FibonacciHeap<Double, V>());
        closed = new HashSet<V>();
        inconsistent = new HashSet<V>();

        setShortestDistanceTo(startVertex, 0.);
        heap.insert(calculateKey(startVertex), startVertex);
    }

    public void setSuboptimalityDecreseStep(double step) {
        this.suboptimalityDecreaseStep = step;
    }

    public Result<V, E> iterate() {
        prepareOpenQueue();
        clearCloseAndInconsistenSet();
        improvePath();
        decreaseEpsilon();
        return result;
    }

    private void prepareOpenQueue() {
        PlanningHeapWrapper<Double, V> oldheap = heap;
        heap = new PlanningHeapWrapper<Double, V>(new FibonacciHeap<Double, V>());

        moveVerticesWithUpdatedKeysIntoQueue(oldheap);
        moveInconsistendVerticesIntoQueue();
    }

    private void moveVerticesWithUpdatedKeysIntoQueue(PlanningHeapWrapper<Double, V> oldheap) {
        for (Iterator<Heap.Entry<Double, V>> it = oldheap.iterator(); it.hasNext();) {
            Heap.Entry<Double, V> entry = it.next();
            V vertex = entry.getValue();
            double key = calculateKey(vertex);
            heap.insert(key, vertex);
        }
    }

    private void moveInconsistendVerticesIntoQueue() {
        for (V vertex : inconsistent) {
            double key = calculateKey(vertex);
            heap.insert(key, vertex);
        }
    }

    private void clearCloseAndInconsistenSet() {
        closed.clear();
        inconsistent.clear();
    }

    private double calculateKey(V vertex) {
        return getShortestDistanceTo(vertex)
                + suboptimalityScale * heuristic.getCostToGoalEstimate(vertex);
    }

    private void decreaseEpsilon() {
        suboptimalityScale -= suboptimalityDecreaseStep;
        if (suboptimalityScale < 1) {
            suboptimalityScale = 1;
        }
    }

    private void improvePath() {
        V foundGoal = null;

        while (!heap.isEmpty()) {

            V vertex = heap.extractMinimum().getValue();
            if (goal.isGoal(vertex)) {
                inconsistent.add(vertex); //goal state should remain in the queue
                foundGoal = vertex;
                break;
            }

            closed.add(vertex);
            double vertexDistance = getShortestDistanceTo(vertex);

            Set<E> edges = specifics.outgoingEdgesOf(vertex);
            for (E edge : edges) {
                V child = Graphs.getOppositeVertex(graph, edge, vertex);

                double edgeCost = graph.getEdgeWeight(edge);
                double childDistance = getShortestDistanceTo(child);
                double candidateDistance = vertexDistance + edgeCost;

                if (childDistance > candidateDistance) {
                    encounterVertex(child, edge, candidateDistance);
                }
            }
        }

        GraphPath<V, E> path;
        if (foundGoal != null) {
            path = reconstructPath(startVertex, foundGoal);
        } else {
            path = null;
        }

        result = new Result(path, suboptimalityScale);
    }

    private void encounterVertex(V vertex, E edge, double distanceToVertex) {
        setShortestDistanceTo(vertex, distanceToVertex);
        setShortestPathTreeEdge(vertex, edge);

        if (closed.contains(vertex)) {
            inconsistent.add(vertex);
        } else {
            heap.insertOrUpdateKey(calculateKey(vertex), vertex);
        }
    }

    public static class Result<V, E> {

        public GraphPath<V, E> path;
        public double suboptimalityScale;

        public Result(GraphPath<V, E> path, double suboptimalityScale) {
            this.path = path;
            this.suboptimalityScale = suboptimalityScale;
        }
    }
}
