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

package checks;

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
import checks.history.HistoryTracker;
import checks.processors.BaseProcessor;
import checks.processors.ImageProcessor;
import checks.processors.operations.*;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
import java.lang.reflect.Field;
import java.util.List;

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
public class BaseTracer< T extends ImageGray, D extends ImageGray>
{
	// displays the video sequence and tracked features
	private ImagePanel gui = new ImagePanel();

	private final ImageProcessor imageProcessor;
	private int pause;
	private final double factor;

	public BaseTracer(ImageProcessor imageProcessor, int pause, double factor) {

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
		gui.setPreferredSize(new Dimension( 600, 500));
		ShowImages.showWindow(gui,"KTL Tracker", true);

		while( sequence.hasNext() ) {

			frame = sequence.next();

			imageProcessor.tracking(frame);
			final BufferedImage image = imageProcessor.processImage(sequence.getGuiImage(), sequence.getFrameNumber());

			updateGUI(image);

			// wait for a fraction of a second so it doesn't process to fast
			BoofMiscOps.pause(pause);
		}
	}

	private void updateGUI(BufferedImage orig) {
		gui.setBufferedImage(orig);
		gui.repaint();
	}

	public static void main( String args[] ) throws FileNotFoundException {

		Class imageType = GrayF32.class;

		MediaManager media = DefaultMediaManager.INSTANCE;

		int pause;
		SimpleImageSequence sequence =
				new LoadFileImageSequence(ImageType.single(imageType), "src/main/resources/readyImages/cubes","png");pause=2000;
				//Tools.readGif("flyer.gif"); pause = 1000;
				//Tools.readGif("racing2.gif"); pause = 1000;
				//media.openVideo("zoom.mjpeg", ImageType.single(imageType)); pause=1500;
//				media.openCamera(null,640,480,ImageType.single(imageType)); pause = 5;

		sequence.setLoop(true);

		final BaseProcessor imageProcessor = new BaseProcessor(imageType, 100, HistoryTracker.Type.DEPTH);
		////FindLinks
		//linkWithPrevPoints
		//draw links
		//findDirs
		//drawdirs
		imageProcessor
				.add(new DrawTracks())
				//.add(new DrawPrevPoints())
				.add(new FindLinks())
				//.add(new ShowLinks())

				.add(new LinkPrevPointForLinks())
				.add(new DrawLinksWithPrevPoints())
				.add(new FindAndCountDirs(320, 240))
				.add(new DrawDirs(320, 240, 0.01));

		BaseTracer app = new BaseTracer(imageProcessor, pause, 3);


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
