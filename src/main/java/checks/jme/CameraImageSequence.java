package checks.jme;

import boofcv.io.image.ConvertBufferedImage;
import boofcv.io.image.SimpleImageSequence;
import boofcv.struct.image.ImageBase;
import boofcv.struct.image.ImageType;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Created by denis on 07.03.17.
 */
public class CameraImageSequence<T extends ImageBase> implements SimpleImageSequence {
    BufferedImage imageGUI;
    T output;
    BufferedImage imageNext;
    ImageType<T> imageType;
    Robot robot;
    {
        try {
            robot = new Robot();
        } catch (AWTException e) {
            e.printStackTrace();
        }
    }

    private volatile int frameNumber;

    public CameraImageSequence(ImageType<T> imageType) {
        this.imageType = imageType;
        this.output = imageType.createImage(1, 1);
        this.loadNext();
    }

    private void loadNext() {
        this.imageNext = robot.createScreenCapture(new Rectangle(550,250,500,500));
    }

    public int getNextWidth() {
        return this.imageNext.getWidth();
    }

    public int getNextHeight() {
        return this.imageNext.getHeight();
    }


    @Override
    public boolean hasNext() {
        return true;
    }

    @Override
    public ImageBase next() {
        frameNumber++;
        if (imageNext == null) {
            loadNext();
        }
        this.imageGUI = this.imageNext;

        this.output.reshape(this.imageGUI.getWidth(), this.imageGUI.getHeight());
        ConvertBufferedImage.convertFrom(this.imageGUI, this.output, true);



        loadNext();
        return output;
    }

    @Override
    public void close() {

    }

    @Override
    public int getFrameNumber() {
        return frameNumber;
    }

    @Override
    public void setLoop(boolean b) {

    }

    @Override
    public ImageType getImageType() {
        return imageType;
    }

    @Override
    public void reset() {

    }

    public BufferedImage getGuiImage() {
        return this.imageGUI;
    }
}
