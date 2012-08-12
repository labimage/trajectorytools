package cz.agents.alite.trajectorytools.graph.jointspatiotemporal.rrtstar;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Random;

import cz.agents.alite.trajectorytools.graph.jointspatiotemporal.JointManeuver;
import cz.agents.alite.trajectorytools.graph.jointspatiotemporal.JointState;
import cz.agents.alite.trajectorytools.graph.spatiotemporal.maneuvers.SpatioTemporalManeuver;
import cz.agents.alite.trajectorytools.graph.spatiotemporal.region.Box4dRegion;
import cz.agents.alite.trajectorytools.graph.spatiotemporal.region.Region;
import cz.agents.alite.trajectorytools.graph.spatiotemporal.rrtstar.GuidedStraightLineDomain;
import cz.agents.alite.trajectorytools.graph.spatiotemporal.rrtstar.SpatioTemporalStraightLineDomain;
import cz.agents.alite.trajectorytools.planner.rrtstar.Domain;
import cz.agents.alite.trajectorytools.planner.rrtstar.Extension;
import cz.agents.alite.trajectorytools.planner.rrtstar.ExtensionEstimate;
import cz.agents.alite.trajectorytools.trajectory.Trajectory;
import cz.agents.alite.trajectorytools.util.SeparationDetector;
import cz.agents.alite.trajectorytools.util.SpatialPoint;
import cz.agents.alite.trajectorytools.util.TimePoint;

public class SynchronousJointSpatioTemporalStraightLineDomain implements Domain<JointState, JointManeuver> {

    SpatioTemporalStraightLineDomain[] domains;
    double separation;
    private Box4dRegion bounds;
    private Random random;
	private double samplingInterval;
    
    public SynchronousJointSpatioTemporalStraightLineDomain(Box4dRegion bounds, double separation, TimePoint[] initialPoints,
            Collection<Region> obstacles, SpatialPoint[] targets, double targetReachedTolerance, double minSpeed,
            double optSpeed, double maxSpeed, double maxPitch, Random random) {
        super();

        this.separation = separation;
        this.bounds = bounds;
        this.random = random;
        this.samplingInterval = (separation / 4) / maxSpeed;

        assert(separated(initialPoints, separation));
        
        domains = new SpatioTemporalStraightLineDomain[initialPoints.length];
        
        for (int i = 0; i < initialPoints.length; i++) {
            domains[i] = new GuidedStraightLineDomain(bounds,
                    initialPoints[i], obstacles, targets[i],
                    targetReachedTolerance, minSpeed, optSpeed, maxSpeed,
                    maxPitch, random);
        }


    }

    @Override
    public JointState sampleState() {
        TimePoint[] points = new TimePoint[nAgents()];
        do {
            double t = bounds.getCorner1().w + random.nextDouble() * (bounds.getCorner2().w - bounds.getCorner1().w);
            for (int i = 0; i < nAgents(); i++) {
                points[i] = domains[i].sampleState(t);
            }
        } while (!separated(points, separation)); /* not necessary probably, will be checked when extending anyway */

        return new JointState(points);
    }

    @Override
    public Extension<JointState, JointManeuver> extendTo(JointState from, JointState to) {

        double cost = 0.0;
        boolean exact = true;
        SpatioTemporalManeuver[] maneuvers = new SpatioTemporalManeuver[from.nAgents()];
        TimePoint[] targets = new TimePoint[from.nAgents()];

        for (int i = 0; i < from.nAgents(); i++) {
            Extension<TimePoint, SpatioTemporalManeuver> extension = domains[i].extendMaintainTime(from.get(i), to.get(i));
            if (extension != null) {
                maneuvers[i] = extension.edge;
                targets[i] = extension.target;
                cost += extension.cost;
                if (!extension.exact) {
                    exact = false;
                }
            } else {
                // extension not possible
                return null;
            }
        }

        // Check separation between individual trajectories

        Collection<Trajectory> trajectories = new LinkedList<Trajectory>();
        for(int i = 0; i < maneuvers.length; i++) {
            trajectories.add(maneuvers[i].getTrajectory());
        }

        if (!SeparationDetector.hasConflict(trajectories, separation, samplingInterval)) {
            return new Extension<JointState, JointManeuver> (from, new JointState(targets), new JointManeuver(maneuvers), cost, exact);
        }
        return null;

    }

    @Override
    public ExtensionEstimate estimateExtension(JointState from, JointState to) {
        double cost = 0.0;
        boolean exact = true;
        for (int i = 0; i < from.nAgents(); i++) {
            Extension<TimePoint, SpatioTemporalManeuver> extension = domains[i].extendMaintainTime(from.get(i), to.get(i));
            if (extension != null) {
                cost += extension.cost;
                if (!extension.exact) {
                    exact = false;
                }
            } else {
                // extension not possible
                return null;
            }
        }
        return new ExtensionEstimate(cost, exact);
    }

    @Override
    public double estimateCostToGo(JointState s) {
        double costToGo = 0.0;
        for (int i = 0; i < s.nAgents(); i++) {
            domains[i].estimateCostToGo(s.get(i));

        }
        return costToGo;
    }

    @Override
    public double distance(JointState s1, JointState s2) {
        return s1.distance(s2);
    }

    @Override
    public double nDimensions() {
        return (3*nAgents()) + 1;
    }

    @Override
    public boolean isInTargetRegion(JointState s) {
        for (int i = 0; i < s.nAgents(); i++) {
           if (!domains[i].isInTargetRegion(s.get(i))) {
               return false;
           }
        }
        return true;
    }

    public int nAgents() {
        return domains.length;
    }

    private static boolean separated(TimePoint[] timePoints, double separation) {
        for (int i=0; i<timePoints.length; i++) {
            for (int j=i+1; j<timePoints.length; j++) {
                if (Math.abs(timePoints[i].getTime() - timePoints[j].getTime()) < 0.001) {
                    if (timePoints[i].getSpatialPoint().distance(timePoints[j].getSpatialPoint()) < separation) {
                        return false;
                    }
                }
            }
        }

        return true;
    }



}
