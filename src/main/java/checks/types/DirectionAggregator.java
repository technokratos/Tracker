package checks.types;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.IntStream;

/**
 * Created by denis on 14.02.17.
 */
public class DirectionAggregator {
    List<Tuple<P2t,P2t>> listPrevAndNextPoint = new LinkedList<>();



    public void add(Tuple<P2t, P2t> prevAndNext) {

        int minCurrent = Integer.MAX_VALUE;
        int minShift = Integer.MAX_VALUE;
        int minPositionCurrent = -1;
        int minPositionShift = -1;
        IntStream.range(0, listPrevAndNextPoint.size()/2)
                .forEach(i-> {

                });

        for (int i = 0; i < listPrevAndNextPoint.size() / 2; i++) {

        }
    }

}
