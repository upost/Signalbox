package de.ludetis.android.signalbox.model;

import android.app.Dialog;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;

import de.ludetis.android.signalbox.R;

/**
 * Created by uwe on 24.01.18.
 */

public class AboutDialog extends Dialog implements View.OnClickListener {
    private final Runnable export;

    public AboutDialog(@NonNull Context context, Runnable export) {
        super(context, android.R.style.Theme_Translucent_NoTitleBar_Fullscreen);
        this.export = export;
        setContentView(R.layout.dlg_about);
        findViewById(R.id.export).setOnClickListener(this);
        findViewById(R.id.okay).setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if(R.id.export==view.getId()) {
            export.run();
        }
        dismiss();
    }
}
