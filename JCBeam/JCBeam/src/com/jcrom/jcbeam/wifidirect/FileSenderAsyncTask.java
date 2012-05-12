package com.jcrom.jcbeam.wifidirect;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import com.jcrom.jcbeam.WiFiDirectActivity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.util.Log;

/**
 * A simple server socket that accepts connection and writes some data on
 * the stream.
 */
public class FileSenderAsyncTask extends AsyncTask<Void, Void, String> {

    public static final String TAG = "FileSenderAsyncTask";
    private String filePath = null;
    private WiFiDirectActivity activity = null;
    private ProgressDialog progressDialog = null;
    private ServerSocket serverSocket;
    /**
     * @param context
     * @param statusText
     */
    public FileSenderAsyncTask(WiFiDirectActivity activity, String path) {
        this.filePath = path;
        this.activity = activity;
    }

    @Override
    protected String doInBackground(Void... params) {
        try {
            serverSocket = new ServerSocket(8988);
            Log.d(TAG, "Server: Socket opened");
            Socket client = serverSocket.accept();
            Log.d(TAG, "Server: connection done");

            File f = new File(filePath);

            Log.d(TAG, "Server socket - " + client.isConnected());
            OutputStream stream = client.getOutputStream();
            InputStream is = null;
            try {
                is = new FileInputStream(f);
            } catch (FileNotFoundException e) {
                Log.d(TAG, e.toString());
            }
            copyFile(is, stream);
            serverSocket.close();
            Log.d(TAG, "Server: Data written");
            activity.obtainMessage(WiFiDirectActivity.MESSAGE_FINISH);
            return f.getAbsolutePath();

        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
            if(!serverSocket.isClosed()){
                try {
                    serverSocket.close();
                } catch (IOException e1) {
                    // Give Up
                    e1.printStackTrace();
                }
            }
            return "IOException";
        }
    }
    /*
     * android.os.AsyncTask#onPreExecute() 転送前処理
     */
    @Override
    protected void onPreExecute() {
        Log.d(TAG, "Opening a server socket");

        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
        progressDialog = ProgressDialog.show(activity, "Press back to cancel",
                "テーマ送信中...", true, true,
                  new DialogInterface.OnCancelListener() {

                      @Override
                      public void onCancel(DialogInterface dialog) {
                          activity.disconnect();
                      }
                  }
                );
        progressDialog.show();

    }

    /*
     * (non-Javadoc)
     * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
     */
    @Override
    protected void onPostExecute(String result) {
        if (result != null) {
            Log.d(TAG, "File copied - " + result);
            if (progressDialog != null && progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
        }
    }

    public static boolean copyFile(InputStream inputStream, OutputStream out) {
        byte buf[] = new byte[1024];
        int len;
        try {
            while ((len = inputStream.read(buf)) != -1) {
                out.write(buf, 0, len);

            }
            out.close();
            inputStream.close();
        } catch (IOException e) {
            Log.d(TAG, e.toString());
            return false;
        }
        return true;
    }
}

