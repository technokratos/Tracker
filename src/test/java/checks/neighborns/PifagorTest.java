package checks.neighborns;

import checks.types.P2;
import org.assertj.core.api.Assertions;
import org.junit.Test;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by denis on 20.02.17.
 */
public class PifagorTest {
    @Test
    public void testFindLinks() throws Exception {
        List<P2> points = Arrays.asList(new P2(0, 0), new P2(10, 0), new P2(5, 3), new P2(7, 7), new P2(10, 10));
        List<Pifagor.Link> links = Pifagor.findLinks(points);
        assertThat(links.size()).isEqualTo(4);


    }

    @Test
    public void testFindLinksDirect() throws Exception {
        List<P2> points = Arrays.asList(new P2(0, 0), new P2(10, 0), new P2(5, 3), new P2(7, 7), new P2(10, 10));

        List<Pifagor.Link> links = checks.neighborns.Pifagor.findLinks(points, (p2, t1) -> (int) (p2.x - t1.x));

        assertThat(links.size()).isEqualTo(4);


    }

}