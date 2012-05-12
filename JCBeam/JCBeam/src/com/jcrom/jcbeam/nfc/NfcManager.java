package com.jcrom.jcbeam.nfc;

import java.nio.charset.Charset;

import com.jcrom.jcbeam.R;
import com.jcrom.jcbeam.WiFiDirectActivity;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcEvent;
import android.nfc.NfcAdapter.CreateNdefMessageCallback;
import android.nfc.NfcAdapter.OnNdefPushCompleteCallback;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

public class NfcManager implements CreateNdefMessageCallback,OnNdefPushCompleteCallback {

    NfcAdapter mNfcAdapter;
    Activity mContext;

    private static final int MESSAGE_SENT = 1;
    private static final int MESSAGE_CANSEL = 2;

    public NfcManager(Activity context) {
        mContext = context;
        mNfcAdapter = NfcAdapter.getDefaultAdapter(context);
        if (mNfcAdapter != null) {
            // コールバックの追加
            mNfcAdapter.setNdefPushMessageCallback(this, context);
            mNfcAdapter.setOnNdefPushCompleteCallback(this, context);
        }
    }

    private JcNfcData mNfcData = null;

	public void createJCBeamMessage(JcNfcData data){
		mNfcData = data;
    }

    @Override
    public void onNdefPushComplete(NfcEvent arg0) {
        // A handler is needed to send messages to the activity when this
        // callback occurs, because it happens from a binder thread


        if(!mNfcData.isEnable()){
            mHandler.obtainMessage(MESSAGE_CANSEL).sendToTarget();
            return;
        }

        // TODO:ここで画面遷移よくない
        mHandler.obtainMessage(MESSAGE_SENT).sendToTarget();
        Intent intent = new Intent(mContext, WiFiDirectActivity.class);
        intent.putExtra("IsSender", true);
        intent.putExtra("FilePath", mNfcData.getFilePath());
        mContext.startActivity(intent);
    }

    /** This handler receives a message from onNdefPushComplete */
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            Resources res = mContext.getResources();

            switch (msg.what) {
            case MESSAGE_SENT:
                Toast.makeText(mContext, res.getString(R.string.toast_nfc_send),
                        Toast.LENGTH_LONG).show();
                break;

            case MESSAGE_CANSEL:
                Toast.makeText(mContext, res.getString(R.string.toast_nfc_cancel),
                        Toast.LENGTH_LONG).show();
                break;
            }
        }
    };

    @Override
    public NdefMessage createNdefMessage(NfcEvent event) {

        NdefMessage msg = new NdefMessage(new NdefRecord[] { createMimeRecord(
                "application/com.jcrom.jcbeam.nfc", mNfcData.getName().getBytes())
        /**
         * The Android Application Record (AAR) is commented out. When a device
         * receives a push with an AAR in it, the application specified in the
         * AAR is guaranteed to run. The AAR overrides the tag dispatch system.
         * You can add it back in to guarantee that this activity starts when
         * receiving a beamed message. For now, this code uses the tag dispatch
         * system.
         */
         //,NdefRecord.createApplicationRecord("org.techbooster.app.sharethemes")

         ,createMimeRecord(
                    "application/com.jcrom.jcbeam.nfc",mNfcData.getAddress().getBytes())
         ,createMimeRecord(
                    "application/com.jcrom.jcbeam.nfc",mNfcData.getFilePath().getBytes())
         ,createMimeRecord(
                    "application/com.jcrom.jcbeam.nfc",mNfcData.getAvailable().getBytes())
        }


                );
        return msg;
    }

    /**
     * Creates a custom MIME type encapsulated in an NDEF record
     *
     * @param mimeType
     */
    public NdefRecord createMimeRecord(String mimeType, byte[] payload) {
        byte[] mimeBytes = mimeType.getBytes(Charset.forName("US-ASCII"));
        NdefRecord mimeRecord = new NdefRecord(NdefRecord.TNF_MIME_MEDIA,
                mimeBytes, new byte[0], payload);
        return mimeRecord;
    }

}
