package checks.processors.operations;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

/**
 * Created by denis on 07.03.17.
 */
public class FunctionList<T,U,R> {

    List<BiFunction> list = new ArrayList<>();

    public static <T,U,R> FunctionList<R,U,? extends Object> get(BiFunction<T, U, R> biFunction){
        final FunctionList<R, U, Object> functionList = new FunctionList<>();
        functionList.list.add(biFunction);
        return functionList;
    }

    public FunctionList<R, U, ? extends Object> add(BiFunction<T,U,R> function ){
        list.add(function);
        return (FunctionList<R, U, ? extends Object>) this;
    }

    public List<BiFunction> getList() {
        return list;
    }
}
