package tt.euclidtime3i.vis;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.Collection;
import java.util.LinkedList;

import javax.vecmath.Point2d;
import javax.vecmath.Point3d;

import tt.euclid2i.Trajectory;
import tt.euclidtime3i.Point;
import tt.euclidtime3i.region.MovingCircle;
import tt.euclidtime3i.region.Region;
import tt.vis.ProjectionTo2d;

import cz.agents.alite.vis.Vis;
import cz.agents.alite.vis.element.implemetation.StyledPointImpl;
import cz.agents.alite.vis.layer.AbstractLayer;
import cz.agents.alite.vis.layer.VisLayer;

public class RegionsLayer extends AbstractLayer {

    public interface RegionsProvider {
        public Collection<Region> getRegions();
    }

	private static final double samplingInterval = 1.0;

    private RegionsProvider regionsProvider;
    private Color edgeColor;
    private Color fillColor;
	private ProjectionTo2d<Point> projection;

    RegionsLayer() {
    }

    public RegionsLayer(RegionsProvider regionsProvider, ProjectionTo2d<Point> projection, Color edgeColor, Color fillColor) {
        this.regionsProvider = regionsProvider;
        this.edgeColor = edgeColor;
        this.fillColor = fillColor;
        this.projection = projection;
    }

    @Override
    public void paint(Graphics2D canvas) {

        super.paint(canvas);

        Collection<Region> regions = regionsProvider.getRegions();

        for (Region region : regions) {
            if (region instanceof MovingCircle) {
                MovingCircle movingCirc = (MovingCircle) region;
                Trajectory traj = movingCirc.getTrajectory();

                for (double time = traj.getMinTime(); time < traj.getMaxTime(); time += samplingInterval) {
                    tt.euclid2i.Point pos = traj.get(time);
                    if (pos != null) {
                        Point2d projectedPoint = projection.project(new Point(pos.x, pos.y, (int) Math.round(time)));
                        if (projectedPoint != null) {

							if (fillColor != null) {
								canvas.setColor(fillColor);
								canvas.fillOval(
										Vis.transX(projectedPoint.x
												- movingCirc.getRadius()),
										Vis.transY(projectedPoint.y
												- movingCirc.getRadius()),
										Vis.transH(movingCirc.getRadius()*2),
										Vis.transW(movingCirc.getRadius()*2));
							}

							canvas.setColor(edgeColor);
							canvas.fillOval(
									Vis.transX(projectedPoint.x
											- movingCirc.getRadius()),
									Vis.transY(projectedPoint.y
											- movingCirc.getRadius()),
									Vis.transH(movingCirc.getRadius()*2),
									Vis.transW(movingCirc.getRadius()*2));
                       }
                    } else {
                        throw new RuntimeException("Position for time " + time + "s is null in trajectory " + traj);
                    }
                }
            }
       }
    }

    public static VisLayer create(final RegionsProvider regionsProvider, ProjectionTo2d<Point> projection, final Color edgeColor, final Color fillColor) {
        return new RegionsLayer(regionsProvider, projection, edgeColor, fillColor);
    }
}
