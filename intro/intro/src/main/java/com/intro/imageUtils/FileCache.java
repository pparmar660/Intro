package com.intro.imageUtils;

import java.io.File;

import android.content.Context;

public class FileCache {

    private File cacheDir;
    String filename;

    public FileCache(Context context) {

        // Find the dir to save cached images
        if (android.os.Environment.getExternalStorageState().equals(
                android.os.Environment.MEDIA_MOUNTED))
            cacheDir = new File(
                    android.os.Environment.getExternalStorageDirectory(),
                    "/Picture/iNTRO/ImgCache");
        else
            cacheDir = context.getCacheDir();
        if (!cacheDir.exists())
            cacheDir.mkdirs();
    }

    public File getFile(String url) {
        filename = String.valueOf(url.hashCode());
        // String filename = URLEncoder.encode(url);
        File f = new File(cacheDir, filename);
        return f;
    }

    public void remove(String url) {
        //File[] files = cacheDir.listFiles();
        File fl = getFile(url);
        //if (files == null)
        //  return;

        // for (File f : files)
        if (fl.exists())
            fl.delete();
    }

    public void clear() {
        File[] files = cacheDir.listFiles();
        if (files == null)
            return;
        for (File f : files)
            f.delete();
    }
}