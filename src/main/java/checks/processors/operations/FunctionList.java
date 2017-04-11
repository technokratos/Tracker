package checks.processors.operations;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * Created by denis on 07.03.17.
 */
public class FunctionList<T> {

    List<Function> list = new ArrayList<>();

    public static <Type, Result> FunctionList<Result> get(Function<Type, Result> Function){
        final FunctionList<Result> functionList = new FunctionList<>();
        functionList.list.add(Function);
        return functionList;
    }

    public <R> FunctionList<R> add(Function<T,R> function ){
        list.add(function);
        return (FunctionList<R>) this;
    }

    public List<Function> getList() {
        return list;
    }
}
