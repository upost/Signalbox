package de.ludetis.android.signalbox;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;

import de.ludetis.android.signalbox.model.Segment;

/**
 * Created by uwe on 02.01.18.
 */


public class RouteFinderTest {

    private Collection<Segment> layout = new HashSet<>();

    @Before
    public void setup() {

    }

    @Test
    public void testRouteFinding() {
        layout.addAll(Arrays.asList(new Segment(Segment.Type.BUMPER_LEFT,"from",1,1),
                new Segment(Segment.Type.STRAIGHT_MARKER,"",2,1),
                new Segment(Segment.Type.STRAIGHT,"",3,1),
                new Segment(Segment.Type.SWITCH_DOWN,"",4,1),
                new Segment(Segment.Type.BUMPER_RIGHT,"to",5,1),
                new Segment(Segment.Type.DOWN_CURVE,"",5,2),
                new Segment(Segment.Type.BUMPER_RIGHT,"to2",6,2)
        ));
        Segment from = layout.stream().filter(s->s.getId().equals("from")).findAny().get();
        Segment to = layout.stream().filter(s->s.getId().equals("to")).findAny().get();
        Segment to2 = layout.stream().filter(s->s.getId().equals("to2")).findAny().get();

        {
            RouteFinder rf = new RouteFinder(layout, from, to);
            Map<Segment, Integer> route = rf.findRoute();
            Assert.assertNotNull(route);
        }
        {
            RouteFinder rf = new RouteFinder(layout, from, to2);
            Map<Segment, Integer> route = rf.findRoute();
            Assert.assertNotNull(route);

        }

    }



}
