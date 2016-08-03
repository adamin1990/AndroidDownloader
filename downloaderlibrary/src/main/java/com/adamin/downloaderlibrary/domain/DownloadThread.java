package com.adamin.downloaderlibrary.domain;

import android.os.Process;

import com.adamin.downloaderlibrary.listener.DownLoadThreadListener;

import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by Adam on 2016/8/3.
 */
public class DownloadThread implements Runnable{
    private static final String TAG=DownloadThread.class.getSimpleName();

    private DownThreadInfo downThreadInfo;
    private DownLoadInfo downLoadInfo;
    private DownLoadThreadListener downLoadThreadListener;

    public DownloadThread(DownThreadInfo downThreadInfo, DownLoadInfo downLoadInfo, DownLoadThreadListener downLoadThreadListener) {
        this.downThreadInfo = downThreadInfo;
        this.downLoadInfo = downLoadInfo;
        this.downLoadThreadListener = downLoadThreadListener;
    }

    @Override
    public void run() {
        Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
        HttpURLConnection connection=null;
        RandomAccessFile randomAccessFile=null;
        InputStream inputStream=null;

        try {
            URL url=new URL(downLoadInfo.downloadUrl);
            connection= (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(20000);
            connection.setReadTimeout(20000);
            connection.setRequestProperty("Range","bytes="+downThreadInfo.start+"-"+downThreadInfo.end);
            randomAccessFile=new RandomAccessFile(downLoadInfo.file,"rwd");
            randomAccessFile.seek(downThreadInfo.start);
            inputStream=connection.getInputStream();
            byte bytes[]=new byte[1024];
            int len;
            while (!downThreadInfo.isStop&&((len=inputStream.read(bytes))!=-1)){
                downThreadInfo.start+=len;
                randomAccessFile.write(bytes,0,len);
                if(downLoadThreadListener!=null){
                    downLoadThreadListener.onProgress(len);
                }
            }

            if(downThreadInfo.isStop){
                downLoadThreadListener.onStop(downThreadInfo);
            }else {
                downLoadThreadListener.onFinish(downThreadInfo);
            }
        } catch (Exception e) {
            downLoadThreadListener.onStop(downThreadInfo);
            e.printStackTrace();
        }finally {

                try {
                    if(null!=inputStream) {
                        inputStream.close();
                    }
                    if(null!=randomAccessFile){
                        randomAccessFile.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            if(null!=connection) connection.disconnect();
        }


    }
}
