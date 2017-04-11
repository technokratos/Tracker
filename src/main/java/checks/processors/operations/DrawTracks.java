package checks.processors.operations;

import boofcv.gui.feature.VisualizeFeatures;
import checks.types.P2t;


import java.awt.*;
import java.util.List;
import java.util.function.Function;

/**
 * Created by denis on 07.03.17.
 */
public class DrawTracks extends ContextFunction<List<P2t>, List<P2t>> {


    @Override
    public List<P2t> apply(List<P2t> p2tList) {
        p2tList.forEach(p->{VisualizeFeatures.drawPoint(g2, p.x, p.y, 2, Color.RED, true);});
        return p2tList;
    }
}
