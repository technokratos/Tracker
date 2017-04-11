package checks.processors.operations;

import checks.neighborns.Pifagor;
import checks.tools.Calc;
import checks.types.*;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by denis on 07.03.17.
 */
public class FindAndCountDirs extends ContextFunction<List<Tuple<Pifagor.Link, Tuple<P2t, P2t>>>, Map<Count, List<Map.Entry<P3, Count>>>> {

    final int x0;
    final int y0;

    public FindAndCountDirs(int x0, int y0) {
        this.x0 = x0;
        this.y0 = y0;
    }

    @Override
    public Map<Count, List<Map.Entry<P3, Count>>> apply(List<Tuple<Pifagor.Link, Tuple<P2t, P2t>>> linkWithPrevPoints) {
        Calc calc = new Calc();
        Random r = new Random();
        g2.setColor(Color.BLACK);

        Map<P3, Count> dirWithCount = new HashMap<>();
        java.util.List<P3> dirs = linkWithPrevPoints.stream()
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
                    if (dir == null) {
                        return null;
                    }
                    //Color color = (dir.z > 0) ? Color.blue : Color.red;
                    //VisualizeFeatures.drawPoint(g2, dir.x, dir.y, 2, color, true);


                    g2.setColor(new Color(0, 0, r.nextInt(255)));
                    final int x = (int) ((dir.z >0 )? - dir.x: dir.x);
                    final int y = (int) ((dir.z >0 )? - dir.y: dir.y);
                    g2.drawPolygon(new int[]{fx, sx, x}, new int[]{fy, sy, y}, 3);
                    g2.setColor(Color.RED);
                    g2.drawLine(fx, fy, px, py);

                    Count count = dirWithCount.get(dir);
                    if (count == null) {
                        dirWithCount.put(dir, new Count());
                    } else{
                        count.inc();
                    }
                    return dir;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        Map<Count, java.util.List<Map.Entry<P3, Count>>> countToDirWithCount = dirWithCount.entrySet().stream().collect(Collectors.groupingBy(Map.Entry::getValue));
        return countToDirWithCount;

    }
}
