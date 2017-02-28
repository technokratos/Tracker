package checks.voronoi;

import checks.types.P2;
import checks.types.Tuple;
import org.assertj.core.data.Percentage;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by denis on 14.02.17.
 */
public class SolveTest {
    @Test
    public void solveSecond() throws Exception {
        double x0 = -13, x1 = 3;
        Tuple<Double, Double> points = Solve.solveQuadratic(1, (-x0 - x1), x0 * x1);
        assertThat(points.a).isEqualTo(x0);
        assertThat(points.b).isEqualTo(x1);
    }
    @Test
    public void solveSecond35() throws Exception {
        double x0 = 3, x1 = 5;
        Tuple<Double, Double> points = Solve.solveQuadratic(1, (-x0 - x1), x0 * x1);
        assertThat(points.a).isEqualTo(x0);
        assertThat(points.b).isEqualTo(x1);
    }

    @Test
    public void chekcTwoZones() throws Exception {
        List<P2> p2s = Arrays.asList(new P2(-3, -3), new P2(3, 3));
        List<VorZone> zones = Solve.findZones(p2s);
        assertThat(zones.get(0).lineDivs.get(0).begin).isEqualTo(new P2(-3d,3d));
        assertThat(zones.get(0).lineDivs.get(0).end).isEqualTo(new P2(3d,-3d));

        assertThat(zones.get(1).lineDivs.get(0).begin).isEqualTo(new P2(-3d,3d));
        assertThat(zones.get(1).lineDivs.get(0).end).isEqualTo(new P2(3d,-3d));
    }

    @Test
    public void chekcTwoZones2() throws Exception {
        List<P2> p2s = Arrays.asList(new P2(2, 2), new P2(10, 4));
        //center 6;3  k = 1/4, for k =4 with [6;3] => [2;-9] , [
        List<VorZone> zones = Solve.findZones(p2s);
        P2 begin = zones.get(0).lineDivs.get(0).begin;
        P2 end = zones.get(0).lineDivs.get(0).end;
        double k = (end.getY() - begin.getY())/(end.getX() - begin.getX());
        assertThat(k).isCloseTo(-4, Percentage.withPercentage(1));

    }

    @Test
    public void chekcTwoZones1() throws Exception {
        List<P2> p2s = Arrays.asList(new P2(1, 1), new P2(4, 6));
        List<VorZone> zones = Solve.findZones(p2s);
        P2 begin = zones.get(0).lineDivs.get(0).begin;
        P2 end = zones.get(0).lineDivs.get(0).end;
        double k = (end.getY() - begin.getY())/(end.getX() - begin.getX());
        assertThat(k).isCloseTo(-3d/5d, Percentage.withPercentage(1));
        assertThat(zones.get(0).lineDivs.get(0).begin).isEqualTo(new P2(-1.6666666,6d));
        assertThat(zones.get(0).lineDivs.get(0).end).isEqualTo(new P2(6.6666666d,1d));

        assertThat(zones.get(1).lineDivs.get(0).begin).isEqualTo(new P2(-1.6666666d,6d));
        assertThat(zones.get(1).lineDivs.get(0).end).isEqualTo(new P2(6.6666666d,1d));
    }


}