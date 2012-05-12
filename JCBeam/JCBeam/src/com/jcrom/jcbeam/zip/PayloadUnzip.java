
package com.jcrom.jcbeam.zip;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import android.os.Environment;
import android.util.Log;

public class PayloadUnzip {

    public String Unzip(String zipFilePath) throws FileNotFoundException {

        File externalStoragePath = Environment.getExternalStorageDirectory();

        ZipInputStream in;
        try {
            in = new ZipInputStream(new BufferedInputStream(new FileInputStream(zipFilePath)));
        } catch (FileNotFoundException e) {
            // TODO 自動生成された catch ブロック
            throw new FileNotFoundException("File not found:" + zipFilePath);
        }

        ZipEntry zipEntry;

        String parentPath = externalStoragePath + "/mytheme/";
        final File parent = new File(parentPath);
        parent.mkdirs();

        try {
            while ((zipEntry = in.getNextEntry()) != null) {

                final File file = new File(parentPath, zipEntry.getName());

                if (zipEntry.isDirectory()) {
                    // 解凍対象がディレクトリ
                    file.mkdirs();
                } else {
                    // 解凍対象がファイル

                    // 出力用ファイルストリームの生成
                    BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(file));

                    // エントリの内容を出力
                    int length;
                    int writeSize = 0;
                    byte []buffer = new byte[4096];

                    while ((length = in.read(buffer)) != -1) {
                        out.write(buffer,0,length);
                        writeSize += length;
                    }

                    out.flush();
                    out.close();

                    Log.d("File","name:" + file.toString() + " size:" + file.length() + " writeSize:" + writeSize );
                }
            }
        } catch (IOException e) {
            // TODO 自動生成された catch ブロック
            e.printStackTrace();
        }
        return zipFilePath;
    }
}
