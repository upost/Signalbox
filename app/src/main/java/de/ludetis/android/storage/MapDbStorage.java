package de.ludetis.android.storage;

import android.os.Environment;

import org.mapdb.DB;
import org.mapdb.DBMaker;

import java.io.File;
import java.util.Collection;
import java.util.concurrent.ConcurrentNavigableMap;

/**
 * Created by uwe on 02.07.15.
 */
public class MapDbStorage implements  MapStorage {

    private DB db;
    private ConcurrentNavigableMap<String,Object> map;

    public MapDbStorage(String name) {
        File dbFile = new File(Environment.getExternalStorageDirectory(), name+".db");
        db = DBMaker.newFileDB(dbFile).closeOnJvmShutdown().make();
        map = db.getTreeMap(name);
    }

    @Override
    public void put(String key, Object value) {
        map.put(key,value);
    }

    @Override
    public Object get(String key) {
        return map.get(key);
    }

    @Override
    public Collection<String> getAllKeys() {
        return map.keySet();
    }

    @Override
    public void flush() {
        db.commit();
    }
}
