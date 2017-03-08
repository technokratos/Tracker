package checks.processors.operations;

import boofcv.abst.feature.tracker.PointTracker;
import checks.history.HistoryTracker;

import java.util.function.BiFunction;

/**
 * Created by denis on 07.03.17.
 */
public abstract class ContextFunction<T, U,R> implements BiFunction<T, U,R> {

    protected HistoryTracker tracker;

    public void setTracker(HistoryTracker tracker) {
        this.tracker = tracker;
    }
}
