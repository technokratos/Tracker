package checks;

import checks.types.P3;

/**
 * Created by denis on 04.02.17.
 */
public class DirHistory {

    final P3[] dirs;

    int position;

    public DirHistory(int depth) {
        dirs = new P3[depth];
    }


    public void setDir(P3 p3) {
        dirs[position] = p3;
    }
    public void incPosition(){
        position++;
        if (position == dirs.length) {
            position -= dirs.length;
        }
    }

    public int getPosition() {
        return position;
    }

    public P3 getPrev(int back) {
        if (back> dirs.length){
            return null;
        }
        int curPos = position - back;
        if (curPos <0) {
            curPos = curPos + dirs.length;
        }
        return dirs[curPos];
    }
}
