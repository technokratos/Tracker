package checks.processors.operations;

import checks.neighborns.Pifagor;
import checks.types.P2t;
import checks.types.Tuple;

import java.awt.*;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Created by denis on 07.03.17.
 */
public class DrawLinksWithPrevPoints extends ContextFunction<java.util.List<Tuple<Pifagor.Link, Tuple<P2t, P2t>>>, java.util.List<Tuple<Pifagor.Link, Tuple<P2t, P2t>>>> {
    @Override
    public List<Tuple<Pifagor.Link, Tuple<P2t, P2t>>> apply(List<Tuple<Pifagor.Link, Tuple<P2t, P2t>>> tuples) {
        g2.setColor(Color.WHITE);
        tuples.stream()
                //.limit(100)
                .forEach(t-> {
                    g2.drawLine((int)t.a.getA().getX(), (int)t.a.getA().getY(), (int)t.b.a.getX(), (int) t.b.a.getY());
                });
        return  tuples;
    }
}
