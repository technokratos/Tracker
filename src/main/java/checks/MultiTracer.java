package checks;

import boofcv.gui.image.ImagePanel;
import boofcv.gui.image.ShowImages;
import boofcv.io.image.ConvertBufferedImage;
import boofcv.struct.image.GrayF32;
import boofcv.struct.image.ImageType;
import checks.history.HistoryTracker;
import checks.processors.BaseProcessor;
import checks.processors.ImageProcessor;
import checks.processors.operations.*;
import checks.types.P2t;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by denis on 03.06.17.
 */
public class MultiTracer {


    final int channelCount;
    final List<ImagePanel> guis = new ArrayList<>();
    private final ImageProcessor imageProcessor;
    final int[] frameCounters;
    private final GrayF32 frame;

    public MultiTracer(int channelCount, int width, int height, int features) {
        this.channelCount = channelCount;

        final FunctionList<List<P2t>> functionList = FunctionList.get(new DrawTracks());
        functionList.add(new FindLinks())
                //.add(new ShowLinks())

                .add(new LinkPrevPointForLinks())
                .add(new DrawLinksWithPrevPoints())
                .add(new FindAndCountDirs(width, height))
                .add(new DrawDirs(width, height, 0.01));
        final Class<GrayF32> imageClass = GrayF32.class;
        imageProcessor = new BaseProcessor(functionList, imageClass, features, HistoryTracker.Type.DEPTH);
        frame = ImageType.single(imageClass).createImage(width, height);

        Toolkit tk = Toolkit.getDefaultToolkit();
        Dimension screen = tk.getScreenSize();
        final int perRow = (int) Math.ceil(Math.sqrt(channelCount));
        for (int i = 0; i < channelCount; i++) {
            final ImagePanel gui = new ImagePanel(width, height);
            guis.add(gui);
            final JFrame jFrame = ShowImages.showWindow(gui, "Tracker " + i);
            jFrame.setLocation(i % perRow * screen.width, i/ perRow * screen.height);
        }
        frameCounters = new int[channelCount];

    }


    public void process(BufferedImage image,int channel) {
        ConvertBufferedImage.convertFrom(image, frame, true);
        imageProcessor.tracking(frame);
        imageProcessor.processImage(image, frameCounters[channel]);
        frameCounters[channel] += 1;

        updateGUI(image, channel);
    }

    private void updateGUI(BufferedImage orig, int channel) {
        final ImagePanel gui = guis.get(channel);
        gui.setBufferedImage(orig);
        gui.repaint();
    }
}
