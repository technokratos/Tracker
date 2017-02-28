package checks.types;

import checks.Calc;

/**
 * Created by denis on 14.02.17.
 */
public class P2 implements Comparable<P2>{


    public final double x;

    public final double y;



    public P2(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public P2(P2 firstPoint) {
        x = firstPoint.x;
        y = firstPoint.y;
    }


    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    @Override
    public int compareTo(P2 p2) {
        return (int) (p2.x - x);
    }

    public double dist(P2 p2n) {
        double dy = p2n.y - y;
        double dx = p2n.x - x;
        return Math.sqrt(dx * dx + dy * dy);
    }

    public boolean near(P2 p) {
        return Math.abs(x -p.x) < Calc.ACCURACY && Math.abs(y - p.y) < Calc.ACCURACY;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        return near((P2) o);
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        temp = Double.doubleToLongBits(x);
        result = (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(y);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    public P2 add(P2 p1) {
        return new P2(x + p1.getX(), y + p1.getY());
    }
    public P2 div(double d) {
        return new P2(x/d, y/d);
    }

    public Operation add(Operation oper){
        return oper.action(this);
    }

    public double dist2(P2 p2n) {
        double dy = p2n.y - y;
        double dx = p2n.x - x;
        return dx * dx + dy * dy;
    }

    @Override
    public String toString() {
        return "[" +
                x +
                ";" + y +
                ']';
    }

    private class Operation {

        public Operation action(P2 p2) {
            return null;
        }
    }
}
