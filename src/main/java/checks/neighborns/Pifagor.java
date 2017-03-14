package checks.neighborns;

import checks.types.P2;
import lombok.EqualsAndHashCode;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.lang.Math.*;

/**
 * Created by denis on 20.02.17.
 */
public class Pifagor {



    public static List<Link> findLinks(List<? extends P2> points) {
        Comparator<P2> compareTo = P2::compareTo;
        return findLinks(points, compareTo);
    }

    public static List<Link> findLinks(List<? extends P2> points, Comparator<P2> comparator) {
        points.sort(comparator);
        LinkedList<Link> activeLinks = new LinkedList<>();
        LinkedList<Link> result = new LinkedList<>();
        if (points.size() <2) {
            return Collections.emptyList();
        }
        P2 firstPoint = points.get(0);
        P2 secondPoint = points.get(1);
        Link firstLink = Link.initLink(firstPoint, secondPoint);
        activeLinks.add(firstLink);

        double minY = points.stream().min((Comparator<P2>) (pA, pB) -> (int) Math.signum(pA.y - pB.y)).get().y;
        double maxY = points.stream().max((Comparator<P2>) (pA, pB) -> (int) Math.signum(pA.y - pB.y)).get().y;

        //double lineX = secondPoint.getX();
        double lastX = secondPoint.getX();
        LastPoint lastLineXPoint = new LastPoint(secondPoint);
        IntStream.range(2, points.size())
                .mapToObj(points::get)
                .forEach(challenger ->{
                    List<Link> toRemove = new ArrayList<>();
                    List<Link> toAdd = new ArrayList<>();

                    final Optional<Link> nearestLinkOpt = activeLinks.stream().min((link1, link2) -> (int) Math.signum(link1.midDist2(challenger) - link2.midDist2(challenger)));

                    final Link nearestLink = nearestLinkOpt.get();
                    //distance to nearest link is greater than dist between  ([ch.x; maxy] and [ch.x;miny]) and active link
                    final P2 downPoint = new P2(challenger.x, maxY);
                    final P2 upPoint = new P2(challenger.x, minY);
                    double distToUp = nearestLink.midDist2(downPoint);
                    double distToDown = nearestLink.midDist2(upPoint);

                    //filter remove not need active links
//                    activeLinks.stream()
//                            .filter(a-> a.midDist2(downPoint) > distToDown && a.midDist2(upPoint)> distToUp)
//                            .forEach(toRemove::add);
                    final List<Link> fromActiveToResult = activeLinks.stream()
                            .filter(a -> a.midDist2(downPoint) > distToDown && a.midDist2(upPoint) > distToUp)
                            .collect(Collectors.toList());
                    toRemove.addAll(fromActiveToResult);
                    result.addAll(fromActiveToResult);


                    addLink(challenger, toRemove, toAdd, nearestLink);

                    activeLinks.removeAll(toRemove);
                    activeLinks.addAll(toAdd);

                });
        result.addAll(activeLinks);
        return result;
    }

    private static void addLink(P2 challenger, List<Link> toRemove, List<Link> toAdd, Link activeLink) {
        P2 a = activeLink.a.p;
        P2 b = activeLink.b.p;
        P2 c = challenger;
        double ca = challenger.dist2(a),
                cb = challenger.dist2(b),
                ab = activeLink.dist2;

        if (ab > ca + cb) {
            //insert point, remove old line, add two lines
            activeLink.remove();
            toRemove.add(activeLink);

            toAdd.add(Link.initLink(a, c));
            toAdd.add(Link.initLink(b, c));
        } else if ( toAdd.isEmpty()
                || (toAdd.get(0).dist2 >= ca && toAdd.get(0).dist2 >= cb) ) {
            // add to near point, and check new links is shorter previous link
            if (toAdd.size() == 1) {
                toAdd.remove(0);
            }
            if (ca < cb) {
                //add to a
                toAdd.add(Link.initLink(a, c));
            } else {
                //add to b
                toAdd.add(Link.initLink(b, c));
            }
        }
    }


    @EqualsAndHashCode
    public static class Link {
        final PointWrapper a,b;
        final double dist2;
        final double dist;


        public Link(PointWrapper a, PointWrapper b) {
            this(a,b, a.p.dist2(b.p));
        }

        public Link(PointWrapper a, PointWrapper b, double dist2) {
            this.a = a;
            this.b = b;
            this.dist2 = dist2;
            this.dist = sqrt(dist2);
        }


        public PointWrapper getA() {
            return a;
        }

        public PointWrapper getB() {
            return b;
        }

        static Link initLink(P2 a, P2 b) {
            return initLink(a, b, a.dist2(b));
        }

        public static Link initLink(P2 a, P2 b, double dist2) {
            PointWrapper aw = new PointWrapper(a);
            PointWrapper bw = new PointWrapper(b);
            Link link = new Link(aw, bw, dist2);
            aw.links.add(link);
            bw.links.add(link);
            return link;
        }

        public void remove() {
            a.links.remove(this);
            b.links.remove(this);
        }

        public boolean minDistGreaterLength(double x) {
            //todo should be impossible if  sing(x-a.p.x) != sign( x - b.p.x)
            return ((x - a.p.x) > dist && (x - b.p.x) > dist) ||
                    ((a.p.x - x) > dist && (b.p.x - x) > dist);
        }
        public double getMinDistToLine(double x) {
            return min(abs(x - a.getX()), abs(x - b.getX()));
        }

        @Override
        public String toString() {
            return "{" +
                    a +
                    ", " + b +
                    ", =" + dist +
                    '}';
        }

        public double midDist2(P2 challenger) {
            final double ad = a.getP().dist2(challenger);
            final double bd = b.getP().dist2(challenger);
            return ad<bd? ad: bd;
        }
    }


    public static class PointWrapper {
        final P2 p;
        List<Link> links = new ArrayList<>();

        public PointWrapper(P2 p) {
            this.p = p;
        }

        public P2 getP() {
            return p;
        }

        @Override
        public String toString() {
            return p.toString();
        }
        public double getX(){
            return p.x;
        }
        public double getY() {
            return p.y;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            PointWrapper that = (PointWrapper) o;

            return p != null ? p.equals(that.p) : that.p == null;
        }

        @Override
        public int hashCode() {
            return p != null ? p.hashCode() : 0;
        }
    }

    private static class Dist {
        double  d = Double.MAX_VALUE;
    }

    private static class LastPoint {
        P2 p;

        public LastPoint(P2 p) {
            this.p = p;
        }
    }
}
