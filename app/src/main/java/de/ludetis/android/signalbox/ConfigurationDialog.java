package de.ludetis.android.signalbox;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.widget.EditText;

/**
 * Created by uwe on 09.09.14.
 */
public class ConfigurationDialog extends Dialog implements View.OnClickListener {
    private final String serverAddress;
    private final int port;
    private final OnConfigurationChangedListener listener;
    private final EditText etServerAddress;
    private final EditText etServerPort;

    interface OnConfigurationChangedListener {
        void onConfigurationChanged(String serverAddress, int serverPort);
    }

    public ConfigurationDialog(Context context, String serverAddress, int port, OnConfigurationChangedListener listener) {
        super(context, android.R.style.Theme_Translucent_NoTitleBar_Fullscreen);
        this.serverAddress = serverAddress;
        this.port = port;
        this.listener = listener;
        setContentView(R.layout.dlg_configuration);
        etServerAddress = (EditText) findViewById(R.id.server_address);
        etServerAddress.setText(serverAddress);
        etServerPort    = (EditText) findViewById(R.id.server_port);
        etServerPort.setText(Integer.toString(port));
        findViewById(R.id.save).setOnClickListener(this);
        findViewById(R.id.cancel).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if(v.getId()==R.id.save) {
            listener.onConfigurationChanged(etServerAddress.getText().toString(), Integer.parseInt(etServerPort.getText().toString()));
            dismiss();
        }
        if(v.getId()==R.id.cancel) {
            dismiss();
        }
    }
}
