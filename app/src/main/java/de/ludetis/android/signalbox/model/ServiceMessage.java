package de.ludetis.android.signalbox.model;

/**
 * Created by uwe on 08.06.15.
 */
public class ServiceMessage {

    public ServiceMessage(Command command) {
        this.command = command;
    }

    public enum Command { DO_CONNECT, DO_DISCONNECT};

    public final Command command;

}
