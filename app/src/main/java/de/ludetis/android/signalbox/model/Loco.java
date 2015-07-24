package de.ludetis.android.signalbox.model;

import java.io.Serializable;
import java.util.UUID;

/**
 * Created by uwe on 08.06.15.
 */
public class Loco implements Serializable {

    public Loco(int bus, int address, int direction, int[] function, String image) {
        this.bus = bus;
        this.address = address;
        this.direction = direction;
        this.function = function;
        this.image = image;
        uuid = UUID.randomUUID();
    }

    public UUID uuid;
    private int bus;
    public int address;
    public int direction;
    public int[] function;
    public String image;
    public boolean initSent;
    public int speed;

    public void reset() {
        initSent=false;
        speed=0;
        function = new int[] {0,0,0,0,0};
    }

    public int activateFunction(int i) {
        function[i] = 1-function[i];
        return function[i];
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Loco loco = (Loco) o;

        return uuid.equals(loco.uuid);

    }

    public int getBus() {
        return bus==0?1:bus;
    }

    @Override
    public int hashCode() {
        return uuid.hashCode();
    }
}
