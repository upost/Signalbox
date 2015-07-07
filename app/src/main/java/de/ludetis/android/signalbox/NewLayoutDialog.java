package de.ludetis.android.signalbox;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.widget.EditText;

/**
 * Created by uwe on 09.09.14.
 */
public class NewLayoutDialog extends Dialog implements View.OnClickListener {

    private final EditText etWidth;
    private final EditText etHeight;
    private final OnDataConfirmedListener listener;

    interface OnDataConfirmedListener {
        void onDataConfirmed(int w, int h);
    }

    public NewLayoutDialog(Context context, int w, int h, OnDataConfirmedListener listener) {
        super(context,android.R.style.Theme_Translucent_NoTitleBar_Fullscreen);
        this.listener = listener;
        setContentView(R.layout.dlg_new);
        etWidth = (EditText) findViewById(R.id.width);
        etHeight= (EditText) findViewById(R.id.height);
        etWidth.setText(Integer.toString(w));
        etHeight.setText(Integer.toString(h));
        findViewById(R.id.create).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if(v.getId()==R.id.create) {
            int w = Integer.valueOf(String.valueOf(etWidth.getText()));
            int h = Integer.valueOf(String.valueOf(etHeight.getText()));
            listener.onDataConfirmed(w,h);
            dismiss();
        }
    }
}
