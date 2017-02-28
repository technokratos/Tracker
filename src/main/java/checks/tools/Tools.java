package checks.tools;

import boofcv.io.image.SimpleImageSequence;
import boofcv.io.wrapper.images.BufferedFileImageSequence;
import boofcv.struct.image.GrayF32;
import boofcv.struct.image.ImageType;
import checks.gif.GifReader;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

/**
 * Created by denis on 11.02.17.
 */
public class Tools {

    public static BufferedImage resize(BufferedImage original, double factor) {
        int newWidth = new Double(original.getWidth() * factor).intValue();
        int newHeight = new Double(original.getHeight() * factor).intValue();
        BufferedImage resized = new BufferedImage(newWidth, newHeight, original.getType());
        Graphics2D g = resized.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(original, 0, 0, newWidth, newHeight, 0, 0, original.getWidth(),
                original.getHeight(), null);
        //g.dispose();
        return resized;
    }

    public static SimpleImageSequence readGif(String s) {
        try {
            return new BufferedFileImageSequence(ImageType.single(GrayF32.class), GifReader.read(s));
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
}
