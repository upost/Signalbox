package de.ludetis.android.signalbox;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import de.greenrobot.event.EventBus;
import de.ludetis.android.signalbox.model.Loco;
import de.ludetis.android.signalbox.model.UIMessage;

/**
 * Created by uwe on 11.06.15.
 */
public class LocoDialog extends Dialog {

    public static final int PICK_IMAGE = 12345;
    private final Loco loco;
    private final EditText etAddress;
    private final ImageView ivImage;
    private final Activity activity;

    interface OnDataConfirmedListener {
        void  onDataConfirmed(Loco l);
    }

    public LocoDialog(Context context, Loco l, final OnDataConfirmedListener listener) {
        super(context, android.R.style.Theme_Translucent_NoTitleBar_Fullscreen);
        activity = (Activity)context;
        this.loco = l;
        setContentView(R.layout.dlg_add_loco);
        etAddress= (EditText) findViewById(R.id.address);
        etAddress.setText(Integer.toString(loco.address));
        ivImage = (ImageView) findViewById(R.id.image);
        if(loco.image!=null && !TextUtils.isEmpty(loco.image)) {
            Uri uri = Uri.parse(loco.image);
            if(uri!=Uri.EMPTY)
                ivImage.setImageURI(uri);
        }

        findViewById(R.id.create).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loco.address = Integer.parseInt(etAddress.getText().toString());
                loco.initSent = false;
                dismiss();
                EventBus.getDefault().unregister(LocoDialog.this);
                listener.onDataConfirmed(loco);
            }
        });

        findViewById(R.id.service_mode).setOnClickListener(new View.OnClickListener() {
                                                               @Override
                                                               public void onClick(View v) {
                                                                   ServiceModeDialog smd = new ServiceModeDialog(getContext(), loco, new ServiceModeDialog.OnDataConfirmedListener() {
                                                                       @Override
                                                                       public void onDataConfirmed(Loco l) {
                                                                           listener.onDataConfirmed(l);
                                                                       }
                                                                   });
                                                                   smd.show();
                                                                   dismiss();
                                                                   EventBus.getDefault().unregister(LocoDialog.this);
                                                               }
                                                           }
        );

        findViewById(R.id.choose).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                activity.startActivityForResult(Intent.createChooser(intent, null), PICK_IMAGE);
            }
        });
        EventBus.getDefault().register(this);
    }

    public void onEvent(UIMessage message) {
        if(UIMessage.Type.IMAGE_CHOSEN.equals(message.getType())) {
            Log.d("LocoDialog", "image chosen: " + loco.image);

            loco.image = message.getValue();
            ivImage.setImageURI(Uri.parse(loco.image));
        }
    }

}