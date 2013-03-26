package tt.euclid2i;

import javax.vecmath.Point2d;


public class Line {
    private static final long serialVersionUID = -2519868162204278196L;

    private Point start;
    private Point end;

    public Line(Point start, Point end) {
        super();
        this.start = start;
        this.end = end;
    }

    public double getDistance() {
        return new Point2d(start.x, start.y).distance(new Point2d(end.x, end.y));
    }

    public Point getStart() {
        return start;
    }

    public Point getEnd() {
        return end;
    }

    @Override
    public String toString() {
        return String.format("(%s : %s)", start, end);
    }

    /*
    @Override
    public Trajectory getTrajectory(final double startTime) {
        return new Trajectory() {

            @Override
            public OrientedPoint getPosition(double t) {
                if (t < startTime || t > startTime + getDuration())
                    throw new IllegalArgumentException("The position for time " + t + " which is undefined for this trajectory. Length: " + getDistance() + ". Trajectory defined for interval (" + startTime + ", " + (startTime + getDuration()) + ")");

                if (getDuration() > 0) {
                    double alpha = (t - startTime) / getDuration();
                    assert(alpha >= -0.00001 && alpha <= 1.00001);

                    Euclidean2dPoint pos = Euclidean2dPoint.interpolate(new Euclidean2dPoint(start.x, start.y), new Euclidean2dPoint(end.x, end.y), alpha);
                    Vector2d dir = new Vector2d(0,1);
                    if (!end.equals(start)) {
                        dir.sub(new Point2d(end.x, end.y), new Point2d(start.x, start.y));
                        dir.normalize();
                    }
                    return new OrientedPoint(new SpatialPoint(pos.x, pos.y, 0), new Vector(dir.x, dir.y, 0));
                } else {
                    return new OrientedPoint(new SpatialPoint(start.x, start.y, 0) , new Vector(0, 1, 0));
                }


            }

            @Override
            public double getMinTime() {
                return startTime;
            }

            @Override
            public double getMaxTime() {
                return startTime + getDuration();
            }
        };
    }*/


}
