package cz.agents.alite.trajectorytools.graph;
import junit.framework.Assert;

import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.AStarShortestPath;
import org.jgrapht.alg.DijkstraShortestPath;
import org.junit.Test;

import cz.agents.alite.trajectorytools.graph.spatial.SpatialGraphs;
import cz.agents.alite.trajectorytools.graph.spatial.SpatialGridFactory;
import cz.agents.alite.trajectorytools.graph.spatial.maneuvers.SpatialManeuver;
import cz.agents.alite.trajectorytools.util.Point;
import cz.agents.alite.trajectorytools.util.Waypoint;

public class AStarShortestPathOnManeuverGraphTest {

    @Test
    public void testAgainstDijkstra() {
        Graph<Waypoint, SpatialManeuver> graph = SpatialGridFactory.create4WayGrid(5, 5, 2, 2, 1.0);
        Waypoint start = SpatialGraphs.getNearestVertex(graph, new Point(0, 0, 0));
        Waypoint end = SpatialGraphs.getNearestVertex(graph,new Point(5.0, 5.0, 0));

        AStarShortestPath.Heuristic<Waypoint> heuristic = new AStarShortestPath.Heuristic<Waypoint>() {
            @Override
            public double getHeuristicEstimate(Waypoint current, Waypoint goal) {
                return current.distance(goal);
            }
        };

        long startTime = System.nanoTime();
        AStarShortestPath<Waypoint, SpatialManeuver> astar = new AStarShortestPath<Waypoint, SpatialManeuver>(
                graph, start, end, heuristic);

        GraphPath<Waypoint, SpatialManeuver> astarPath = astar.getPath();
        System.out.println("A* search");
        printPath(astarPath);
        System.out.println("time:" + (System.nanoTime() - startTime)/1000000 + " ms. Length:" + astarPath.getWeight());

        System.out.println();

        startTime = System.nanoTime();
        DijkstraShortestPath<Waypoint, SpatialManeuver> dijkstra = new DijkstraShortestPath<Waypoint, SpatialManeuver>(
                graph, start, end);

        GraphPath<Waypoint, SpatialManeuver> dijkstraPath = dijkstra
                .getPath();
        System.out.println("Dijkstra");
        printPath(dijkstraPath);
        System.out.println("search time:" + (System.nanoTime() - startTime)/1000000 + " ms Length:" + dijkstraPath.getWeight());


        if (astarPath.getEdgeList().equals(dijkstraPath.getEdgeList())) {
            System.out.println("Both methods found identical paths.");
        }

        Assert.assertEquals(dijkstraPath.getWeight() + " vs. " + astarPath.getWeight(), dijkstraPath.getWeight(), astarPath.getWeight(), 0.001 );
    }

    @Test
    public void testRandomWaypointGraphsAgainstDijkstra() {
        final int N = 100;

        long astarTime = 0;
        long dijkstraTime = 0;

        for (int seed=1; seed<N; seed++) {

            Graph<Waypoint, SpatialManeuver> graph = SpatialGridFactory.createRandom(5, 5, 6, 3, seed, 1.0);
            Waypoint start = SpatialGraphs.getNearestVertex(graph, new Point(0, 0, 0));
            Waypoint end = SpatialGraphs.getNearestVertex(graph, new Point(5.0, 5.0, 0));

            AStarShortestPath.Heuristic<Waypoint> heuristic = new AStarShortestPath.Heuristic<Waypoint>() {
                @Override
                public double getHeuristicEstimate(Waypoint current, Waypoint goal) {
                    return current.distance(goal);
                }
            };

            long startTime = System.nanoTime();
            AStarShortestPath<Waypoint, SpatialManeuver> astar = new AStarShortestPath<Waypoint, SpatialManeuver>(
                    graph, start, end, heuristic);

            GraphPath<Waypoint, SpatialManeuver> astarPath = astar.getPath();

            astarTime += System.nanoTime()-startTime;



            startTime = System.nanoTime();
            DijkstraShortestPath<Waypoint, SpatialManeuver> dijkstra = new DijkstraShortestPath<Waypoint, SpatialManeuver>(
                    graph, start, end);

            GraphPath<Waypoint, SpatialManeuver> dijkstraPath = dijkstra.getPath();

            dijkstraTime += System.nanoTime()-startTime;

            Assert.assertFalse((dijkstraPath == null) && (astarPath != null));
            Assert.assertFalse((dijkstraPath != null) && (astarPath == null));

            if (astarPath != dijkstraPath && !astarPath.getEdgeList().equals(dijkstraPath.getEdgeList())) {
                System.out.println("The two methods found different paths.");
            }

            if (dijkstraPath != null) {
                if (Math.abs(dijkstraPath.getWeight()-astarPath.getWeight()) > 0.0001) {
                    System.out.println("The paths found by dijkstra and A* have different length!");
                    System.out.println(" --------- " + seed);
                    System.out.println(" g: " + graph);
                    System.out.println("Dijkstra");
                    printPath(dijkstraPath);
                    System.out.println("A*");
                    printPath(astarPath);
                }
            }

            if (dijkstraPath != null)
                Assert.assertEquals(dijkstraPath.getWeight() + " vs. " + astarPath.getWeight(), dijkstraPath.getWeight(), astarPath.getWeight(), 0.001 );
        }

        System.out.println("Examined " + N + " random graphs. Dijkstra avg. time: " + dijkstraTime/(1000*N) + "ms" + ". A* avg. time: " + astarTime/(1000*N)+"ms.");
    }

    private void printPath(GraphPath<Waypoint, SpatialManeuver> path) {
         if (path == null) {
             System.out.println("no path found");
             return;
         }
         System.out.print(path.getStartVertex() + ", ");
         for (SpatialManeuver edge : path.getEdgeList()) {
             if (edge == null) {
                 System.out.println("Edge is null");
             }
             System.out.print(path.getGraph().getEdgeSource(edge) + "-"
                     + path.getGraph().getEdgeTarget(edge) + ", ");
         }
         System.out.print(path.getEndVertex());
         System.out.print(" Path weight:" + path.getWeight());
         System.out.println();
    }

}