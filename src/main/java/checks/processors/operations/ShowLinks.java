package checks.processors.operations;

import checks.neighborns.Pifagor;

import java.awt.*;
import java.util.List;
import java.util.function.Function;

/**
 * Created by denis on 07.03.17.
 */
public class ShowLinks extends ContextFunction<List<Pifagor.Link>, List<Pifagor.Link>> {


    @Override
    public List<Pifagor.Link> apply(List<Pifagor.Link> links) {
        g2.setColor(Color.BLUE);
        links.forEach(e->{
            final int x = (int) e.getA().getX();
            final int y = (int) e.getA().getY();
            final int x1 = (int) e.getB().getX();
            final int y1 = (int) e.getB().getY();
            g2.drawLine(x,y,x1,y1);
        });
        return links;
    }
}
