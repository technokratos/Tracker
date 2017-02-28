package checks.types;

/**
 * Created by denis on 25.02.17.
 */
public class Count {
    int value = 0;

    public void inc() {
        value++;
    }

    public int getValue() {
        return value;
    }

    @Override
    public String toString() {
        return  Integer.toString(value) ;
    }
}
