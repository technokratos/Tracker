package checks.processors;

import boofcv.abst.feature.detect.interest.ConfigFastHessian;
import boofcv.abst.feature.detect.interest.ConfigGeneralDetector;
import boofcv.abst.feature.tracker.PointTracker;
import boofcv.alg.filter.derivative.GImageDerivativeOps;
import boofcv.alg.tracker.klt.PkltConfig;
import boofcv.factory.feature.tracker.FactoryPointTracker;
import boofcv.gui.feature.VisualizeFeatures;
import boofcv.struct.image.ImageGray;
import checks.history.HistoryTracker;
import checks.neighborns.Pifagor;
import checks.tools.Calc;
import checks.types.*;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;


import static java.lang.Math.abs;
import static java.util.stream.Collectors.toList;

/**
 * Created by denis on 04.03.17.
 */
public class DetectDirsProcessor<T extends ImageGray, D extends ImageGray> implements ImageProcessor {

    // type of input image
    private final Class<T> imageType;
    private final Class<D> derivType;

    public static final int NEAR_POSITION = 5;



    // tracks point features inside the image
    private HistoryTracker<T> tracker;

    public DetectDirsProcessor(Class<T> imageType) {
        this.imageType = imageType;
        this.derivType = GImageDerivativeOps.getDerivativeType(imageType);
        createKLT();
    }

    /**
     * A simple way to create a Kanade-Lucas-Tomasi (KLT) tracker.
     */
    public void createKLT() {
        PkltConfig config = new PkltConfig();
        config.templateRadius = 3;
        config.pyramidScaling = new int[]{1,2,4,8};

        PointTracker<T> pointTracker = FactoryPointTracker.klt(config,
                new ConfigGeneralDetector(100, 6, 1),
                imageType, derivType);
        PointTracker<T> secondTracker = FactoryPointTracker.klt(config,
                new ConfigGeneralDetector(100, 6, 1),
                imageType, derivType);
        tracker = new HistoryTracker<>(pointTracker, secondTracker, HistoryTracker.Type.DEPTH);
    }

    /**
     * Creates a SURF feature tracker.
     */
    public void createSURF() {
        ConfigFastHessian configDetector = new ConfigFastHessian();
        configDetector.maxFeaturesPerScale = 250;
        configDetector.extractRadius = 3;
        configDetector.initialSampleSize = 2;
        PointTracker<T> tPointTracker = FactoryPointTracker.dda_FH_SURF_Fast(configDetector, null, null, imageType);
        PointTracker<T> secondTracker = FactoryPointTracker.dda_FH_SURF_Fast(configDetector, null, null, imageType);
        tracker = new HistoryTracker<>(tPointTracker, secondTracker, HistoryTracker.Type.DEPTH);
    }


    @Override
    /**
     * Draw tracked features in blue, or red if they were just spawned.
     */
    public BufferedImage processImage(BufferedImage orig, int frameNumber) {
//        int w2 = orig.getWidth()/2;
//        int h2 = orig.getHeight()/2;
        Graphics2D g2 = orig.createGraphics();

        java.util.List<P2t> tracks = tracker.getActiveTracks();

        g2.setColor(Color.BLACK);
        java.util.List<Tuple<P2t,P2t>> poitnsWithPrev = tracks.stream()
                //.peek(p->VisualizeFeatures.drawPoint(g2, (int)p.x, (int)p.y, Color.RED))
                .map(p-> new Tuple<>(tracker.getPrevTrack(p), p))
                .filter(p-> p.a != null)
                .filter(t-> (abs(t.a.x - t.b.x) > NEAR_POSITION || abs(t.a.y - t.b.y) < NEAR_POSITION))
                .peek(t-> VisualizeFeatures.drawPoint(g2, 100 * (int) t.b.x, 100 * (int) t.b.y, 1, Color.white ))
                //.peek(t-> g2.drawLine((int) t.a.x + 1, (int) t.a.y + 1, (int) t.b.x + 1, (int) t.b.y + 1))
                .collect(toList());


        System.out.println("Points with prev points " + poitnsWithPrev.size() );

        java.util.List<Pifagor.Link> links = Pifagor.findLinks(tracks);
        g2.setColor(Color.WHITE);
        //links.forEach(l-> g2.drawLine( (int)l.getA().getX(), (int)l.getA().getY(), (int)l.getB().getX(), (int)l.getB().getY()));

        //new Calc().findDir(lines.a, lines.b)

        java.util.List<Tuple<Pifagor.Link, Tuple<P2t, P2t>>> linkWithPrevPoints = links.stream()
                .filter(l -> tracker.getPrevTrack((P2t) l.getA().getP()) != null
                        && tracker.getPrevTrack((P2t) l.getB().getP()) != null)
                .map(t -> new Tuple<>(t, new Tuple<>(tracker.getPrevTrack((P2t) t.getA().getP()),
                        tracker.getPrevTrack((P2t) t.getB().getP()))))
                .filter(t -> t.b.a != null && t.b.b != null)
                .collect(Collectors.toList());


        linkWithPrevPoints.stream()
                //.limit(100)
                .forEach(t-> {
                    g2.setColor(Color.WHITE);
                    g2.drawLine((int)t.a.getA().getX(), (int)t.a.getA().getY(), (int)t.a.getB().getX(), (int) t.a.getB().getY());
                });

        Calc calc = new Calc();
        Random r = new Random();
        g2.setColor(Color.BLACK);

        Map<P3, Count> dirWithCount = new HashMap<>();
        java.util.List<P3> dirs = linkWithPrevPoints.stream()
                //.limit(100)
                .map(t -> {
                    Pifagor.Link link = t.a;
                    Tuple<P2t, P2t> prev = t.b;
                    g2.setColor(Color.red);
                    int fx = (int) link.getA().getX();
                    int fy = (int) link.getA().getY();
                    int sx = (int) link.getB().getX();
                    int sy = (int) link.getB().getY();
                    int px = (int) prev.a.getX();
                    int py = (int) prev.a.getY();

                    Tuple<? extends P2, ? extends P2> firstLine = new Tuple<>(link.getA().getP(), prev.a);
                    Tuple<? extends P2, ? extends P2> secondLine = new Tuple<>(link.getB().getP(), prev.b);
                    P3 dir = calc.findDir(firstLine, secondLine);
                    //Color color = (dir.z > 0) ? Color.blue : Color.red;
                    //VisualizeFeatures.drawPoint(g2, dir.x, dir.y, 2, color, true);


                    g2.setColor(new Color(0, 0, r.nextInt(255)));
                    final int x = (int) ((dir.z >0 )? - dir.x: dir.x);
                    final int y = (int) ((dir.z >0 )? - dir.y: dir.y);
                    g2.drawPolygon(new int[]{fx, sx, x}, new int[]{fy, sy, y}, 3);
                    g2.setColor(Color.RED);
                    g2.drawLine(fx, fy, px, py);

                    Count count = dirWithCount.get(dir);
                    if (count == null) {
                        dirWithCount.put(dir, new Count());
                    } else{
                        count.inc();
                    }
                    return dir;
                })
                .collect(Collectors.toList());

        Map<Count, java.util.List<Map.Entry<P3, Count>>> countToDirWithCount = dirWithCount.entrySet().stream().collect(Collectors.groupingBy(Map.Entry::getValue));
        int limit = (int) (dirs.size() * 0.01);
        countToDirWithCount.entrySet().stream()
                .filter(e->e.getKey().getValue() > limit)
                .sorted((e0, e1)-> e1.getKey().getValue() - e0.getKey().getValue())
                .peek(e -> {
                    Map.Entry<P3, Count> next = e.getValue().iterator().next();
                    P3 dir = next.getKey();
                    Color color = (dir.z > 0) ? Color.blue : Color.red;
                    final double x = (dir.z >0 )? - dir.x: dir.x;
                    final double y = (dir.z >0 )? -dir.y: dir.y;
                    VisualizeFeatures.drawPoint(g2, x, y, 2, color, true);
                })
                .forEach(e-> System.out.println("Count: " + e.getKey() + " " + e.getValue().size()));

        if (tracker.getSpawnCount()%10 ==0) {
            System.out.println("25 frames");

        }
        System.out.println("Number of frame " + frameNumber);
//		p2List.stream().

        return orig;

    }


    @Override
    public void tracking(ImageGray frame) {
        tracker.process((T) frame);
    }
}
