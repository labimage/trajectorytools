package cz.agents.alite.trajectorytools.graph.spatial;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import org.jgrapht.DummyEdgeFactory;
import org.jgrapht.Graph;
import org.jgrapht.graph.DirectedWeightedMultigraph;

import cz.agents.alite.trajectorytools.graph.spatial.maneuvers.SpatialManeuver;
import cz.agents.alite.trajectorytools.graph.spatial.maneuvers.Straight;
import cz.agents.alite.trajectorytools.graph.spatial.maneuvers.Wait;
import cz.agents.alite.trajectorytools.util.Waypoint;

public class SpatialGridFactory {
       public static Graph<Waypoint, SpatialManeuver> createNWayGrid(double sizeX, double sizeY, int gridX, int gridY, double speed, int[][] edgePattern, boolean allowWaitManeuver) {
            Graph<Waypoint, SpatialManeuver> graph 	= new DirectedWeightedMultigraph<Waypoint, SpatialManeuver>(new DummyEdgeFactory<Waypoint, SpatialManeuver>());
            Waypoint waypoints[][] = new Waypoint[gridX+1][gridY+1];
            int waypointCounter = 0;

            double xStep = sizeX/gridX;
            double yStep = sizeY/gridY;

            // Generate vertices
            for (int x=0; x <= gridX; x++) {
                for (int y=0; y <= gridY; y++) {
                    Waypoint w = new Waypoint(waypointCounter++, x*xStep, y*yStep);
                    waypoints[x][y] = w;
                    graph.addVertex(w);
                    if (allowWaitManeuver) {
                        SpatialManeuver wait = new Wait(w, xStep/speed);
                        graph.addEdge(w, w, wait);
                    }

                }
            }

            // Generate edges
            for (int x=0; x <= gridX; x++) {
                for (int y=0; y <= gridY; y++) {
                    Waypoint v1 = waypoints[x][y];
                         for (int[] edgeOffset : edgePattern) {
                             int destX = x + edgeOffset[0];
                             int destY = y + edgeOffset[1];

                             if (destX >= 0 && destX <= gridX && destY >= 0 && destY <= gridY) {
                                    Waypoint v2 = waypoints[destX][destY];

                                    if (!graph.containsEdge(v1, v2)) {
                                        SpatialManeuver maneuverForward = new Straight(v1, v2, speed);
                                        graph.addEdge(v1, v2, maneuverForward);
                                    }

                                    if (!graph.containsEdge(v2, v1)) {
                                        SpatialManeuver maneuverBack = new Straight(v2, v1, speed);
                                        graph.addEdge(v2, v1, maneuverBack);
                                    }
                             }
                         }
                    }
                }
            return graph;
        }




    static public Graph<Waypoint, SpatialManeuver> create4WayGrid(double sizeX, double sizeY,
            int gridX, int gridY, double speed) {

        final int[][] EDGE_PATTERN = {           {0,-1},
                                         {-1, 0},         { 1, 0},
                                                  {0, 1},          };


        return createNWayGrid(sizeX, sizeY, gridX, gridY, speed, EDGE_PATTERN, true);
    }

    static public Graph<Waypoint, SpatialManeuver> create8WayGrid(double sizeX, double sizeY,
            int gridX, int gridY, double speed) {

        final int[][] EDGE_PATTERN = {{-1,-1}, {0,-1}, { 1,-1},
                                       {-1, 0},         { 1, 0},
                                       {-1, 1}, {0, 1}, { 1, 1}};


        return createNWayGrid(sizeX, sizeY, gridX, gridY, speed, EDGE_PATTERN, true);
    }


    static public Graph<Waypoint, SpatialManeuver> create(double sizeX, double sizeY, int gridX, int gridY, double speed) {
        Graph<Waypoint, SpatialManeuver> graph = new DirectedWeightedMultigraph<Waypoint, SpatialManeuver>(new DummyEdgeFactory<Waypoint, SpatialManeuver>());
        Waypoint waypoints[][] = new Waypoint[gridX+1][gridY+1];
        int waypointCounter = 0;

        double xStep = sizeX/gridX;
        double yStep = sizeY/gridY;

        // Generate vertices
        for (int x=0; x <= gridX; x++) {
            for (int y=0; y <= gridX; y++) {
                Waypoint w = new Waypoint(waypointCounter++,x*xStep, y*yStep);
                waypoints[x][y] = w;
                graph.addVertex(w);
            }
        }

        // Generate edges of complete graph
        for (int x=0; x <= gridX; x++) {
            for (int y=0; y <= gridY; y++) {
                Waypoint v1 = waypoints[x][y];
                int starty = y+1;
                for (int x2=x; x2 <= gridX; x2++) {
                    for (int y2=starty; y2 <= gridY; y2++) {
                        Waypoint v2 = waypoints[x2][y2];
                        if (v1 != v2) {
                            SpatialManeuver maneuver = new Straight(v1, v2, speed);
                            graph.addEdge(v1, v2, maneuver);
                        }
                    }
                    starty = 0;
                }
            }
        }

        return graph;
    }


    public static Graph<Waypoint, SpatialManeuver> createRandom(double sizeX, double sizeY, int nPoints, int branchFactor, int seed, double speed) {
        Graph<Waypoint, SpatialManeuver> graph = new DirectedWeightedMultigraph<Waypoint, SpatialManeuver>(new DummyEdgeFactory<Waypoint, SpatialManeuver>());

        Waypoint waypoints[] = new Waypoint[nPoints];
        int waypointCounter = 0;
        Random random = new Random(seed);

        // Generate vertices
        for (int i=0; i < nPoints; i++) {
                Waypoint w = new Waypoint(waypointCounter++, random.nextDouble()*sizeX, random.nextDouble()*sizeY);
                waypoints[i] = w;
                graph.addVertex(w);
        }

        // Generate edges
        for (int i=0; i < nPoints; i++) {

            // Find a given number of closest vertices
            Set<Waypoint> neighbors = new HashSet<Waypoint>();
            for (int n=0; n < branchFactor; n++) {

                // Find the closest point
                Waypoint closestWaypoint = null;
                Double closestDist = Double.MAX_VALUE;

                for (int j = 0; j < nPoints; j++) {
                    if (waypoints[i].distance(waypoints[j]) < closestDist
                            && i != j
                            && !neighbors.contains(waypoints[j])
                    ) {
                        closestDist = waypoints[i].distance(waypoints[j]);
                        closestWaypoint = waypoints[j];
                    }
                }
                neighbors.add(closestWaypoint);
            }

            for (Waypoint neighbor : neighbors) {
                if (graph.getEdge(waypoints[i], neighbor) == null) {
                    SpatialManeuver maneuver = new Straight(waypoints[i], neighbor, speed);
                    graph.addEdge(waypoints[i], neighbor, maneuver);
                }
            }
        }
        return graph;
    }


}
