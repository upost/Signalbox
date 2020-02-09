package de.ludetis.android.signalbox;

/**
 * copyright 2015 Uwe Post
 */

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Vibrator;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.SoundEffectConstants;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;

import de.greenrobot.event.EventBus;
import de.ludetis.android.signalbox.model.AboutDialog;
import de.ludetis.android.signalbox.model.Loco;
import de.ludetis.android.signalbox.model.Segment;
import de.ludetis.android.signalbox.model.ServiceMessage;
import de.ludetis.android.signalbox.model.SrcpGenericAccessoryInfoMessage;
import de.ludetis.android.signalbox.model.SrcpLocoInfoMessage;
import de.ludetis.android.signalbox.model.SrcpMessage;
import de.ludetis.android.signalbox.model.StatusMessage;
import de.ludetis.android.signalbox.model.UIMessage;
import de.ludetis.android.storage.MapDbStorage;
import de.ludetis.android.storage.MapStorage;
import de.ludetis.android.view.VerticalSeekBar;


public class MainActivity extends Activity implements View.OnClickListener, SeekBar.OnSeekBarChangeListener, View.OnLongClickListener {

    private static final int MY_PERMISSIONS_REQUEST = 893;
    public static final String[] PERMISSIONS = {Manifest.permission.READ_EXTERNAL_STORAGE};
    private static final int DEFAULT_FUNCTION_KEYS = 5;
    private static final int THUMBNAIL_SIZE = 128;

    enum RouteSetStepType { NONE, WAIT_START, WAIT_END };

    private static final int[] FUNCTION_BUTTONS = new int[] {R.id.function0, R.id.function1, R.id.function2, R.id.function3,
            R.id.function4,R.id.function5, R.id.function6, R.id.function7, R.id.function8, R.id.function9, R.id.function10,
            R.id.function11, R.id.function12, R.id.function13, R.id.function14, R.id.function15, R.id.function16, R.id.function17,
            R.id.function18, R.id.function19};
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
    private static final int DEFAULT_BUS = 1;
    private String server = "192.168.178.39";
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
    private VerticalSeekBar seekBar;
    private TextView speedDisplay;
    private Vibrator vibrateSvc;
    private RouteSetStepType routeSetStep;
    private Segment routeStart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        vibrateSvc = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        if(!vibrateSvc.hasVibrator()) {
            Log.d(LOG_TAG, "no vibrator!");
        }

        setContentView(R.layout.activity_main);

        buttonPower = findViewById(R.id.power);
        buttonPower.setOnClickListener(this);
        buttonConnection =  findViewById(R.id.connection);
        buttonConnection.setOnClickListener(this);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST: {
                int granted = 0;
                for (int i = 0; i < grantResults.length; i++) {
                    if (grantResults[i] == PackageManager.PERMISSION_GRANTED) granted++;
                }
                if (granted == PERMISSIONS.length) {

                    Toast.makeText(MainActivity.this, "permissions granted", Toast.LENGTH_LONG).show();

                    init();

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(MainActivity.this, "cannot run without permission", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    private void init() {
        storage = new MapDbStorage(this,SrcpService.DB_FILENAME);
        checkForExportFile(storage);

        locoManager = new LocoManager(storage);

        findViewById(R.id.configuration).setOnClickListener(this);
        findViewById(R.id.edit).setOnClickListener(this);
        findViewById(R.id.loco_image).setOnClickListener(this);
        findViewById(R.id.loco_image).setOnLongClickListener(this);
        findViewById(R.id.close_controller).setOnClickListener(this);
        findViewById(R.id.route_start).setOnClickListener(this);
        findViewById(R.id.img).setOnClickListener(this);

        findViewById(R.id.directionBack).setOnClickListener(this);
        findViewById(R.id.directionForward).setOnClickListener(this);
        findViewById(R.id.powerDown).setOnClickListener(this);
        findViewById(R.id.powerUp).setOnClickListener(this);

        findViewById(R.id.stop).setOnClickListener(this);
        for(int r : FUNCTION_BUTTONS) {
            findViewById(r).setOnClickListener(this);
        }
        seekBar = findViewById(R.id.speedControl);
        seekBar.setOnSeekBarChangeListener(this);
        speedDisplay = findViewById(R.id.powerLevel);
        speedDisplay.setText("");

        routeSetStep = RouteSetStepType.NONE;

        startService(new Intent(this, SrcpService.class));

        showLocoList();
        for(Loco l : locoManager.getLocoList()) {
            l.initSent=false;
        }

        loadSettings();

        if(server!=null) {
            loadLayout();
            fillContainer();
        }

        connected=false;
        buttonConnection.setImageResource(R.drawable.disconnected);
        buttonPower.setVisibility(View.INVISIBLE);

        EventBus.getDefault().register(this);

        checkPower();

    }


    private void showLocoList() {
        findViewById(R.id.chooser).setVisibility(View.VISIBLE);
        findViewById(R.id.controller).setVisibility(View.GONE);
        ViewGroup container = findViewById(R.id.locolist);
        container.removeAllViews();
        for(Loco loco : locoManager.getLocoList()) {
            View lv = getLayoutInflater().inflate(R.layout.include_loco,null);
            lv.findViewById(R.id.image).setTag(R.id.TAGKEY_LOCO, loco);
            lv.findViewById(R.id.image).setOnClickListener(this);
            if(loco.address==0)
                ((ImageView) lv.findViewById(R.id.image)).setImageResource(R.drawable.analog_loco);
            else {
                try {
                    ((ImageView) lv.findViewById(R.id.image)).setImageBitmap(getThumbnail(Uri.parse(loco.image)));

                } catch (IOException e) {
                    ((ImageView) lv.findViewById(R.id.image)).setImageResource(R.drawable.default_loco);
                }
            }

            container.addView(lv);

        }
        View lv = getLayoutInflater().inflate(R.layout.include_addloco,null);
        lv.findViewById(R.id.add_loco).setOnClickListener(this);
        container.addView(lv);
    }

    public  Bitmap getThumbnail(Uri uri) throws  IOException{
        InputStream input = this.getContentResolver().openInputStream(uri);

        BitmapFactory.Options onlyBoundsOptions = new BitmapFactory.Options();
        onlyBoundsOptions.inJustDecodeBounds = true;
        onlyBoundsOptions.inDither=true;//optional
        onlyBoundsOptions.inPreferredConfig=Bitmap.Config.ARGB_8888;//optional
        BitmapFactory.decodeStream(input, null, onlyBoundsOptions);
        input.close();

        if ((onlyBoundsOptions.outWidth == -1) || (onlyBoundsOptions.outHeight == -1)) {
            return null;
        }

        int originalSize = (onlyBoundsOptions.outHeight > onlyBoundsOptions.outWidth) ? onlyBoundsOptions.outHeight : onlyBoundsOptions.outWidth;

        double ratio = (originalSize > THUMBNAIL_SIZE) ? (originalSize / THUMBNAIL_SIZE) : 1.0;

        BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
        bitmapOptions.inSampleSize = getPowerOfTwoForSampleRatio(ratio);
        bitmapOptions.inDither = true; //optional
        bitmapOptions.inPreferredConfig=Bitmap.Config.ARGB_8888;//
        input = this.getContentResolver().openInputStream(uri);
        Bitmap bitmap = BitmapFactory.decodeStream(input, null, bitmapOptions);
        input.close();
        return bitmap;
    }

    private static int getPowerOfTwoForSampleRatio(double ratio){
        int k = Integer.highestOneBit((int)Math.floor(ratio));
        if(k==0) return 1;
        else return k;
    }

    private void showLocoController(Loco loco) {
        currentloco = loco;
        findViewById(R.id.chooser).setVisibility(View.GONE);
        findViewById(R.id.controller).setVisibility(View.VISIBLE);
        if(loco.address==0)
            ((ImageView)findViewById(R.id.loco_image)).setImageResource(R.drawable.analog_loco);
        else
            ((ImageView)findViewById(R.id.loco_image)).setImageURI(Uri.parse(loco.image));
        // get current values
        sendGetGenericLocoCommand(1, loco.address);
        updateController();
    }

    @Override
    protected void onResume() {
        super.onResume();

        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        if (permissionCheck == PackageManager.PERMISSION_DENIED ) {
            ActivityCompat.requestPermissions(this, PERMISSIONS, MY_PERMISSIONS_REQUEST);
        } else {

            init();
        }

    }

    private void checkForExportFile(MapStorage storage) {
        Log.d(LOG_TAG, "checking for import file in "+Environment.getExternalStorageDirectory());
        File file = new File(Environment.getExternalStorageDirectory(), "signalbox.json");

        if(file.exists()) {
            try {
                BufferedReader br = new BufferedReader(new FileReader(file));
                try {
                    StringBuilder sb = new StringBuilder();
                    String line = br.readLine();

                    while (line != null) {
                        sb.append(line);
                        sb.append("\n");
                        line = br.readLine();
                    }

                    Gson gson = new Gson();
                    Export export = gson.fromJson(sb.toString(), Export.class);
                    storage.put("server", export.server);
                    storage.put("port", export.port);
                    storage.put("locos2", export.locos);
                    storage.put("layout", export.layout);
                    storage.flush();

                    Log.d(LOG_TAG, "imported from signalbox.json");

                } finally {
                    br.close();
                }

            } catch (IOException e) {
                Log.e(LOG_TAG, "ioexception during export",e);
            }
        }
    }

    class Export {
        String server;
        int port;
        ArrayList<Loco> locos;
        ArrayList<Segment> layout;
    }

    private void export() {
        Gson gson = new Gson();
        JsonObject o = new JsonObject();
        o.addProperty("server", server);
        o.addProperty("port", port);
        o.add("layout", gson.toJsonTree(layout));
        o.add("locos", gson.toJsonTree(locoManager.getLocoList()));
        String export = o.toString();

        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_SUBJECT,"signalbox export");
        sendIntent.putExtra(Intent.EXTRA_TEXT, export);
        sendIntent.setType("application/json");
        startActivity(sendIntent);

//
//        File file = new File(Environment.getExternalStorageDirectory(), "signalbox.json");
//        FileOutputStream fos = null;
//        try {
//            fos = new FileOutputStream(file);
//            fos.write(export.getBytes());
//
//            fos.close();
//            Toast.makeText(this, "exported to signalbox.json", Toast.LENGTH_SHORT).show();
//        } catch (IOException e) {
//            Toast.makeText(this, "could not export", Toast.LENGTH_SHORT).show();
//            Log.e(LOG_TAG, "ioexception during export",e);
//        }

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
                    storage.put(LAYOUT, layout);
                    storage.flush();
                    Log.d(LOG_TAG, "created layout");
                    fillContainer();
                }
            });
            d.show();
            layout = LayoutFactory.createDemo();
            storage.put(LAYOUT, layout);
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

        if(segmentView.getSegment().isSwitch() || segmentView.getSegment().isSemaphore()
                || segmentView.getSegment().isGenericAccessory() || segmentView.getSegment().isGenericFunction()
                || segmentView.getSegment().isRoutePoint())  {
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
        for(int i =0; i<FUNCTION_BUTTONS.length; i++) {
            if(v.getId()==FUNCTION_BUTTONS[i]) {
                if(connected) {
                    currentloco.activateFunction(i);
                    updateController();
                    sendLocoData(currentloco);
                }
            }
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
                if(connected) {
                    if (power) {
                        send("SET " + DEFAULT_BUS + " POWER OFF");
                    } else {
                        send("SET " + DEFAULT_BUS + " POWER ON");
                    }
                }

                break;
            case R.id.configuration:
                configure(); break;
            case R.id.edit:
                toggleEdit();
                break;
            case R.id.directionForward:
                if(connected) {
                    currentloco.direction = 0;
                    updateController();
                    stopLoco();
                    sendLocoData(currentloco);
                }
                break;
            case R.id.directionBack:
                if(connected) {
                    currentloco.direction = 1;
                    updateController();
                    stopLoco();
                    sendLocoData(currentloco);
                }
                break;
            case R.id.powerDown:
                if(connected && currentloco.speed>0) {
                    currentloco.speed--;
                    sendLocoData(currentloco);
                    updateController();
                }
                break;
            case R.id.powerUp:
                if(connected && currentloco.speed<seekBar.getMax()) {
                    currentloco.speed++;
                    sendLocoData(currentloco);
                    updateController();
                }
                break;
            case R.id.stop:
                emergencyStop();
                break;
            case R.id.add_loco:
                final Loco l = new Loco(DEFAULT_BUS,0,0,new int[LocoManager.FUNCTION_MAX], DEFAULT_FUNCTION_KEYS, "");
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
            case R.id.close_controller:
                showLocoList();
                break;
            case R.id.route_start:
                if(routeSetStep==RouteSetStepType.NONE) {
                    showToast(R.string.tapRouteStart);
                    routeSetStep = RouteSetStepType.WAIT_START;
                    ((ImageButton)findViewById(R.id.route_start)).setImageResource(R.drawable.route_wait);
                } else {
                    showToast(R.string.setRouteCancelled);
                    routeSetStep = RouteSetStepType.NONE;
                    ((ImageButton)findViewById(R.id.route_start)).setImageResource(R.drawable.route_wait);
                }
                break;
            case R.id.img:
                showAboutDialog();
                break;
        }
    }

    private void showAboutDialog() {
        AboutDialog dlg = new AboutDialog(this, this::export);
        dlg.show();
    }

    private void showToast(int strRes) {
        Toast.makeText(this,strRes,Toast.LENGTH_SHORT).show();
    }

    private void showRemoveLocoQuestion() {
        AlertDialog.Builder b = new AlertDialog.Builder(this).setTitle(R.string.stop)
                .setMessage(R.string.really_remove)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        locoManager.removeLoco(currentloco);
                        locoManager.saveAll();
                        showLocoList();
                    }
                })
                .setCancelable(true);
        b.create().show();
    }

    private void updateController() {
        for(int i =0; i<FUNCTION_BUTTONS.length; i++) {
            (findViewById(FUNCTION_BUTTONS[i])).setVisibility(i<currentloco.getFunctionKeys()?View.VISIBLE:View.GONE);
            (findViewById(FUNCTION_BUTTONS[i])).setBackgroundColor(currentloco.getFunction(i)>0 ? COLOR_SET : COLOR_NOTSET);
        }

        (findViewById(R.id.directionForward)).setBackgroundColor( currentloco.direction==0 ? COLOR_SET : COLOR_NOTSET );
        (findViewById(R.id.directionBack)).setBackgroundColor( currentloco.direction==1 ? COLOR_SET : COLOR_NOTSET );
        seekBar.setProgress(currentloco.speed);
        speedDisplay.setText(Integer.toString(currentloco.speed));
    }

    private void showLocoDialog(Loco l, LocoDialog.OnDataConfirmedListener listener) {
        Dialog dlg = new LocoDialog(this, l, listener, new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                showRemoveLocoQuestion();

            }
        });
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
        seekBar.setProgress(0);
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
        ImageButton b = (ImageButton) findViewById(R.id.edit);
        edit = !edit;
        b.setImageResource(edit ? R.drawable.ic_menu_edit_active: R.drawable.ic_menu_edit);
        if(edit) {
//
        } else {
            storage.put(LAYOUT, layout);
            storage.flush();
            Log.d(LOG_TAG, "saved layout");

        }

    }


    private void send(final String what) {
        EventBus.getDefault().post(new SrcpMessage(what));
    }


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
    private void sendSetGenericAccessoryCommand(int bus, int address, int port, int delay) {
        sendSetGenericAccessoryCommand(bus,address,port,1,delay);
    }
    private void sendSetGenericAccessoryCommand(int bus, int address, int port, int value, int delay) {
        send("SET " + bus + " GA " + address + " " + port + " "+value+" "+delay); // format: SET <bus> GA <addr> <port> <value> <delay>
    }

    private void sendInitGenericAccessoryCommand(int bus, int address) {
        send("INIT " + bus + " GA " + address + " N");
    }

    private void sendGetGenericAccessoryCommand(int bus, int address, int port) {
        send("GET " + bus + " GA " + address  + " " + port);
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
    //if(!currentloco.initSent)
    //            sendInitGenericLocoCommand(bus,address, SPEED_STEPS,5);
        String fu="";
        for(int f:functions) {
            fu+=Integer.toString(f)+" ";
        }
        send("SET "+bus+" GL "+address+" "+direction + " "+speed + " " +maxSpeed+" " +fu);
    }

    private void sendGetGenericLocoCommand(int bus, int address) {
//        if(!loco.initSent)
//            sendInitGenericLocoCommand(bus,loco.address, SPEED_STEPS,5);
        Log.d(LOG_TAG, "sending get gl for " + address);
        send("GET "+bus+" GL " + address);
    }

    private void sendInitGenericLocoCommand(int bus, int address, int speedSteps, int functions) {
        if(address==0) {
            // analog
            send("INIT " + bus + " GL " + address + " A 1 "+speedSteps + " 0" );
        } else {
            send("INIT " + bus + " GL " + address + " " + SrcpService.DEFAULT_PROTOCOL + " 1 " + speedSteps + " " + functions);
        }

    }

    private void initCurrentLoco() {
        if(currentloco!=null) {
            Log.d(LOG_TAG,"init current loco...");
            sendInitGenericLocoCommand(currentloco.getBus(), currentloco.address, SPEED_STEPS, FUNCTION_BUTTONS.length);
            currentloco.initSent=true;
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int speed, boolean fromUser) {
        if(currentloco!=null && currentloco.speed!=speed && speed>=0
                && (lastProgressChanged+MIN_PROGRESS_CHANGE_THRESHOLD_MS < System.currentTimeMillis() || speed==0)) {
            changeLocoSpeed(speed);
            seekBar.playSoundEffect(SoundEffectConstants.CLICK);

        }
    }

    private void changeLocoSpeed(int speed) {
        currentloco.speed=speed;
        sendLocoData(currentloco);
        lastProgressChanged=System.currentTimeMillis();
        updateController();
        if(vibrateSvc.hasVibrator()) {
            vibrateSvc.vibrate(50);
        }
    }

    private void sendLocoData(Loco lo) {
        sendSetGenericLocoCommand(lo.getBus(), lo.address, lo.direction, lo.speed, 100, lo.getFunctions());

    }

    public void emergencyStop() {
        sendSetGenericLocoCommand(currentloco.getBus(), currentloco.address, 2, 0, 100, currentloco.getFunctions());
        stopLoco();
        updateController();
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        if(vibrateSvc.hasVibrator()) {
            vibrateSvc.vibrate(100);
        }
        seekBar.playSoundEffect(SoundEffectConstants.CLICK);


    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

        if(currentloco!=null && currentloco.speed!=seekBar.getProgress()) {
            changeLocoSpeed(seekBar.getProgress());
        }
//        if(vibrateSvc.hasVibrator()) {
//            vibrateSvc.vibrate(200);
//        }
//        seekBar.playSoundEffect(SoundEffectConstants.CLICK);
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
                    if(connected) {
                        if(sv.getSegment().isRoutePoint() && routeSetStep==RouteSetStepType.WAIT_START) {
                            routeStart = sv.getSegment();
                            routeSetStep = RouteSetStepType.WAIT_END;
                            showToast(R.string.tapRouteEnd);
                            return;
                        }
                        if(sv.getSegment().isRoutePoint() && routeSetStep==RouteSetStepType.WAIT_END) {
                            activateRouteTo(sv.getSegment());
                            return;
                        }

                        if(sv.getSegment().isGenericFunction()) {
                            showFunctionDecoderDlg(sv);
                        } else {
                            switchSegment(sv.getSegment());
                        }
                        sv.invalidate();
                    }
                }
            }
        }
    }

    private void activateRouteTo(Segment to) {
        // find route
        RouteFinder rf = new RouteFinder(layout,routeStart,to);
        Map<Segment,Integer> route = rf.findRoute();
        if(route==null) {
            showToast(R.string.noRouteFound);
        } else {
            // activate route from routeStart to to
            for(Segment s : route.keySet()) {
                Log.d("Route", "route: " + s);
                if(route.get(s)!=null) {
                    switchSegment(s,route.get(s));
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        ;
                    }
                    SegmentView sv = findSegmentView(s);
                    if(sv!=null)
                        sv.invalidate();
                }
            }

            // then:
            showToast(R.string.setRouteComplete);
        }
        routeSetStep = RouteSetStepType.NONE;
        ((ImageButton)findViewById(R.id.route_start)).setImageResource(R.drawable.route_off);
    }

    private SegmentView findSegmentView(Segment s) {
        FrameLayout container = (FrameLayout) findViewById(R.id.container);
        for(int i=0; i<container.getChildCount(); i++) {
            View v = container.getChildAt(i);
            if(v instanceof SegmentView) {
                if(((SegmentView)v).getSegment().equals(s)) return (SegmentView)v;
            }
        }
        throw new IllegalArgumentException("no segmentview found for segment");
    }


    private class EditSwitchListener implements View.OnLongClickListener {
        @Override
        public boolean onLongClick(View v) {
            if(v instanceof SegmentView) {
                SegmentView sv = (SegmentView) v;
                if(sv.getSegment().isSwitch() || sv.getSegment().isSemaphore() || sv.getSegment().isGenericAccessory() || sv.getSegment().isGenericFunction()) {
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
                storage.put(LAYOUT, layout);
                storage.flush();
            }
        });
        dlg.show();
    }

    private void showFunctionDecoderDlg(final SegmentView sv) {
        FunctionDecoderDialog dlg = new FunctionDecoderDialog(this, sv, new FunctionDecoderDialog.OnFunctionChangedListener() {
            @Override
            public void onFunctionChanged(SegmentView segmentView, int function, boolean value) {
                segmentView.invalidate();
                Segment segment = segmentView.getSegment();
                Log.i(LOG_TAG, "switching " + segment.getId() + " function " + function + " to " + value);
                sendInitGenericLocoCommand(segment.getBus(), segment.getAddress(), SPEED_STEPS, segment.function.length);
                sendSetGenericLocoCommand(segment.getBus(), segment.getAddress(),0,0,0,segment.function);
                //sendSetGenericAccessoryCommand(segment.getBus(), segment.getAddress(), function, value?1:0);
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
        EditDialog dlg = new EditDialog(this, new EditDialog.OnEditListener() {
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

            @Override
            public void onMove(int x, int y) {
                for(Segment s : layout) {
                    s.setX(s.getX()+x);
                    s.setY(s.getY()+y);
                }
                if(x==1) {
                    for(y=0; y<10; y++) {
                        layout.add(new Segment(Segment.Type.EMPTY,"", 0,y,0,0));
                    }
                }
                if(y==1) {
                    for(x=0; x<10; x++) {
                        layout.add(new Segment(Segment.Type.EMPTY,"", 0,y,0,0));
                    }
                }
                storage.put(LAYOUT, layout);
                storage.flush();
                loadLayout();
                fillContainer();
            }
        });
        dlg.show();
    }

    private void editSegmentSettings(final SegmentView segmentView) {
        SegmentSettingDialog dlg = new SegmentSettingDialog(this, segmentView.getSegment(), new SegmentSettingDialog.OnSegmentSettingsChangedListener() {
            @Override
            public void onSegmentSettingChanged(Segment s) {
                sendInitGenericAccessoryCommand(segmentView.getSegment().getBus(), segmentView.getSegment().getAddress());
                segmentView.invalidate();
                storage.put(LAYOUT, layout);
                storage.flush();
            }

            @Override
            public void test(int bus, int address, int port) {
                Log.i(LOG_TAG, "testing switching " + bus + ":" + address + " to " + port);
                sendSetGenericAccessoryCommand(bus, address, port,100);
            }
        });
        dlg.show();
    }

    private void switchSegment(Segment segment) {
        if(segment.isSwitch()) {
            toggleSwitch(segment);
        } else if(segment.isSemaphore()) {
            switchSemaphore(segment);
        }
    }

    private void switchSegment(Segment segment, int value) {
        if(segment.isSwitch()) {
            switchGenericAccessory(segment, value,100);
        } else if(segment.isSemaphore()) {
            switchGenericAccessory(segment, value,-1);
        }
    }


    private void toggleSwitch(Segment segment) {
        int newState = 1- segment.getState();
        switchGenericAccessory(segment, newState,100);
    }

    private void switchSemaphore(Segment segment) {
        if(segment.getType()== Segment.Type.SEMAPHORE3_TOP || segment.getType()== Segment.Type.SEMAPHORE3_BOTTOM) {
            int newState = segment.getState()+1;
            if(newState==3) newState=0;
            if(newState<2)
                switchGenericAccessory(segment, newState, -1);
            else {// Hp2
                switchGenericAccessory(segment, 0, -1, 1);
                segment.setState(newState);
            }
        } else if(segment.getType()== Segment.Type.SEMAPHORE4_TOP || segment.getType()== Segment.Type.SEMAPHORE4_BOTTOM) {
            int newState = segment.getState()+1;
            if(newState==4) newState=0;
            if(newState<2)
                switchGenericAccessory(segment, newState, -1);
            else {// Hp2 or Hp0Sr1
                switchGenericAccessory(segment, newState-2, -1, 1);
                segment.setState(newState);
            }
        } else{
            int newState = 1 - segment.getState();
            switchGenericAccessory(segment, newState, -1);

        }
    }

    private void switchGenericAccessory(Segment segment, int newState, int delay) {
        switchGenericAccessory(segment,newState,delay,0);
    }
    private void switchGenericAccessory(Segment segment, int newState, int delay,int offset) {
        Log.i(LOG_TAG, "switching " + segment.getId() + " to " + newState);
        sendSetGenericAccessoryCommand(segment.getBus(), segment.getAddress()+offset, newState, delay);
        segment.setState(newState);
    }

    @Override
    protected void onPause() {
        EventBus.getDefault().unregister(this);
        super.onPause();
    }

    public void onEventMainThread(SrcpLocoInfoMessage msg) {
        Segment s = findSegment(msg.getBus(), msg.getAddress());
        if(s!=null) {
            // function decoder
            s.function = msg.getFunctions();
        } else

        if(currentloco!=null && msg.getAddress()==currentloco.address) {
            currentloco.direction = msg.getDirection();
            currentloco.speed = msg.getSpeed();
            currentloco.setFunctions( msg.getFunctions() );
            updateController();
        }
    }

    private Segment findSegment(int bus, int address) {
        for(Segment s : layout) {
            if(s.getBus()==bus && s.getAddress()==address) return  s;
        }
        return null;
    }

    public void onEventMainThread(SrcpGenericAccessoryInfoMessage msg) {
        if(!msg.isAvailable()) {
            sendInitGenericAccessoryCommand(msg.getBus(), msg.getAddress());
        } else {
            for(Segment s : layout) {
                if(s.getBus()==msg.getBus() && s.getAddress()==msg.getAddress()) {
                    if(msg.getValue()>0) {
                        s.setState(msg.getPort());
                        Log.d(LOG_TAG, "ga " + s.getAddress() + " is currently set to" + s.getState());
                        break;
                    }
                }
            }
        }
    }

    public void onEventMainThread(StatusMessage msg) {
        if(progressDialog!=null && progressDialog.isShowing()) progressDialog.dismiss();
        switch(msg.status) {
            case  CONNECTED:
                if(!connected) {
                    Toast.makeText(MainActivity.this, getString(R.string.connected), Toast.LENGTH_SHORT).show();
                    connected = true;
                    for(Loco l : locoManager.getLocoList()) {
                        l.initSent=false;
                    }
                    checkPower();
                }
                buttonConnection.setImageResource(R.drawable.connected);
                buttonPower.setVisibility(View.VISIBLE);
                break;
            case DISCONNECTED:
                Toast.makeText(MainActivity.this, getString(R.string.disconnected), Toast.LENGTH_SHORT).show();
                connected=false;
                buttonConnection.setImageResource(R.drawable.disconnected);
                buttonPower.setVisibility(View.INVISIBLE);
                break;
            case POWER_ON:
                power=true;
                buttonPower.setVisibility(View.VISIBLE);
                buttonPower.setImageResource(R.drawable.power_on);
                checkGenericAccessorySettings();
                checkFunctionDecoderSettings();
                break;
            case POWER_OFF:
                power=false;
                buttonConnection.setVisibility(View.VISIBLE);
                buttonPower.setImageResource(R.drawable.power_off);
                break;
            case CURRENT_LOCO_UNKNOWN:
                initCurrentLoco();
                break;
            case CONNECTIVITY_AVAIL:
                Toast.makeText(MainActivity.this, getString(R.string.connectivity_available), Toast.LENGTH_SHORT).show();
                buttonConnection.setEnabled(true);
                break;
            case CONNECTIVITY_LOST:
                Toast.makeText(MainActivity.this, getString(R.string.no_connectivity), Toast.LENGTH_SHORT).show();
                connected=false;
                buttonConnection.setImageResource(R.drawable.disconnected);
                buttonPower.setVisibility(View.INVISIBLE);
                buttonConnection.setEnabled(false);
                break;
        }
    }

    private void checkGenericAccessorySettings() {
        for(Segment s : layout) {
            if(s.isSwitch() || s.isSemaphore() || s.isGenericAccessory()) {
                sendGetGenericAccessoryCommand(s.getBus(), s.getAddress(), 0);
                sendGetGenericAccessoryCommand(s.getBus(), s.getAddress(), 1);
                if(s.isSemaphore()) {
                    sendInitGenericAccessoryCommand(s.getBus(), s.getAddress());
                    sendInitGenericAccessoryCommand(s.getBus(), s.getAddress()+1);
                }
            }
        }
    }

    private void checkFunctionDecoderSettings() {
        for(Segment s : layout) {
            if(s.isGenericFunction()) {
                sendGetGenericLocoCommand(s.getBus(), s.getAddress());
            }
        }
    }


}

// service mode:
// INIT bus SM protocol = INIT 1 SM NMRA
// SET bus SM adr CV cvaddress value (conf. var.)   -> cv=1 is address
// SET bus SM adr CVBIT cvaddress bit value (conf. var.)
// SET bus SM adr REG regaddress value

// TERM bus SM
