package com.example.steven.sjtu_lib_v2.dialog;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;

import com.example.steven.sjtu_lib_v2.R;

/**
 * Created by steven on 2016/2/15.
 */
public class LoadingDialog extends ProgressDialog{

    public LoadingDialog(Context context) {
        super(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.loading_dialog);
    }
}
