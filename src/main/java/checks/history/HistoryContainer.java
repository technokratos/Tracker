package checks.history;

import checks.types.P2t;

import java.util.List;

/**
 * Created by denis on 04.02.17.
 */
public interface HistoryContainer {

    public void add(List<P2t> tracks);

    public P2t getPrev(P2t p2);

    void reset();
}
