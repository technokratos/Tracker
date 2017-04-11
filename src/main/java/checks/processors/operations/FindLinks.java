package checks.processors.operations;

import checks.neighborns.Pifagor;
import checks.neighborns.Pifagor.Link;
import checks.types.P2t;

import java.awt.*;

import java.util.*;
import java.util.List;
import java.util.function.BiFunction;

/**
 * Created by denis on 07.03.17.
 */
public class FindLinks extends ContextFunction<java.util.List<P2t>, List<Link>> {


    @Override
    public List<Link> apply(java.util.List<P2t> tracks) {
        return Pifagor.findLinks(tracks);
    }
}
