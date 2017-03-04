package checks.dir;

import checks.tools.Calc;
import checks.neighborns.Pifagor.Link;
import checks.types.P2;
import checks.types.P2t;
import checks.types.P3;
import checks.types.Tuple;

import java.util.*;
import java.util.stream.IntStream;

/**
 * Created by denis on 21.02.17.
 */
public class DirCollector {
    final static int DIR_NUMBER = 6;
    final static double ANGLE = Math.PI/DIR_NUMBER;
    final static double COS = Math.cos(ANGLE);

    List<Direction> directions = new ArrayList<>();
    public static final Calc CALC = new Calc();
    private P3 prevDir;

    {
        //
        //new P2(i / 2 == 0 ? minx : maxx, (i % 2 == 1  ^ i /2 == 1)? maxy : miny),
        //        new P2(((i  + 1) % 4 )/ 2 == 0 ? minx : maxx, ((i+1) % 2 == 1  ^ (i+1) /2 == 1)? maxy : miny)))
        IntStream.range(0, 6).mapToObj( i -> new PointDirection(
                new P3(i%3==0? Math.signum(i - 3):0, (i+1)%3 == 0? Math.signum(i - 3):0, (i+2)%3==0? Math.signum(i - 3):0))
        ).forEach(directions::add);
    }

    public Map<P3, Integer> addLinks(List<Tuple<Link, Tuple<P2t, P2t>>> linkWithPrevPoints, double accurancy) {


        Map<P3, Integer> dirWithCount = new HashMap<>();
        if (linkWithPrevPoints.size()>1) {
            prevDir = getDir(linkWithPrevPoints.get(0));

            IntStream.range(1, linkWithPrevPoints.size()).forEach(i -> {

                P3 curDir = getDir(linkWithPrevPoints.get(i));
                double dist = curDir.dist(prevDir);
                System.out.print(""+ dist + ";");
                if (dist< accurancy) {
                    Integer integer = dirWithCount.get(curDir);
                    if (integer == null) {
                        dirWithCount.put(curDir, 1);
                    } else {
                        dirWithCount.put(curDir, integer++);
                    }

                }
                prevDir = curDir;

            });

        }
        System.out.println("\nCount of directions" + dirWithCount.size());

//        linkWithPrevPoints.forEach(lp-> {
//
//            P3 dir = getDir(lp);
//            lp.a.
//
////            directions.stream().anyMatch(d-> {
////                return d.tryToAdd(dir, link, prevPoints);
////            });
//        });
        return dirWithCount;
    }

    private P3 getDir(Tuple<Link, Tuple<P2t, P2t>> lp) {
        Link link = lp.a;
        Tuple<P2t, P2t> prevPoints = lp.b;
        return CALC
                .findDir(new Tuple<>(link.getA().getP(), prevPoints.a), new Tuple<>(link.getB().getP(), prevPoints.b))
                .norm();
    }


    public static abstract class Direction {
        //history of directions
        final List<Direction> history = new ArrayList<>();
        final List<P2> points = new ArrayList<>();
        final List<P3> dirs = new ArrayList<>();


        public abstract boolean tryToAdd(P3 d, Link link, Tuple<P2t, P2t> prevPoints);

        public List<P3> getDirs() {
            return dirs;
        }
    }

    public static class PointDirection extends Direction{

        P3 dir;
        double sigma;

        public PointDirection(P3 dir) {
            this.dirs.add(dir);
            this.dir = dir;
        }

        @Override
        public boolean tryToAdd(P3 d, Link link, Tuple<P2t, P2t> prevPoints) {

            double cos = d.cos(dir);
            int n = dirs.size();
            if (cos < COS/ n) {
                dirs.add(d);
                dir = new P3((dir.x * n + d.x)/(n+1),(dir.y * n + d.y)/(n+1),(dir.z * n + d.z)/(n+1)).norm();
                return true;
            }

            return false;
        }

        public P3 getDir() {
            return dir;
        }

        @Override
        public String toString() {
            return "PointDirection{" +
                    "dir=" + dir +
                    '}';
        }
    }
}
