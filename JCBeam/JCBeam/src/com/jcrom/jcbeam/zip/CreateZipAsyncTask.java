
package com.jcrom.jcbeam.zip;

import java.io.IOException;

import com.jcrom.jcbeam.NfcActivity;
import com.jcrom.jcbeam.R;

import android.app.ProgressDialog;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

public class CreateZipAsyncTask extends AsyncTask<Void, Void, String> {

    private String path = null;

    private NfcActivity activity = null;

    private ProgressDialog progressDialog = null;

    /**
     * @param activity
     * @param path
     */
    public CreateZipAsyncTask(NfcActivity activity, String path) {
        this.activity = activity;
        this.path = path;
    }

    @Override
    protected String doInBackground(Void... params) {

        Log.d("CreateZipAsyncTask", "Create Start");
        PayloadFactory factory = new PayloadFactory();
        String filePath = null;

        try {
            filePath = factory.CreatePayloadData(path);
        } catch (IOException e) {
            // TODO 自動生成された catch ブロック
            e.printStackTrace();
        }
        Log.d("CreateZipAsyncTask", "Create End");

        return filePath;

    }

    /*
     * android.os.AsyncTask#onPreExecute() 前処理
     */
    @Override
    protected void onPreExecute() {

        Resources res = activity.getResources();
        String title = res.getString(R.string.dialog_zip);
        String msg = res.getString(R.string.exec_zip);

        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
        progressDialog = ProgressDialog.show(activity, title, msg, true, false, null);
        progressDialog.show();

    }

    /*
     * android.os.AsyncTask#onPostExecute() 後処理
     */
    @Override
    protected void onPostExecute(String result) {
        if (result != null) {
            if (progressDialog != null && progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
        }
        Resources res = activity.getResources();
        Toast.makeText(activity, res.getString(R.string.complate_zip), Toast.LENGTH_LONG).show();
        activity.initTheme();//TODO 初期化だがViewPagerの一部だけでもよかった
    }
}
