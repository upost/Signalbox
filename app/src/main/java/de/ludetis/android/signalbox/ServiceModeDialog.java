package de.ludetis.android.signalbox;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.widget.EditText;

import de.greenrobot.event.EventBus;
import de.ludetis.android.signalbox.model.Loco;
import de.ludetis.android.signalbox.model.ServiceModeMessage;

/**
 * Created by uwe on 11.06.15.
 */
public class ServiceModeDialog extends Dialog {

    private final EditText etAddress;
    private final EditText etCv;
    private final EditText etValue;
    private final Loco loco;

    interface OnDataConfirmedListener {
        void  onDataConfirmed(Loco l);
    }

    public ServiceModeDialog(Context context, Loco l, final OnDataConfirmedListener listener) {
        super(context, android.R.style.Theme_Translucent_NoTitleBar_Fullscreen);
        this.loco = l;
        setContentView(R.layout.dlg_service_mode);
        etAddress= (EditText) findViewById(R.id.address);
        etAddress.setText(Integer.toString(loco.address));
        etCv = (EditText) findViewById(R.id.cv);
        etCv.setText("1");
        etValue = (EditText) findViewById(R.id.value);
        etValue.setText(Integer.toString(l.address));

        findViewById(R.id.done).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                listener.onDataConfirmed(loco);
            }
        });

        findViewById(R.id.program).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int address = Integer.parseInt(etAddress.getText().toString());
                int cv = Integer.parseInt(etCv.getText().toString());
                int value = Integer.parseInt(etValue.getText().toString());
                doProgram(loco,address,cv,value);
            }
        });
    }

    private void doProgram(Loco loco, int address, int cv, int value) {
        //ProgressDialog pd = new ProgressDialog(getContext());
        //pd.setProgress(0);
        EventBus.getDefault().post(new ServiceModeMessage(address, cv, value));
        // did we change the loco's address this way?
        if(cv==1) {
            loco.address = value;
            loco.initSent = false;
        }
    }

}
