package checks.voronoi;

import checks.types.P2;

/**
 * Created by denis on 14.02.17.
 */
public class LineDiv {
    public final P2 begin,end;
    public VorZone z0, z1;

    public LineDiv(P2 begin, P2 end) {
        this(begin,end, null,null);
    }
    public LineDiv(P2 begin, P2 end, VorZone z0, VorZone z1) {
        this.begin = begin;
        this.end = end;
        this.z0 = z0;
        this.z1 = z1;
    }
}
