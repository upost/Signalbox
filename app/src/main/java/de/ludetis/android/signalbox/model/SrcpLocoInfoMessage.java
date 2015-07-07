package de.ludetis.android.signalbox.model;

/**
 * Created by uwe on 11.06.15.
 */
public class SrcpLocoInfoMessage {

    private int bus;
    private int address, direction,speed,steps;
    private int[] functions;

    public SrcpLocoInfoMessage(int bus, int address, int direction, int speed, int steps, int[] functions) {
        this.bus = bus;
        this.address = address;
        this.direction = direction;
        this.speed = speed;
        this.steps = steps;
        this.functions = functions;
    }

    public int getBus() {
        return bus;
    }

    public int getAddress() {
        return address;
    }

    public int getDirection() {
        return direction;
    }

    public int getSpeed() {
        return speed;
    }

    public int getSteps() {
        return steps;
    }

    public int[] getFunctions() {
        return functions;
    }
}
