package checks.voronoi;

import checks.types.P2;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by denis on 14.02.17.
 */
public class VorZone  {

    public final List<LineDiv> lineDivs = new ArrayList<>();
    private final P2 p2;

    public VorZone(double x, double y) {
        this.p2 = new P2(x,y);
    }

    public VorZone(P2 p0) {
        p2 = p0;
    }

    public void addLine(LineDiv div) {
        lineDivs.add(div);
    }
}
