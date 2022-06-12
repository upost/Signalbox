package de.ludetis.android.signalbox.model;

import java.io.Serializable;
import java.util.Arrays;

/**
 * Created by uwe on 08.09.14.
 */
public class Segment implements Serializable {

    static final long serialVersionUID =-3402530285538005618L;
    public static final int FUNCTION_DECODER_FUNCTIONS = 8;



    public enum Type {EMPTY, STRAIGHT, CURVE_UP, CURVE_DOWN, UP_CURVE, DOWN_CURVE, ACROSS_UP, ACROSS_DOWN,
        SWITCH_UP, SWITCH_DOWN, UP_SWITCH, DOWN_SWITCH, MARKER, STRAIGHT_MARKER, SWITCHS_DOWN, DOWN_SWITCHS, SWITCHS_UP, UP_SWITCHS,
    STRAIGHT_PLATFORM_BELOW, STRAIGHT_PLATFORM_ABOVE, BUMPER_LEFT, BUMPER_RIGHT, SEMAPHORE_TOP, SEMAPHORE_BOTTOM,
        GENERIC_ON_OFF_SWITCH, GENERIC_FUNCTION, STRAIGHT_ROUTE_MARKER, UP_ROUTE_MARKER, SEMAPHORE3_BOTTOM, SEMAPHORE3_TOP, SEMAPHORE4_BOTTOM, SEMAPHORE4_TOP, DOWN_ROUTE_MARKER,
        V_UP_RIGHT, V_UP_LEFT, V_DOWN_RIGHT, V_DOWN_LEFT, V_UP, V_UP_MARKER,
    };

    public static final Type[] SWITCH_TYPES = {Type.SWITCH_DOWN, Type.SWITCH_UP, Type.UP_SWITCH, Type.DOWN_SWITCH,
    Type.SWITCHS_DOWN, Type.SWITCHS_UP, Type.UP_SWITCHS, Type.DOWN_SWITCHS};
    public static final Type[] EDIT_TYPES = { Type.MARKER, Type.STRAIGHT_MARKER };
    public static final Type[] SEMAPHORE_TYPES = { Type.SEMAPHORE_BOTTOM, Type.SEMAPHORE_TOP,Type.SEMAPHORE3_BOTTOM, Type.SEMAPHORE3_TOP,Type.SEMAPHORE4_BOTTOM, Type.SEMAPHORE4_TOP};
    public static final Type[] GENERIC_ACCESSORY_TYPES = {Type.GENERIC_ON_OFF_SWITCH };
    public static final Type[] GENERIC_FUNCTION_TYPES = { Type.GENERIC_FUNCTION };
    public static final Type[] ROUTE_TYPES = { Type.STRAIGHT_ROUTE_MARKER, Type.UP_ROUTE_MARKER, Type.DOWN_ROUTE_MARKER, Type.V_UP_MARKER,
            Type.SEMAPHORE_BOTTOM, Type.SEMAPHORE_TOP,Type.SEMAPHORE3_BOTTOM, Type.SEMAPHORE3_TOP,Type.SEMAPHORE4_BOTTOM, Type.SEMAPHORE4_TOP, Type.BUMPER_LEFT, Type.BUMPER_RIGHT };

    private Type type = Type.EMPTY;
    private String id;
    private int x,y;
    private int address;
    private int bus=1;
    private int state=0;
    public int[] function;

    public Segment() {
        function = new int[FUNCTION_DECODER_FUNCTIONS];
    }

    public Segment(Type type,String id, int x, int y) {
        this.type = type;
        this.id = id;
        this.x = x;
        this.y = y;
    }

    public Segment(Type type, String id, int x, int y, int address, int bus) {
        this.type = type;
        this.id = id;
        this.x = x;
        this.y = y;
        this.address = address;
        this.bus = bus;
        function = new int[FUNCTION_DECODER_FUNCTIONS];
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

    public boolean isGenericAccessory() { return Arrays.asList(GENERIC_ACCESSORY_TYPES).contains(type); }
    public boolean isGenericFunction() { return Arrays.asList(GENERIC_FUNCTION_TYPES).contains(type); }
    public boolean isRoutePoint() { return Arrays.asList(ROUTE_TYPES).contains(type); }

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

    public boolean getFunctionValue(int i) {
        if(function==null)  function = new int[FUNCTION_DECODER_FUNCTIONS];
        return function[i]!=0;
    }

    public void setFunction(int i, boolean v) {
        if(function==null)  function = new int[FUNCTION_DECODER_FUNCTIONS];
        function[i] = v?1:0;
    }

    public int activateFunction(int i) {
        function[i] = 1-function[i];
        return function[i];
    }

    @Override
    public String toString() {
        return "["+x+";"+y+"]:"+type;
    }
}
