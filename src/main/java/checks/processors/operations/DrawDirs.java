package checks.processors.operations;

import boofcv.gui.feature.VisualizeFeatures;
import checks.types.Count;
import checks.types.P3;

import java.awt.*;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Created by denis on 07.03.17.
 */
public class DrawDirs extends ContextFunction<Map<Count,List<Map.Entry<P3,Count>>>,Map<Count,List<Map.Entry<P3,Count>>>> {


    final int x0;
    final int y0;
    final private double limitLevel;


    public DrawDirs(int x0, int y0, double limitLevel) {
        this.x0 = x0;
        this.y0 = y0;
        this.limitLevel = limitLevel;
    }

    @Override
    public Map<Count, List<Map.Entry<P3, Count>>> apply(Map<Count, List<Map.Entry<P3, Count>>> countToDirWithCount ) {

        final int limit = countToDirWithCount.entrySet().stream().mapToInt(e -> e.getValue().stream().mapToInt(c -> c.getValue().getValue()).sum()).sum();

        int locLimit = (int) (limit * limitLevel);
        countToDirWithCount.entrySet().stream()
                .filter(e->e.getKey().getValue() > locLimit)
                .sorted((e0, e1)-> e1.getKey().getValue() - e0.getKey().getValue())
                .peek(e -> {
                    Map.Entry<P3, Count> next = e.getValue().iterator().next();
                    P3 dir = next.getKey();
                    Color color = (dir.z > 0) ? Color.blue : Color.red;
                    final double x = (dir.z >0 )? - dir.x: dir.x;
                    final double y = (dir.z >0 )? -dir.y: dir.y;
                    VisualizeFeatures.drawPoint(g2, x, y, 2, color, true);
                })
                .forEach(e-> System.out.println("Count: " + e.getKey() + " " + e.getValue().size()));

        return countToDirWithCount;
    }
}
