package tt.euclid2i.probleminstance;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Random;

import tt.euclid2i.Point;
import tt.euclid2i.region.Rectangle;
import tt.euclid2i.region.Region;

public class Environment {

    protected int seed;
    protected Rectangle bounds;
    protected Collection<Region> obstacles = new LinkedList<Region>();

    public Environment(int n, int maxSize, int seed) {
        Random random = new Random(seed);
        bounds = new Rectangle(new Point(0,0), new Point(1000,1000));
        this.seed = seed;
        createObstacles(n, maxSize, random);
    }

    private void createObstacles(int n, int maxSize, Random random) {
        for (int i=0; i<n; i++) {
            int size = random.nextInt(maxSize);
            int x = bounds.getCorner1().x + random.nextInt(bounds.getCorner2().x - bounds.getCorner1().x);
            int y = bounds.getCorner1().y + random.nextInt(bounds.getCorner2().y - bounds.getCorner1().y);
            Rectangle obstacle = new Rectangle(new Point(x, y), new Point(x+size,y+size));
            obstacles.add(obstacle);
        }
    }

    public Collection<Region> getObstacles() {
        return obstacles;
    }

    public Rectangle getBounds() {
        return bounds;
    }

    public int getSeed() {
        return seed;
    }

}
