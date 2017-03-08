package checks.types;

import boofcv.abst.feature.tracker.PointTrack;
import checks.tools.Calc;
import lombok.Data;

/**
 * Created by denis on 01.02.17.
 */
@Data

public class P2t extends P2 implements Comparable<P2>{

    public final long id;
    public final int series;




    public P2t(PointTrack p, int series) {
        this(p.featureId, series, p.x, p.y);
    }

    public P2t(long id, int series, double x, double y) {
        super(x,y);
        this.id = id;
        this.series = series;

    }

    public P2t(double x, double y, int series) {
        this(-1, series, x,y);
    }

    public long getId() {
        return id;
    }



    public boolean near(P2t p) {
        return Math.abs(x -p.x) < Calc.ACCURACY && Math.abs(y - p.y) < Calc.ACCURACY;
    }
    public static boolean near(PointTrack p1, PointTrack p2) {
        return Math.abs(p1.x -p2.x) < Calc.ACCURACY && Math.abs(p1.y - p2.y) < Calc.ACCURACY;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        P2t p2 = (P2t) o;

        if (id != p2.id) return false;
        if (series != p2.series) return false;
        if (Double.compare(p2.x, x) != 0) return false;
        return Double.compare(p2.y, y) == 0;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = (int) (id ^ (id >>> 32));
        result = 31 * result + series;
        temp = Double.doubleToLongBits(x);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(y);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    @Override
    public String toString() {
        return "["+ x+ ";"+y+"]id="+id+";s="+series +";";
    }
}
