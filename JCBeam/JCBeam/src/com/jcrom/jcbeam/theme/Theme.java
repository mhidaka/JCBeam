package com.jcrom.jcbeam.theme;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import android.os.Environment;

import com.jcrom.jcbeam.zip.PayloadFactory;

public class Theme {

    private static final String THEMES_DIRECTORY = "/mnt/sdcard/mytheme";
    private static final String THEMES_ZIP = ".zip";

    private ArrayList<String> dir = null;
    private ArrayList<String> zip = null;
    private ArrayList<String> themes = null;

    public Theme(){

        dir = new ArrayList<String>();
        zip = new ArrayList<String>();
        themes = new ArrayList<String>();

        searchThemes();
    }

    private void searchThemes() {

        //ディレクトリを探す
        File d = new File(THEMES_DIRECTORY);
        if( !d.exists() ) return;
        List<File>f = Arrays.asList(d.listFiles());

        for(int i=0;i< f.size(); i++){

            File target = f.get(i);
            String name = target.getName();

            if(target.isDirectory()){
                dir.add(name);
                //ディレクトリは無条件に追加
                themes.add(name);
            }else{

                if( 0 <= name.lastIndexOf(THEMES_ZIP) ){
                    //TODO zipファイルなら、カウントしておく。だけど手抜き。
                    zip.add(name);
                }
            }
        }

        //dirとzipで重複を調べる
        for(int i=0;i< zip.size(); i++){
            String target = zip.get(i);
            boolean isExist = false;

            for(int j=0;j< dir.size(); j++){
                String name = dir.get(j) + THEMES_ZIP;

                if(name.equals(target)){
                    isExist =true;
                }
            }

            if(!isExist){
                //Zipファイルがどこにも存在してないものであれば
                String theme = PayloadFactory.getDirectoryName(target);
                themes.add(theme);
            }
        }

        //ソートして綺麗に
        Collections.sort(themes);

    }


    public int length() {
        return themes.size();
    }

    public String name(int pos) {
        return themes.get(pos);
    }

    public String getThemePath(int pos) {

        if(themes.size() == 0){
            return null;
        }

        return THEMES_DIRECTORY + "/" + themes.get(pos);
    }

    public String getThemeImage(int pos) {

        String wallpaper = THEMES_DIRECTORY + "/" + themes.get(pos) + "/launcher/launcher_wallpaper.png";

        File f = new File(wallpaper);

        if( f.exists() ){
            return wallpaper;
        }
        return null;
    }

    public boolean isExistZip(int pos) {

        String path = THEMES_DIRECTORY + "/" + themes.get(pos) + THEMES_ZIP;

        File f = new File(path);

        if( f.exists() ){
            return true;
        }
        return false;
    }

    public static void moveThemeZip(String zipPath) {

        File zip = new File(zipPath);
        File dir = new File(Environment.getExternalStorageDirectory() + "/mytheme");

        if (!dir.exists())
            dir.mkdir();

        zip.renameTo(new File(dir, zip.getName()));

        return;
    }
}