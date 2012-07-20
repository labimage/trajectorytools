package cz.agents.alite.trajectorytools.trajectory;

import cz.agents.alite.trajectorytools.util.OrientedPoint;

/**
 * A trajectory having a certain cost. Typically used as
 * a wrapper of a non-valued trajectory.
 */
public class ValuedTrajectory implements Trajectory {

    Trajectory trajectory;
    double cost;

    public ValuedTrajectory(Trajectory trajectory, double cost) {
        super();
        this.trajectory = trajectory;
        this.cost = cost;
    }

    @Override
    public double getMinTime() {
        return trajectory.getMinTime();
    }

    @Override
    public double getMaxTime() {
        return trajectory.getMaxTime();
    }

    @Override
    public OrientedPoint getPosition(double t) {
        return trajectory.getPosition(t);
    }

    public double getCost() {
        return cost;
    }
}
