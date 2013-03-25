package tt.euclid2i.probleminstance;

import java.util.Collection;
import java.util.Random;

import tt.euclid2i.Point;
import tt.euclid2i.region.Rectangle;
import tt.euclid2i.region.Region;
import tt.euclid2i.util.Util;

public class ShortestPathProblem {

    final protected int targetRegionSide = 30;
    protected int seed;
    protected Environment environment;
    protected Point start;
    protected Region targetRegion;
    protected Point targetPoint;


    public ShortestPathProblem(int seed) {
        this.seed = seed;
        this.environment = new Environment(40, 200,seed);
        Random random = new Random(seed);

        start = Util.sampleFreeSpace(environment.getBounds(), environment.getObstacles(), random);
        targetPoint = Util.sampleFreeSpace(environment.getBounds(), environment.getObstacles(), random);
        targetRegion = new Rectangle(
                new Point(targetPoint.x - targetRegionSide / 2, targetPoint.y - targetRegionSide / 2),
                new Point(targetPoint.x + targetRegionSide / 2, targetPoint.y + targetRegionSide / 2));
    }


    public Collection<Region> getObstacles() {
        return environment.getObstacles();
    }

    public Point getStart() {
        return start;
    }

    public Point getTargetPoint() {
        return targetPoint;
    }

    public Region getTargetRegion() {
        return targetRegion;
    }

    public Rectangle getBounds() {
        return environment.getBounds();
    }

    public int getSeed() {
        return seed;
    }

    public int getTargetRegionRadius() {
        return targetRegionSide / 2;
    }

}
