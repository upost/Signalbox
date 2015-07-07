package de.ludetis.android.signalbox;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.Toast;

import java.util.Collection;
import java.util.HashSet;

import de.greenrobot.event.EventBus;
import de.ludetis.android.signalbox.model.Loco;
import de.ludetis.android.signalbox.model.Segment;
import de.ludetis.android.signalbox.model.ServiceMessage;
import de.ludetis.android.signalbox.model.SrcpLocoInfoMessage;
import de.ludetis.android.signalbox.model.SrcpMessage;
import de.ludetis.android.signalbox.model.StatusMessage;
import de.ludetis.android.signalbox.model.UIMessage;
import de.ludetis.android.storage.MapDbStorage;
import de.ludetis.android.storage.MapStorage;
import de.ludetis.android.view.VerticalSeekBar;


public class MainActivity extends Activity implements View.OnClickListener, SeekBar.OnSeekBarChangeListener, View.OnLongClickListener {

    private static final String LOG_TAG = "SRCPClient";
    private static final int SEGMENT_WIDTH = 200;
    private static final int SEGMENT_HEIGHT = 100;
    private static final int RETRIES = 1;
    private static final long RETRY_DELAY_MS = 300;
    private static final long MIN_PROGRESS_CHANGE_THRESHOLD_MS = 100;
    public static final String LAYOUT = "layout";
    private static final int COLOR_SET = Color.rgb(100,100,0);
    private static final int COLOR_NOTSET = Color.rgb(20,20,20);
    public static final int SPEED_STEPS = 28;
    private String server = "192.168.178.39"; // make configurable
    private int port = 4303;
    private Collection<Segment> layout = new HashSet<Segment>();
    private SwitchListener switchListener = new SwitchListener();
    private SimpleListener simpleListener = new SimpleListener();
    private EditSwitchListener editSwitchListener = new EditSwitchListener();
    private EditIdListener editIdListener = new EditIdListener();
    private boolean edit;
    private MapStorage storage;
    private long lastProgressChanged;
    private Loco currentloco;
    private LocoManager locoManager;
    private ProgressDialog progressDialog;
    private boolean connected;
    private boolean power;
    private ImageButton buttonPower,buttonConnection;
//    private Loco currentloco = new Loco(6,0,new int[5],"loco_260r");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        storage = new MapDbStorage("signalbox");

        locoManager = new LocoManager(storage);

        setContentView(R.layout.activity_main);

        buttonPower = (ImageButton)findViewById(R.id.power);
        buttonPower.setOnClickListener(this);
        buttonConnection = (ImageButton) findViewById(R.id.connection);
        buttonConnection.setOnClickListener(this);

        findViewById(R.id.configuration).setOnClickListener(this);
        findViewById(R.id.edit).setOnClickListener(this);
        findViewById(R.id.loco_image).setOnClickListener(this);
        findViewById(R.id.loco_image).setOnLongClickListener(this);

        findViewById(R.id.directionBack).setOnClickListener(this);
        findViewById(R.id.directionForward).setOnClickListener(this);
        findViewById(R.id.stop).setOnClickListener(this);
        findViewById(R.id.function0).setOnClickListener(this);
        findViewById(R.id.function1).setOnClickListener(this);
        findViewById(R.id.function2).setOnClickListener(this);
        findViewById(R.id.function3).setOnClickListener(this);
        findViewById(R.id.function4).setOnClickListener(this);
        ((VerticalSeekBar)findViewById(R.id.speedControl)).setOnSeekBarChangeListener(this);

        startService(new Intent(this, SrcpService.class));

        showLocoList();

        for(Loco l : locoManager.getLocoList()) {
            l.initSent=false;
        }

    }

    private void showLocoList() {
        findViewById(R.id.chooser).setVisibility(View.VISIBLE);
        findViewById(R.id.controller).setVisibility(View.GONE);
        ViewGroup container = (ViewGroup) findViewById(R.id.locolist);
        container.removeAllViews();
        for(Loco loco : locoManager.getLocoList()) {
            View lv = getLayoutInflater().inflate(R.layout.include_loco,null);
            lv.findViewById(R.id.image).setTag(R.id.TAGKEY_LOCO, loco);
            lv.findViewById(R.id.image).setOnClickListener(this);
            ((ImageView)lv.findViewById(R.id.image)).setImageURI(Uri.parse(loco.image));
            container.addView(lv);
        }
        View lv = getLayoutInflater().inflate(R.layout.include_addloco,null);
        lv.findViewById(R.id.add_loco).setOnClickListener(this);
        container.addView(lv);
    }

    private void showLocoController(Loco loco) {
        currentloco = loco;
        findViewById(R.id.chooser).setVisibility(View.GONE);
        findViewById(R.id.controller).setVisibility(View.VISIBLE);
        ((ImageView)findViewById(R.id.loco_image)).setImageURI(Uri.parse(loco.image));
        // get current values
        sendGetGenericLocoCommand(1, loco);
        updateController();
    }

    @Override
    protected void onResume() {
        super.onResume();

        loadSettings();

        if(server!=null) {
            loadLayout();
            fillContainer();
        }

        EventBus.getDefault().register(this);

        checkPower();


    }

    private void checkPower() {
        // check for connection: try to get the power state
        send("GET 1 POWER");
    }

    private void loadLayout() {
        // load
        layout = (Collection<Segment>) storage.get(LAYOUT);
        if(layout==null) {
            // create new layout
            NewLayoutDialog d = new NewLayoutDialog(this,20,10, new NewLayoutDialog.OnDataConfirmedListener() {
                @Override
                public void onDataConfirmed(int w, int h) {
                    layout = LayoutFactory.create(w, h);
                    storage.put("layout", layout);
                    storage.flush();
                    Log.d(LOG_TAG, "created layout");
                    fillContainer();
                }
            });
            d.show();
            layout = LayoutFactory.createDemo();
            storage.put("layout", layout);
            storage.flush();
            fillContainer();
        } else {
            Log.d(LOG_TAG, "loaded layout");
        }
    }

    /**
     * fill container with layout
     */
    private void fillContainer() {
        FrameLayout container = (FrameLayout) findViewById(R.id.container);
        container.removeAllViews();
        int w=0;
        for(Segment s : layout) {
            SegmentView segmentView = new SegmentView(this);
            segmentView.setSegment(s);
            FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(SEGMENT_WIDTH,SEGMENT_HEIGHT);
            lp.leftMargin = SEGMENT_WIDTH * s.getX();
            lp.topMargin = SEGMENT_HEIGHT * s.getY();
            if(s.getX()>w) w= s.getX();
            wireSegmentViewClickListener(segmentView);
            container.addView(segmentView, lp);
        }
        ViewGroup.LayoutParams layoutParams = container.getLayoutParams();
        layoutParams.width = SEGMENT_WIDTH*(w+1);
        container.setLayoutParams(layoutParams);
    }

    private void wireSegmentViewClickListener(SegmentView segmentView) {

        if(segmentView.getSegment().isSwitch()) {
            segmentView.setOnClickListener(switchListener);
            segmentView.setOnLongClickListener(editSwitchListener);
        } else {
            segmentView.setOnClickListener(simpleListener);
            if(segmentView.getSegment().isEditable()) {
                segmentView.setOnLongClickListener(editIdListener);
            }
        }
    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        //disconnect();
    }



    @Override
    public void onClick(View v) {
        if(v.getTag(R.id.TAGKEY_LOCO)!=null) {
            showLocoController((Loco) v.getTag(R.id.TAGKEY_LOCO));
            return;
        }
        switch (v.getId()) {
            case R.id.connection:
                if(connected) {
                    stopLoco();
                    EventBus.getDefault().post(new ServiceMessage(ServiceMessage.Command.DO_DISCONNECT));
                } else {
                    progressDialog = ProgressDialog.show(this,getString(R.string.app_name), getString(R.string.connecting), true);
                    EventBus.getDefault().post(new ServiceMessage(ServiceMessage.Command.DO_CONNECT));
                }

                break;
            case R.id.power:
                if(power) {
                    //if(currentloco!=null) currentloco.reset();
                    send("SET 1 POWER OFF");
                } else {
                    send("SET 1 POWER ON");
                }

                break;
            case R.id.configuration:
                configure(); break;
            case R.id.edit:
                toggleEdit();
                break;
            case R.id.function0:
                currentloco.activateFunction(0);
                updateController();
                sendLocoData(currentloco);
                break;
            case R.id.function1:
                currentloco.activateFunction(1);
                updateController();
                sendLocoData(currentloco);
                break;
            case R.id.function2:
                currentloco.activateFunction(2);
                updateController();
                sendLocoData(currentloco);
                break;
            case R.id.function3:
                currentloco.activateFunction(3);
                updateController();
                sendLocoData(currentloco);
                break;
            case R.id.function4:
                currentloco.activateFunction(4);
                updateController();
                sendLocoData(currentloco);
                break;
            case R.id.directionForward:
                currentloco.direction=0;
                updateController();
                stopLoco();
                sendLocoData(currentloco);
                break;
            case R.id.directionBack:
                currentloco.direction=1;
                updateController();
                stopLoco();
                sendLocoData(currentloco);
                break;
            case R.id.stop:
                emergencyStop();
                break;
            case R.id.add_loco:
                final Loco l = new Loco(0,0,new int[5],"");
                showLocoDialog(l, new LocoDialog.OnDataConfirmedListener() {
                    @Override
                    public void onDataConfirmed(Loco loco) {
                        locoManager.addLoco(loco);
                        locoManager.saveAll();
                        showLocoList();
                    }
                });
                break;
            case R.id.loco_image:
                showLocoList();
                break;
        }
    }

    private void updateController() {
        ((Button)findViewById(R.id.function0)).setBackgroundColor(currentloco.function[0]>0 ? COLOR_SET : COLOR_NOTSET);
        ((Button)findViewById(R.id.function1)).setBackgroundColor(currentloco.function[1] > 0 ? COLOR_SET : COLOR_NOTSET);
        ((Button)findViewById(R.id.function2)).setBackgroundColor(currentloco.function[2] > 0 ? COLOR_SET : COLOR_NOTSET);
        ((Button)findViewById(R.id.function3)).setBackgroundColor(currentloco.function[3] > 0 ? COLOR_SET : COLOR_NOTSET);
        ((Button)findViewById(R.id.function4)).setBackgroundColor(currentloco.function[4] > 0 ? COLOR_SET : COLOR_NOTSET);
        ((Button)findViewById(R.id.directionForward)).setBackgroundColor( currentloco.direction==0 ? COLOR_SET : COLOR_NOTSET );
        ((Button)findViewById(R.id.directionBack)).setBackgroundColor( currentloco.direction==1 ? COLOR_SET : COLOR_NOTSET );
        ((ProgressBar)findViewById(R.id.speedControl)).setProgress(currentloco.speed);
    }

    private void showLocoDialog(Loco l, LocoDialog.OnDataConfirmedListener listener) {
        Dialog dlg = new LocoDialog(this,l,listener);
        dlg.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==LocoDialog.PICK_IMAGE && resultCode==RESULT_OK && data!=null) {
            Uri imageUri = data.getData();
            Log.d(LOG_TAG, "chosen image Uri: " + imageUri.toString());
            EventBus.getDefault().post(new UIMessage(UIMessage.Type.IMAGE_CHOSEN, imageUri.toString()));
        }
    }

    private void stopLoco() {
        ((VerticalSeekBar)findViewById(R.id.speedControl)).setProgress(0);
    }

    private void configure() {
        Dialog dlg = new ConfigurationDialog(this, server, port, new ConfigurationDialog.OnConfigurationChangedListener() {
            @Override
            public void onConfigurationChanged(String serverAddress, int serverPort) {
                server = serverAddress;
                port = serverPort;
                saveSettings();
                loadLayout();
                fillContainer();
            }
        });
        dlg.show();
    }

    private void saveSettings() {
        storage.put("server", server);
        storage.put("port", port);
        storage.flush();
    }

    private void loadSettings() {
        server = (String) storage.get("server");
        if(server==null) {
            configure();
        } else {
            port = (Integer) storage.get("port");
        }
    }

    private void toggleEdit() {
        Button b = (Button) findViewById(R.id.edit);
        edit = !edit;
        b.setTextColor(edit ? getResources().getColor(android.R.color.holo_green_light) : getResources().getColor(android.R.color.white));
        if(edit) {
            b.setText(R.string.save);
        } else {
            b.setText(R.string.edit);
            Log.d(LOG_TAG, "saving layout");
            storage.put("layout", layout);
            storage.flush();
        }

    }


    private void send(final String what) {
        EventBus.getDefault().post(new SrcpMessage(what));
//        executor.execute(new Runnable() {
//            @Override
//            public void run() {
//                sendInBackground(what);
//            }
//        });
    }

//    private synchronized void sendInBackground(String what) {
//        if (session == null || session.getCommandChannel() == null) {
//            showConnectionError();
//            return;
//        }
//        try {
//            for (int i = 0; i < RETRIES; i++) {
//                String res = session.getCommandChannel().send(what);
//                Log.d(LOG_TAG, "result: " + res);
//                Thread.sleep(RETRY_DELAY_MS);
//            }
//
//        } catch (SRCPException e) {
//            Log.e(LOG_TAG, "exception", e);
//        } catch (InterruptedException e) {
//            ;
//        }
//    }

    private void showConnectionError() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.this, "not connected", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * activates an accessory for 100ms
     * @param bus
     * @param address
     * @param port i.e. 0 or 1 to set a switch
     */
    private void sendSetGenericAccessoryCommand(int bus, int address, int port) {
        send("INIT " + bus + " GA " + address + " N");
        send("SET " + bus + " GA " + address + " " + port + " 1 100"); // format: SET <bus> GA <addr> <port> <value> <delay>
    }

    /**
     *
     * @param bus
     * @param address
     * @param direction 0 or 1
     * @param speed      0..14
     *
     *                   SET 1 GL 6 0 37 100 0 0 0 0 0
     */
    private void sendSetGenericLocoCommand(int bus, int address, int direction, int speed, int maxSpeed, int[] functions) {
        if(!currentloco.initSent)
            sendInitGenericLocoCommand(bus,address, SPEED_STEPS,5);
        String fu="";
        for(int f:functions) {
            fu+=Integer.toString(f)+" ";
        }
        send("SET "+bus+" GL "+address+" "+direction + " "+speed + " " +maxSpeed+" " +fu);
    }

    private void sendGetGenericLocoCommand(int bus, Loco loco) {
        if(!loco.initSent)
            sendInitGenericLocoCommand(bus,loco.address, SPEED_STEPS,5);
        send("GET "+bus+" GL " + loco.address);
    }

    private void sendInitGenericLocoCommand(int bus, int address, int speedSteps, int functions) {
        send("INIT "+bus+" GL "+address+" N 1 "+speedSteps+" "+functions);
        currentloco.initSent=true;
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int speed, boolean fromUser) {
        if(currentloco!=null && currentloco.speed!=speed && speed>=0 && (lastProgressChanged+MIN_PROGRESS_CHANGE_THRESHOLD_MS < System.currentTimeMillis() || speed==0)) {
            currentloco.speed=speed;
            sendLocoData(currentloco);
            lastProgressChanged=System.currentTimeMillis();
        }
    }

    private void sendLocoData(Loco lo) {
        sendSetGenericLocoCommand(1, lo.address, lo.direction, lo.speed, 100, lo.function);

    }

    public void emergencyStop() {
        sendSetGenericLocoCommand(1, currentloco.address, 2, 0, 100, currentloco.function);
        stopLoco();
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public boolean onLongClick(View v) {
        if(v.getId()==R.id.loco_image) {
            showLocoDialog(currentloco, new LocoDialog.OnDataConfirmedListener() {
                @Override
                public void onDataConfirmed(Loco l) {
                    locoManager.updateLoco(l);
                }
            });
            return true;
        }
        return false;
    }


    private class SwitchListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            if(v instanceof SegmentView) {
                SegmentView sv = (SegmentView) v;
                if(edit) {
                    editSegment(sv);
                } else {
                    switchSwitch(sv.getSegment());
                    sv.invalidate();
                }
            }
        }
    }

    private class EditSwitchListener implements View.OnLongClickListener {
        @Override
        public boolean onLongClick(View v) {
            if(v instanceof SegmentView) {
                SegmentView sv = (SegmentView) v;
                if(sv.getSegment().isSwitch()) {
                    editSegmentSettings(sv);
                    return true;
                }
            }
            return false;
        }
    }

    private class EditIdListener implements View.OnLongClickListener {
        @Override
        public boolean onLongClick(View v) {
            if(v instanceof SegmentView) {
                SegmentView sv = (SegmentView) v;
                if(sv.getSegment().isEditable()) {
                    editSegmentId(sv);
                    return true;
                }
            }
            return false;
        }
    }

    private void editSegmentId(final SegmentView segmentView) {
        MarkerIdDialog dlg = new MarkerIdDialog(this,segmentView.getSegment(), new MarkerIdDialog.OnMarkerIdChangedListener() {
            @Override
            public void onMarkerIdChanged(Segment s) {
                segmentView.invalidate();
                storage.put("layout", layout);
                storage.flush();
            }
        });
        dlg.show();
    }


    private class SimpleListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            if(v instanceof SegmentView) {
                if(edit) {
                    editSegment((SegmentView) v);
                }
            }
        }
    }



    private void editSegment(final SegmentView segmentView) {
        final boolean isSwitch = segmentView.getSegment().isSwitch();
        EditDialog dlg = new EditDialog(this, new EditDialog.OnSegmentTypeChangedListener() {
            @Override
            public void onSegmentTypeChanged(Segment.Type t, String id) {
                segmentView.getSegment().setType(t);
                segmentView.getSegment().setId(id);
                wireSegmentViewClickListener(segmentView);
                segmentView.invalidate();
                if(segmentView.getSegment().isSwitch() && !isSwitch) {
                    // now is switch and needs config.
                    editSegmentSettings(segmentView);
                }
            }
        });
        dlg.show();
    }

    private void editSegmentSettings(final SegmentView segmentView) {
        SegmentSettingDialog dlg = new SegmentSettingDialog(this, segmentView.getSegment(), new SegmentSettingDialog.OnSegmentSettingsChangedListener() {
            @Override
            public void onSegmentSettingChanged(Segment s) {
                segmentView.invalidate();
                storage.put("layout", layout);
                storage.flush();
            }

            @Override
            public void test(int bus, int address, int port) {
                Log.i(LOG_TAG, "testing switching " + bus + ":" + address + " to " + port);
                sendSetGenericAccessoryCommand(bus, address, port);
            }
        });
        dlg.show();
    }


    private void switchSwitch(Segment segment) {
        int newState = 1- segment.getState();
        Log.i(LOG_TAG, "switching " + segment.getId() + " to " + newState);
        sendSetGenericAccessoryCommand(segment.getBus(), segment.getAddress(), newState);
        segment.setState(newState);
    }

    @Override
    protected void onPause() {
        EventBus.getDefault().unregister(this);
        super.onPause();
    }

    public void onEventMainThread(SrcpLocoInfoMessage msg) {
        if(currentloco!=null && msg.getAddress()==currentloco.address) {
            currentloco.direction = msg.getDirection();
            currentloco.speed = msg.getSpeed();
            currentloco.function = msg.getFunctions();
            updateController();
        }
    }

    public void onEventMainThread(StatusMessage msg) {
        if(progressDialog!=null && progressDialog.isShowing()) progressDialog.dismiss();
        switch(msg.status) {
            case  CONNECTED:
                Toast.makeText(MainActivity.this, getString(R.string.connected), Toast.LENGTH_SHORT).show();
                connected=true;
                buttonConnection.setImageResource(R.drawable.connected);
                buttonPower.setVisibility(View.VISIBLE);
                checkPower();
                break;
            case DISCONNECTED:
                Toast.makeText(MainActivity.this, getString(R.string.disconnected), Toast.LENGTH_SHORT).show();
                connected=false;
                buttonConnection.setImageResource(R.drawable.disconnected);
                buttonPower.setVisibility(View.INVISIBLE);
                break;
            case POWER_ON:
                power=true;
                buttonConnection.setVisibility(View.VISIBLE);
                buttonPower.setImageResource(R.drawable.power_on);
                break;
            case POWER_OFF:
                power=false;
                buttonConnection.setVisibility(View.VISIBLE);
                buttonPower.setImageResource(R.drawable.power_off);
                break;
        }
    }
}

// service mode:
// INIT bus SM protocol = INIT 1 SM NMRA
// SET bus SM adr CV cvaddress value (conf. var.)   -> cv=1 is address
// SET bus SM adr CVBIT cvaddress bit value (conf. var.)
// SET bus SM adr REG regaddress value

// TERM bus SM
