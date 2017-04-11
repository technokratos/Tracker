package checks.processors.operations;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Created by denis on 07.03.17.
 */
public class FunctionList<T,R> {

    List<Function> list = new ArrayList<>();

    public static <T,R> FunctionList<R,? extends Object> get(Function<T, R> biFunction){
        final FunctionList<R, R> functionList = new FunctionList<>();
        functionList.list.add(biFunction);
        return functionList;
    }

    public FunctionList<R, ? extends Object> add(Function<T,R> function ){
        list.add(function);
        return (FunctionList<R, ? extends Object>) this;
    }

    public List<Function> getList() {
        return list;
    }
}
