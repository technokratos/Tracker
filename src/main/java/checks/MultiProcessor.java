package checks;

import boofcv.abst.feature.detect.interest.ConfigGeneralDetector;
import boofcv.abst.feature.tracker.PointTracker;
import boofcv.alg.tracker.klt.PkltConfig;
import boofcv.factory.feature.tracker.FactoryPointTracker;
import boofcv.gui.image.ImagePanel;
import boofcv.gui.image.ShowImages;
import boofcv.io.image.ConvertBufferedImage;
import boofcv.struct.image.GrayF32;
import boofcv.struct.image.ImageGray;
import boofcv.struct.image.ImageType;
import checks.history.HistoryTracker;
import checks.history.MultiTracker;
import checks.processors.ImageProcessor;
import checks.processors.operations.*;
import checks.types.Count;
import checks.types.P2t;
import checks.types.P3;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.IntStream;

/**
 * Created by denis on 03.06.17.
 */
public class MultiProcessor {


    final int channelCount;
    final ImagePanel[] guis;
//    private final ImageProcessor<ImageGray> imageProcessor;
    final MultiTracker tracker;
    final int[] frameCounters;
    private final ImageGray frame;

    private FunctionList preChannelProcessors;
    private FunctionList commonProcessors;
    private FunctionList postChannelProcessors;
    private Object[] channelResults;

    public MultiProcessor(int channelCount, int width, int height, int features) {
        this.channelCount = channelCount;

        final FunctionList<List<P2t>> functionList = FunctionList.get(new DrawTracks());
        functionList.add(new FindLinks())
                //.add(new ShowLinks())

                .add(new LinkPrevPointForLinks())
                .add(new DrawLinksWithPrevPoints())
                .add(new FindAndCountDirs(width, height))
                .add(new DrawDirs(width, height, 0.01));
        final Class<GrayF32> imageClass = GrayF32.class;

        tracker = new MultiTracker<>(channelCount, this::createKLT, HistoryTracker.Type.DEPTH);
        frame = ImageType.single(imageClass).createImage(width, height);

        Toolkit tk = Toolkit.getDefaultToolkit();
        Dimension screen = tk.getScreenSize();
        final int perRow = (int) Math.ceil(Math.sqrt(channelCount));
        guis = new ImagePanel[channelCount];
        for (int i = 0; i < channelCount; i++) {
            final ImagePanel gui = new ImagePanel(width, height);
            guis[i]=gui;
            final JFrame jFrame = ShowImages.showWindow(gui, "Tracker " + i);
            jFrame.setLocation(i % perRow * screen.width, i/ perRow * screen.height);
        }
        frameCounters = new int[channelCount];
        channelResults = new Object[channelCount];

    }

    public PointTracker<GrayF32> createKLT() {
        PkltConfig config = new PkltConfig();
        config.templateRadius = 3;
        config.pyramidScaling = new int[]{1, 2, 4, 8};

        return FactoryPointTracker.klt(config,
                new ConfigGeneralDetector(100, 6, 1),
                GrayF32.class, ImageGray.class);
    }

    public void process(BufferedImage image, int channel) {
        ConvertBufferedImage.convertFrom(image, frame, true);
        tracker.process(frame, channel);
        processImage(image, frameCounters[channel], channel);
        frameCounters[channel] += 1;

        updateGUI(image, channel);
    }

    private void updateGUI(BufferedImage orig, int channel) {
        final ImagePanel gui = guis[channel];
        gui.setBufferedImage(orig);
        gui.repaint();
    }


    /**
     * Draw tracked features in blue, or red if they were just spawned.
     */
    public BufferedImage processImage(BufferedImage orig, int frameNumber, int channel) {
        Graphics2D g2 = orig.createGraphics();

        java.util.List<P2t> tracks = tracker.getActiveTracks(channel);

        Object prevResult = tracks;
        for (Function f: (java.util.List<Function>) preChannelProcessors.getList()) {
            if (f instanceof ContextFunction) {

                //((ContextFunction) f).setTracker(tracker);

                ((ContextFunction) f).setG2(g2);

            }
            prevResult = f.apply(prevResult);
        }


            channelResults[channel] = prevResult;




//        if (tracker.getSpawnCount()%10 ==0) {
//            System.out.println("25 frames");
//
//        }
//        System.out.println("Number of frame " + frameNumber);
////		p2List.stream().

        return orig;

    }

    public BufferedImage processImage() {

        Object[] prevResult = channelResults;
        for (Function f: (java.util.List<Function>) commonProcessors.getList()) {

            prevResult = (Object[]) f.apply(prevResult);
        }

        channelResults = prevResult;


//        if (tracker.getSpawnCount()%10 ==0) {
//            System.out.println("25 frames");
//
//        }
//        System.out.println("Number of frame " + frameNumber);
////		p2List.stream().

        return null;

    }

}
