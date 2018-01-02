package de.ludetis.android.signalbox;

import de.ludetis.android.signalbox.model.Segment;

/**
 * Created by uwe on 02.01.18.
 */

public class SegmentConnector {


    private final Segment.Type segmentType;
    private boolean[] left = {false,false,false};
    private boolean[] right = {false,false,false};

    public SegmentConnector(Segment.Type st) {
        segmentType = st;
        switch(st) {
            case STRAIGHT_PLATFORM_ABOVE:
            case STRAIGHT_PLATFORM_BELOW:
            case STRAIGHT_MARKER:
            case SEMAPHORE_BOTTOM:
            case SEMAPHORE_TOP:
            case STRAIGHT:
                left[1]=true;
                right[1]=true;
                break;
            case CURVE_UP:
                left[1]=true;
                right[0]=true;
                break;
            case CURVE_DOWN:
                left[1]=true;
                right[2]=true;
                break;
            case UP_CURVE:
                left[2]=true;
                right[1]=true;
                break;
            case DOWN_CURVE:
                left[0]=true;
                right[1]=true;
                break;
            case SWITCH_UP:
                left[1]=true;
                right[0]=true;
                right[1]=true;
                break;
            case SWITCH_DOWN:
                left[1]=true;
                right[2]=true;
                right[1]=true;
                break;
            case UP_SWITCH:
                left[1]=true;
                left[2]=true;
                right[1]=true;
                break;
            case DOWN_SWITCH:
                left[1]=true;
                left[0]=true;
                right[1]=true;
                break;
            case SWITCHS_DOWN:
                left[1]=true;
                left[0]=true;
                right[2]=true;
                break;
            case SWITCHS_UP:
                left[0]=true;
                right[2]=true;
                right[1]=true;
                break;
            case DOWN_SWITCHS:
                left[2]=true;
                right[0]=true;
                right[1]=true;
                break;
            case UP_SWITCHS:
                left[1]=true;
                left[2]=true;
                right[0]=true;
                break;
            case ACROSS_UP:
                left[2]=true;
                right[0]=true;
                break;
            case ACROSS_DOWN:
                left[0]=true;
                right[2]=true;
                break;
            case BUMPER_LEFT:
                right[1]=true;
                break;
            case BUMPER_RIGHT:
                left[1]=true;
                break;
        }
    }

    public boolean leftTop() {
        return left[0];
    }
    public boolean leftCenter() {
        return left[1];
    }
    public boolean leftBottom() {
        return left[2];
    }

    public boolean rightTop() {
        return right[0];
    }
    public boolean rightCenter() {
        return right[1];
    }
    public boolean rightBottom() {
        return right[2];
    }

    public boolean[] getLeft() {
        return left;
    }

    public boolean[] getRight() {
        return right;
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
