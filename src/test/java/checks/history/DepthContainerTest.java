package checks.history;

import checks.types.P2t;
import org.junit.Test;

import java.util.Collections;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;


/**
 * Created by denis on 11.02.17.
 */
public class DepthContainerTest {

    @Test
    public void testPrevOnggeStep() {
        DepthContainer container = new DepthContainer(4);
        P2t p0= new P2t(1, 1,0, 0);
        container.add(Collections.singletonList(p0));
        P2t p1 = new P2t(1, 1, 1,1);
        assertThat(container.getPrev(p1)).isEqualTo(p0);
        assertThat(container.getPrev(p1,1)).isEqualTo(p0);
        assertThat(container.getPrev(p1,2)).isNull();
        assertThat(container.getPrev(p1,3)).isNull();
        assertThat(container.getPrev(p1,4)).isNull();

        container.add(Collections.singletonList(p1));
        P2t p2 = new P2t(1, 1, 2,2);
        assertThat(container.getPrev(p2)).isEqualTo(p1);
        assertThat(container.getPrev(p2,1)).isEqualTo(p1);
        assertThat(container.getPrev(p2,2)).isEqualTo(p0);
        assertThat(container.getPrev(p2,3)).isNull();
        assertThat(container.getPrev(p2,4)).isNull();
        container.add(Collections.singletonList(p2));

        P2t p3 = new P2t(1, 1, 3,3);
        assertThat(container.getPrev(p3)).isEqualTo(p2);
        assertThat(container.getPrev(p3,1)).isEqualTo(p2);
        assertThat(container.getPrev(p3,2)).isEqualTo(p1);
        assertThat(container.getPrev(p3,3)).isEqualTo(p0);
        assertThat(container.getPrev(p3,4)).isNull();

        container.add(Collections.singletonList(p3));
        P2t p4 = new P2t(1, 1, 4,4);
        assertThat(container.getPrev(p4)).isEqualTo(p3);
        assertThat(container.getPrev(p4,1)).isEqualTo(p3);
        assertThat(container.getPrev(p4,2)).isEqualTo(p2);
        assertThat(container.getPrev(p4,3)).isEqualTo(p1);
        assertThat(container.getPrev(p4,4)).isEqualTo(p0);
        assertThat(container.getPrev(p4,5)).isNull();
    }


    @Test
    public void testPrevs() {
        Random r = new Random();
        DepthContainer container = new DepthContainer(4);
        for (int i = 0; i < 100; i++) {


            P2t p3 = new P2t(1, 1, r.nextInt(), r.nextInt());

            container.add(Collections.singletonList(p3));
            P2t p4 = new P2t(1, 1, r.nextInt(), r.nextInt());
            assertThat(container.getPrev(p4)).isEqualTo(p3);
        }

    }

}