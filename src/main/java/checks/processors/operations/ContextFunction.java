package checks.processors.operations;

import boofcv.abst.feature.tracker.PointTracker;
import checks.history.HistoryTracker;

import java.awt.*;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Created by denis on 07.03.17.
 */
public abstract class ContextFunction<T, R> implements Function<T,R> {

    protected HistoryTracker tracker;
    protected Graphics2D g2;

    public void setTracker(HistoryTracker tracker) {
        this.tracker = tracker;
    }

    public void setG2(Graphics2D g2) {
        this.g2 = g2;
    }
}
