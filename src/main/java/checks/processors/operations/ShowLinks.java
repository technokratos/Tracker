package checks.processors.operations;

import boofcv.gui.feature.VisualizeFeatures;
import checks.neighborns.Pifagor;

import java.awt.*;
import java.util.List;
import java.util.function.BiFunction;

/**
 * Created by denis on 07.03.17.
 */
public class ShowLinks implements BiFunction<List<Pifagor.Link>, Graphics2D, List<Pifagor.Link>> {
    @Override
    public List<Pifagor.Link> apply(List<Pifagor.Link> links, Graphics2D graphics2D) {
        graphics2D.setColor(Color.BLUE);
        links.forEach(e->{
            final int x = (int) e.getA().getX();
            final int y = (int) e.getA().getY();
            final int x1 = (int) e.getB().getX();
            final int y1 = (int) e.getB().getY();
            graphics2D.drawLine(x,y,x1,y1);
        });
        return links;
    }
}
