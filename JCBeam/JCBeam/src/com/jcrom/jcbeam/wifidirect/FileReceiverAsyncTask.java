package com.jcrom.jcbeam.wifidirect;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

import com.jcrom.jcbeam.WiFiDirectActivity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;

/**
 * A simple server socket that accepts connection and writes some data on
 * the stream.
 */
public class FileReceiverAsyncTask extends AsyncTask<Void, Void, String> {

    private static final String TAG = "FileReceiverAsyncTask";

    private static final int SOCKET_TIMEOUT = 60000;
    public static final String ACTION_SEND_FILE = "com.jcrom.jcbeam.SEND_FILE";
    public static final String EXTRAS_FILE_PATH = "file_url";
    public static final String EXTRAS_GROUP_OWNER_ADDRESS = "go_host";
    public static final String EXTRAS_GROUP_OWNER_PORT = "go_port";

    private WiFiDirectActivity activity = null;
    private ProgressDialog progressDialog = null;

    private String path;
    private String host;
    private Socket socket;
    private int port;


    /**
     * @param context
     * @param intentなのはintentServiceからの転用だから。手抜きでごめんね
     */
    public FileReceiverAsyncTask(WiFiDirectActivity activity, Intent intent) {
        this.activity = activity;

        path = intent.getExtras().getString(EXTRAS_FILE_PATH);
        host = intent.getExtras().getString(EXTRAS_GROUP_OWNER_ADDRESS);
        socket = new Socket();
        port = intent.getExtras().getInt(EXTRAS_GROUP_OWNER_PORT);
    }

    @Override
    protected String doInBackground(Void... params) {
        try {
            Log.d(TAG, "Opening client socket - ");
            socket.bind(null);
            socket.connect((new InetSocketAddress(host, port)), SOCKET_TIMEOUT);

            Log.d(TAG, "Client socket - " + socket.isConnected());

            File f = new File(path);

            File dirs = new File(f.getParent());
            if (!dirs.exists())
                dirs.mkdirs();
            f.createNewFile();

            Log.d(TAG, "Client: copying files " + f.toString());

            InputStream inputstream = socket.getInputStream();
            copyFile(inputstream, new FileOutputStream(f));

        }catch (IOException e) {
            Log.e(TAG, e.getMessage());
            return "IOException: FileError";
        }

        if (socket != null) {
           if (socket.isConnected()) {
                try {
                    socket.close();
                } catch (IOException e) {
                    // Give up
                    e.printStackTrace();
                    return "IOException: socket.close";
                }
            }
        }
        Log.d(TAG, "Client: Data written");

        return path;
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
                "テーマ受信中...", true, true,
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
            activity.obtainMessage(WiFiDirectActivity.MESSAGE_FINISH);
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

