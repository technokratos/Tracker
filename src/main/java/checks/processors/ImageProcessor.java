package checks.processors;

import boofcv.struct.image.ImageGray;

import java.awt.image.BufferedImage;

/**
 * Created by denis on 04.03.17.
 */
public interface ImageProcessor<T extends ImageGray> {

    BufferedImage processImage(BufferedImage orig, int frameNumber);

    void tracking(T frame);
}
