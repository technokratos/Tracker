package checks.voronoi;

import checks.types.Tuple;
import org.junit.Test;

;import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by denis on 16.02.17.
 */
public class ParabolaXTest {
    @Test
    public void crossGetY() throws Exception {
        //focus [0;0], line +1
        ParabolaX p0 = new ParabolaX(-0.5, 0.5, 0);


        //focus [0;2], line +1
        ParabolaX p1 = new ParabolaX(-0.5, 0.5, 2);
        assertThat(p0.crossGetY(p1)).isEqualTo(new Tuple<>(0d, 2d));


        ParabolaX p2 = new ParabolaX(2, 1, 1);
        assertThat(p0.crossGetY(p2)).isNull();

        ParabolaX p3 = new ParabolaX(-0.5,  3, -2);
        Tuple<Double, Double> crossP0_P3 = p0.crossGetY(p3);
        assertThat(crossP0_P3).isEqualTo(new Tuple<>(-2d,0d));


        //focus [0;0], line -1
        ParabolaX p4 = new ParabolaX(0.5,  -0.5, 0);
        assertThat(p0.crossGetY(p4)).isEqualTo(new Tuple<>(-1d,1d));


    }

}