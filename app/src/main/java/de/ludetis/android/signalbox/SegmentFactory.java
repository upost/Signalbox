package de.ludetis.android.signalbox;

import de.ludetis.android.signalbox.model.Segment;

/**
 * Created by uwe on 09.09.14.
 */
public class SegmentFactory {

    public static Segment newSegment(Segment.Type type) {
        Segment s = new Segment(type,"",0,0,0,1);
        if(s.isEditable()) s.setId("Marker");
        return  s;
    }
}
