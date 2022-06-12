package de.ludetis.android.signalbox;

import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.ludetis.android.signalbox.model.Segment;

/**
 * Created by uwe on 02.01.18.
 */

public class RouteFinder {
    private static final String LOG_TAG = "RouteFinder";
    private final Collection<Segment> layout;
    private final Segment from;
    private final Segment to;

    public RouteFinder(Collection<Segment> layout, Segment from, Segment to) {
        this.layout = layout;
        this.from = from;
        this.to = to;
    }

    public Map<Segment,Integer> findRoute() {
        int x= from.getX();
        int y= from.getY();
        int dir = (int)Math.signum(to.getX()-x);



        List<Segment> route = new ArrayList<>();
        route.add(from);
        route = findRoute(route,dir);
        // any route found?
        if(route.isEmpty()) return null;

        // find semaphores and switches
        Map<Segment,Integer> routeAndSettings = new HashMap<>();
        for(Segment s : route) {
            Integer v = calcSettingForSegment(route, s);
            routeAndSettings.put(s, v);
        }
        return routeAndSettings;
    }

    private Integer calcSettingForSegment(List<Segment> route, Segment segment) {
        // semaphore?
        if(segment.isSemaphore()) {
            // last semaphore RED
            if(segment==to) {
                return 0;
            } else {
                // other semaphores dep. on direction
                int i = route.indexOf(segment);
                if(i+1<route.size()) {
                    int xdiff = route.get(i+1).getX() - segment.getX();
                    if(xdiff<0 && segment.getType().equals(Segment.Type.SEMAPHORE_TOP))
                        return 1;
                    if(xdiff<0 && segment.getType().equals(Segment.Type.SEMAPHORE_BOTTOM))
                        return 0;
                    if(xdiff>0 && segment.getType().equals(Segment.Type.SEMAPHORE_TOP))
                        return 0;
                    if(xdiff>0 && segment.getType().equals(Segment.Type.SEMAPHORE_BOTTOM))
                        return 1;
                }
                return 0;
            }
        } else if(segment.isSwitch()) {
            // get next segment
            int i = route.indexOf(segment);
            if(i+1<route.size() && i>0) {
                SegmentConnector sc = new SegmentConnector(segment.getType());
                // distance between switch and next segment
                // dir right?
                if(route.get(i+1).getX() > segment.getX()) {
                    int ydiffLeft = route.get(i - 1).getY() - segment.getY();
                    int ydiffRight = route.get(i + 1).getY() - segment.getY();
                    return sc.calcSwitchSetting(ydiffLeft, ydiffRight);
                } else {
                    // left
                    int ydiffRight = route.get(i - 1).getY() - segment.getY();
                    int ydiffLeft = route.get(i + 1).getY() - segment.getY();
                    return sc.calcSwitchSetting(ydiffLeft, ydiffRight);
                }
            }
        }
        return null;
    }


    private List<Segment> findRoute(List<Segment> route, int dir) {
        Segment segment = route.get(route.size() - 1);
        Log.d(LOG_TAG, "finding route from: "+segment);
        Set<Segment> nextSegments = findNextSegmentsFrom(segment,dir);
        if(nextSegments.isEmpty()) {
            Log.d(LOG_TAG, "no more segments");
            return Collections.emptyList();
        }
        for(Segment s : nextSegments) {
            Log.d(LOG_TAG,"nextSegment:"+s);
            List<Segment> newRoute = new ArrayList<>();
            newRoute.addAll(route);
            newRoute.add(s);
            if(s.getX()==to.getX() && s.getY()==to.getY()) {
                Log.d(LOG_TAG, "reached destination");
                return  newRoute;
            }
            List<Segment> nextRoute = findRoute(newRoute,dir);
            if(!nextRoute.isEmpty()) {
                Log.d(LOG_TAG, "found route, returning it...");
                return nextRoute;
            }
        }
        Log.d(LOG_TAG, "returning empty list");
        return Collections.emptyList();
    }

    private Set<Segment> findNextSegmentsFrom(Segment s, int dir) {
        Set<Segment> res = new HashSet<>();
        SegmentConnector sc = new SegmentConnector(s.getType());
        Set<SegmentConnector.ConnectAt> dirs = new HashSet<>();
        if(dir>0) {
            // use right or up
            dirs.addAll(Arrays.asList(SegmentConnector.ConnectAt.N, SegmentConnector.ConnectAt.NE, SegmentConnector.ConnectAt.E, SegmentConnector.ConnectAt.SE));
        } else {
            dirs.addAll(Arrays.asList(SegmentConnector.ConnectAt.S, SegmentConnector.ConnectAt.SW, SegmentConnector.ConnectAt.W, SegmentConnector.ConnectAt.NW));
        }
        for(SegmentConnector.ConnectAt d : dirs) {
            Segment next = findSegmentAt(s.getX()+d.getDx(),s.getY()+d.getDy());
            if(next!=null) {
                // check if there is a fitting connector
                SegmentConnector other = new SegmentConnector(next.getType());
                for(SegmentConnector.ConnectAt ca : other.getConnectAt()) {
                    if(d==ca.getOpposite()) {
                        res.add(next);
                    }
                }
            }
        }
        return res;
    }

    private Segment findSegmentAt(int x, int y) {
        for(Segment s : layout) {
            if(s.getY()==y && s.getX()==x) return s;
        }
        return null;
    }

}
