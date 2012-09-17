package com.jcrom.jcbeam;

import com.jcrom.jcbeam.theme.Theme;

import android.app.Activity;
import android.content.Intent;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class NfcReceiveActivity extends Activity{

    public static final String TAG = "NfcReceiveActivity";


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog);
    }

    @Override
    public void onResume() {
        Log.i(TAG, "onResume");
        super.onResume();

        String action = getIntent().getAction();

        if(Intent.ACTION_VIEW.equals(action)){

            Intent i = getIntent();
            final String path = i.getData().getEncodedPath();

            Button okButton = (Button) findViewById(R.id.ok);
                okButton.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Theme.moveThemeZip(path);

                        String className = NfcActivity.class.getCanonicalName();
                        String packageName = getApplication().getPackageName();
                        Intent intent = new Intent();
                        intent.setClassName(packageName, className);
                        startActivity(intent);

                        finish();
                    }
                });

            Button cancelButton = (Button) findViewById(R.id.cancel);
            cancelButton.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        finish();
                    }
                });
        }
    }
}
