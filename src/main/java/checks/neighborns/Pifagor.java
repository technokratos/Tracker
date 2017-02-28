package checks.neighborns;

import checks.types.P2;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
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
        P2 firstPoint = points.get(0);
        P2 secondPoint = points.get(1);
        Link firstLink = Link.initLink(firstPoint, secondPoint);
        activeLinks.add(firstLink);


        double lineX = secondPoint.getX();
        LastPoint lastLineXPoint = new LastPoint(secondPoint);
        IntStream.range(2, points.size())
                .mapToObj(points::get)
                .forEach(challenger ->{
                    List<Link> toRemove = new ArrayList<>();
                    List<Link> toAdd = new ArrayList<>();

                    activeLinks.forEach(activeLink -> {
                        //remove active Link if all distances is less then length of to line


                        if (toAdd.size() > 0) {
                            //if already added link for this point, then filter next by  distance between last added points
                            // or length of link is lesser then distance to line
                            //second greater the first then 1;
                            //if (comparator.compare(challenger, lastLineXPoint.p) >= 0
//                            if (comparator.compare(lastLineXPoint.p, challenger) >= 0
//                                    && activeLink.minDistGreaterLength(challenger.x)) {
                            if (activeLink.minDistGreaterLength(challenger.x)) {
                                toRemove.add(activeLink);
                                result.add(activeLink);
                                return;

                            }
                            lastLineXPoint.p = challenger;
                        }
                        //if (lastLineXPoint.p != challenger) {

                        //}

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
                    });
                    activeLinks.removeAll(toRemove);
                    activeLinks.addAll(toAdd);

                });
        result.addAll(activeLinks);
        return result;
    }



    @EqualsAndHashCode
    public static class Link {
        final PointWrapper a,b;
        final double dist2;
        final double dist;


        public Link(PointWrapper a, PointWrapper b) {
            this(a,b, a.p.dist(b.p));
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

        public static Link initLink(P2 a, P2 b, double dist) {
            PointWrapper aw = new PointWrapper(a);
            PointWrapper bw = new PointWrapper(b);
            Link link = new Link(aw, bw, dist);
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
