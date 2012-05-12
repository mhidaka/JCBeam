package com.jcrom.jcbeam;

import com.jcrom.jcbeam.nfc.JcNfcData;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;

import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class NfcReceiveActivity extends Activity{

    public static final String TAG = "NfcReceiveActivity";
    private JcNfcData mReceiveInfo = null;
    private Context ctx;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog);

        ctx = this.getApplicationContext();

        actDialog();
    }

    private void actDialog() {
        Button okButton = (Button) findViewById(R.id.ok);
        okButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                if(mReceiveInfo != null){
                    Intent intent = new Intent(ctx, WiFiDirectActivity.class);
                    intent.putExtra("JcNfcData", mReceiveInfo);
                    startActivity(intent);
                    finish();
                }
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

    @Override
    public void onResume() {
        Log.i(TAG, "onResume");
        super.onResume();

        // Check to see that the Activity started due to an JC Beam
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(getIntent().getAction())) {
            mReceiveInfo = processIntent(getIntent());

            if(!mReceiveInfo.isEnable()){
                TextView tv = (TextView) findViewById(R.id.textView);

                tv.setText(R.string.need_prepare);

                Button okButton = (Button) findViewById(R.id.ok);
                okButton.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                            finish();
                    }
                });

                Button cancelButton = (Button) findViewById(R.id.cancel);
                cancelButton.setEnabled(false);
            }
        }
    }

    /**
     * Parses the NDEF Message from the intent
     */
    public JcNfcData processIntent(Intent intent) {
        Parcelable[] rawMsgs = intent
                .getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
        // only one message sent during the beam
        NdefMessage msg = (NdefMessage) rawMsgs[0];
        // record 0 contains the MIME type, record 1 is the AAR, if present

        String payload0 = new String(msg.getRecords()[0].getPayload());
        String payload1 = new String(msg.getRecords()[1].getPayload());
        String payload2 = new String(msg.getRecords()[2].getPayload());
        String payload3 = new String(msg.getRecords()[3].getPayload());
        //Toast.makeText(this, payload0 + payload1 + payload2, Toast.LENGTH_LONG).show();


        Log.i(TAG, "Parses the NDEF Message from the intent");
        Log.i(TAG, "payload0:" + payload0);
        Log.i(TAG, "payload1:" + payload1);
        Log.i(TAG, "payload2:" + payload2);
        Log.i(TAG, "payload3:" + payload3);

        return new JcNfcData(payload0, payload1, payload2, payload3.equals(JcNfcData.ENABLE) );
    }
}
