package de.ludetis.android.signalbox;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import de.ludetis.android.signalbox.model.Segment;

/**
 * Created by uwe on 02.01.18.
 */

public class SegmentConnector {


    private final Segment.Type segmentType;
    private boolean[] left = {false,false,false};
    private boolean[] right = {false,false,false};
    public  enum ConnectAt {
        NW(-1,-1),N(0,-1), NE(1,-1),
        W(-1,0),SW(-1,1),S(0,1), SE(1,1), E(1,0);
        private ConnectAt(final int _dx,final int _dy) { dx=_dx; dy=_dy;}
        private int dx,dy;
        public int getDx() {
            return dx;
        }

        public int getDy() {
            return dy;
        }
        public ConnectAt getOpposite() {
            for(ConnectAt ca : ConnectAt.values()) {
                if(dx==-ca.dx && dy==-ca.dy) return ca;
            }
            return null;
        }
    };
    private Set<ConnectAt> connectAt= new HashSet<>();

    public SegmentConnector(Segment.Type st) {
        segmentType = st;
        switch(st) {
            case STRAIGHT_PLATFORM_ABOVE:
            case STRAIGHT_PLATFORM_BELOW:
            case STRAIGHT_MARKER:
            case SEMAPHORE_BOTTOM:
            case SEMAPHORE_TOP:
            case STRAIGHT:
            case STRAIGHT_ROUTE_MARKER:
                connectAt.addAll(Arrays.asList(ConnectAt.W,ConnectAt.E));
                break;
            case CURVE_UP:
                connectAt.addAll(Arrays.asList(ConnectAt.W,ConnectAt.NE));
                break;
            case CURVE_DOWN:
                connectAt.addAll(Arrays.asList(ConnectAt.W,ConnectAt.SE));
                break;
            case UP_CURVE:
                connectAt.addAll(Arrays.asList(ConnectAt.SW,ConnectAt.E));
                break;
            case DOWN_CURVE:
                connectAt.addAll(Arrays.asList(ConnectAt.NW,ConnectAt.E));
                break;
            case SWITCH_UP:
                connectAt.addAll(Arrays.asList(ConnectAt.W,ConnectAt.NE,ConnectAt.E));
                break;
            case SWITCH_DOWN:
                connectAt.addAll(Arrays.asList(ConnectAt.W,ConnectAt.SE,ConnectAt.E));
                break;
            case UP_SWITCH:
                connectAt.addAll(Arrays.asList(ConnectAt.SW,ConnectAt.W,ConnectAt.E));
                break;
            case DOWN_SWITCH:
                connectAt.addAll(Arrays.asList(ConnectAt.NW,ConnectAt.W,ConnectAt.E));
                break;
            case SWITCHS_DOWN:
                connectAt.addAll(Arrays.asList(ConnectAt.NW,ConnectAt.W,ConnectAt.SE));
                break;
            case SWITCHS_UP:
                connectAt.addAll(Arrays.asList(ConnectAt.SE,ConnectAt.NW,ConnectAt.E));
                break;
            case DOWN_SWITCHS:
                connectAt.addAll(Arrays.asList(ConnectAt.NE,ConnectAt.W,ConnectAt.SW));
                break;
            case UP_SWITCHS:
                connectAt.addAll(Arrays.asList(ConnectAt.SW,ConnectAt.W,ConnectAt.NE));
                break;
            case ACROSS_UP:
            case UP_ROUTE_MARKER:
                connectAt.addAll(Arrays.asList(ConnectAt.SW,ConnectAt.NE));
                break;
            case ACROSS_DOWN:
            case DOWN_ROUTE_MARKER:
                connectAt.addAll(Arrays.asList(ConnectAt.NW,ConnectAt.SE));
                break;
            case BUMPER_LEFT:
                connectAt.addAll(Arrays.asList(ConnectAt.E));
                break;
            case BUMPER_RIGHT:
                connectAt.addAll(Arrays.asList(ConnectAt.W));
                break;
            case V_UP:
            case V_UP_MARKER:
                connectAt.addAll(Arrays.asList(ConnectAt.N,ConnectAt.S));
                break;
            case V_UP_LEFT:
                connectAt.addAll(Arrays.asList(ConnectAt.S,ConnectAt.NW));
                break;
            case V_UP_RIGHT:
                connectAt.addAll(Arrays.asList(ConnectAt.S,ConnectAt.NE));
                break;
            case V_DOWN_LEFT:
                connectAt.addAll(Arrays.asList(ConnectAt.SW,ConnectAt.N));
                break;
            case V_DOWN_RIGHT:
                connectAt.addAll(Arrays.asList(ConnectAt.SE,ConnectAt.N));
                break;

        }
    }

    public Set<ConnectAt> getConnectAt() {
        return connectAt;
    }

    public int calcSwitchSetting(int dyLeft, int dyRight) {
        switch(segmentType) {
            case SWITCH_UP:
                return dyRight<0 ? 1 : 0 ;

            case SWITCH_DOWN:
                return dyRight>0 ? 1 : 0 ;

            case UP_SWITCH:
                return dyLeft>0 ? 1 : 0 ;

            case DOWN_SWITCH:
                return dyLeft<0 ? 1 : 0 ;

            case SWITCHS_DOWN:
                return dyLeft<0 ? 0 : 1 ;

            case SWITCHS_UP:
                return dyLeft>0 ? 0 : 1 ;

            case DOWN_SWITCHS:
                return dyRight<0 ? 0 : 1 ;

            case UP_SWITCHS:
                return dyRight>0 ? 0 : 1 ;
        }
        return  0;
    }
}
