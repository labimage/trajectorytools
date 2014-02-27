package tt.euclid2i.vis;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.Collection;

import tt.euclid2i.Point;
import tt.euclid2i.Region;
import tt.euclid2i.region.Polygon;
import tt.euclid2i.region.Rectangle;
import cz.agents.alite.vis.Vis;
import cz.agents.alite.vis.layer.AbstractLayer;
import cz.agents.alite.vis.layer.VisLayer;

public class RegionsLayer extends AbstractLayer {

    public interface RegionsProvider {
        Collection<? extends Region> getRegions();
    }

    private RegionsProvider regionsProvider;
    private Color edgeColor;
    private Color fillColor;

    RegionsLayer() {
    }

    public RegionsLayer(RegionsProvider regionsProvider, Color edgeColor, Color fillColor) {
        this.regionsProvider = regionsProvider;
        this.edgeColor = edgeColor;
        this.fillColor = fillColor;
    }

    public RegionsLayer(RegionsProvider regionsProvider, Color edgeColor) {
        this(regionsProvider, edgeColor, null);
    }


    @Override
    public void paint(Graphics2D canvas) {

        super.paint(canvas);

        Collection<? extends Region> regions = regionsProvider.getRegions();

        for (Region region : regions) {
            if (region instanceof Rectangle) {
                Rectangle rect = (Rectangle) region;

                if (fillColor != null) {
                    canvas.setColor(fillColor);
                    canvas.fillRect(Vis.transX(rect.getCorner1().x), Vis.transY(rect.getCorner1().y),
                            Vis.transX(rect.getCorner2().x) - Vis.transX(rect.getCorner1().x),
                            Vis.transY(rect.getCorner2().y) - Vis.transY(rect.getCorner1().y));
                }

                canvas.setColor(edgeColor);
                canvas.drawRect(Vis.transX(rect.getCorner1().x), Vis.transY(rect.getCorner1().y),
                        Vis.transX(rect.getCorner2().x) - Vis.transX(rect.getCorner1().x),
                        Vis.transY(rect.getCorner2().y) - Vis.transY(rect.getCorner1().y));
            }

            if (region instanceof Polygon) {
                Polygon polygon = (Polygon) region;
                Point[] points = polygon.getPoints();

                int n = points.length;
                int x[] = new int[n];
                int y[] = new int[n];

                for (int i = 0; i < n; i++) {
                    x[i] = Vis.transX(points[i].x);
                    y[i] = Vis.transY(points[i].y);
                }

                if (n == 1) {
                    canvas.setColor(edgeColor);
                    canvas.fillOval(x[0], y[0], 2, 2);

                } else {
                    if (fillColor != null) {
                        if (polygon.isFilledInside()) {
                            canvas.setColor(fillColor);
                            canvas.fillPolygon(x, y, n);
                        } else {
                            // the polygon is filled outside

                            // create a larger rectangular polygon  (having the same size as the window) that
                            // encompasses the smaller one.

                            java.awt.Rectangle windowBounds = Vis.getWindowBounds();
                            int width = windowBounds.width;
                            int height = windowBounds.height;

                            int[] outX = new int[4+x.length+2];
                            int[] outY = new int[4+y.length+2];

                            outX[0] = 0; outY[0] = 0;
                            outX[1] = width; outY[1] = 0;
                            outX[2] = width; outY[2] = height;
                            outX[3] = 0; outY[3] = height;

                            for (int i=4; i<outX.length-2; i++) {
                                outX[i] = x[i-4];
                                outY[i] = y[i-4];
                            }

                            // close the inner polygon
                            outX[3+x.length+1] = x[0];
                            outY[3+y.length+1] = y[0];

                            //close the gap between inner and outer polygon
                            outX[3+x.length+2] = outX[3];
                            outY[3+y.length+2] = outY[3];

                            canvas.setColor(fillColor);
                            canvas.fillPolygon(outX, outY, outX.length);
                        }
                    }

                    canvas.setColor(edgeColor);
                    canvas.drawPolygon(x, y, n);

                    // draw normal vectors to see the inside and outside of a polygon...

                    Point[] polygonPoints = polygon.getPoints();
                    for (int i=1; i < polygonPoints.length; i++) {
                        int dx = polygonPoints[i].x - polygonPoints[i-1].x;
                        int dy = polygonPoints[i].y - polygonPoints[i-1].y;

                        double cx = (polygonPoints[i].x + polygonPoints[i-1].x) / 2.0;
                        double cy = (polygonPoints[i].y + polygonPoints[i-1].y) / 2.0;

                        double len = Math.sqrt(dx*dx + dy*dy);
                        double nx = (-dy / len) * 3;
                        double ny = (dx / len) * 3;

                        canvas.setColor(edgeColor.brighter().brighter());
                        canvas.drawLine(Vis.transX(cx), Vis.transY(cy), Vis.transX(cx) + (int) nx , Vis.transY(cy) + (int) ny);
                    }
                }
            }
        }

    }

    public static VisLayer create(final RegionsProvider regionsProvider, final Color edgeColor) {
        return new RegionsLayer(regionsProvider, edgeColor);
    }

    public static VisLayer create(final RegionsProvider regionsProvider, final Color edgeColor, final Color fillColor) {
        return new RegionsLayer(regionsProvider, edgeColor, fillColor);
    }
}
