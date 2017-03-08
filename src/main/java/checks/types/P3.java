package checks.types;

import static java.lang.Math.abs;

/**
 * Created by denis on 04.02.17.
 */
public class P3 {
    public final double x,y,z;
    private final double accuracy = 1;

    public P3(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public double getXWithZ(){return (z> 0)? -x:x;}
    public double getYWithZ(){return (z> 0)? -y:y;}

    public P3(P3 p1) {
        this(p1.x,p1.y, p1.y);
    }

    public P2t getSimpleProjection(double focus) {
        double rel = -focus/z;
        return new P2t(rel * x, rel * y, -1);
    }

    public P3 add(P3 p3) {
       return new P3(x+p3.x, y+p3.y, z+p3.z);
        //return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        P3 p3 = (P3) o;
//        if (abs(x - p3.x) > accuracy) return false;
//        if (abs(z - p3.z) > accuracy) return false;
//        if (abs(z - p3.z) > accuracy) return false;

        // z= 1, acc = accurancy
        // z= 0.1, acc = accurancy/z;

        double accuracy = this.accuracy /p3.z;
        if ((long) (x/ accuracy) != (long) (p3.x/ accuracy)) return false;
        if ((long) (y/ accuracy) != (long) (p3.y/ accuracy)) return false;
        if ((long) (z/ accuracy) != (long) (p3.z/ accuracy)) return false;

        return true;
    }
    public boolean near(P3 o, double accuracy) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        P3 p3 = (P3) o;
        if ((long) (x/accuracy) != (long) (p3.x/ accuracy)) return false;
        if ((long) (y/accuracy) != (long) (p3.y/accuracy)) return false;
        if ((long) (z/accuracy) != (long) (p3.z/accuracy)) return false;
//        if (abs(x - p3.x) > accuracy) return false;
//        if (abs(z - p3.z) > accuracy) return false;
//        if (abs(z - p3.z) > accuracy) return false;

        return true;
    }

    public boolean parrall(P3 p) {
        double len1 = Math.sqrt(p.dist2());
        double len = Math.sqrt(dist2());
        return abs(x/len - p.x/len1 ) < accuracy
                && abs(y/len - p.y/len1)< accuracy
                && abs(z/len - p.z/len1)< accuracy;
    }

    private double dist2() {
        return x*x + y*y + z*z;
    }


    @Override
    public int hashCode() {
        int result;
        long temp;

//        if ((long) (x/accuracy) != (long) (p3.x/accuracy)) return false;
//        if ((long) (y/accuracy) != (long) (p3.y/accuracy)) return false;
//        if ((long) (z/accuracy) != (long) (p3.z/accuracy)) return false;

        double accuracy = this.accuracy/z;
        temp = (long) (x/ accuracy);
        result = (int) (temp ^ (temp >>> 32));
        temp = (long) (y/ accuracy);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = (long) (z/ accuracy);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    @Override
    public String toString() {
        return "P3(" + x +", " + y + ", " + z +")";
    }

    public P3 norm() {
        double len = Math.sqrt(dist2());
        return new P3(x/len,y/len, z/len);

    }

    public double cos(P3 dir) {

        double cos = x * dir.x + y* dir.y + z * dir.z;
        return 0;
    }

    public double dist(P3 p) {
        double dx = p.x - x,
                dy = p.y - y,
                dz = p.z - z;
        return Math.sqrt(dx*dx + dy*dy + dz*dz);
    }
}
