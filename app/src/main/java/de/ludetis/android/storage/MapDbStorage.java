package de.ludetis.android.storage;

import android.os.Environment;

import com.google.gson.Gson;

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
    private Gson gson = new Gson();

    public MapDbStorage(String name) {
        File dbFile = new File(Environment.getExternalStorageDirectory(), name+".db");
        db = DBMaker.newFileDB(dbFile).closeOnJvmShutdown().make();
        map = db.getTreeMap(name);
    }

    @Override
    public void put(String key, Object value) {
        map.put(key, value);
        //Log.d("MapDbStorage", "gson: " + key + " = " + gson.toJson(value));
        //map.put(key+"_json", gson.toJson(value) );
    }

    @Override
    public Object get(String key) {
        Object res = map.get(key);
//        Log.d("MapDbStorage","gson copy: " + key+" = " + gson.toJson(res));
//        map.put(key+"_json", gson.toJson(res) );
//        db.commit();
        return res;
//        String o = (String)map.get(key + "_json");
//        if("port".equals(key)) return Integer.valueOf(o);
//        if("locos2".equals(key)) {
//            Type t = new TypeToken<Collection<Loco>>(){}.getType();
//            return gson.fromJson(o,t);
//        }
//        if("layout".equals(key)) {
//            Type t = new TypeToken<Collection<Segment>>(){}.getType();
//            return gson.fromJson(o, t);
//        }
//        return o;
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
