package de.ludetis.android.signalbox.model;

/**
 * Created by uwe on 08.06.15.
 */
public class StatusMessage {

    public StatusMessage(Status status) {
        this.status = status;
    }

    public enum Status {CONNECTED,DISCONNECTED,POWER_ON,POWER_OFF, CURRENT_LOCO_UNKNOWN, ERROR, CONNECTIVITY_LOST, CONNECTIVITY_AVAIL};

    public final Status status;

    @Override
    public String toString() {
        return "StatusMessage{" +
                "status=" + status +
                '}';
    }
}
