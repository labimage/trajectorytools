package cz.agents.alite.trajectorytools.graph.spatiotemporal.rrtstar;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;

import cz.agents.alite.trajectorytools.graph.spatiotemporal.region.Box4dRegion;
import cz.agents.alite.trajectorytools.graph.spatiotemporal.region.Region;
import cz.agents.alite.trajectorytools.util.SpatialPoint;
import cz.agents.alite.trajectorytools.util.TimePoint;

public class GuidedStraightLineDomain extends SpatioTemporalStraightLineDomain {


    Queue<TimePoint> samplesPool = new LinkedList<TimePoint>();




    public GuidedStraightLineDomain(Box4dRegion bounds, TimePoint initialPoint,
            Collection<Region> obstacles, SpatialPoint target,
            double targetReachedTolerance, double minSpeed, double optSpeed,
            double maxSpeed, double maxPitch, double minEdgeLength,
            Random random) {
        super(bounds, initialPoint, obstacles, target, targetReachedTolerance,
                minSpeed, optSpeed, maxSpeed, maxPitch, minEdgeLength, random);
        samplesPool.add(getTargetSample(initialPoint));
    }




    private TimePoint getTargetSample(TimePoint from) {
        double duration = from.getSpatialPoint().distance(target) / optSpeed;
        return new TimePoint(target, from.getTime() + duration);
    }




    @Override
    public TimePoint sampleState() {
        if (samplesPool.isEmpty()) {
            // generate new random sample
            TimePoint randomSample = super.sampleState();
            samplesPool.offer(randomSample);

            // Bias towards goal at optimal speed
            samplesPool.offer(getTargetSample(randomSample));

            // Bias towards random sample from initial point at optimal speed
            double optTime = initialPoint.getTime() +
                             initialPoint.getSpatialPoint().distance(randomSample.getSpatialPoint())
                             / optSpeed;

            samplesPool.offer(new TimePoint(randomSample.x, randomSample.y, randomSample.z, optTime));
            // Bias towards start altitude
            samplesPool.offer(new TimePoint(randomSample.x, randomSample.y, initialPoint.z, randomSample.getTime()));
            // Bias towards start altitude and optimal speed
            samplesPool.offer(new TimePoint(randomSample.x, randomSample.y, initialPoint.z, optTime));
        }

        return samplesPool.poll();

    }





}