package de.ludetis.android.signalbox.model;

/**
 * Created by uwe on 11.06.15.
 */
public class ServiceModeMessage {

    private int address, cv, value;

    public ServiceModeMessage(int address, int cv, int value) {
        this.address = address;
        this.cv = cv;
        this.value = value;
    }

    public int getAddress() {
        return address;
    }

    public int getCv() {
        return cv;
    }

    public int getValue() {
        return value;
    }
}
