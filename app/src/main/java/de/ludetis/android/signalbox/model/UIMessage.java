package de.ludetis.android.signalbox.model;

/**
 * Created by uwe on 02.07.15.
 */
public class UIMessage {

    public enum Type { IMAGE_CHOSEN };
    private final Type type;
    private final String value;

    public UIMessage(Type type, String value) {
        this.type = type;
        this.value = value;
    }

    public Type getType() {
        return type;
    }

    public String getValue() {
        return value;
    }
}
