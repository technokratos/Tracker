package checks.history;

import boofcv.abst.feature.tracker.PointTrack;
import boofcv.abst.feature.tracker.PointTracker;
import boofcv.struct.image.ImageGray;
import checks.types.P2t;

import java.util.List;
import java.util.function.IntConsumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Created by denis on 12.06.17.
 */
public class MultiTracker<I extends ImageGray> implements PointTracker<I> {
    public static final int HISTORY_DEPTH = 10;

    private final PointTracker<I>[] trackers;
    private final HistoryContainer[] containers;
    private int spawnCount = 0;
    private int spawnRepeat = 10;

    public MultiTracker(int channels, Supplier<PointTracker<I>> supplier, HistoryTracker.Type prev) {
        trackers = Stream.generate(supplier).limit(channels).collect(Collectors.toList()).toArray((PointTracker<I>[]) new PointTracker[channels]);
        final Supplier<? extends HistoryContainer> containerSupplier;
        if (prev.equals(HistoryTracker.Type.DEPTH)) {
            containerSupplier = () -> new DepthContainer(HISTORY_DEPTH);
        } else {
            containerSupplier = () -> new FirstContainer();
        }

        containers = Stream.generate(containerSupplier).collect(Collectors.toList()).toArray(new HistoryContainer[channels]);

    }

    @Override
    public void process(I t) {
        throw new UnsupportedOperationException();
    }

    public void process(I t, int channel) {
        containers[channel].add(trackers[channel].getActiveTracks(null).stream()
                .map(p -> new P2t(p, channel))
                .collect(Collectors.toList()));

        if (spawnCount % spawnRepeat == channel && spawnCount > 0) {
            trackers[channel].spawnTracks();
            containers[channel].reset();
        } else {
            trackers[channel].process(t);
        }

        ;
        spawnCount++;
    }


    @Override
    public void reset() {
        Stream.of(trackers).forEach(t -> t.reset());
        Stream.of(containers).forEach(c -> c.reset());
    }

    @Override
    public void dropAllTracks() {
        Stream.of(trackers).forEach(t -> t.dropAllTracks());
        Stream.of(containers).forEach(c -> c.reset());
    }

    @Override
    public boolean dropTrack(PointTrack pointTrack) {
        return Stream.of(trackers).anyMatch(t -> t.dropTrack(pointTrack));

    }

    @Override
    public List<PointTrack> getAllTracks(List<PointTrack> list) {
        return this.getActiveTracks(list);
    }


    public List<P2t> getActiveTracks(int channel) {
        return trackers[channel].getActiveTracks(null).stream()
                        .map(p -> new P2t(p, 0))
                .collect(Collectors.toList());
//        List<P2t> list = new ArrayList<>(2 * firstTraker.getActiveTracks(null).size());
//        list.addAll(trackers[0].getActiveTracks(null).stream()
//                .map(p -> new P2t(p, 0))
//                .collect(Collectors.toList()));
//        list.addAll(secondTracker.getActiveTracks(null).stream()
//                .map(p -> new P2t(p, 1))
//                .collect(Collectors.toList()));
//        return list;
    }

    @Override
    public List<PointTrack> getActiveTracks(List<PointTrack> list) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<PointTrack> getInactiveTracks(List<PointTrack> list) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<PointTrack> getDroppedTracks(List<PointTrack> list) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<PointTrack> getNewTracks(List<PointTrack> list) {

        throw new UnsupportedOperationException();
    }

    @Override
    public void spawnTracks() {
        Stream.of(trackers).parallel().forEach(PointTracker::spawnTracks);
    }


    private void dropAllReadyExistTracks(PointTracker<I> varTracker, PointTracker<I> anotherTracker) {
        List<PointTrack> newActiveTracks = varTracker.getActiveTracks(null);
        List<PointTrack> secondTracks = anotherTracker.getActiveTracks(null);
        int size = newActiveTracks.size();
        newActiveTracks.stream()
                .filter(n -> secondTracks.stream().anyMatch(o -> P2t.near(n, o)))
                .forEach(n -> {
                    varTracker.dropTrack(n);
                });//todo for container
        System.out.println("Try to add " + size + ", added " + varTracker.getActiveTracks(null).size());
    }

    public P2t getPrevTrack(P2t p2) {
        return containers[p2.series].getPrev(p2);
    }

    public int getSpawnCount() {
        return spawnCount;
    }

}
