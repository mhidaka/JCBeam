package com.jcrom.jcbeam;

import java.io.File;

import com.jcrom.jcbeam.nfc.JcNfcData;
import com.jcrom.jcbeam.nfc.NfcManager;
import com.jcrom.jcbeam.nfc.SimpleWiFiDirectBroadcastReceiver;
import com.jcrom.jcbeam.theme.Theme;
import com.jcrom.jcbeam.theme.ThemePagerAdapter;
import com.jcrom.jcbeam.zip.CreateZipAsyncTask;
import com.jcrom.jcbeam.zip.PayloadFactory;
import com.jcrom.jcbeam.zip.UnzipAsyncTask;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.DialogInterface;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;

import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

public class NfcActivity extends Activity{

    public static final String TAG = "NfcActivity";
    NfcManager mNfcManager;

    private final IntentFilter intentFilter = new IntentFilter();
    private BroadcastReceiver receiver = null;
    private WifiP2pDevice mDevice = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //NFCのPush設定を行う
        mNfcManager = new NfcManager(this);

        //NFCで自分のデバイス情報を送るためWiFiDirectブロードキャストを受信する
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

    }

    public void initTheme() {
        setContentView(R.layout.theme_list);
        mTheme = new Theme();//TODO こいつはUIスレッドでファイルアクセスしてる･･･
        //TODO テーマの数が変わったらmPagerAdapterに通知しないといけないのにやってない。いまのところ再初期化で対応
        mPagerAdapter = new ThemePagerAdapter(this, mTheme);
        mPageChangeListener = new MyOnPageChangeListener();

        mViewPager = (ViewPager) findViewById(R.id.viewpager);
        mViewPager.setAdapter(mPagerAdapter);
        mViewPager.setOnPageChangeListener(mPageChangeListener);
    }

    @Override
    public void onResume() {
        Log.i(TAG, "onResume");
        super.onResume();

        receiver = new SimpleWiFiDirectBroadcastReceiver(this);
        registerReceiver(receiver, intentFilter);

        initTheme();
    }

    @Override
    public void onPause() {
        Log.i(TAG, "onPause");
        super.onPause();
        unregisterReceiver(receiver);
    }

    public static final int MENU_SELECT_ADD = 0;
    public static final int MENU_SELECT_SHARE = 1;
    public static final int MENU_SELECT_RELOAD = 2;

    public boolean onCreateOptionsMenu(Menu menu) {
        // メニューの要素を追加して取得
        MenuItem actionItem = null;

        actionItem = menu.add(0, MENU_SELECT_RELOAD, 0, "Theme Refresh");
        actionItem.setIcon(android.R.drawable.ic_popup_sync);
        actionItem.setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS
                | MenuItem.SHOW_AS_ACTION_WITH_TEXT);

        actionItem = menu.add(0, MENU_SELECT_ADD, 1, "Add Gallery");
        actionItem.setIcon(android.R.drawable.ic_menu_set_as);
        actionItem.setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS
                | MenuItem.SHOW_AS_ACTION_WITH_TEXT);

        actionItem = menu.add(0, MENU_SELECT_SHARE, 2, "Create Zip");
        actionItem.setIcon(android.R.drawable.ic_menu_send);
        actionItem.setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS
                | MenuItem.SHOW_AS_ACTION_WITH_TEXT);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch(item.getItemId()){

            case MENU_SELECT_RELOAD:
                initTheme();
                break;
            case MENU_SELECT_ADD:
                processUnzipPayload();
                break;

            case MENU_SELECT_SHARE:
                processCreatePayload();
                break;

            default:
                break;
        }


        return true;
    }


    private void processUnzipPayload() {

        int current = mPageChangeListener.getCurrentPage();
        String dirPath = mTheme.getThemePath(current);

        if(dirPath != null){
            File f = new File(dirPath);

            int msgId;

            if (!f.exists()) {
                // 実行確認
                msgId = R.string.create_unzip;
            } else {
                // 上書き確認
                msgId = R.string.overwrite_unzip;
            }

            AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
            alertDialog.setTitle(R.string.title_unzip);
            alertDialog.setMessage(msgId);
            alertDialog.setIcon(R.drawable.icon);
            alertDialog.setNegativeButton(android.R.string.no, null);
            alertDialog.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    // 展開処理
                    unzipPayload();
                }
            });

            alertDialog.create();
            alertDialog.show();
        }else{
            Resources res = getResources();
            Toast.makeText(this, res.getString(R.string.no_theme), Toast.LENGTH_SHORT).show();
        }
    }


    private void unzipPayload(){
        Log.i(TAG, "unzipPayload");
        int current = mPageChangeListener.getCurrentPage();

        String zipFilePath = PayloadFactory.getCompressFileName( mTheme.getThemePath(current));
        new UnzipAsyncTask(this,zipFilePath).execute();
    }



    private void processCreatePayload() {

        int current = mPageChangeListener.getCurrentPage();

        String name = mTheme.getThemePath(current);


        if(name != null){
            String zipFilePath = PayloadFactory.getCompressFileName(name);

            File f = new File(zipFilePath);

            int msgId;

            if (!f.exists()) {
                // 実行確認
                msgId = R.string.create_zip;
            } else {
                // 上書き確認
                msgId = R.string.overwrite_zip;
            }

            AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
            alertDialog.setTitle(R.string.title_zip);
            alertDialog.setMessage(msgId);
            alertDialog.setIcon(R.drawable.icon);
            alertDialog.setNegativeButton(android.R.string.no, null);
            alertDialog.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    // 圧縮処理
                    createPayload();
                }
            });

            alertDialog.create();
            alertDialog.show();
        }else{
            Resources res = getResources();
            Toast.makeText(this, res.getString(R.string.no_theme), Toast.LENGTH_SHORT).show();
        }
    }

    private void createPayload(){
        Log.i(TAG, "createPayload");
        int current = mPageChangeListener.getCurrentPage();
        String dirPath = mTheme.getThemePath(current);
        new CreateZipAsyncTask(this,dirPath).execute();
    }

    // 自分のWiFiデバイス情報の更新
    public void updateThisDevice(WifiP2pDevice device) {
        Log.i(TAG, "updateThisDevice");
        this.mDevice = device;

        updateJCBeamMessage();
    }

    /* Android Beam送信データ作成 */
    private void updateJCBeamMessage() {

        int current = mPageChangeListener.getCurrentPage();

        if(mTheme.length() == 0){
            return;
        }

        String zipFilePath = PayloadFactory
                .getCompressFileName( mTheme.getThemePath(current) );

        boolean beamAvailable = mTheme.isExistZip(current);

        if(mDevice != null ){
            Log.d(TAG, "updateJCBeamMessage:"+zipFilePath);
            JcNfcData nfcData = new JcNfcData(mDevice.deviceName, mDevice.deviceAddress, zipFilePath, beamAvailable );
            mNfcManager.createJCBeamMessage(nfcData);
        }
    }

    /* テーマ表示 */
    private Theme mTheme;
    private MyOnPageChangeListener mPageChangeListener;
    private ViewPager mViewPager;
    private ThemePagerAdapter mPagerAdapter;

    private class MyOnPageChangeListener implements ViewPager.OnPageChangeListener {

        private int currentPage;

        public int getCurrentPage() {
            return currentPage;
        }

        @Override
        public void onPageScrollStateChanged(int state)  {
            // スクロール中・停止などステータス通知
        }

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels){
            // スクロールしているページ情報
        }

        @Override
        public void onPageSelected(int position)  {
            // 現在表示している位置
            currentPage = position;
            updateJCBeamMessage();
        }
    }


}
