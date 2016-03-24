package tt.euclidyaw3i.demo;

import cz.agents.alite.creator.Creator;
import cz.agents.alite.planner.spatialmaneuver.maneuver.Maneuver;
import cz.agents.alite.vis.VisManager;
import cz.agents.alite.vis.VisManager.SceneParams;
import cz.agents.alite.vis.layer.common.ColorLayer;
import cz.agents.alite.vis.layer.common.VisInfoLayer;
import cz.agents.alite.vis.layer.toggle.KeyToggleLayer;
import org.jgrapht.DirectedGraph;
import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.AStarShortestPath;
import org.jgrapht.alg.AStarShortestPathSimple;
import org.jgrapht.util.HeuristicToGoal;
import org.jgrapht.util.heuristics.ZeroHeuristic;
import tt.euclid2i.Line;
import tt.euclid2i.Point;
import tt.euclid2i.Region;
import tt.euclid2i.discretization.LazyGrid;
import tt.euclid2i.discretization.VisibilityGraph;
import tt.euclid2i.region.Circle;
import tt.euclid2i.region.Polygon;
import tt.euclid2i.region.Rectangle;
import tt.euclid2i.util.Util;
import tt.euclid2i.vis.ProjectionTo2d;
import tt.euclid2i.vis.RegionsLayer;
import tt.euclid2i.vis.RegionsLayer.RegionsProvider;
import tt.euclidyaw3i.discretization.ManeuverTree;
import tt.euclidyaw3i.discretization.PathSegment;
import tt.euclidyaw3i.discretization.PathSegmentGraphLayer;
import tt.euclidyaw3i.vis.AxisLayer;
import tt.euclidyaw3i.vis.FootPrintLayer;
import tt.euclidyaw3i.vis.PathSegmentLayer;
import tt.euclidyaw3i.vis.PointLayer;
import tt.vis.GraphLayer;
import tt.vis.GraphLayer.GraphProvider;
import tt.vis.GraphPathLayer;
import tt.vis.GraphPathLayer.PathProvider;

import javax.vecmath.Point2d;
import java.awt.Color;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class ParkingDemo implements Creator {

    final static Polygon carFootprint = (new Rectangle(new Point(-1000, -900), new Point(3000, 900))).toPolygon();
    final static Polygon duckiebotFootprint = (new Rectangle(new Point(-1600, -900), new Point(700, 900))).toPolygon();

    final static Polygon smallDuckiebotFootprint = (new Rectangle(new Point(-160 * 2, -90 * 2), new Point(70 * 2, 90 * 2))).toPolygon();

    List<Polygon> obstacles = new LinkedList<>();

    @Override
    public void init(String[] strings) {}

    public ParkingDemo() {
        initVisualization();

        obstacles.addAll(createColumns(6,6));
        // create other cars
        obstacles.add(carFootprint.getRotated(new Point(0,0), 0.1f).getTranslated(new Point(3200, 1500)));
        obstacles.add(carFootprint.getRotated(new Point(0,0), -0.05f).getTranslated(new Point(2600, 9300)));
    }

    public void obstaclesDemo() {
        final Polygon footprint = duckiebotFootprint;
        VisManager.registerLayer(RegionsLayer.create(() -> obstacles, Color.BLACK, Color.BLACK));
    }

    public void minkowskiDemo(Polygon footprint) {
        List<Polygon> minkowskiObstacles = new LinkedList<>();

        float[] angleHolder = new float[1];

        VisManager.registerLayer(RegionsLayer.create(() -> minkowskiObstacles, Color.LIGHT_GRAY, Color.LIGHT_GRAY));
        VisManager.registerLayer(RegionsLayer.create(() -> obstacles, Color.BLACK, Color.BLACK));

        VisManager.registerLayer(FootPrintLayer.create(footprint, () ->
                        Collections.singleton(new tt.euclidyaw3i.Point(0,0,angleHolder[0])),
                Color.RED, null));

        for (int i=0; i < 1000; i++) {

            float angle = (float) ((((i % 100)-50) / 100.0) * 2 * Math.PI);
            Polygon footprintR = footprint.getRotated(new tt.euclid2i.Point (0,0),angle);
            angleHolder[0] = angle;

            minkowskiObstacles.clear();
            for (Polygon obstacle : obstacles) {
                minkowskiObstacles.add(obstacle.minkowskiSum(footprintR));
            }

            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void configurationSpaceDemo() {
        final Polygon footprint = duckiebotFootprint;
        VisManager.registerLayer(FootPrintLayer.create(footprint, () ->
                Collections.singleton(new tt.euclidyaw3i.Point(0, 0, 0)),
                Color.RED, null));

        VisManager.registerLayer(FootPrintLayer.create(footprint, () ->
                        Collections.singleton(new tt.euclidyaw3i.Point(5000, 3000, (float) Math.PI / 4)),
                Color.RED, null));

        VisManager.registerLayer(FootPrintLayer.create(footprint, () ->
                        Collections.singleton(new tt.euclidyaw3i.Point(0, 5000, (float) Math.PI)),
                Color.RED, null));
    }

    public void visGraphDemo(Polygon footprint, int agentRadius) {
        tt.euclidyaw3i.Point initConf = new tt.euclidyaw3i.Point(0, 0, (float) Math.PI/2);
        tt.euclidyaw3i.Point goalConf = new tt.euclidyaw3i.Point(4000, 5500, (float) 0);

        obstacles.clear();
        obstacles.addAll(createColumns(0,3));
        // create other cars
        obstacles.add(carFootprint.getRotated(new Point(0,0), 0.1f).getTranslated(new Point(3200, 1500)));

        Collection<Region> inflatedObstaclesForCollisionChecking = Util.inflateRegions(obstacles, agentRadius);
        Collection<Region> inflatedObstaclesForGraph = Util.inflateRegions(obstacles, agentRadius+5);

        DirectedGraph<Point, Line> visGraph = VisibilityGraph.createVisibilityGraph(
                initConf.getPos(),
                goalConf.getPos(),
                inflatedObstaclesForCollisionChecking,
                inflatedObstaclesForGraph);

        VisManager.registerLayer(RegionsLayer.create(() -> inflatedObstaclesForGraph, Color.GRAY));


        VisManager.registerLayer(
                KeyToggleLayer.create("g", false,
                GraphLayer.create(
                        () -> visGraph
                        , new tt.euclid2i.vis.ProjectionTo2d(), Color.LIGHT_GRAY, Color.GRAY, 1,4)
                 ));

        VisManager.registerLayer(FootPrintLayer.create(footprint, () -> Collections.singleton(initConf),
                Color.BLACK, Color.RED));
        VisManager.registerLayer(RegionsLayer.create(() -> Collections.singleton(new Circle(goalConf.getPos(), agentRadius)), Color.RED, null));

        VisManager.registerLayer(RegionsLayer.create(() -> obstacles, Color.BLACK, Color.BLACK));
        VisManager.registerLayer(RegionsLayer.create(() -> Collections.singleton(new Circle(new Point(0,0), agentRadius)), Color.RED, null));

        final GraphPath<Point, Line> path = AStarShortestPathSimple.findPathBetween(visGraph, new ZeroHeuristic(), initConf.getPos(), goalConf.getPos());

        VisManager.registerLayer(
                KeyToggleLayer.create("p", false,
                GraphPathLayer.create(
                () -> path,
                new ProjectionTo2d(), Color.BLUE, Color.BLUE, 2,4)));

    }

    public void motionPrimitivesForCar() {
        Polygon footprint = carFootprint;
        tt.euclidyaw3i.Point initConf = new tt.euclidyaw3i.Point(0, 0, (float) Math.PI / 2);

        //visualize initial configuration
        VisManager.registerLayer(FootPrintLayer.create(footprint, () -> Collections.singleton(initConf), Color.RED, null));

        Collection<PathSegment> maneuvers = ManeuverTree.getConstantCurvatureArcs(new double[]{-0.1 / 1000.0, -0.05 / 1000.0, 0, +0.05 / 1000.0, 0.1 / 1000.0}, 4000.0f , 100.0f);
        Collection<PathSegment> maneuversFromInit = ManeuverTree.applyManeuvers(initConf, maneuvers);

        VisManager.registerLayer(PathSegmentLayer.create(() -> maneuversFromInit, Color.BLUE));
    }

    public void motionPrimitivesForDuckiebot() {
        Polygon footprint = duckiebotFootprint;
        tt.euclidyaw3i.Point initConf = new tt.euclidyaw3i.Point(0, 0, (float) Math.PI / 2);

        //visualize initial configuration
        VisManager.registerLayer(FootPrintLayer.create(footprint, () -> Collections.singleton(initConf), Color.RED, null));

        Collection<PathSegment> maneuvers = ManeuverTree.getStraights(
                new double[] { 0, Math.PI / 4, Math.PI / 2, (3.0/4.0) * Math.PI, Math.PI,
                                  -Math.PI / 4, -Math.PI / 2, -(3.0/4.0) * Math.PI, -Math.PI}, 4000.0f, 100.0f);

        Collection<PathSegment> maneuversFromInit = ManeuverTree.applyManeuvers(initConf, maneuvers);

        VisManager.registerLayer(PathSegmentLayer.create(() -> maneuversFromInit, Color.BLUE));
    }

    public void maneuverTree(Polygon footprint) {
        tt.euclidyaw3i.Point initConf = new tt.euclidyaw3i.Point(0, 0, (float) Math.PI/2);
        tt.euclidyaw3i.Point goalConf = new tt.euclidyaw3i.Point(4000, 5500, (float) 0);

        //visualize initial configuration
        VisManager.registerLayer(FootPrintLayer.create(footprint, () -> Collections.singleton(initConf), Color.RED, null));

        Collection<PathSegment> maneuvers = ManeuverTree.getConstantCurvatureArcs(new double[]{-0.1/1000.0, -0.05/1000.0, 0, +0.05/1000.0, 0.1/1000.0}, 4000.0f, 1000.0f);
        Collection<PathSegment> maneuversFromInit = ManeuverTree.applyManeuvers(initConf, maneuvers);

        VisManager.registerLayer(PathSegmentLayer.create(() -> maneuversFromInit, Color.BLUE));

        DirectedGraph<tt.euclidyaw3i.Point, PathSegment> graph = ManeuverTree.buildTree(initConf, maneuvers, 1000, footprint, obstacles);

        VisManager.registerLayer(PathSegmentGraphLayer.create(graph, false));

//        VisManager.registerLayer(
//                KeyToggleLayer.create("g", false,
//                        GraphLayer.create(
//                                () -> visGraph
//                                , new tt.euclid2i.vis.ProjectionTo2d(), Color.LIGHT_GRAY, Color.GRAY, 1,4)
//                ));



        // visualize obstacles
        VisManager.registerLayer(RegionsLayer.create(() -> obstacles, Color.BLACK, Color.BLACK));

        // find path
        //final GraphPath<Point, Line> path = AStarShortestPathSimple.findPathBetween(visGraph, new ZeroHeuristic(), initConf.getPos(), goalConf.getPos());

        // visualize path
//        VisManager.registerLayer(
//                KeyToggleLayer.create("p", false,
//                        GraphPathLayer.create(
//                                () -> path,
//                                new ProjectionTo2d(), Color.BLUE, Color.BLUE, 2,4)));

    }

    @Override
    public void create() {
        final Polygon footprint = duckiebotFootprint;

        tt.euclidyaw3i.Point initConf = new tt.euclidyaw3i.Point(0, 0, (float) Math.PI/2);
        tt.euclidyaw3i.Point goalConf = new tt.euclidyaw3i.Point(4000, 5500, (float) 0);

        VisManager.registerLayer(FootPrintLayer.create(footprint, () -> Collections.singleton(initConf),
                Color.BLACK, Color.RED));

        VisManager.registerLayer(FootPrintLayer.create(footprint, () -> Collections.singleton(goalConf),
                Color.RED, null));

        VisManager.registerLayer(RegionsLayer.create(() -> obstacles, Color.BLACK, Color.BLACK));







//        // Show initial configuration
//        VisManager.registerLayer(RegionsLayer.create(new RegionsProvider() {
//            @Override
//            public Collection<? extends Region> getRegions() {
//                Polygon r = footprint.getTranslated(initConf.getPos());
//                r = r.getRotated(new Point(0,0), initConf.getYawInRads());
//                return Collections.singleton(r);
//            }
//        }, Color.RED, null));
//
//        // Show goal configuration
//        VisManager.registerLayer(RegionsLayer.create(new RegionsProvider() {
//            @Override
//            public Collection<? extends Region> getRegions() {
//                Polygon r = footprint.getRotated(new Point(0,0), goalConf.getYawInRads());
//                r = r.getTranslated(goalConf.getPos());
//                return Collections.singleton(r);
//            }
//        }, Color.BLUE, null));

//        // plan the shortest path between these two points
//        final Point start = new Point(-5, -35);
//        final Point goal = new Point(0, 30);
//
//        // obstacle to avoid
//        Region obstacle = new Circle(new Point(0, 0), 22);
//        final Collection<Region> obstacles = Arrays.asList(new Region[] {obstacle});
//
//        // create grid discretization
//        // Note that this is "lazy" implementation, i.e. the graph is not stored in memory,
//        // but instead edges are computed on request
//        final DirectedGraph<Point, Line> graph = new LazyGrid(start,
//                obstacles, new Rectangle(new Point(-50, -50),
//                        new Point(50, 50)), LazyGrid.PATTERN_8_WAY, 5);
//
//        // or  a roadmap...
//        //        Rectangle bounds = new Rectangle(new Point(-100, -100), new Point(200,200));
//        //        final DirectedGraph<Point, Line> graph = new ProbabilisticRoadmap(1000, 14, new Point[] {start, goal}, bounds, obstacles , new Random(1));
//
//
//
//        // visualize graph
//        VisManager.registerLayer(GraphLayer.create(new GraphProvider<Point, Line>() {
//
//            @Override
//            public Graph<Point, Line> getGraph() {
//                if (graph instanceof LazyGrid) {
//                    return ((LazyGrid) graph).generateFullGraph();
//                } else {
//                    return graph;
//                }
//            }
//        }, new ProjectionTo2d(), Color.GRAY, Color.GRAY, 1, 4));
//
//        // visualize obstacles
//        VisManager.registerLayer(RegionsLayer.create(new RegionsProvider() {
//
//			@Override
//			public Collection<? extends Region> getRegions() {
//				return obstacles;
//			}
//		}, Color.BLACK));
//
//        // run A* on the graph
//        final GraphPath<Point, Line> pathBetween = AStarShortestPath.findPathBetween(graph, new HeuristicToGoal<Point>() {
//
//            @Override
//            public double getCostToGoalEstimate(Point current) {
//                return current.distance(goal);
//            }
//        }, start, goal);
//
//        // visualize the shortest path
//        VisManager.registerLayer(GraphPathLayer.create(new PathProvider<Point, Line>() {
//
//            @Override
//            public GraphPath<Point, Line> getPath() {
//                return pathBetween;
//            }
//
//        }, new ProjectionTo2d(), Color.RED, Color.RED, 2, 4));


    }




    private List<Polygon> createColumns(int nLeft, int nRight) {
        List<Polygon> obstacles = new LinkedList<>();

        int ystart = -500;
        int yspacing = 4000;
        int size = 600;

        for (int i = 0; i < nLeft; i++) {
            obstacles.add((new Rectangle(-5000, ystart + i*yspacing - size/2, -5000+size, ystart + i*yspacing + size/2)).toPolygon());
        }

        for (int i = 0; i < nRight; i++) {
            obstacles.add((new Rectangle(1500, ystart + i*yspacing - size/2, 1500+size, ystart + i*yspacing + size/2)).toPolygon());
        }

        return obstacles;
    }

    private void initVisualization() {
        VisManager.setInitParam("Trajectory Tools Vis", 1024, 768, 30000, 30000);
        VisManager.setSceneParam(new SceneParams() {

            @Override
            public Point2d getDefaultLookAt() {
                return new Point2d(0, 0);
            }

            @Override
            public double getDefaultZoomFactor() {
                return 0.05;
            }
        });

        VisManager.init();
        VisManager.setInvertYAxis(true);

        // background
        VisManager.registerLayer(ColorLayer.create(Color.WHITE));

        // Overlay
        VisManager.registerLayer(VisInfoLayer.create());

        VisManager.registerLayer(AxisLayer.create(1000));
    }


    public static void main(String[] args) {

        // Visibility graph demo
        //new ParkingDemo().visGraphDemo(ParkingDemo.smallDuckiebotFootprint, 400);

        // Maneuver family demo
        //new ParkingDemo().motionPrimitivesForCar();

        // Maneuver family demo
        //new ParkingDemo().motionPrimitivesForDuckiebot();

        // Maneuver tree demo
        new ParkingDemo().maneuverTree(ParkingDemo.carFootprint);

    }

}
