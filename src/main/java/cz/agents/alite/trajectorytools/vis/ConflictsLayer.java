package cz.agents.alite.trajectorytools.vis;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import cz.agents.alite.trajectorytools.util.TimePoint;
import cz.agents.alite.vis.element.Circle;
import cz.agents.alite.vis.element.aggregation.CircleElements;
import cz.agents.alite.vis.element.implemetation.CircleImpl;
import cz.agents.alite.vis.layer.GroupLayer;
import cz.agents.alite.vis.layer.VisLayer;
import cz.agents.alite.vis.layer.common.CommonLayer;
import cz.agents.alite.vis.layer.terminal.CircleLayer;

public class ConflictsLayer extends CommonLayer {

    public static class ConflictsHolder {
        public List<TimePoint> conflicts;

        public ConflictsHolder(List<TimePoint> conflicts) {
            super();
            this.conflicts = conflicts;
        }
    }

    public static VisLayer create(final ConflictsHolder conflictsHolder, final double separation) {
        GroupLayer group = GroupLayer.create();

        group.addSubLayer(CircleLayer.create(new CircleElements() {

            @Override
            public Iterable<? extends Circle> getCircles() {
                ArrayList<Circle> circles = new ArrayList<Circle>();

                for (TimePoint conflict : conflictsHolder.conflicts) {
                    circles.add(new CircleImpl(conflict, separation/2.0));
                }

                return circles;
            }

            @Override
            public Color getColor() {
                return Color.RED;
            }

            @Override
            public int getStrokeWidth() {
                return 1;
            }

        }));

        return group;
    }
}
