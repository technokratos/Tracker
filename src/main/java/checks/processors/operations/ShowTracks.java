package checks.processors.operations;

import boofcv.gui.feature.VisualizeFeatures;
import checks.types.P2t;

import java.awt.*;
import java.util.List;
import java.util.function.Function;

import static java.lang.Math.abs;

/**
 * Created by denis on 07.03.17.
 */
public class ShowTracks extends ContextFunction<List<P2t>, List<P2t>> {



    @Override
    public List<P2t> apply(List<P2t> tracks) {
        tracks.forEach(p->VisualizeFeatures.drawPoint(g2, (int)p.x, (int)p.y, Color.RED));

        return tracks;
    }
}
