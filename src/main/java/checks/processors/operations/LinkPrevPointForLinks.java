package checks.processors.operations;

import checks.neighborns.Pifagor;
import checks.types.P2t;
import checks.types.Tuple;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by denis on 07.03.17.
 */
public class LinkPrevPointForLinks extends ContextFunction<java.util.List<Pifagor.Link>, Graphics2D, java.util.List<Tuple<Pifagor.Link, Tuple<P2t, P2t>>>> {
    @Override
    public List<Tuple<Pifagor.Link, Tuple<P2t, P2t>>> apply(List<Pifagor.Link> links, Graphics2D graphics2D) {
        return links.stream()
                .filter(l -> tracker.getPrevTrack((P2t) l.getA().getP()) != null
                        && tracker.getPrevTrack((P2t) l.getB().getP()) != null)
                .map(t -> new Tuple<>(t, new Tuple<>(tracker.getPrevTrack((P2t) t.getA().getP()),
                        tracker.getPrevTrack((P2t) t.getB().getP()))))
                .filter(t -> t.b.a != null && t.b.b != null)
                .collect(Collectors.toList());
    }
}
