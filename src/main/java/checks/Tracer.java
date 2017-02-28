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
import boofcv.io.wrapper.images.JpegByteImageSequence;
import boofcv.misc.BoofMiscOps;
import boofcv.struct.image.*;
import checks.dir.DirCollector;
import checks.dir.DirCollector.PointDirection;
import checks.neighborns.Pifagor;
import checks.tools.Tools;
import checks.types.*;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
import java.lang.reflect.Field;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

import static checks.HistoryTracker.MAX_FEATURES;
import static java.lang.Math.abs;
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
public class Tracer< T extends ImageGray, D extends ImageGray>
{
	public static final int DIVIDE_ZONE = 4;
	public static final int NEAR_POSITION = 5;
	// type of input image
	Class<T> imageType;
	Class<D> derivType;

	// tracks point features inside the image
	HistoryTracker<T> tracker;


	// displays the video sequence and tracked features
	ImagePanel gui = new ImagePanel();

	int pause;
	private final double factor;



	public Tracer(Class<T> imageType, int pause, double factor) {
		this.imageType = imageType;
		this.derivType = GImageDerivativeOps.getDerivativeType(imageType);
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
//			try {
//				Thread.sleep(400);
//			} catch (InterruptedException e) {
//				e.printStackTrace();
//			}
			frame = sequence.next();

			// tell the tracker to process the frame
			tracker.process(frame);

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
		int w2 = orig.getWidth()/2;
		int h2 = orig.getHeight()/2;
		Graphics2D g2 = orig.createGraphics();

		java.util.List<P2t> tracks = tracker.getActiveTracks();
		DirCollector dirCollector = new DirCollector();


		g2.setColor(Color.BLACK);
		List<Tuple<P2t,P2t>> p2List = tracks.stream()
				//.peek(p->VisualizeFeatures.drawPoint(g2, (int)p.x, (int)p.y, Color.RED))
				.map(p-> new Tuple<>(tracker.getPrevTrack(p), p))
				.filter(p-> p.a != null)
				.filter(t-> (abs(t.a.x - t.b.x) > NEAR_POSITION || abs(t.a.y - t.b.y) < NEAR_POSITION))
				.peek(t-> VisualizeFeatures.drawPoint(g2, 100 * (int) t.b.x, 100 * (int) t.b.y, 1, Color.white ))
				//.peek(t-> g2.drawLine((int) t.a.x + 1, (int) t.a.y + 1, (int) t.b.x + 1, (int) t.b.y + 1))
				.collect(toList());


		System.out.println("Points with prev points " + p2List.size() );

		List<Pifagor.Link> links = Pifagor.findLinks(tracks);
		g2.setColor(Color.WHITE);
		//links.forEach(l-> g2.drawLine( (int)l.getA().getX(), (int)l.getA().getY(), (int)l.getB().getX(), (int)l.getB().getY()));

		//new Calc().findDir(lines.a, lines.b)

		List<Tuple<Pifagor.Link, Tuple<P2t, P2t>>> linkWithPrevPoints = links.stream()
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
		List<P3> dirs = linkWithPrevPoints.stream()
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



//					dirWithCount.compute(dir, (p3, count)->  {
//						if (count != null) {
//
//						} else {
//							count.inc();
//							return count;
//						}
//					})

					Count count = dirWithCount.get(dir);
					if (count == null) {
						dirWithCount.put(dir, new Count());
					} else{
						count.inc();
					}
					return dir;
				})
				.collect(Collectors.toList());

		Map<Count, List<Map.Entry<P3, Count>>> countToDirWithCount = dirWithCount.entrySet().stream().collect(Collectors.groupingBy(Map.Entry::getValue));
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
					VisualizeFeatures.drawPoint(g2, x, y, e.getKey().getValue(), color, true);
				})
				.forEach(e-> System.out.println("Count: " + e.getKey() + " " + e.getValue().size()));
		//System.out.println("Dirs " + dirWithCount.size() + " dirWithCount " + dirWithCount);
		//dirWithCount.forEach(e-> System.out.println(d));

		//Map<P3, Integer> directions = dirCollector.addLinks(linkWithPrevPoints, 0.2);
//
//		directions.entrySet().stream()
//				.peek(e->{
//					//PointDirection dir = (PointDirection) e.getKey();
//					P3 dir = e.getKey();
//					Color color = (dir.z>0)? Color.BLACK: Color.WHITE;
//					double size = 2;// Math.log(e.getValue());
//					double x = 100 * dir.x;
//					double y = 100 * dir.y;
//					VisualizeFeatures.drawPoint(g2, x, y, size, color, true );
//				}).forEach(e-> {});
//
//
				//.forEach(System.out::println);

//		List<Tuple<Tuple<Tuple<P2t, P2>, Tuple<P2t, P2>>, P3>> data = links.stream()
////				.filter(l-> tracker.getPrevTrack((P2t) l.getA().getP())!= null
////						&& tracker.getPrevTrack((P2t) l.getB().getP())!= null)
//				.map(t -> new Tuple<>(new Tuple<>(tracker.getPrevTrack((P2t) t.getA().getP()), t.getA().getP()),
//						new Tuple<>(tracker.getPrevTrack((P2t) t.getB().getP()), t.getB().getP())))
//				.filter(t -> t.a.a != null && t.b.a != null)
//				.map(lines -> new Tuple<>(lines, new Calc().findDir(lines.a, lines.b)))
//
//				.peek(linesDir -> VisualizeFeatures.drawPoint(g2, (int) linesDir.b.getXWithZ(), (int) linesDir.b.getYWithZ(), 1, Color.GRAY))
//////				.peek(tr -> { //line between
//////					g2.setColor(new Color(r.nextInt()));
//////					g2.drawLine((int) tr.a.a.b.x, (int) tr.a.a.b.y, (int) tr.b.x, (int) tr.b.y);
//////					g2.drawLine((int) tr.a.b.b.x, (int) tr.a.b.b.y, (int) tr.b.x, (int) tr.b.y);
//////				})
//				.collect(Collectors.toList());


//		List<Tuple<Tuple<Tuple<P2t, P2t>, Tuple<P2t, P2t>>, P3>> data = neighbors.stream()
//				.map(t -> new Tuple<>(new Tuple<>(tracker.getPrevTrack(t.a), t.a), new Tuple<>(tracker.getPrevTrack(t.b), t.b)))
//				.filter(t -> t.a.a != null && t.b.a != null)
////				.peek(t -> {
////					g2.setColor(Color.BLUE);
////					g2.drawLine((int) t.a.b.x, (int) t.a.b.y, (int) t.b.b.x, (int) t.b.b.y);
////				})
//				.map(lines -> new Tuple<>(lines, new Calc().findDir(lines.a, lines.b)))
//				.peek(linesDir -> VisualizeFeatures.drawPoint(g2, (int)linesDir.b.getXWithZ(), (int)linesDir.b.getYWithZ(),1, Color.GRAY))
////				.peek(tr -> { //line between
////					g2.setColor(new Color(r.nextInt()));
////					g2.drawLine((int) tr.a.a.b.x, (int) tr.a.a.b.y, (int) tr.b.x, (int) tr.b.y);
////					g2.drawLine((int) tr.a.b.b.x, (int) tr.a.b.b.y, (int) tr.b.x, (int) tr.b.y);
////				})
//				.collect(Collectors.toList());
		//List<P3> dirs = data.stream().map(t->t.b).collect(Collectors.toList());
				//.peek(t->VisualizeFeatures.drawPoint(g2, (int)t.b.x, (int)t.y, Color.BLUE))
//				.map(P3::norm)
//				.peek(p3 -> g2.drawLine(w2 / 2, h2 / 2, 100*(int) p3.x, 100*(int) p3.y))

		//System.out.println("dirs " + dirs.size());

		if (tracker.getSpawnCount()%10 ==0) {
			System.out.println("25 frames");

		}
		System.out.println("Number of frame " + sequence.getFrameNumber());
//		p2List.stream().



		// tell the GUI to update

		BufferedImage resized = Tools.resize(orig, factor);
		gui.setBufferedImage(resized);
		gui.repaint();
	}


	private Map<Integer, List<P2t>> reduceByX(List<P2t> tracks) {
		return tracks.stream().sorted()
                    .collect(Collectors.groupingBy(p -> (int) p.x % DIVIDE_ZONE));
	}
	private Map<Integer, List<P2t>> reduceByY(List<P2t> tracks) {
		return tracks.stream().sorted()
				.collect(Collectors.groupingBy(p -> (int) p.y % DIVIDE_ZONE));
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
				//Tools.readGif("flyer.gif"); pause = 1000;
				//Tools.readGif("racing2.gif"); pause = 1000;
				media.openVideo("zoom.mjpeg", ImageType.single(imageType)); pause=1500;

		//setSubList((JpegByteImageSequence) sequence, 10,3);

//				media.openCamera(null,640,480,ImageType.single(imageType)); pause = 5;
		sequence.setLoop(true);

		Tracer app = new Tracer(imageType,pause, 3);

		// Comment or un-comment to change the type of tracker being used
		app.createKLT();
//		app.createSURF();

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
