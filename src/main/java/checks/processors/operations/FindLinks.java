package checks.processors.operations;

import checks.neighborns.Pifagor;
import checks.neighborns.Pifagor.Link;
import checks.types.P2t;

import java.util.List;

/**
 * Created by denis on 07.03.17.
 */
public class FindLinks extends ContextFunction<List<P2t>, List<Link>> {


    @Override
    public List<Link> apply(java.util.List<P2t> tracks) {
        return Pifagor.findLinks(tracks);
    }
}
