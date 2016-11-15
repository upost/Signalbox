package de.ludetis.android.signalbox;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import de.ludetis.android.signalbox.model.Segment;

/**
 * Created by uwe on 09.09.14.
 */
public class FunctionDecoderDialog extends Dialog implements View.OnClickListener {

    private static final int SEGMENT_WIDTH = 200;
    private static final int SEGMENT_HEIGHT = 100;
    private final OnFunctionChangedListener listener;


    interface OnFunctionChangedListener {
        void onFunctionChanged(SegmentView segmentView, int function, boolean value);
    }

    public FunctionDecoderDialog(final Context context, final SegmentView sv, final OnFunctionChangedListener listener) {
        super(context,android.R.style.Theme_Translucent_NoTitleBar_Fullscreen);
        setCanceledOnTouchOutside(true);
        this.listener = listener;
        setContentView(R.layout.dlg_function_decoder);
        ((TextView)findViewById(R.id.function_title)).setText(sv.getSegment().getId());
        findViewById(R.id.close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });
        ViewGroup vg = (ViewGroup) findViewById(R.id.container);
        for(int i = 0; i< Segment.FUNCTION_DECODER_FUNCTIONS; i++) {
            View v = ((Activity)context).getLayoutInflater().inflate(R.layout.include_function_button,null);
            vg.addView(v);
            ((TextView)v.findViewById(R.id.function_number)).setText(Integer.toString(i+1));
            final ImageView b = (ImageView) v.findViewById(R.id.function_button);
            b.setImageDrawable(sv.getSegment().getFunctionValue(i) ? context.getResources().getDrawable(R.drawable.circle_green) : context.getResources().getDrawable(R.drawable.circle_grey));
            final int index=i;
            b.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    sv.getSegment().activateFunction(index);
                    b.setImageDrawable(sv.getSegment().getFunctionValue(index) ? context.getResources().getDrawable(R.drawable.circle_green) : context.getResources().getDrawable(R.drawable.circle_grey));
                    listener.onFunctionChanged(sv,index,sv.getSegment().getFunctionValue(index));
                }
            });
        }

    }

    @Override
    public void onClick(View v) {

    }
}
