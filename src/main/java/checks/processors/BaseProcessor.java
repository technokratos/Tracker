package checks.processors;

import boofcv.abst.feature.detect.interest.ConfigFastHessian;
import boofcv.abst.feature.detect.interest.ConfigGeneralDetector;
import boofcv.abst.feature.tracker.PointTracker;
import boofcv.alg.filter.derivative.GImageDerivativeOps;
import boofcv.alg.tracker.klt.PkltConfig;
import boofcv.factory.feature.tracker.FactoryPointTracker;
import boofcv.struct.image.ImageGray;
import checks.history.HistoryTracker;
import checks.processors.operations.ContextFunction;
import checks.processors.operations.FunctionList;
import checks.types.*;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.function.BiFunction;

import static java.lang.Math.abs;

/**
 * Created by denis on 04.03.17.
 */
public class BaseProcessor<T extends ImageGray, D extends ImageGray> implements ImageProcessor {

    // type of input image
    private final Class<T> imageType;
    private final Class<D> derivType;


    FunctionList functionList;// = new FunctionList();

    public static final int NEAR_POSITION = 5;



    // tracks point features inside the image
    private HistoryTracker<T> tracker;
    private final int features;

    public BaseProcessor(Class<T> imageType, int features, HistoryTracker.Type first) {
        this.imageType = imageType;
        this.derivType = GImageDerivativeOps.getDerivativeType(imageType);
        this.features = features;
        createKLT(first);
    }

    public <T,U,R> FunctionList<R,U, ? extends Object> add(BiFunction<T,U,R> function) {
        functionList = FunctionList.get(function);
        return functionList;
    }

    /**
     * A simple way to create a Kanade-Lucas-Tomasi (KLT) tracker.
     * @param first
     */
    public void createKLT(HistoryTracker.Type first) {
        PkltConfig config = new PkltConfig();
        config.templateRadius = 3;
        config.pyramidScaling = new int[]{1,2,4,8};

        PointTracker<T> pointTracker = FactoryPointTracker.klt(config,
                new ConfigGeneralDetector(features, 6, 1),
                imageType, derivType);
        PointTracker<T> secondTracker = FactoryPointTracker.klt(config,
                new ConfigGeneralDetector(features, 6, 1),
                imageType, derivType);
        tracker = new HistoryTracker<>(pointTracker, secondTracker, first);
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
        Graphics2D g2 = orig.createGraphics();

        java.util.List<P2t> tracks = tracker.getActiveTracks();

        Object prevResult = tracks;
        for (BiFunction f: (java.util.List<BiFunction>) functionList.getList()) {
            if (f instanceof ContextFunction) {
                ((ContextFunction) f).setTracker(tracker);
            }
            prevResult = f.apply(prevResult, g2);
        }

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
