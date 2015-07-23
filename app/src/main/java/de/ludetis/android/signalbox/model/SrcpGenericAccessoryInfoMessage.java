package de.ludetis.android.signalbox.model;

/**
 * Created by uwe on 22.07.15.
 */
public class SrcpGenericAccessoryInfoMessage {

    private int value;
    private boolean available;
    private  int bus,address,port;

    public SrcpGenericAccessoryInfoMessage(int bus, int address, int port, boolean available) {
        this.available = available;
        this.bus = bus;
        this.address = address;
        this.port = port;
    }

    public SrcpGenericAccessoryInfoMessage(int bus, int address, int port, int value) {
        this.value = value;
        this.available = true;
        this.bus = bus;
        this.address = address;
        this.port = port;

    }

    public boolean isAvailable() {
        return available;
    }

    public int getBus() {
        return bus;
    }

    public int getAddress() {
        return address;
    }

    public int getPort() {
        return port;
    }

    public int getValue() {
        return value;
    }
}
