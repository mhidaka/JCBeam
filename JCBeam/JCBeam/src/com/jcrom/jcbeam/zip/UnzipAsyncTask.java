
package com.jcrom.jcbeam.zip;

import java.io.FileNotFoundException;

import com.jcrom.jcbeam.NfcActivity;
import com.jcrom.jcbeam.R;

import android.app.ProgressDialog;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

public class UnzipAsyncTask extends AsyncTask<Void, Void, String> {

    private String path = null;

    private NfcActivity activity = null;

    private ProgressDialog progressDialog = null;

    /**
     * @param activity
     * @param path
     */
    public UnzipAsyncTask(NfcActivity activity, String path) {
        this.activity = activity;
        this.path = path;
    }

    @Override
    protected String doInBackground(Void... params) {

        Log.d("UnzipAsyncTask", "Unzip Start");
        String filePath = null;

        PayloadUnzip payload = new PayloadUnzip();
        try {
            filePath = payload.Unzip(path);
        } catch (FileNotFoundException e) {
            // TODO 自動生成された catch ブロック
            e.printStackTrace();
        }

        Log.d("UnzipAsyncTask", "Unzip End");

        return filePath;

    }

    /*
     * android.os.AsyncTask#onPreExecute() 前処理
     */
    @Override
    protected void onPreExecute() {

        Resources res = activity.getResources();
        String title = res.getString(R.string.dialog_unzip);
        String msg = res.getString(R.string.exec_unzip);

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
        Toast.makeText(activity, res.getString(R.string.complate_unzip), Toast.LENGTH_LONG).show();
        activity.initTheme();//TODO 初期化だがViewPagerの一部だけでもよかった
    }
}
