package de.ludetis.android.signalbox.model;

import java.io.Serializable;
import java.util.Arrays;

/**
 * Created by uwe on 08.09.14.
 */
public class Segment implements Serializable {

    static final long serialVersionUID =-3402530285538005618L;

    public enum Type {EMPTY, STRAIGHT, CURVE_UP, CURVE_DOWN, UP_CURVE, DOWN_CURVE, ACROSS_UP, ACROSS_DOWN,
        SWITCH_UP, SWITCH_DOWN, UP_SWITCH, DOWN_SWITCH, MARKER, STRAIGHT_MARKER, SWITCHS_DOWN, DOWN_SWITCHS, SWITCHS_UP, UP_SWITCHS,
    STRAIGHT_PLATFORM_BELOW, STRAIGHT_PLATFORM_ABOVE, BUMPER_LEFT, BUMPER_RIGHT, SEMAPHORE_TOP, SEMAPHORE_BOTTOM
    };

    public static final Type[] SWITCH_TYPES = {Type.SWITCH_DOWN, Type.SWITCH_UP, Type.UP_SWITCH, Type.DOWN_SWITCH,
    Type.SWITCHS_DOWN, Type.SWITCHS_UP, Type.UP_SWITCHS, Type.DOWN_SWITCHS};
    public static final Type[] EDIT_TYPES = { Type.MARKER, Type.STRAIGHT_MARKER };
    public static final Type[] SEMAPHORE_TYPES = { Type.SEMAPHORE_BOTTOM, Type.SEMAPHORE_TOP};

    private Type type = Type.EMPTY;
    private String id;
    private int x,y;
    private int address;
    private int bus=1;
    private int state=0;

    public Segment(Type type, String id, int x, int y, int address, int bus) {
        this.type = type;
        this.id = id;
        this.x = x;
        this.y = y;
        this.address = address;
        this.bus = bus;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public int getAddress() {
        return address;
    }

    public void setAddress(int address) {
        this.address = address;
    }

    public int getBus() {
        return bus;
    }

    public void setBus(int bus) {
        this.bus = bus;
    }

    public boolean isSwitch() {
        return Arrays.asList(SWITCH_TYPES).contains(type);
    }

    public boolean isSemaphore() {
        return Arrays.asList(SEMAPHORE_TYPES).contains(type);
    }

    public boolean isEditable() {
        return Arrays.asList(EDIT_TYPES).contains(type);
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }


}
