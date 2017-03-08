package checks.processors.operations;

import boofcv.gui.feature.VisualizeFeatures;
import checks.types.P2t;
import checks.types.Tuple;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.function.BiFunction;

import static checks.processors.BaseProcessor.NEAR_POSITION;
import static java.lang.Math.abs;

/**
 * Created by denis on 07.03.17.
 */
public class ShowTracks implements BiFunction<java.util.List<P2t>, Graphics2D, java.util.List<P2t>> {
    @Override
    public List<P2t> apply(List<P2t> tracks, Graphics2D g2) {
        tracks.forEach(p->VisualizeFeatures.drawPoint(g2, (int)p.x, (int)p.y, Color.RED));

        return tracks;
    }
}
