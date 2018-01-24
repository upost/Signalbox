package de.ludetis.android.storage;

import android.content.Context;

import org.mapdb.DB;
import org.mapdb.DBMaker;

import java.io.File;
import java.util.Collection;
import java.util.concurrent.ConcurrentNavigableMap;

/**
 * Created by uwe on 02.07.15.
 */
public class MapDbStorage implements MapStorage {

    private DB db;
    private ConcurrentNavigableMap<String,Object> map;


    public MapDbStorage(Context context, String name) {
        File fileDir = context.getFilesDir();
        File dbDir = new File(fileDir, "signalbox");
        dbDir.mkdir();
        File dbFile = new File(/*Environment.getExternalStorageDirectory()*/ dbDir, name+".db");
        db = DBMaker.newFileDB(dbFile).closeOnJvmShutdown().make();
        map = db.getTreeMap(name);
    }

    @Override
    public void put(String key, Object value) {
        map.put(key, value);
    }

    @Override
    public Object get(String key) {
        Object res = map.get(key);
        return res;
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
