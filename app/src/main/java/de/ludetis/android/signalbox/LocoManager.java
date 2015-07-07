package de.ludetis.android.signalbox;

import java.util.ArrayList;
import java.util.List;

import de.ludetis.android.signalbox.model.Loco;
import de.ludetis.android.storage.MapStorage;

/**
 * Created by uwe on 11.06.15.
 */
public class LocoManager {

    private static final String LOCOLIST = "locos2";
    private final List<Loco> locoList = new ArrayList<Loco>();
    private final MapStorage storage;


    public LocoManager(MapStorage storage) {

        this.storage = storage;
        if(storage.get(LOCOLIST)!=null) {
            locoList.addAll((List<Loco>) storage.get(LOCOLIST));
        }
    }

    public void saveAll() {
        storage.put(LOCOLIST, locoList);
        storage.flush();
    }

    public List<Loco> getLocoList() {
        return locoList;
    }

    public void addLoco(Loco loco) {
        locoList.add(loco);
    }

    public void updateLoco(Loco l1) {
        for(Loco l : locoList) {
            if(l1.equals(l)) {
                l.address = l1.address;
                l.initSent = l1.initSent;
                l.image = l1.image;
            }
        }
        saveAll();
    }
}
