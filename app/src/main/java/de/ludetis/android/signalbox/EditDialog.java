package de.ludetis.android.signalbox;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import de.ludetis.android.signalbox.model.Segment;

/**
 * Created by uwe on 09.09.14.
 */
public class EditDialog extends Dialog implements View.OnClickListener {

    private static final int SEGMENT_WIDTH = 200;
    private static final int SEGMENT_HEIGHT = 100;
    private final OnEditListener listener;


    interface OnEditListener {
        void onSegmentTypeChanged(Segment.Type t, String id);
        void onMove(int x, int y);
        void onCopy();
        void onPaste();
    }

    public EditDialog(Context context, OnEditListener listener) {
        super(context,android.R.style.Theme_Translucent_NoTitleBar_Fullscreen);
        this.listener = listener;
        setContentView(R.layout.dlg_edit);
        ViewGroup container = (ViewGroup) findViewById(R.id.container);
        for(Segment.Type type : Segment.Type.values()) {
            SegmentView segmentView = new SegmentView(getContext());
            segmentView.setSegment(SegmentFactory.newSegment(type));
            FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(SEGMENT_WIDTH,SEGMENT_HEIGHT);
            segmentView.setOnClickListener(this);
            findViewById(R.id.move_down).setOnClickListener(this);
            findViewById(R.id.move_right).setOnClickListener(this);
            findViewById(R.id.copy).setOnClickListener(this);
            findViewById(R.id.paste).setOnClickListener(this);
            container.addView(segmentView, lp);
        }
    }

    @Override
    public void onClick(View v) {
        if(v instanceof SegmentView) {
            listener.onSegmentTypeChanged(((SegmentView) v) .getSegment().getType(), ((SegmentView) v) .getSegment().getId());
            dismiss();
        }
        switch (v.getId()) {
            case R.id.move_down:
                listener.onMove(0,1);
                break;
            case R.id.move_right:
                listener.onMove(1,0);
                break;
            case R.id.copy:
                listener.onCopy();
                break;
            case R.id.paste:
                listener.onPaste();
                break;
            default:
                break;
        }

    }
}
