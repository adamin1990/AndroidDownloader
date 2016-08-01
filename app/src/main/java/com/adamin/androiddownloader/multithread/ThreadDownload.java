package com.adamin.androiddownloader.multithread;

import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by Adam on 2016/7/30.
 */
public class ThreadDownload implements Runnable {
    public int threadId;   //从0开始
    private RandomAccessFile randomAccessFile;
    private String path;
    public int currentDownSize =0;  //当前下载量
    public  boolean isFinished;
  private DownLoadService downLoadService;
    public int start;
    public int end;

    public ThreadDownload(int threadId, String path, Integer havedownload, int block, File saveFile,DownLoadService downLoadService) throws FileNotFoundException {
        this.threadId = threadId;
        this.path = path;
        if(havedownload != null){
            this.currentDownSize = havedownload;

        }
        this.downLoadService=downLoadService;
        this.randomAccessFile=new RandomAccessFile(saveFile,"rwd");
        start=threadId*block+currentDownSize;
        end=(threadId+1)*block;
    }

    @Override
    public void run() {
        try {
            URL url=new URL(path);
            HttpURLConnection connection= (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(10000);
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Range","bytes="+start+"-"+end);
            InputStream inputStream=connection.getInputStream();
            byte [] buffer=new byte[1024];
            int len=0;
            randomAccessFile.seek(start);
            while ((len=inputStream.read(buffer))!=-1){
                randomAccessFile.write(buffer,0,len);
                currentDownSize+=len;
            }
            randomAccessFile.close();
            inputStream.close();
            connection.disconnect();
            if (!downLoadService.isPause) Log.i(DownLoadService.TAG, "Thread " + (this.threadId + 1) + "finished");
            isFinished=true;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("文件下载失败"+e.getMessage());
        }

    }
}
