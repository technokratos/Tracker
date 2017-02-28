package checks.history;

import checks.types.P2t;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

/**
 * Created by denis on 04.02.17.
 */
public class DepthContainer implements HistoryContainer {
    private final int depth;
    private int currentPosition = 0;

    private final ConcurrentMap<Long, P2t[]> pointMap = new ConcurrentHashMap<>();
    private final ConcurrentMap<Long, P2t> firstMap = new ConcurrentHashMap<>();

    public DepthContainer(int depth) {
        this.depth = depth;
    }

    public void add(List<P2t> tracks) {

        tracks.forEach(t-> {
            P2t[] p2 = this.pointMap.computeIfAbsent(t.getId(), k -> new P2t[depth]);
            firstMap.putIfAbsent(t.getId(), t);
            p2[currentPosition] = t;
        });

        currentPosition++;
        if (currentPosition == depth) {
            currentPosition = 0;
        }


        List<Long> collectId = tracks.stream().map(P2t::getId).collect(Collectors.toList());
        firstMap.keySet().retainAll(collectId);
        pointMap.keySet().retainAll(collectId);

    }

    @Override
    public P2t getPrev(P2t p2) {
        return getPrev(p2, 1);
    }

    public P2t getPrev(P2t p2, int back) {
        P2t[] p2s = pointMap.get(p2.getId());
        if (p2s != null) {

            int prevPosition = currentPosition - back;
            //System.out.println("prev " + prevPosition +" current position " + currentPosition + " depth " + depth + " back "+ back );
            if (prevPosition < 0) {
                prevPosition = depth + prevPosition;
                if( prevPosition < 0) {
                    return null;
                }
            }
//            if (prevPosition == 5) {
//                throw new IllegalStateException("current position " + currentPosition + " depth " + depth + " back "+ back );
//            }
            P2t re = p2s[prevPosition];
            if (p2.equals(re)) {
                System.out.println();
            }
            return re;
        } else {
            return null;
        }
    }


    @Override
    public void reset() {
        firstMap.clear();
    }
}
