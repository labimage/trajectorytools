package cz.agents.alite.trajectorytools.trajectorymetrics;

import cz.agents.alite.trajectorytools.planner.PlannedPath;

/**
 * Trajectory Distance Metric 
 * 
 * Used e.g. in 'Alexandra Coman: Generating Diverse Plans Using Quantitative and Qualitative Plan Distance Metrics'.
 * 
 * @author honza
 *
 * @param <V> 
 * @param <E>
 */
public interface TrajectoryMetric<V, E> {

    double getTrajectoryDistance(PlannedPath<V, E> path, PlannedPath<V, E> otherPath);
    
    String getName();
}
