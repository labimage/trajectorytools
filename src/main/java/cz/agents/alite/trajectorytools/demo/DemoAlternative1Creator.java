package cz.agents.alite.trajectorytools.demo;

import java.awt.Color;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultWeightedEdge;

import cz.agents.alite.creator.Creator;
import cz.agents.alite.trajectorytools.alterantiveplanners.AlternativePathPlanner;
import cz.agents.alite.trajectorytools.alterantiveplanners.AlternativePlannerSelector;
import cz.agents.alite.trajectorytools.alterantiveplanners.DifferentStateMetricPlanner;
import cz.agents.alite.trajectorytools.alterantiveplanners.ObstacleExtensions;
import cz.agents.alite.trajectorytools.alterantiveplanners.TrajectoryDistanceMaxMinMetricPlanner;
import cz.agents.alite.trajectorytools.alterantiveplanners.TrajectoryDistanceMetricPlanner;
import cz.agents.alite.trajectorytools.alterantiveplanners.VoronoiDelaunayPlanner;
import cz.agents.alite.trajectorytools.graph.ObstacleGraphView;
import cz.agents.alite.trajectorytools.graph.ObstacleGraphView.ChangeListener;
import cz.agents.alite.trajectorytools.graph.spatial.SpatialGraphs;
import cz.agents.alite.trajectorytools.graph.spatial.SpatialGridFactory;
import cz.agents.alite.trajectorytools.graph.spatial.maneuvers.SpatialManeuver;
import cz.agents.alite.trajectorytools.planner.AStarPlanner;
import cz.agents.alite.trajectorytools.planner.HeuristicFunction;
import cz.agents.alite.trajectorytools.planner.PlannedPath;
import cz.agents.alite.trajectorytools.trajectorymetrics.DifferentStateMetric;
import cz.agents.alite.trajectorytools.trajectorymetrics.ObstacleAvoidanceMetric;
import cz.agents.alite.trajectorytools.trajectorymetrics.TrajectoryDistanceMetric;
import cz.agents.alite.trajectorytools.trajectorymetrics.TrajectoryMetric;
import cz.agents.alite.trajectorytools.trajectorymetrics.TrajectorySetMetrics;
import cz.agents.alite.trajectorytools.util.SpatialPoint;
import cz.agents.alite.trajectorytools.util.Waypoint;
import cz.agents.alite.trajectorytools.vis.GraphPathLayer;
import cz.agents.alite.vis.Vis;
import cz.agents.alite.vis.VisManager;
import cz.agents.alite.vis.layer.common.ColorLayer;
import cz.agents.alite.vis.layer.common.VisInfoLayer;

public class DemoAlternative1Creator implements Creator {

    private static final int WORLD_SIZE = 10;

    private static final int PATH_SOLUTION_LIMIT = 5;

    private ObstacleGraphView graph;
    private List<PlannedPath<SpatialPoint, DefaultWeightedEdge>> paths = new ArrayList<PlannedPath<SpatialPoint,DefaultWeightedEdge>>();

    private static final AStarPlanner<SpatialPoint, DefaultWeightedEdge> planner = new AStarPlanner<SpatialPoint, DefaultWeightedEdge>();
    {
        planner.setHeuristicFunction(new HeuristicFunction<SpatialPoint>() {
        @Override
            public double getHeuristicEstimate(SpatialPoint current, SpatialPoint goal) {
                return current.distance(goal) + ( current.x > current.y ? 0.1 : -0.1 );
            }
        });
    }

    private static final List<AlternativePathPlanner<SpatialPoint, DefaultWeightedEdge>> alternativePlanners = new ArrayList<AlternativePathPlanner<SpatialPoint,DefaultWeightedEdge>>();
    {
        alternativePlanners.add( 
                new DifferentStateMetricPlanner<SpatialPoint, DefaultWeightedEdge>( planner, PATH_SOLUTION_LIMIT )
                );
        alternativePlanners.add( 
                new TrajectoryDistanceMetricPlanner<SpatialPoint, DefaultWeightedEdge>( planner, PATH_SOLUTION_LIMIT, 2)
                );
        alternativePlanners.add( 
                new TrajectoryDistanceMaxMinMetricPlanner<SpatialPoint, DefaultWeightedEdge>( planner, PATH_SOLUTION_LIMIT, 2 )
                );
        alternativePlanners.add( 
                new ObstacleExtensions<SpatialPoint, DefaultWeightedEdge>(planner) 
                );
        alternativePlanners.add( 
                new AlternativePlannerSelector<SpatialPoint, DefaultWeightedEdge>( new ObstacleExtensions<SpatialPoint, DefaultWeightedEdge>(planner), PATH_SOLUTION_LIMIT)
                );
        alternativePlanners.add( 
                new VoronoiDelaunayPlanner<SpatialPoint, DefaultWeightedEdge>( planner )
                );
        alternativePlanners.add( 
                new AlternativePlannerSelector<SpatialPoint, DefaultWeightedEdge>( new VoronoiDelaunayPlanner<SpatialPoint, DefaultWeightedEdge>(planner), PATH_SOLUTION_LIMIT)
                );
    }

    private static final List<TrajectoryMetric<SpatialPoint, DefaultWeightedEdge>> trajectoryMetrics = new ArrayList<TrajectoryMetric<SpatialPoint,DefaultWeightedEdge>>();
    {
        trajectoryMetrics.add( 
                new DifferentStateMetric<SpatialPoint, DefaultWeightedEdge>()
                );
        trajectoryMetrics.add( 
                new TrajectoryDistanceMetric<SpatialPoint, DefaultWeightedEdge>()
                );
        trajectoryMetrics.add( 
                new ObstacleAvoidanceMetric<SpatialPoint, DefaultWeightedEdge>()
                );
    }

    private static final int CURRENT_PLANNER = 4;
    @Override
    public void init(String[] args) {
    }

    @Override
    public void create() {
        Graph<Waypoint, SpatialManeuver> originalGraph = SpatialGridFactory.create4WayGrid(WORLD_SIZE, WORLD_SIZE, WORLD_SIZE, WORLD_SIZE, 1.0);

        graph = ObstacleGraphView.createFromGraph(originalGraph, new ChangeListener() {
            @Override
            public void graphChanged() {
                replan();
            }
        } );

        createVisualization();
    }

    private void createVisualization() {
        VisManager.setInitParam("Trajectory Tools Vis", 1024, 768, 20, 20);
        VisManager.setPanningBounds(new Rectangle(-500, -500, 1600, 1600));
        VisManager.init();

        Vis.setPosition(50, 50, 1);

        // background
        VisManager.registerLayer(ColorLayer.create(Color.WHITE));

        // graph with obstacles
        graph.createVisualization();

        // draw the shortest path
        VisManager.registerLayer(GraphPathLayer.create(graph, paths, 2, 4));

        // Overlay
        VisManager.registerLayer(VisInfoLayer.create());
    }

    protected void replan() {
           try {
                paths.clear();

                long startTime = System.currentTimeMillis();

                paths.addAll(
                        alternativePlanners.get(CURRENT_PLANNER).planPath(
                                graph,
                                SpatialGraphs.getNearestVertex(graph, new SpatialPoint(0, 0, 0)),
                                SpatialGraphs.getNearestVertex(graph, new SpatialPoint(WORLD_SIZE, WORLD_SIZE, 0))
                                ) );

                System.out.println("Time: " + (System.currentTimeMillis() - startTime) + " ms");

                System.out.println("paths: " + paths.size());
                for (PlannedPath<SpatialPoint, DefaultWeightedEdge> path : paths) {
                    System.out.println("path.getWeight(): " + path.getWeight());
                }

                for (TrajectoryMetric<SpatialPoint, DefaultWeightedEdge> metric : trajectoryMetrics) {
                    System.out.println(metric.getName() + ": " + TrajectorySetMetrics.getPlanSetAvgDiversity(paths, metric));
                }

            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
                e.printStackTrace();
                paths.clear();
            }
    }
}
