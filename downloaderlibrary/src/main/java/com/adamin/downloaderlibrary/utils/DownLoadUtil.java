package com.adamin.downloaderlibrary.utils;

import java.io.File;
import java.io.IOException;

/**
 * Created by Adam on 2016/8/3.
 */
public class DownLoadUtil {
   public static final int LENGTH_PER_THREAD = 10485760;  //10M

    public static String onbatinFileName(String url){
        int index=url.lastIndexOf(".");
        if(index>0){
          return   url.substring(index);
        }else {
            return "temp.temp";
        }

    }

    /**
     *
     * @param path
     * @param fileName
     * @return
     */
  public   static synchronized boolean createFile(String path, String fileName) {
        boolean hasFile = false;
        try {
            File dir = new File(path);
            boolean hasDir = dir.exists() || dir.mkdirs();
            if (hasDir) {
                File file = new File(dir, fileName);
                hasFile = file.exists() || file.createNewFile();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return hasFile;
    }
}
