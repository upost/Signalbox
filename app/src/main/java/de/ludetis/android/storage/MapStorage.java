package de.ludetis.android.storage;

import java.util.Collection;

/**
 * Created by uwe on 06.03.14.
 */
public interface MapStorage {

    void put(String key, Object value);
    Object get(String key);
    Collection<String> getAllKeys();
    void flush();

}
