/*
 * Copyright (c) 2011-2016, Peter Abeles. All Rights Reserved.
 *
 * This file is part of BoofCV (http://boofcv.org).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package checks.processors.cubes;

import boofcv.abst.feature.tracker.PointTracker;
import boofcv.gui.image.ImagePanel;
import boofcv.gui.image.ShowImages;
import boofcv.io.MediaManager;
import boofcv.io.image.SimpleImageSequence;
import boofcv.io.wrapper.DefaultMediaManager;
import boofcv.io.wrapper.images.JpegByteImageSequence;
import boofcv.io.wrapper.images.LoadFileImageSequence;
import boofcv.misc.BoofMiscOps;
import boofcv.struct.image.GrayF32;
import boofcv.struct.image.ImageGray;
import boofcv.struct.image.ImageType;
import checks.processors.DetectDirsProcessor;
import checks.processors.ImageProcessor;
import checks.types.P2t;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>
 * Example of how to use the {@link PointTracker} to track different types of point features.
 * ImagePointTracker hides much of the complexity involved in tracking point features and masks
 * the very different underlying structures used by these different trackers.  The default trackers
 * provided in BoofCV are general purpose trackers, that might not be the best tracker or utility
 * the underlying image features the best in all situations.
 * </p>
 *
 * @author Peter Abeles
 */
public class Tracer< T extends ImageGray, D extends ImageGray>
{
	public static final int DIVIDE_ZONE = 4;

	// displays the video sequence and tracked features
	private ImagePanel gui = new ImagePanel();

	private final ImageProcessor imageProcessor;

	private int pause;
	private final double factor;

	public Tracer(ImageProcessor imageProcessor, int pause, double factor) {

		this.imageProcessor = imageProcessor;
		this.pause = pause;
		this.factor = factor;
	}

	/**
	 * Processes the sequence of images and displays the tracked features in a window
	 */
	public void process(SimpleImageSequence<T> sequence) {

		// Figure out how large the GUI window should be
		T frame = sequence.next();
		gui.setPreferredSize(new Dimension( (int)(factor * frame.getWidth()), (int) (factor * frame.getHeight())));
		ShowImages.showWindow(gui,"KTL Tracker", true);


		// process each frame in the image sequence
		while( sequence.hasNext() ) {

			frame = sequence.next();

			// tell the tracker to process the frame
			//tracker.process(frame);
			imageProcessor.tracking(frame);

			// visualize tracking results
			final BufferedImage image = imageProcessor.processImage(sequence.getGuiImage(), sequence.getFrameNumber());

			updateGUI(image);

			// wait for a fraction of a second so it doesn't process to fast
			BoofMiscOps.pause(pause);
		}
	}



	/**
	 * Draw tracked features in blue, or red if they were just spawned.
	 */
	private void updateGUI(BufferedImage orig) {
		Graphics2D g2 = orig.createGraphics();

		// tell the GUI to update
		gui.setBufferedImage(orig);
		gui.repaint();
	}

	public static Map<Integer, List<P2t>> reduceByX(List<P2t> tracks) {
		return tracks.stream().sorted()
                    .collect(Collectors.groupingBy(p -> (int) p.x % DIVIDE_ZONE));
	}
	public static Map<Integer, List<P2t>> reduceByY(List<P2t> tracks) {
		return tracks.stream().sorted()
				.collect(Collectors.groupingBy(p -> (int) p.y % DIVIDE_ZONE));
	}




	public static void main( String args[] ) throws FileNotFoundException {

		Class imageType = GrayF32.class;

		MediaManager media = DefaultMediaManager.INSTANCE;

		int pause;
		SimpleImageSequence sequence =
				new LoadFileImageSequence(ImageType.single(imageType), "src/main/resources/readyImages/cubes","png");pause=1000;
				//Tools.readGif("flyer.gif"); pause = 1000;
				//Tools.readGif("racing2.gif"); pause = 1000;
				//media.openVideo("zoom.mjpeg", ImageType.single(imageType)); pause=1500;
//				media.openCamera(null,640,480,ImageType.single(imageType)); pause = 5;

		//setSubList((JpegByteImageSequence) sequence, 10,3);

		sequence.setLoop(true);

		Tracer app = new Tracer(new DetectDirsProcessor<>(imageType), pause, 3);


		app.process(sequence);
	}

	static void setSubList(JpegByteImageSequence sequence, int from, int count){
		//((JpegByteImageSequence) sequence).jpegData = ((JpegByteImageSequence) sequence).jpegData.subList(10,3);
		try {
			Field jpegData = sequence.getClass().getDeclaredField("jpegData");
			jpegData.setAccessible(true);
			List<byte[]> frames = (List<byte[]>) jpegData.get(sequence);
			List<byte[]> o1 = frames.subList(from, from + count);
			jpegData.set(sequence, o1);
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}

	}


}
