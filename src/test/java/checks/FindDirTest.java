package checks;

import checks.tools.Calc;
import checks.types.P2t;
import checks.types.P3;
import checks.types.Tuple;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.List;

/**
 * Created by denis on 07.02.17.
 */
@RunWith(Parameterized.class)
public class FindDirTest {


    private P3 dir,p0, p1;


    public FindDirTest(P3 dir, P3 p0, P3 p1) {
        this.dir = dir;
        this.p0 = p0;
        this.p1 = p1;
    }
    /*
    false   new P3(5.0, 4.0, 7.0), new P3(7.0, 8.0, -21.0), new P3(9.0, 6.0, -18.0), found P3(0.1454762853726581, 0.11797528895974463, 1.0)
{ new P3(3.0, 6.0, 2.0), new P3(3.0, 1.0, -16.0), new P3(5.0, 4.0, -24.0)},
{ new P3(8.0, 2.0, 5.0), new P3(3.0, 2.0, -19.0), new P3(1.0, 1.0, -25.0)},
{ new P3(6.0, 5.0, 3.0), new P3(6.0, 7.0, -23.0), new P3(0.0, 7.0, -23.0)},
{ new P3(0.0, 0.0, 3.0), new P3(4.0, 1.0, -19.0), new P3(7.0, 7.0, -22.0)},
     */

    @Parameterized.Parameters
    public static List<Object[]> data(){
        Object[][] objects = new Object[][]{
                { new P3(0.0, 4.0, -2.0), new P3(-3.0, -1.0, -16.0), new P3(5.0, 4.0, -20.0)},
                { new P3(0.0, 6.0, 2.0), new P3(-3.0, -1.0, -16.0), new P3(5.0, 4.0, -20.0)},





                { new P3(3.0, 0, 0), new P3(-3.0, -1.0, -16.0), new P3(5.0, 4.0, -20.0)},
                { new P3(-3.0, 0, 0), new P3(-3.0, -1.0, -16.0), new P3(5.0, 4.0, -20.0)},
                { new P3(0.0, 3, 0), new P3(-3.0, -1.0, -16.0), new P3(5.0, 4.0, -20.0)},
                { new P3(0.0, -3, 0), new P3(-3.0, -1.0, -16.0), new P3(5.0, 4.0, -20.0)},
                { new P3(0.0, 0.0, 3.0), new P3(4.0, 1.0, -19.0), new P3(7.0, 7.0, -22.0)},
                { new P3(0.0, 0.0, -3.0), new P3(4.0, 1.0, -19.0), new P3(7.0, 7.0, -22.0)},

                { new P3(3.0, 0.0, 2.0), new P3(-3.0, -1.0, -16.0), new P3(5.0, 4.0, -20.0)},
                { new P3(3.0, 0.0, -2.0), new P3(-3.0, -1.0, -16.0), new P3(5.0, 4.0, -20.0)},
                { new P3(-1.0, 0.0, 3.0), new P3(4.0, 1.0, -19.0), new P3(7.0, 7.0, -22.0)},
                { new P3(-1.0, 0.0, -3.0), new P3(4.0, 1.0, -19.0), new P3(7.0, 7.0, -22.0)},

                { new P3(3.0, 4.0, 2.0), new P3(-3.0, -1.0, -16.0), new P3(5.0, 4.0, -20.0)},
                { new P3(0.0, 4.0, -2.0), new P3(-3.0, -1.0, -16.0), new P3(5.0, 4.0, -20.0)},
                { new P3(0.0, 6.0, 2.0), new P3(-3.0, -1.0, -16.0), new P3(5.0, 4.0, -20.0)},

                { new P3(3.0, 4.0, -2.0), new P3(-3.0, -1.0, -16.0), new P3(5.0, 4.0, -20.0)},
                { new P3(5.0, 2.0, -5.0), new P3(3.0, 2.0, -19.0), new P3(1.0, -1.0, -25.0)},
                { new P3(6.0, 5.0, 3.0), new P3(6.0, 7.0, -23.0), new P3(0.0, 7.0, -23.0)},
                { new P3(5.0, 4.0, 7.0), new P3(7.0, 8.0, -21.0), new P3(9.0, 6.0, -18.0)},
                { new P3(5.0, 4.0, -7.0), new P3(7.0, 8.0, -21.0), new P3(9.0, 6.0, -18.0)}
        };
        return Arrays.asList(objects);
    }



    @Test
    public void testFindDir() {


        double f = 1;

        P2t a00 = p0.getSimpleProjection(f);
        P2t a01 = p0.add(dir).getSimpleProjection(f);

        P2t a10 = p1.getSimpleProjection(f);
        P2t a11 = p1.add(dir).getSimpleProjection(f);

        P3 foundDir = new Calc().findDir(new Tuple<>(a00, a01), new Tuple<>(a10, a11));

        String message = "expected " + dir + " but found " + foundDir + " z0 " + p0 + " z1 " +p1;
        Assert.assertTrue(message, dir.parrall(foundDir));
        System.out.printf(message);

    }
}
