package de.ludetis.android.signalbox;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.widget.EditText;

import de.ludetis.android.signalbox.model.Segment;

/**
 * Created by uwe on 09.09.14.
 */
public class SegmentSettingDialog extends Dialog implements View.OnClickListener{

    private final Segment segment;
    private final OnSegmentSettingsChangedListener listener;
    private final EditText etBus, etAddress, etId;
    private final SegmentView segmentView;
    private int port=0;


    interface OnSegmentSettingsChangedListener {
        void onSegmentSettingChanged(Segment s);
        void test(int bus, int address, int port);
    }


    public SegmentSettingDialog(Context context, Segment segment, OnSegmentSettingsChangedListener listener) {
        super(context, android.R.style.Theme_Translucent_NoTitleBar_Fullscreen);
        this.segment = segment;
        this.listener = listener;
        setContentView(R.layout.dlg_segment_setting);
        findViewById(R.id.save).setOnClickListener(this);
        findViewById(R.id.test).setOnClickListener(this);
        etId = (EditText) findViewById(R.id.id);
        etBus = (EditText) findViewById(R.id.bus);
        etAddress = (EditText) findViewById(R.id.address);
        etId.setText(segment.getId());
        etBus.setText(Integer.toString(segment.getBus()));
        etAddress.setText(Integer.toString(segment.getAddress()));
        segmentView = (SegmentView) findViewById(R.id.segment);
        segmentView.setSegment(segment);
        port = segment.getState();
    }

    @Override
    public void onClick(View v) {
        if(v.getId()==R.id.test) {
            int address = Integer.parseInt(etAddress.getText().toString());
            int bus = Integer.parseInt(etBus.getText().toString());
            listener.test(bus,address,port);
            segmentView.getSegment().setState(port);
            segmentView.invalidate();
            port = 1-port;
        }
        if(v.getId()==R.id.save) {
            int address = Integer.parseInt(etAddress.getText().toString());
            int bus = Integer.parseInt(etBus.getText().toString());
            segment.setId(etId.getText().toString());
            segment.setAddress(address);
            segment.setBus(bus);
            listener.onSegmentSettingChanged(segment);
            dismiss();
        }

    }
}
