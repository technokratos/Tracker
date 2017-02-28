package checks.history;

import checks.types.P2t;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

/**
 * Created by denis on 04.02.17.
 */
@Slf4j
public class FirstContainer implements HistoryContainer{

    private final ConcurrentMap<Long, P2t> firstMap = new ConcurrentHashMap<>();

    public void add(List<P2t> tracks) {

        //System.out.println("ADD size of mape: " + firstMap.size() + ", size of list" + tracks.size());
        tracks.forEach(t-> {
            firstMap.putIfAbsent(t.getId(), t);

        });
        //System.out.println("AFTER ADD size of mape: " + firstMap.size() + ", size of list" + tracks.size());
        List<Long> collectId = tracks.stream().map(P2t::getId).collect(Collectors.toList());
        firstMap.keySet().retainAll(collectId);
        //System.out.println("AFTER RETAIN size of mape: " + firstMap.size() + ", size of list" + tracks.size());
    }

    @Override
    public P2t getPrev(P2t p2) {
        return firstMap.get(p2.getId());
    }

    @Override
    public void reset() {
        firstMap.clear();
    }
}
