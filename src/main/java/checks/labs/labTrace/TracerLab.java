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

package checks.labs.labTrace;

import boofcv.abst.feature.detect.interest.ConfigFastHessian;
import boofcv.abst.feature.detect.interest.ConfigGeneralDetector;
import boofcv.abst.feature.tracker.PointTracker;
import boofcv.alg.filter.derivative.GImageDerivativeOps;
import boofcv.alg.tracker.klt.PkltConfig;
import boofcv.factory.feature.tracker.FactoryPointTracker;
import boofcv.gui.feature.VisualizeFeatures;
import boofcv.gui.image.ImagePanel;
import boofcv.gui.image.ShowImages;
import boofcv.io.MediaManager;
import boofcv.io.image.SimpleImageSequence;
import boofcv.io.wrapper.DefaultMediaManager;
import boofcv.misc.BoofMiscOps;
import boofcv.struct.image.GrayF32;
import boofcv.struct.image.ImageGray;
import boofcv.struct.image.ImageType;
import checks.HistoryTracker;
import checks.types.P2t;
import checks.types.Tuple;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static checks.HistoryTracker.MAX_FEATURES;
import static java.util.stream.Collectors.toList;

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
public class TracerLab< T extends ImageGray, D extends ImageGray>
{
	public static final int DIVIDE_ZONE = 4;
	// type of input image
	Class<T> imageType;
	Class<D> derivType;

	// tracks point features inside the image
	HistoryTracker<T> tracker;


	// displays the video sequence and tracked features
	ImagePanel gui = new ImagePanel();

	int pause;

	public TracerLab(Class<T> imageType , int pause ) {
		this.imageType = imageType;
		this.derivType = GImageDerivativeOps.getDerivativeType(imageType);
		this.pause = pause;
	}

	/**
	 * Processes the sequence of images and displays the tracked features in a window
	 */
	public void process(SimpleImageSequence<T> sequence) {

		// Figure out how large the GUI window should be
		T frame = sequence.next();
		gui.setPreferredSize(new Dimension(frame.getWidth(),frame.getHeight()));
		ShowImages.showWindow(gui,"KTL Tracker", true);

		// process each frame in the image sequence
		while( sequence.hasNext() ) {
			frame = sequence.next();

			// tell the tracker to process the frame
			tracker.process(frame);

			// if there are too few tracks spawn more
			if( tracker.getActiveTracks(null).size() < 130 )
				tracker.spawnTracks();

			// visualize tracking results
			updateGUI(sequence);

			// wait for a fraction of a second so it doesn't process to fast
			BoofMiscOps.pause(pause);
		}
	}

	/**
	 * Draw tracked features in blue, or red if they were just spawned.
	 */
	private void updateGUI(SimpleImageSequence<T> sequence) {
		BufferedImage orig = sequence.getGuiImage();
		Graphics2D g2 = orig.createGraphics();

		List<P2t> tracks = tracker.getActiveTracks();

		List<Tuple<P2t,P2t>> p2List = tracks.stream()
				.peek(p->VisualizeFeatures.drawPoint(g2, (int)p.getX(), (int)p.getY(), Color.RED))
				.map(p-> new Tuple<>(p, tracker.getPrevTrack(p)))
				.filter(p-> p.b != null)
				.collect(toList());

		p2List.stream()
				.filter(Objects::nonNull)
				.forEach(t-> g2.drawLine((int) t.a.getX(), (int) t.a.getY(), (int) t.b.getX(), (int) t.b.getY()));


		g2.setColor(Color.green);
		//tuples.forEach(t-> g2.drawLine((int) t.a.x, (int) t.a.y, (int) t.b.x, (int) t.b.y));

		// tell the GUI to update
		gui.setBufferedImage(orig);
		gui.repaint();
	}

	private List<Tuple<P2t, P2t>> findNeighbors(List<P2t> tracks) {

		Map<Integer, List<P2t>> byX = reduceByX(tracks);

//		Map<Integer, List<Tuple<Integer, Map<Integer, List<P2t>>>>> groupByY = byX.entrySet().stream()
//				.map(e -> new Tuple<Integer, Map<Integer, List<P2t>>>(e.getKey(), reduceByY(e.getValue())))
//				.collect(Collectors.groupingBy(t -> t.a));

//		List<P2t> collect = groupByY.entrySet().stream().flatMap(e -> e.getValue().stream().flatMap(l -> l.b.entrySet().stream().flatMap(e1 -> e1.getValue()).collect(Collectors.toList())).collect(Collectors.toList())).collect(Collectors.toList());
//		List<Tuple<P2t,P2t>> tuples = new ArrayList<>(tracks.size()/2);

		Map<Tuple<Integer, Integer>, List<P2t>> tupleListMap = tracks.stream().
				collect(Collectors.groupingBy(p -> new Tuple<>(((int) p.getX()) % 4, ((int) p.getY()) % 4)));

		//tupleListMap.entrySet().stream().map(Map.Entry::getValue).flatMap(this::findNeighBorsByEach).collect(Collectors.toList());
		List<Tuple<P2t,P2t>> result = new ArrayList<>(tracks.size());
		tupleListMap.entrySet().forEach(e->result.addAll( findNeighBorsByEach(e.getValue())));

		return findNeighBorsByEach(tracks);
		//return result;
	}

	private List<Tuple<P2t,P2t>> findNeighBorsByEach(List<P2t> tracks) {
		List<Tuple<P2t,P2t>> tuples = new ArrayList<>(tracks.size()/2);
		for (int i = 0; i < tracks.size()-1; i++) {

			P2t p2 = tracks.get(i);
			P2t p2n = tracks.get(i+1);
			double minDist =  p2.dist(p2n);

			for (int j = i+2; j < tracks.size(); j++) {
				P2t p2n1 = tracks.get(j);
				double dist = p2.dist(p2n1);
				if (dist< minDist) {
					p2n = p2n1;
					minDist = dist;
				}
			}
			tuples.add(new Tuple<>(p2, p2n));

		}
		return tuples;
	}

	private Map<Integer, List<P2t>> reduceByX(List<P2t> tracks) {
		return tracks.stream().sorted()
                    .collect(Collectors.groupingBy(p -> (int) p.getX() % DIVIDE_ZONE));
	}
	private Map<Integer, List<P2t>> reduceByY(List<P2t> tracks) {
		return tracks.stream().sorted()
				.collect(Collectors.groupingBy(p -> (int) p.getY() % DIVIDE_ZONE));
	}



	/**
	 * A simple way to create a Kanade-Lucas-Tomasi (KLT) tracker.
	 */
	public void createKLT() {
		PkltConfig config = new PkltConfig();
		config.templateRadius = 3;
		config.pyramidScaling = new int[]{1,2,4,8};

		PointTracker<T> pointTracker = FactoryPointTracker.klt(config,
				new ConfigGeneralDetector(MAX_FEATURES, 6, 1),
				imageType, derivType);
		PointTracker<T> secondTracker = FactoryPointTracker.klt(config,
				new ConfigGeneralDetector(MAX_FEATURES, 6, 1),
				imageType, derivType);
		tracker = new HistoryTracker<>(pointTracker, secondTracker);
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
		tracker = new HistoryTracker<>(tPointTracker, secondTracker);
	}

	public static void main( String args[] ) throws FileNotFoundException {

		Class imageType = GrayF32.class;

		MediaManager media = DefaultMediaManager.INSTANCE;

		int pause;
		SimpleImageSequence sequence =
//				media.openVideo(UtilIO.pathExample("zoom.mjpeg"), ImageType.single(imageType)); pause=100;
				media.openCamera(null,640,480,ImageType.single(imageType)); pause = 5;
		sequence.setLoop(true);

		TracerLab app = new TracerLab(imageType,pause);

		// Comment or un-comment to change the type of tracker being used
		app.createKLT();
//		app.createSURF();

		app.process(sequence);
	}
}
