
package com.jcrom.jcbeam.zip;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

// 引用:http://d.hatena.ne.jp/koichiarchi/20060828/1156758449

public class PayloadFactory {

    private String targetDirPath;

    private static final String EXTENSION_ZIP = ".zip";

    public PayloadFactory() {
    }

    /**
     * 指定ディレクトリをZIP形式に圧縮します。
     *
     * @param directoryPath 圧縮対象ディレクトリ(フルパス指定)
     * @return 圧縮ファイル名(フルパス)
     * @throws IOException 入出力関連エラーが発生した場合
     */
    public String CreatePayloadData(String directoryPath) throws IOException {

        String zipFilePath;
        targetDirPath = directoryPath;

        // ディレクトリ存在チェック
        File targetDirectory = new File(directoryPath);
        if (!targetDirectory.isDirectory()) {
            throw new FileNotFoundException("Directory not found:" + directoryPath);
        }

        // 指定ディレクトリ直下のファイル一覧を取得
        List<File> rootFiles = Arrays.asList(targetDirectory.listFiles());

        // 圧縮先ファイルへのストリームを開く
        zipFilePath = getCompressFileName(directoryPath);
        ZipOutputStream out = new ZipOutputStream(new FileOutputStream(zipFilePath));

        // ディレクトリ自体を書き込む
        putEntryDirectory(out, targetDirectory);

        // ファイルリスト分の圧縮処理
        compress(out, rootFiles);

        // 出力ストリームを閉じる
        out.flush();
        out.close();

        return zipFilePath;
    }

    private void compress(ZipOutputStream out, List<File> fileList) throws IOException {

        // ファイルリスト分の圧縮処理
        for (File file : fileList) {

            if (file.isFile()) {

                // ファイル書き込み
                putEntryFile(out, file);

            } else {

                // ディレクトリ自体を書き込む
                putEntryDirectory(out, file);

                // ディレクトリ内のファイルについては再帰的に本メソッドを処理する
                List<File> inFiles = Arrays.asList(file.listFiles());
                compress(out, inFiles);

            }

        }

    }

    private void putEntryFile(ZipOutputStream out, File file) throws FileNotFoundException,
            IOException {

        byte[] buf = new byte[128];

        // 圧縮元ファイルへのストリームを開く
        BufferedInputStream in = new BufferedInputStream(new FileInputStream(file));

        // エントリを作成する
        ZipEntry entry = new ZipEntry(getZipEntryName(file.getPath()));
        out.putNextEntry(entry);

        // データを圧縮して書き込む
        int size;
        while ((size = in.read(buf, 0, buf.length)) != -1) {
            out.write(buf, 0, size);
        }

        // エントリと入力ストリームを閉じる
        out.closeEntry();
        in.close();

    }

    private void putEntryDirectory(ZipOutputStream out, File file) throws IOException {

        ZipEntry entry = new ZipEntry(getZipEntryName(file.getPath()) + "/");
        entry.setSize(0);
        out.putNextEntry(entry);

    }

    private String getZipEntryName(String filePath) {

        String parantPath = (new File(targetDirPath)).getParent();
        parantPath = removeLastSeparator(parantPath);
        return filePath.substring(parantPath.length() + 1);

    }

    public static String getDirectoryName(String zipFileName){

    	int index = zipFileName.lastIndexOf(EXTENSION_ZIP);
		return zipFileName.substring(0, index);
    }

    public static String getCompressFileName(String fileName) {

        int tmp1 = fileName.lastIndexOf("\\");
        int tmp2 = fileName.lastIndexOf("/");

        int sepIndex = tmp1 > tmp2 ? tmp1 : tmp2;
        int extIndex = fileName.lastIndexOf(".");

        String zipName;
        if (sepIndex >= extIndex) {
            zipName = fileName + EXTENSION_ZIP;
        } else {
            zipName = fileName.substring(0, extIndex) + EXTENSION_ZIP;
        }
        return zipName;

    }

    private String removeLastSeparator(String path) {
        String separator = System.getProperty("file.separator");
        if (!path.endsWith(separator)) {
            return path;
        }
        return path.substring(0, path.length() - 1);
    }

}
