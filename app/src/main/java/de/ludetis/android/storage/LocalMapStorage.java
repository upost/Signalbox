package de.ludetis.android.storage;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;
import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;


/**
 * Implements a basic key-value store based on Android's SharedPreferences.
 * Values are stores as base64 encoded Strings internally
 * Values of keys are generally Objects but they MUST implement Serializable, otherwise an Exception is thrown.
 * Notice: You MUST call flush() after you are done with calls to put().
 * Created by uwe on 06.03.14.
 */
public class LocalMapStorage implements de.ludetis.android.storage.MapStorage {

    private final SharedPreferences prefs;
    private SharedPreferences.Editor editor;
    private final static String LOG_TAG="LocalMapStorage";

    public LocalMapStorage(Context context) {
        prefs = context.getSharedPreferences("LSTOR",0);
    }

    @Override
    public void put(String key, Object value) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            ObjectOutputStream os = new ObjectOutputStream(bos);
            os.writeObject(value); // will throw if object is not serializable
            os.close();
            if(editor==null) {
                editor = prefs.edit();
            }
            editor.putString(key, Base64.encodeToString(bos.toByteArray(), Base64.DEFAULT) );
            //Log.d(LOG_TAG, "wrote key: "+key);
        } catch (IOException e) {
            Log.e(LOG_TAG, "ioexception: " + e);
        }
    }

    @Override
    public Object get(String key) {
        String res = prefs.getString(key,null);
        if(res==null) {
            Log.d(LOG_TAG, "key not found: "+key);
            return null;
        }
        //Log.d(LOG_TAG, "key found: "+key + "=" + res);
        ByteArrayInputStream bis = new ByteArrayInputStream(Base64.decode(res, Base64.DEFAULT));
        ObjectInputStream oInputStream = null;
        try {
            oInputStream = new ObjectInputStream(bis);
            //Log.d(LOG_TAG, "reading key: "+key);
            return oInputStream.readObject();
        } catch (IOException e) {
            Log.e(LOG_TAG,"ioexception: " + e);
        } catch (ClassNotFoundException e) {
            Log.e(LOG_TAG,"class not found: " + e);
        }
        return null;
    }

    @Override
    public Collection<String> getAllKeys() {
        throw new IllegalArgumentException("not implemented");
    }

    @Override
    public void flush() {
        if(editor!=null) {
            editor.commit();
            Log.d(LOG_TAG, "flushing");
            editor=null;
        }
    }


}
