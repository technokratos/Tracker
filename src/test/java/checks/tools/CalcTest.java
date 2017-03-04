package checks.tools;


import checks.tools.Calc;
import checks.types.P2;
import checks.types.P2t;
import checks.types.P3;
import checks.types.Tuple;
import org.assertj.core.api.Assertions;
import org.assertj.core.data.Percentage;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * Created by denis on 04.02.17.
 */

public class CalcTest {



    private Random r = new Random();

    @Test
    public void testSimpleSolveRandom() {
        Random r = new Random();
        for (int i = 0; i < 1000; i++) {
            double a = r.nextInt(),
                    b = r.nextInt(),
                    c = r.nextInt(),
                    d = r.nextInt(),
                    x0 = r.nextInt(),
                    x1 = r.nextInt();


            P2t B = new Calc().multAX(a, b, c, d, x0, x1);
            // => 5; 11;

            P2t x = new Calc().multInvMatrixOnB(a, b, c, d, B.x, B.y);
            //P2t x = new Calc().multInvMatrixOnB(1, 2, 3, 4, 5, 11);
            Assertions.assertThat(x.x).isCloseTo(x0, Percentage.withPercentage(0.01));
            Assertions.assertThat(x.y).isCloseTo(x1, Percentage.withPercentage(0.01));
            //Assert.assertTrue(Math.abs(x0 - x.x) < 0.0001);
            //Assert.assertTrue(Math.abs(x1 - x.y) < 0.0001);
        }
    }

    @Test
    public void testSimpleSolve() {

        P2t B = new Calc().multAX(1, 2, 3, 4, 1, 2);
        // => 5; 11;

        P2t x = new Calc().multInvMatrixOnB(1, 2, 3, 4, B.x, B.y);
        //P2t x = new Calc().multInvMatrixOnB(1, 2, 3, 4, 5, 11);
        Assert.assertTrue(Math.abs(1 - x.x) < 0.0001);
        Assert.assertTrue(Math.abs(2 - x.y) < 0.0001);
    }

    @Test
    public void findX() {




    /*
    try to find k and b
    two points [x0,y0] and [x1, y1] should be have line
    y = k*x + b;

    y0=k*x0 + b;
    y1=k*x1 + b;

    A = [ x0, 1; x1, 1], B = [y0;y1]
     */
        Random r = new Random();
        for (int i = 0; i < 1000; i++) {


            double x0 = r.nextDouble(),
                    x1 = r.nextDouble(),
                    y0 = r.nextDouble(),
                    y1 = r.nextDouble();
            P2 kb = new Calc().findKb2(x0, x1, y0, y1);
            //P2t kx = new Calc().multInvMatrixOnB(x0, 1, x1, 1, y0, y1);

            double k = kb.x,
                    bLine = kb.y;
        /*
        check what found fisrt and second point in the same line;
         k*x0 - y0 + b = 0;
         k*x1 - y1 + b = 0;

         */
            Assert.assertTrue(Math.abs(k * x0 - y0 + bLine) < 0.00000001);
            Assert.assertTrue(Math.abs(k * x1 - y1 + bLine) < 0.00000001);
        }


    }

    @Test
    public void findDir() {

    }

    @Test
    /**
     * x0 .. x1     x   x1'  x0' - far => z = -1;
     *  x1 .. x0     x   x0'  x1' - far => z = 1;
     */
    public void testNearOrFar() {
        Assert.assertTrue(-1 == new Calc().nextOrPrev(-2, -1, 0));
        Assert.assertTrue(1 == new Calc().nextOrPrev(1, 2, 0));

        Assert.assertTrue(-1 == new Calc().nextOrPrev(-10, -7, -5));
        Assert.assertTrue(1 == new Calc().nextOrPrev(-3, -1, -5));

        Assert.assertTrue(-1 == new Calc().nextOrPrev(10, 7, 5));
        Assert.assertTrue(1 == new Calc().nextOrPrev(3, 1, 5));
        Assert.assertTrue( 1== new Calc().nextOrPrev(0.2, 0.25, 0));
        Assert.assertTrue( -1== new Calc().nextOrPrev(0.2, -0.18, -0.33));
    }
    @Test
    public void testFindDirForPointsInOneLine() {

        P3 dir = new P3(0,0,1);
        double f = 1;
        P3 p0 = new P3(4, 4, - 4);
        P3 p1 = new P3( 7, 7, - 7);

        P2t a00 = p0.getSimpleProjection(f);
        P2t a10 = p1.getSimpleProjection(f);

        P2t a01 = p0.add(dir).getSimpleProjection(f);
        P2t a11 = p1.add(dir).getSimpleProjection(f);

        P3 foundDir = new Calc().findDir(new Tuple<>(a00, a01), new Tuple<>(a10, a11));

        Assert.assertTrue(dir.equals(foundDir));

    }

    @Test
    public void testFindDir() {


        P3 dir = new P3(0,0,1);
        double f = 1;
        P3 p0 = new P3(4, 4, - 4);
        P3 p1 = new P3( -7, 7, - 7);

        P2t a00 = p0.getSimpleProjection(f);
        P2t a10 = p1.getSimpleProjection(f);

        P2t a01 = p0.add(dir).getSimpleProjection(f);
        P2t a11 = p1.add(dir).getSimpleProjection(f);

        P3 foundDir = new Calc().findDir(new Tuple<>(a00, a01), new Tuple<>(a10, a11));

        Assert.assertTrue(dir.equals(foundDir));

    }

    @Test
    public void testFindDir2() {

//P3(8.0, 2.0, 6.0) but found P3(1.3333333333333335, -0.3333333333333336, 1.0) z0 P3(11.0, 10.0, 3.0) z1 P3(17.0, 11.0, -1.0)
        P3 dir = new P3(8.0, 2.0, 6.0);
        double f = 1;
        P3 p0 = new P3(11.0, 10.0, -13.0);
        P3 p1 = new P3(17.0, 11.0, -10.0);

        P2t a00 = p0.getSimpleProjection(f);
        P2t a10 = p1.getSimpleProjection(f);

        P2t a01 = p0.add(dir).getSimpleProjection(f);
        P2t a11 = p1.add(dir).getSimpleProjection(f);

        P3 foundDir = new Calc().findDir(new Tuple<>(a00, a01), new Tuple<>(a10, a11));

        String message = "expected " + dir + " but found " + foundDir + " z0 " + p0 + " z1 " +p1;
        Assert.assertTrue(message, dir.parrall(foundDir));

    }


    @Test
    public void testFindDirRandom() {




        for (int i = 0; i < 100; i++) {


            P3 dir = new P3(rand(), rand(), rand());
            double f = 1;
            P3 p0 = new P3(rand(), rand(), randZ(f));
            P3 p1 = new P3(rand(), rand(), randZ(f));

            P2t a00 = p0.getSimpleProjection(f);
            P2t a10 = p1.getSimpleProjection(f);

            P2t a01 = new P3(p0).add(dir).getSimpleProjection(f);
            P2t a11 = new P3(p1).add(dir).getSimpleProjection(f);

            P3 foundDir = new Calc().findDir(new Tuple<>(a00, a01), new Tuple<>(a10, a11));

            String message = "  new " + dir  + ", new " + p0 + ", new " +p1 + ", found " + foundDir;
            //System.out.print(message);
            System.out.println(dir.parrall(foundDir) + " " + message);
            //Assert.assertTrue(message, dir.parrall(foundDir));
        }

    }

    private double randZ(double f) {
        return -15 -f - rand();
    }

    private double rand() {
        //return 10 * r.nextDouble();
        return r.nextInt(10);
    }

    @Test
    public void testNeighbors(){
        P2t p0 = new P2t(0, 1, 1,1);
        P2t p1 = new P2t(1, 1, 2,1);
        P2t p2 = new P2t(2, 1, 2,0);
        P2t p3 = new P2t(3, 1, 0,0);

        List<Tuple<P2t, P2t>> neighBorsByEach = Calc.findNeighBorsByEach(Arrays.asList(p0, p1, p2, p3));
        neighBorsByEach.forEach(t-> System.out.println(t));


    }

    @Test
    public void testFindDirs(){
        /*
          a = {P2t@2097} "P2t(id=0, series=0, x=604.0, y=13.0)"
  b = {P2t@2098} "P2t(id=0, series=0, x=612.41796875, y=9.36729621887207)"
 b = {Tuple@2096}
  a = {P2t@2102} "P2t(id=1, series=1, x=608.0, y=14.0)"
  b = {P2t@2104} "P2t(id=1, series=1, x=612.7106323242188, y=9.477686882019043)"
b = {P3@2092} "P3(613.1584955635882, 9.04772808603144, -1.0)"

         */
        Tuple<P2t, P2t> firstLine = new Tuple<>(new P2t(0, 0, 604.0, 13.0),
                new P2t(0, 0, 612.41796875, 9.36729621887207));
        Tuple<P2t, P2t> secondLine = new Tuple<>(new P2t(1, 1, 608, 14),
                new P2t(1, 1, 612.7106323242188, 9.477686882019043));
        P3 dir = new Calc().findDir(firstLine,
                secondLine);
        P3 p3 = new P3(613.1584955635882, 9.04772808603144, -1.0);

        int inFirst = new Calc().isLine(firstLine, new P2t(p3.x, p3.y, 0));
        int inSecond = new Calc().isLine(secondLine, new P2t(p3.x, p3.y, 0));

        boolean equals = p3.equals(dir);

    }
}