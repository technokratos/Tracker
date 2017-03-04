package checks.tools;

import checks.types.P2;
import checks.types.P2t;
import checks.types.P3;
import checks.types.Tuple;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.lang.Math.sqrt;

/**
 * Created by denis on 03.02.17.
 */
public class Calc {
    public static final double ACCURACY = 0.00001;

    public static List<Tuple<P2t, P2t>> findNeighbors(List<P2t> tracks, int zones) {

        Map<Tuple<Integer, Integer>, List<P2t>> tupleListMap = tracks.stream()
                .collect(Collectors.groupingBy(p -> new Tuple<>(((int) p.x) % zones, ((int) p.y) % zones)));

        //tupleListMap.entrySet().stream().map(Map.Entry::getValue).flatMap(this::findNeighBorsByEach).collect(Collectors.toList());
        List<Tuple<P2t,P2t>> result = new ArrayList<>(tracks.size());
        tupleListMap.entrySet().forEach(e->result.addAll( findNeighBorsByEach(e.getValue())));

        return findNeighBorsByEach(tracks);
        //return result;
    }

    public static List<Tuple<P2t,P2t>> findNeighBorsByEach(List<P2t> tracks) {
        List<Tuple<P2t,P2t>> tuples = new ArrayList<>(tracks.size()/2);
        for (int i = 0; i < tracks.size(); i++) {

            P2t p2 = tracks.get(i);
            P2t p2n = null;
            double minDist = Double.MAX_VALUE;

            for (int j = 0; j < tracks.size(); j++) {
                if (j != i) {
                    P2t p2n1 = tracks.get(j);
                    double dist = p2.dist(p2n1);
                    if (dist < minDist) {
                        p2n = p2n1;
                        minDist = dist;
                    }
                }
            }
            if (p2n == null) {
                throw new IllegalStateException("Not found neighbors");
            }
            tuples.add(new Tuple<>(p2, p2n));

        }
        return tuples;
    }

    public static Tuple<Double, Double> solveQuadratic(double A, double B, double C) {
        double D = B*B - 4 * A* C;
        if (D <0 ) {
            return null;
        } else {
            double d = sqrt(D);
            return new Tuple<>((-B - d) / 2, (-B + d) / 2);
        }
    }


    public P2t multInvMatrixOnB(double a, double b, double c, double d, double b0, double b1) {
        double det = getDet(a, b, c, d);
        P2t p2 = multAX(d / det, -b / det, -c / det, a / det, b0, b1);
        return p2;
    }

    private static double getDet(double a, double b, double c, double d) {
        return a * d - b * c;
    }

    public static P2t multAX(double a, double b, double c, double d, double x0, double x1){
        double b0  = a * x0 + b * x1, b1 = c * x0 + d * x1;
        return new P2t(b0, b1, -1);
    }

    public P2t findKb(double x0, double x1, double y0, double y1) {
        double a = x0,
                b = 1,
                c = x1,
                d = 1,
                b0 = y0,
                b1 = y1;
        return new Calc().multInvMatrixOnB(a, b, c, d, y0, y1);
    }

    public static P2 findKb2(double x0, double x1, double y0, double y1) {

      double k = (y1 - y0)/(x1 - x0),
              b= - k * x0 + y0;
      return new P2(k,b);

    }

    /**
     * find k, b: <br>
     * A = [x0 1; x1 1]<br>
     * B = [y0;  y1]<br>
     * X = [k; b]<br>
     *<br>
     *
     * y=k0*x + b0 => -k0*x + y = b0;
     * y=k1*x + b1 => -k1*x + y = b1;
     * A= [ -k0, 1; -k1, 1];
     * B = [b0;b1]
     *
     * invA = [1, -1; k1, -k0]
     * det = -k0 + k1; = k1-k0;
     *
     * find intersection<br>
     * A = [k0 -1; k1 -1]<br>
     * B = [-b0; -b1]<br>
     * X = [x;y]<br>
     *
     * @param firstLine
     * @param secondLine
     * @return
     */

    public P3 findDir(Tuple<? extends P2, ? extends P2> firstLine, Tuple<? extends P2, ? extends P2> secondLine){

        //line A
        // z0, z1
        P2 firstLineKb = findKb2(firstLine.a.x, firstLine.b.x, firstLine.a.y, firstLine.b.y);
        P2 secondLineKb = findKb2(secondLine.a.x, secondLine.b.x, secondLine.a.y, secondLine.b.y);

        /*
        y = k0 * x + b0;
        y = k1 * x + b1;
        k0 * x + y = b0;
        k1 * x + y = b1;
        A= [k0, 1; k1, 1]
        B = [b0; b1]

         */

        double x00 = firstLine.a.x,
                y00 = firstLine.a.y,
                x01 = firstLine.b.x,
                y01 = firstLine.b.y,
                x10 = secondLine.a.x,
                y10 = secondLine.a.y,
                x11 = secondLine.b.x,
                y11 = secondLine.b.y;


        double k0 = firstLineKb.x,
                b0 = firstLineKb.y,
                k1 = secondLineKb.x,
                b1 = secondLineKb.y;
        double det = getDet(k0, (double) 1, k1, (double) 1);
        //invA = [1, -1; k1, -k0]
        //det = -k0 + k1; = k1-k0;
        double detm = k1 - k0;
        double x, y, z;
        if (Math.abs(det)> ACCURACY) {
            P2t p2 = multAX((double) 1 / det, -(double) 1 / det, -k1 / det, k0 / det, b0, b1);
            P2t p2m = multAX((double) 1 / detm, -(double) 1 / detm, k1 / detm, -k0 / detm, b0, b1);
            x = p2m.x; //todo check why -
            y = p2m.y;
            if (k0 > 1){//=> decide on z
                double y0 = firstLine.a.y,
                        y1 = firstLine.b.y;
                z= nextOrPrev(y0, y1, y);
            } else {//decide on x
                double x0 = firstLine.a.x,
                        x1 = firstLine.b.x;
                z= nextOrPrev(x0, x1, x);
            }
            if (z>0) {
                x = -x;
                y = -y;
            }

        } else {
            z = 0;
            x = firstLine.b.x - firstLine.a.x;
            y = firstLine.b.y - firstLine.a.y;
        }


        return new P3(x,y,z);

    }


    public P2 intersectionLine(Tuple<P2, P2> firstLine, Tuple<P2, P2> secondLine){

        P2 firstLineKb = findKb2(firstLine.a.x, firstLine.b.x, firstLine.a.y, firstLine.b.y);

        double k0 = firstLineKb.x,
                b0 = firstLineKb.y;

        return intersectionLine(k0, b0, secondLine);

    }

    public static P2 intersectionLine(double k0, double b0, Tuple<P2, P2> secondLine) {
        P2 secondLineKb = findKb2(secondLine.a.x, secondLine.b.x, secondLine.a.y, secondLine.b.y);
        double        k1 = secondLineKb.x,
                b1 = secondLineKb.y;
        double det = getDet(k0, (double) 1, k1, (double) 1);
        //invA = [1, -1; k1, -k0]
        //det = -k0 + k1; = k1-k0;
        double detm = k1 - k0;
        double x, y, z;
        if (Math.abs(det)> ACCURACY) {
            P2t p2 = multAX((double) 1 / det, -(double) 1 / det, -k1 / det, k0 / det, b0, b1);
            P2t p2m = multAX((double) 1 / detm, -(double) 1 / detm, k1 / detm, -k0 / detm, b0, b1);
            x = p2m.x; //todo check why -
            y = p2m.y;
            return new P2(x, y);
        } else {
            return null;
        }
    }

    /**     *
     *
     * @return 2 in the point in direct order, 1 between, 0 prev, -1, in not the line
     *
     */
    public int isLine(Tuple<P2t, P2t> line, P2t check) {

        //line A
        // z0, z1
        P2 firstLineKb = findKb2(line.a.x, line.b.x, line.a.y, line.b.y);


        /*
        y = k * x + b;
        k * x + y = b;
         */

        double x0 = line.a.x,
                y0 = line.a.y,
                x1 = line.b.x,
                y1 = line.b.y;


        double k = firstLineKb.x,
                b = firstLineKb.y;

        double x = check.x;
        double y = k * x + b;
        if (Math.abs(y - check.y) < ACCURACY) {
            final int result;
            if (k > 1){//=> decide on z

                result = positionInLine(y0, y1, y);
            } else {//decide on x
                result = positionInLine(x0, x1, x);
            }
            if (positionInLine(y0, y1, y) != positionInLine(x0, x1, x)) {
                throw new IllegalStateException("diffrent position in OX and OY");
            }
            return result;
        } else {
            return -1;
        }
    }

    int positionInLine(double x0, double x1, double x) {
        //x>x1>x0 || x<x1<x0
        if ((x > x1 && x1 > x0) || (x < x1 && x1 < x0)) {
            return 2;
        }
        //x>x0>x1 || x<x0<x1
        if ((x > x0 && x0>x1 ) || ( x < x0 && x0 < x1)) {
            return 0;
        }
        //x0>x>x1 || x0<x<x1
        if ((x0 > x && x>x1 ) || ( x0 < x && x < x1)) {
            return 1;
        }
        throw new IllegalArgumentException("x between x0 and x1 " + x + ", " + x0 + ", " +x1);
    }

    /**
     *  x0 .. x1     x   x1'  x0' - far => z = -1;
     *  x1 .. x0     x   x0'  x1' - far => z = 1;
     *
     */
    double nextOrPrev(double x0, double x1, double x) {
        double z;
        z = (x1 > x0) ? ((x > x1)? -1 : 1) : (( x < x1 )? -1: 1);
        return z;
//        //x>x1>x0 || x<x1<x0
//        if ((x > x1 && x1 > x0) || (x < x1 && x1 < x0)) {
//            return -1;
//        }
//        //x>x0>x1 || x<x0<x1
//        if ((x > x0 && x0>x1 ) || ( x < x0 && x0 < x1)) {
//            return 1;
//        }
//        throw new IllegalArgumentException("x between x0 and x1 " + x + ", " + x0 + ", " +x1);
    }
}
