package de.ludetis.android.signalbox;

import java.util.Collection;
import java.util.HashSet;

import de.ludetis.android.signalbox.model.Segment;

/**
 * Created by uwe on 08.09.14.
 */
public class LayoutFactory {
    public static Collection<Segment> createDemo() {

        Collection<Segment> res = new HashSet<Segment>();

        res.add(new Segment(Segment.Type.STRAIGHT,"",0,0, 0,1));
        res.add(new Segment(Segment.Type.STRAIGHT,"",0,1, 0,1));
        res.add(new Segment(Segment.Type.CURVE_DOWN,"",1,0, 0,1));
        res.add(new Segment(Segment.Type.STRAIGHT,"",1,1, 0,1));
        res.add(new Segment(Segment.Type.EMPTY,"",2,0, 0,1));
        res.add(new Segment(Segment.Type.DOWN_SWITCH,"1",2,1, 1,1));

        res.add(new Segment(Segment.Type.STRAIGHT,"",0,2, 0,1));
        res.add(new Segment(Segment.Type.STRAIGHT,"",0,3, 0,1));
        res.add(new Segment(Segment.Type.CURVE_DOWN,"",1,2, 0,1));
        res.add(new Segment(Segment.Type.STRAIGHT,"",1,3, 0,1));
        res.add(new Segment(Segment.Type.EMPTY,"",2,2, 0,1));
        res.add(new Segment(Segment.Type.DOWN_SWITCH,"2",2,3, 2,1));

        return res;
    }

    public static Collection<Segment> create(int w, int h) {
        Collection<Segment> res = new HashSet<Segment>();
        for(int x=0; x<w; x++) {
            for(int y=0; y<h; y++) {
                res.add(new Segment(Segment.Type.EMPTY, "", x,y,0,1));
            }
        }
        return res;
    }
}
