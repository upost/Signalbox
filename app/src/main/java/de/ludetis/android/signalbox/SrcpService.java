package de.ludetis.android.signalbox;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import de.dermoba.srcp.client.SRCPSession;
import de.dermoba.srcp.common.exception.SRCPException;
import de.greenrobot.event.EventBus;
import de.ludetis.android.signalbox.model.ServiceMessage;
import de.ludetis.android.signalbox.model.ServiceModeMessage;
import de.ludetis.android.signalbox.model.SrcpGenericAccessoryInfoMessage;
import de.ludetis.android.signalbox.model.SrcpLocoInfoMessage;
import de.ludetis.android.signalbox.model.SrcpMessage;
import de.ludetis.android.signalbox.model.StatusMessage;
import de.ludetis.android.storage.MapDbStorage;
import de.ludetis.android.storage.MapStorage;

/**
 * Created by uwe on 08.06.15.
 */
public class SrcpService extends Service {

    private static final String LOG_TAG = "SrcpService";
    private static final int RETRIES = 1;
    private static final long RETRY_DELAY_MS = 100;
    private SRCPSession session;
    private MapStorage storage;


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        storage = new MapDbStorage("signalbox");
        EventBus.getDefault().register(this);
    }

    @Override
    public void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    public void  onEventBackgroundThread(SrcpMessage msg) {
        String what = msg.cmd;
        if (session == null || session.getCommandChannel() == null) {
            EventBus.getDefault().post(new StatusMessage(StatusMessage.Status.ERROR));
            return;
        }
        sendSrcpMessage(what);

    }

    private boolean sendSrcpMessage(String what) {
        boolean result = true;
        try {
            Log.d(LOG_TAG, "sending to SRCP server: " + what);
            for (int i = 0; i < RETRIES; i++) {
                String res = session.getCommandChannel().send(what);
                //Log.d(LOG_TAG, "result: " + res);
                if(res.contains("100 INFO")) {
                    processInfoResult(res);
                } else
                if(res.contains("200 OK")) {
                    if(what.contains("POWER ON")) EventBus.getDefault().post(new StatusMessage(StatusMessage.Status.POWER_ON));
                    if(what.contains("POWER OFF")) EventBus.getDefault().post(new StatusMessage(StatusMessage.Status.POWER_OFF));
                } else
                if(!res.contains("200 OK")) {
                    Log.w(LOG_TAG, "error result: " + res);
                    result=false;
                }
                if(i!=RETRIES-1) Thread.sleep(RETRY_DELAY_MS);
            }

        } catch (SRCPException e) {
            EventBus.getDefault().post(new StatusMessage(StatusMessage.Status.ERROR));
            Log.e(LOG_TAG, "exception", e);
            result=false;
            if(what.startsWith("SET") && what.contains("GL")) {
                EventBus.getDefault().post(new StatusMessage(StatusMessage.Status.CURRENT_LOCO_UNKNOWN));
            } else if(what.startsWith("GET") && what.contains("GA")) {
                Log.d(LOG_TAG, "GET GA failed, need to init...");
                String[] s = TextUtils.split(what," ");
                EventBus.getDefault().post(new SrcpGenericAccessoryInfoMessage(Integer.parseInt(s[1]),Integer.parseInt(s[3]),Integer.parseInt(s[4]),false));
            } else if(what.startsWith("GET 1 POWER")){
                disconnect();
            }

        } catch (InterruptedException e) {
            EventBus.getDefault().post(new StatusMessage(StatusMessage.Status.ERROR));
            result=false;
        }
        return result;
    }

    private void processInfoResult(String res) {
        // 1434049117.039 100 INFO 1 GL 3 1 0 28 1 0 0 0 0
        String[] s = TextUtils.split(res," ");
        if("GL".equals(s[4])) {
            // GL info
            Log.d(LOG_TAG,"incoming info gl result: " + res);
            List<Integer> funcs = new ArrayList<Integer>();
            int[] fu = new int[s.length-9];
            for(int i=9; i<s.length; i++) {
                fu[i-9]= Integer.parseInt(s[i]);
            }
            EventBus.getDefault().post(new SrcpLocoInfoMessage(Integer.parseInt(s[3]),
                    Integer.parseInt(s[5]), Integer.parseInt(s[6]),
                    Integer.parseInt(s[7]), Integer.parseInt(s[8]),
                    fu));
        }
        if("GA".equals(s[4])) {
            // GA info
            Log.d(LOG_TAG,"incoming info ga result: " + res);
            EventBus.getDefault().post(new SrcpGenericAccessoryInfoMessage(Integer.parseInt(s[3]),
                    Integer.parseInt(s[5]),Integer.parseInt(s[6]), Integer.parseInt(s[7]) ));
                    //List<Integer> funcs = new ArrayList<Integer>();
        }
        if("POWER".equals(s[4])) {
            Log.d(LOG_TAG,"incoming get power result: " + res);
            if("ON".equals(s[5])) {
                EventBus.getDefault().post(new StatusMessage(StatusMessage.Status.POWER_ON));
            }
            if("OFF".equals(s[5])) {
                EventBus.getDefault().post(new StatusMessage(StatusMessage.Status.POWER_OFF));
            }
        }
    }

    public void onEventBackgroundThread(ServiceMessage msg) {
        switch(msg.command) {
            case DO_CONNECT:
                connect();
                break;
            case DO_DISCONNECT:
                disconnect();
                break;
        }
    }

    public void onEventBackgroundThread(ServiceModeMessage msg) {
        if (session == null || session.getCommandChannel() == null) {
            EventBus.getDefault().post(new StatusMessage(StatusMessage.Status.DISCONNECTED));
            return;
        }
        //Log.d(LOG_TAG, "service mode: " + msg);
        if(sendSrcpMessage("INIT 1 SM NMRA")) {
            sendSrcpMessage("SET 1 SM " + msg.getAddress() + " CV " + msg.getCv() + " " + msg.getValue());
            sendSrcpMessage("TERM 1 SM");
        }

    }

    private void connect() {
        Log.d(LOG_TAG, "connecting...");
        try {
            String server = (String) storage.get("server");
            int port = (Integer) storage.get("port");

            session = new SRCPSession(server, port);
            session.connect();
            //session.getCommandChannel().send("SET PROTOCOL SRCP 0.8.3");

            //session.getCommandChannel().send("SET 1 POWER ON");
            EventBus.getDefault().post(new StatusMessage(StatusMessage.Status.CONNECTED));
        } catch (SRCPException e) {
            EventBus.getDefault().post(new StatusMessage(StatusMessage.Status.DISCONNECTED));
            Log.e(LOG_TAG, "exception", e);
        }
    }

    private void disconnect() {
        Log.i(LOG_TAG, "disconnect...");

        try {
            if (session != null) {
//                session.getCommandChannel().send("SET 1 POWER OFF");
                session.disconnect();
                session=null;

            }
        } catch (SRCPException e) {
            Log.e(LOG_TAG, "exception", e);
        }
        EventBus.getDefault().post(new StatusMessage(StatusMessage.Status.DISCONNECTED));


    }
}
