package checks.processors.operations;

import checks.types.P2t;
import checks.types.Tuple;

import java.awt.*;
import java.util.List;

import static checks.processors.BaseProcessor.NEAR_POSITION;
import static java.lang.Math.abs;

/**
 * Created by denis on 07.03.17.
 */
public class DrawPrevPoints extends ContextFunction<List<P2t>,  List<P2t>> {
    final Graphics2D g2;

    public DrawPrevPoints(Graphics2D g2) {
        this.g2 = g2;
    }

    @Override
    public List<P2t> apply(List<P2t> tracks) {
        g2.setColor(Color.RED);
        tracks.stream()
                .map(p-> new Tuple<>(tracker.getPrevTrack(p), p))
                .filter(p-> p.a != null)
                .filter(t-> (abs(t.a.x - t.b.x) > NEAR_POSITION || abs(t.a.y - t.b.y) < NEAR_POSITION))
                .forEach(t-> g2.drawLine( (int)t.a.getX(), (int)t.a.getY(), (int)t.b.getX(), (int) t.b.getY()));
                //.peek(t-> g2.drawLine((int) t.a.x + 1, (int) t.a.y + 1, (int) t.b.x + 1, (int) t.b.y + 1))
        return tracks;
    }

    /*

     */
}
