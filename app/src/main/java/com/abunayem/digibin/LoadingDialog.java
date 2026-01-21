package com.abunayem.digibin;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.view.Window;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.core.content.ContextCompat;

public class LoadingDialog extends Dialog {
    private TextView messageTextView;

    public LoadingDialog(Context context) {
        super(context);
        initDialog(context, "Loading");
    }

    public LoadingDialog(Context context, String message) {
        super(context);
        initDialog(context, message);
    }

    private void initDialog(Context context, String message) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_loading);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        setCancelable(false);

        ProgressBar progressBar = findViewById(R.id.progressBar);
        progressBar.getIndeterminateDrawable().setColorFilter(
                ContextCompat.getColor(context, R.color.colorPrimary),
                PorterDuff.Mode.SRC_IN
        );


    }

    public void setMessage(String message) {
        if (messageTextView != null) {
            messageTextView.setText(message);
        }
    }
}