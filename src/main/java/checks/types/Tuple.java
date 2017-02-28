package checks.types;

/**
 * Created by denis on 01.02.17.
 */
public class Tuple<T, T1> {
    public T a;
    public T1 b;

    public Tuple(T a, T1 b) {
        this.a =a;
        this.b =b;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Tuple<?, ?> t = (Tuple<?, ?>) o;
        if ((a.equals(t.a) && b.equals(t.b)) || (a.equals(t.b) && b.equals(t.a))) {
            return true;
        }

        return (a != null ? a.equals(t.a) : t.a == null) && (b != null ? b.equals(t.b) : t.b == null);
    }

    @Override
    public int hashCode() {
        int result = a != null ? a.hashCode() : 0;
        result = 31 * result + (b != null ? b.hashCode() : 0);
        return result;
    }
}
