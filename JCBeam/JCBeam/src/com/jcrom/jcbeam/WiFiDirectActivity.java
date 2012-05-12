package com.jcrom.jcbeam;

import java.util.ArrayList;
import java.util.List;

import com.jcrom.jcbeam.nfc.JcNfcData;
import com.jcrom.jcbeam.wifidirect.FileReceiverAsyncTask;
import com.jcrom.jcbeam.wifidirect.FileSenderAsyncTask;
import com.jcrom.jcbeam.wifidirect.WiFiDirectBroadcastReceiver;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.ActionListener;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.net.wifi.p2p.WifiP2pManager.ConnectionInfoListener;
import android.net.wifi.p2p.WifiP2pManager.PeerListListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class WiFiDirectActivity extends Activity implements PeerListListener, ConnectionInfoListener {

    public static final String TAG = "WiFiDirect";
    private WifiP2pManager manager;
    private boolean isWifiP2pEnabled = false;

    private final IntentFilter intentFilter = new IntentFilter();
    private Channel channel;
    private BroadcastReceiver receiver = null;

    private Context mContext;
    private TextView mMessage = null;
    private boolean mIsSender = false;
    private JcNfcData mNfcInfo = null;
    private boolean mIsStarted = false;
    WifiP2pDevice mDevice = null;
    WakeLock wl = null;

    //TODO あとでデータ構造しっかりする
    private String sender_filePath = null;;

    public void setIsWifiP2pEnabled(boolean isWifiP2pEnabled) {
        this.isWifiP2pEnabled = isWifiP2pEnabled;

        Log.d(TAG, "isWifiP2pEnabled:"+isWifiP2pEnabled);
    }

    public void resetData() {
        //データリセット
    	if(mIsStarted == true){
    		disconnect();
    	}
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.wifitest);

        mContext = getApplicationContext();
        mMessage = (TextView) findViewById(R.id.textView1);

        mIsSender = getIntent().getBooleanExtra("IsSender", false);
        if(mIsSender){
            setMessage("送信側だよ。クライアント待ち...");
            sender_filePath = getIntent().getStringExtra("FilePath");
        }else{
            setMessage("受信側だよ");
            Bundle b =  getIntent().getExtras();
            mNfcInfo = (JcNfcData) b.get("JcNfcData");
        }

        forDebug();

        // add necessary intent values to be matched.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

        manager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        channel = manager.initialize(this, getMainLooper(), null);


        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wl = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "WiFiDirectActivity");
    }

    /** register the BroadcastReceiver with the intent values to be matched */
    @Override
    public void onResume() {
        Log.i(TAG, "onResume");
        super.onResume();
        receiver = new WiFiDirectBroadcastReceiver(manager, channel, this);
        registerReceiver(receiver, intentFilter);
        wl.acquire();
    }

    @Override
    public void onPause() {
        Log.i(TAG, "onPause");
        super.onPause();
        unregisterReceiver(receiver);
        wl.release();
    }




    /************************************************************************/

    public void openWiFiSetting() {
        AlertDialog.Builder alertDialog = null;

        if (isWifiP2pEnabled == false) {
            alertDialog = new AlertDialog.Builder(this);
            alertDialog.setTitle("WiFi Direct設定を開く");// タイトル
            alertDialog.setMessage("WiFi Direct設定が無効です。有効にするためSettingsを開きます");// 内容
            alertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {

                    Toast.makeText(mContext, "Wi-Fi Directのチェックボックスをオンにして有効にしてください", Toast.LENGTH_LONG).show();
                    startActivity(new Intent(Settings.ACTION_WIRELESS_SETTINGS));

                }
            });
            alertDialog.setCancelable(false);
            alertDialog.create();
            alertDialog.show();
        }
    }

    /************************************************************************/
    //PeerListListenerの実装
    private List<WifiP2pDevice> peers = new ArrayList<WifiP2pDevice>();
    private ProgressDialog progressDialog = null;

    /* Peer発見 */
    @Override
    public void onPeersAvailable(WifiP2pDeviceList peerList) {
        Log.i(TAG, "onPeersAvailable");

        // TODO 自動生成されたメソッド・スタブ
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
        peers.clear();
        peers.addAll(peerList.getDeviceList());
        if (peers.size() == 0) {
            Log.d(TAG, "No devices found");
            setMessage("相手が見つかりません。JCBeamしなおしてください。");
            return;
        }

        for(int i=0 ; i<peers.size(); i++){
            WifiP2pDevice device = peers.get(i);
            Log.d(TAG, "peer("+ i +"): " + device.deviceName);
            Log.d(TAG, "peer("+ i +"): " + device.deviceAddress);
            Log.d(TAG, "peer("+ i +"): " + Integer.toString(device.status) );

            if (mNfcInfo != null){
            	if(mNfcInfo.getAddress().equals(device.deviceAddress)) {
            		mDevice = device;
            		this.obtainMessage(MESSAGE_CONNECT);
            	}
            }
        }
    }

    /* Peer検索開始時に表示するダイアログ */
    private void onInitiateDiscovery() {

        Log.i(TAG, "onInitiateDiscovery");

        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
        progressDialog = ProgressDialog.show(this, "Press back to cancel", "finding peers", true,
                true, new DialogInterface.OnCancelListener() {

                    public void onCancel(DialogInterface dialog) {

                    }
                });
        progressDialog.show();
        manager.discoverPeers(channel, new WifiP2pManager.ActionListener() {

            public void onSuccess() {
                setMessage("相手を検索中...");
                Log.d(TAG, "Discovery Success");
            }

            public void onFailure(int reasonCode) {
                setMessage("検索に失敗しました... ErrCode:"+reasonCode);
                Log.d(TAG, "Discovery Failed : " + reasonCode);
            }
        });
    }
    /************************************************************************/
    // ConnectionInfoListenerの実装
    @Override
    public void onConnectionInfoAvailable(WifiP2pInfo info) {

        Log.i(TAG, "onConnectionInfoAvailable");
        Log.d(TAG, "info.isGroupOwner: " + info.isGroupOwner);
        Log.d(TAG, "Group Owner IP   : " + info.groupOwnerAddress.getHostAddress() );


        // After the group negotiation, we assign the group owner as the file
        // server. The file server is single threaded, single connection server
        // socket.
        //if (info.groupFormed && info.isGroupOwner) {
        if (info.groupFormed && mIsSender) {
            Log.d(TAG, "I am Sender");
            processSender();
        } else if (info.groupFormed) {
            Log.d(TAG, "I am Receiver");
            processReceiver(info);
        }else{
        	// do nothing
        }
    }

	private void processReceiver(WifiP2pInfo info) {
		setMessage("テーマを受信しています...");
		Intent intent = new Intent(this, FileReceiverAsyncTask.class);
		intent.setAction(FileReceiverAsyncTask.ACTION_SEND_FILE);
		intent.putExtra(FileReceiverAsyncTask.EXTRAS_FILE_PATH, mNfcInfo.getFilePath() );
		intent.putExtra(FileReceiverAsyncTask.EXTRAS_GROUP_OWNER_ADDRESS,
		        info.groupOwnerAddress.getHostAddress());
		intent.putExtra(FileReceiverAsyncTask.EXTRAS_GROUP_OWNER_PORT, 8988);

		new FileReceiverAsyncTask(this, intent).execute();
	}

	private void processSender() {
		setMessage("テーマを送信しています...");
		new FileSenderAsyncTask(this, sender_filePath).execute();
	}


    public void connect(WifiP2pConfig config) {
        Log.i(TAG, "connect");
        setMessage("相手と接続中...");
        manager.connect(channel, config, new ActionListener() {

            public void onSuccess() {
                // WiFiDirectBroadcastReceiver will notify us. Ignore for now.
            }

            public void onFailure(int reasonCode) {
                Log.d(TAG, "Connect failed. Reason :" + reasonCode);
                Toast.makeText(WiFiDirectActivity.this, "Connect failed. Retry.",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void disconnect() {
        Log.i(TAG, "disconnect");
        manager.removeGroup(channel, new ActionListener() {

            public void onSuccess() {
                // WiFiDirectBroadcastReceiver will notify us. Ignore for now.
            }

            public void onFailure(int reasonCode) {
                Log.d(TAG, "Disconnect failed. Reason :" + reasonCode);
                setMessage("切断処理に失敗しました。Wi-Fi Directを一度OFFにしてください");
            }


        });
    }

    private void startSearch(){
        if(!mIsSender && !mIsStarted){
            //受信側で処理を開始していない場合、相手を自動的に検索する
            onInitiateDiscovery();
        }
     }

    private void startConnect() {
        if (!mIsSender && !mIsStarted) {
            // 相手側アドレスが判明したので接続処理を開始
            WifiP2pConfig config = new WifiP2pConfig();
            config.deviceAddress = mDevice.deviceAddress;
            config.wps.setup = WpsInfo.PBC;

            connect(config);
            mIsStarted = true;
        }
    }

    private void startFinish() {
    	disconnect();
        Toast.makeText(WiFiDirectActivity.this, "テーマ交換が完了しました",
                Toast.LENGTH_SHORT).show();

    	finish();
    }

    public static final int MESSAGE_SEARCH = 1;
    public static final int MESSAGE_CONNECT = 2;
    public static final int MESSAGE_FINISH = 3;

    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case MESSAGE_SEARCH:
                startSearch();
                break;
            case MESSAGE_CONNECT:
                startConnect();
                break;
            case MESSAGE_FINISH:
                startFinish();
                break;
            }
        }
    };

    public void obtainMessage(int message){
        mHandler.obtainMessage(message).sendToTarget();
    }

    private void setMessage(String msg){
        mMessage.setText(msg);
    }

    private void forDebug() {

        Button testButton = (Button) findViewById(R.id.button1);
        testButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (!isWifiP2pEnabled) {
                    Toast.makeText(WiFiDirectActivity.this, R.string.p2p_off_warning,
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                onInitiateDiscovery();
            }
        });

        testButton = (Button) findViewById(R.id.button2);
        testButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                WifiP2pConfig config = new WifiP2pConfig();
                config.deviceAddress = mDevice.deviceAddress;
                config.wps.setup = WpsInfo.PBC;
                connect(config);
            }
        });

        testButton = (Button) findViewById(R.id.button3);
        testButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                disconnect();
            }
        });
    }

}
