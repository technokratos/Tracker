package checks;

import boofcv.abst.feature.tracker.PointTrack;
import boofcv.abst.feature.tracker.PointTracker;
import boofcv.struct.image.ImageGray;
import checks.history.DepthContainer;
import checks.history.FirstContainer;
import checks.history.HistoryContainer;
import checks.types.P2t;
import checks.types.Tuple;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by denis on 01.02.17.
 */
public class HistoryTracker<I extends ImageGray> implements PointTracker<I> {
    public static final int HISTORY_DEPTH = 10;
    public static final int FIRST = 0;
    public static final int SECOND = 1;
    public static int MAX_FEATURES = 100;



    private final PointTracker<I> firstTraker;
    private final PointTracker<I> secondTracker;

    private int spawnCount = 0;
    private int spawnRepeat = 10;
//    final HistoryContainer firstContainer = new FirstContainer();//HISTORY_DEPTH);
//    final HistoryContainer secondContainer = new FirstContainer();//(HISTORY_DEPTH)

    final HistoryContainer firstContainer = new DepthContainer(HISTORY_DEPTH);
    final HistoryContainer secondContainer = new DepthContainer(HISTORY_DEPTH);

    //private final List<PointTrack> pointTracks = new ArrayList<>(2 * MAX_FEATURES);

    public HistoryTracker(PointTracker<I> klt, PointTracker<I> secondTracker) {
        firstTraker = klt;
        this.secondTracker = secondTracker;
    }

    @Override
    public void process(I t) {

        firstContainer.add(firstTraker.getActiveTracks(null).stream().map((p) -> new P2t(p, FIRST)).collect(Collectors.toList()));
        secondContainer.add(secondTracker.getActiveTracks(null).stream().map((p) -> new P2t(p, SECOND)).collect(Collectors.toList()));

        Pool.exec(()-> this.firstTraker.process(t),()-> this.secondTracker.process(t));

      //  pointTracks.clear();
        spawnTracksSeparate();

    }

    //public void addToHistory(List<PointTrack> list, )

    @Override
    public void reset() {
        //pointMap.clear();
        firstTraker.reset();
        secondTracker.reset();
    }

    @Override
    public void dropAllTracks() {
        firstTraker.dropAllTracks();
        secondTracker.dropAllTracks();
    }

    @Override
    public boolean dropTrack(PointTrack pointTrack) {
        return
                firstTraker.dropTrack(pointTrack) || secondTracker.dropTrack(pointTrack);
    }

    @Override
    public List<PointTrack> getAllTracks(List<PointTrack> list) {
        return this.getActiveTracks(list);
    }



    public List<P2t> getActiveTracks() {
        List<P2t> list = new ArrayList<>(2 * MAX_FEATURES);
        list.addAll(firstTraker.getActiveTracks(null).stream()
                .map(p -> new P2t(p, 0))
                .collect(Collectors.toList()));
        list.addAll(secondTracker.getActiveTracks(null).stream()
                .map(p -> new P2t(p, 1))
                .collect(Collectors.toList()));
        return list;
    }

    @Override
    public List<PointTrack> getActiveTracks(List<PointTrack> list) {
        return firstTraker.getActiveTracks(secondTracker.getActiveTracks(null));
    }

    @Override
    public List<PointTrack> getInactiveTracks(List<PointTrack> list) {
        return secondTracker.getInactiveTracks(firstTraker.getInactiveTracks(list));
    }

    @Override
    public List<PointTrack> getDroppedTracks(List<PointTrack> list) {
        return secondTracker.getDroppedTracks(firstTraker.getDroppedTracks(list));
    }

    @Override
    public List<PointTrack> getNewTracks(List<PointTrack> list) {
        return firstTraker.getNewTracks(list);
    }

    @Override
    public void spawnTracks() {
        Pool.exec(this.firstTraker::spawnTracks, this.secondTracker::spawnTracks);
    }

    public void spawnTracksSeparate(){

        if (spawnCount % spawnRepeat == 0) {
            firstContainer.reset();
            firstTraker.spawnTracks();
            //dropAllReadyExistTracks(firstTraker, secondTracker);
            System.out.println("Respawn FIRST container container and firstTraker " + spawnCount);
        } else if (spawnCount % spawnRepeat == spawnRepeat /2) {
            secondTracker.spawnTracks();
            secondContainer.reset();
            //dropAllReadyExistTracks(secondTracker, firstTraker);
            System.out.println("Respawn SECOND container container and firstTraker " + spawnCount);
        }

        spawnCount++;
    }

    private void dropAllReadyExistTracks(PointTracker<I> varTracker, PointTracker<I> anotherTracker) {
        List<PointTrack> newActiveTracks = varTracker.getActiveTracks(null);
        List<PointTrack> secondTracks = anotherTracker.getActiveTracks(null);
        int size = newActiveTracks.size();
        newActiveTracks.stream()
                .filter(n-> secondTracks.stream().anyMatch(o-> P2t.near(n,o)))
                .forEach(n->{varTracker.dropTrack(n);});//todo for container
        System.out.println("Try to add " + size + ", added " + varTracker.getActiveTracks(null).size());
    }

    public P2t getPrevTrack(P2t p2) {
        HistoryContainer historyContainer = (p2.series == FIRST)? firstContainer: secondContainer;
        return historyContainer.getPrev(p2);
//        return firstContainer.getPrev(p2);

    }

    public List<Tuple> getNeighbors(){
        //FactoryNearestNeighbor.kdtree(2);
       throw new UnsupportedOperationException();
    }

    public int getSpawnCount() {
        return spawnCount;
    }
}
