package de.ludetis.android.signalbox;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.widget.EditText;

import de.ludetis.android.signalbox.model.Segment;

/**
 * Created by uwe on 09.09.14.
 */
public class MarkerIdDialog extends Dialog implements View.OnClickListener {

    private final Segment segment;
    private final OnMarkerIdChangedListener listener;
    private final EditText etId;

    interface OnMarkerIdChangedListener {
        void onMarkerIdChanged(Segment s);
    }


    public MarkerIdDialog(Context context, Segment segment, OnMarkerIdChangedListener listener) {
        super(context, android.R.style.Theme_Translucent_NoTitleBar_Fullscreen);
        this.segment = segment;
        this.listener = listener;
        setContentView(R.layout.dlg_marker_id);
        findViewById(R.id.save).setOnClickListener(this);
        etId = (EditText) findViewById(R.id.id);
        etId.setText(segment.getId());
    }

    @Override
    public void onClick(View v) {
        if(v.getId()==R.id.save) {
            segment.setId(etId.getText().toString());
            listener.onMarkerIdChanged(segment);
            dismiss();
        }
    }
}
