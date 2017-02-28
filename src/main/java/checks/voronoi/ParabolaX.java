package checks.voronoi;

import checks.types.P2;
import checks.types.Tuple;

import static java.lang.Math.sqrt;

/**
 * x=A*(y-yf)2 + C;
 */
public class ParabolaX {
    final double A,C, yf;

    ParabolaX(double a, double c, double yf) {
        A = a;
        this.yf = yf;
        C = c;
    }

    ParabolaX(P2 focus, double xl) {
        double xf = focus.x;
        this.yf = focus.y;
        A = 1/ (2 *(xf - xl));
        C = (xf + xl)/ 2;
    }
    double getX(double y) {
        double div = y - yf;
        return A* div * div + C;
    }
    static ParabolaX from(P2 focus, P2 lineX) {
        double xf = focus.x;
        double yf = focus.y;
        double xl = lineX.x;
        double div = 1/ (2*xf - 2* xl);
        return new ParabolaX(1/div, (xf + xl)/2, yf);
    }

    /**
     *
     *
     * A0*(y-yf)2 + C0 =
     *  A1*(y-y1)2 + C1;
     *  (A0 - A1)*y^2 + (-2*A0*yf + 2*A1*y1)*y + A0*yf^2  - A1*y1^2 + C0 - C1 = 0
     *     A                   B                              C
     * @param q
     * @return two y points
     */
    Tuple<Double, Double> crossGetY(ParabolaX q) {
        double A0 = A, A1= q.A,
                yf1= q.yf, C1 = q.C;

        double Ai = (A0 - A1);
        double Bi_2 = (-A0 * yf + A1*yf1);
        double Ci = A0* yf * yf - A1*yf1*yf1 + C - C1;

        double D = Bi_2 * Bi_2 - Ai * Ci;
        if (D <0) {
            return null;
        }
        double d = sqrt(D);

        Tuple<Double, Double> solve = Solve.solveQuadratic(Ai, 2 * Bi_2, Ci);

        return new Tuple<>(-Bi_2 - d, -Bi_2 + d);

    }
}
