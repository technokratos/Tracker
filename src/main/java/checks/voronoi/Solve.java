package checks.voronoi;

import checks.tools.Calc;
import checks.types.P2;
import checks.types.Tuple;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Created by denis on 14.02.17.
 */

public class Solve {


    public static List<VorZone> findZones(List<P2> points) {
        if (points.size() <=2) {
            return findMinimalZones(points);
        }
        points.sort(P2::compareTo);
        List<P2> activeLeftPoints = new LinkedList<>();
        List<P2> readyPoints = new ArrayList<>();
        points.forEach(p2 -> {
            P2 firstProcessPoint = activeLeftPoints.get(0);
            for (int i = 1; i < activeLeftPoints.size(); i++) {
                P2 secondProcessPoint = activeLeftPoints.get(i);
//                LineDiv lineDiv = Solve.findCross(firstProcessPoint, secondProcessPoint, p2);
                ParabolaX firstEquation = ParabolaX.from(firstProcessPoint, p2);
                ParabolaX secondEquation = ParabolaX.from(secondProcessPoint, p2);
                Tuple<Double, Double> crossY = firstEquation.crossGetY(secondEquation);

                final double yBetween;
                if (crossY.a > firstProcessPoint.getY() && crossY.a < secondProcessPoint.getY()) {
                    yBetween = crossY.a;
                } else if (crossY.b > firstProcessPoint.getY() && crossY.b < secondProcessPoint.getY()) {
                    yBetween = crossY.b;
                } else {
                    //todo illegal state
                    throw new IllegalStateException("impossible find cross between two focus");

                }
                double xBetween = firstEquation.getX(yBetween);
                double k = getDirLineBetweenFocus(firstProcessPoint, secondProcessPoint, new P2(xBetween, yBetween));

            }
        });

        return null;
    }

    private static List<VorZone> findMinimalZones(List<P2> points) {
        if (points.size() == 0) {
            return Collections.emptyList();
        }
        if (points.size() == 1) {
            //todo select one zone
        }

        if (points.size() > 2) {
            throw new IllegalArgumentException("too many points");
        }

        double minx = points.stream().min((a,b)-> (int)(a.x - b.x)).get().x;
        double maxx = points.stream().max((a,b)-> (int)(a.x - b.x)).get().x;
        double miny = points.stream().min((a,b)-> (int)(a.y - b.y)).get().y;
        double maxy = points.stream().max((a,b)-> (int)(a.y - b.y)).get().y;


        // -1. 1    1.1
        // -1.-1    1.-1
//        List<P2> corners = IntStream.range(0, 3)
//                .mapToObj(i -> new P2(i / 2 == 0 ? minx : maxx, i % 2 == 0 ? miny : maxy))
//                .collect(Collectors.toList());

        //
        //      0  1  2  3
        // /2   0  0  1  1
        // %2   0  1  0  1

        List<LineDiv> lines = IntStream.range(0, 4)
                .mapToObj(i -> new LineDiv(
                        new P2(i / 2 == 0 ? minx : maxx, (i % 2 == 1  ^ i /2 == 1)? maxy : miny),
                        new P2(((i  + 1) % 4 )/ 2 == 0 ? minx : maxx, ((i+1) % 2 == 1  ^ (i+1) /2 == 1)? maxy : miny)))
                .collect(Collectors.toList());


        P2 p0 = points.get(0);
        P2 p1 = points.get(1);
        double kOriginal = (p1.getY() - p0.getY()) / (p1.getX() - p0.getX());
        double bOriginal = p0.y - kOriginal*p0.x;

        P2 p2avr = p0.add(p1).div(2);
        double k = -1/ kOriginal;//todo remove
        //y=k*x + b;
        //y0 = k* x0 + b;
        //b = y0 - k*x0;
        double b = p2avr.y - k*p2avr.x;

        List<Tuple<P2, LineDiv>> sortedIntersectionWithBorders = lines.stream()
                .map(l -> new Tuple<>( Calc.intersectionLine(k, b, new Tuple<>(l.begin, l.end)), l))
                .filter(l-> !Double.isNaN(l.a.getX()) &&  !Double.isNaN(l.a.getY()))
                .sorted((fp, sp) -> (int)(fp.a.dist(p0) - sp.a.dist(p0))).collect(Collectors.toList());
        Tuple<P2, LineDiv> firstInterSection = sortedIntersectionWithBorders.get(0);
        Tuple<P2, LineDiv> secondInterSection = sortedIntersectionWithBorders.get(1);
        LineDiv lineDiv = new LineDiv(firstInterSection.a, secondInterSection.a);
        VorZone firstZone = new VorZone(p0);
        VorZone secondZone = new VorZone(p1);
        firstZone.addLine(lineDiv);
        secondZone.addLine(lineDiv);
        lineDiv.z0 = firstZone;
        lineDiv.z1 = secondZone;



        //  1   2
        //  0   3

        return Arrays.asList(firstZone, secondZone);
    }

    private static double getDirLineBetweenFocus(P2 p0, P2 p1, P2 pc) {
        double k = (p1.y - p0.y)/(p1.x - p0.x);
        return -k;
    }

    private static LineDiv findCross(P2 firstFocus, P2 secondFocus, P2 lineX) {

        return null;
    }

}
