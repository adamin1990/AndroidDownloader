package com.adamin.downloaderlibrary.domain;

import android.content.Context;
import android.os.Process;
import android.text.TextUtils;
import android.util.Log;

import com.adamin.downloaderlibrary.listener.DownLoadThreadListener;
import com.adamin.downloaderlibrary.utils.DownLoadUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.UUID;

/**
 * Created by Adam on 2016/8/3.
 */
public class DownLoadTask implements Runnable,DownLoadThreadListener {
    private static final String TAG=DownLoadTask.class.getSimpleName();

    private DownLoadInfo downLoadInfo;
    private Context context;

    private int totalProgess;
    private long lastTime=System.currentTimeMillis();
    private int count;


    public DownLoadTask(Context context, DownLoadInfo downLoadInfo) {
        this.context = context;
        this.downLoadInfo = downLoadInfo;
        this.totalProgess=downLoadInfo.currentsize;
        //如果不是再次启动 那么把文件信息写入数据库
        if(!downLoadInfo.isStop){

        }
    }

    @Override
    public synchronized  void onProgress(int progress) {
        totalProgess += progress;
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastTime > 1000) {
            Log.d(TAG, totalProgess + "");
            if (downLoadInfo.hasListener) downLoadInfo.listener.onProgress(totalProgess);
            lastTime = currentTime;
        }
    }

    @Override
    public void onStop(DownThreadInfo downThreadInfo) {
        if(null==downThreadInfo){
            //TODO 删除任务
            //TODO 删除任务信息
            if(downLoadInfo.hasListener){
                downLoadInfo.listener.onProgress(downLoadInfo.totalsize);
                downLoadInfo.listener.onStop(downLoadInfo.totalsize);
            }
            return;
        }
        // todo 更新线程信息
        count++;
        if(count>=downLoadInfo.downThreadInfos.size()){
            Log.d(TAG,"所有线程都停止了");
            downLoadInfo.currentsize=totalProgess;
            //todo 删除task
            //todo 更新task信息
            count=0;
            if(downLoadInfo.hasListener)downLoadInfo.listener.onStop(totalProgess);
        }


    }

    @Override
    public void onFinish(DownThreadInfo downThreadInfo) {
        if (null == downThreadInfo) {
            //todo  移除task
            //todo 删除 task
            if (downLoadInfo.hasListener) {
                downLoadInfo.listener.onProgress(downLoadInfo.totalsize);
                downLoadInfo.listener.onFinish(downLoadInfo.file);
            }
            return;
        }
        downLoadInfo.removeDownloadThread(downThreadInfo);
        //todo 删除线程信息
        Log.d(TAG, "Thread size " + downLoadInfo.downThreadInfos.size());
        if (downLoadInfo.downThreadInfos.isEmpty()) {
            Log.d(TAG, "Task was finished.");
            //todo 移除任务
            //todo 删除任务
            if (downLoadInfo.hasListener) {
                downLoadInfo.listener.onProgress(downLoadInfo.totalsize);
                downLoadInfo.listener.onFinish(downLoadInfo.file);
            }
            //todo 添加
        }
    }

    @Override
    public void run() {
        Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
        HttpURLConnection connection=null;
        try {
            URL url=new URL(downLoadInfo.downloadUrl);
            connection= (HttpURLConnection) url.openConnection();
            connection.setInstanceFollowRedirects(false);
            connection.setConnectTimeout(20000);
            connection.setReadTimeout(20000);
            final int code=connection.getResponseCode();
            switch (code){
                case 200:
                case 206:
                    downLoadInit(connection,code);
                    return;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void downLoadInit(HttpURLConnection connection, int code) throws IOException {
        final String transferEncoding=connection.getHeaderField("Transfer-Encoding");  //分块传输
        if(TextUtils.isEmpty(transferEncoding)){
            try {
                downLoadInfo.totalsize=Integer.parseInt(connection.getHeaderField("Content-Length"));
            } catch (NumberFormatException e) {
                downLoadInfo.totalsize=-1;
                e.printStackTrace();
            }
        }else {
            downLoadInfo.totalsize=-1;
        }

        if(downLoadInfo.totalsize==-1&&(TextUtils.isEmpty(transferEncoding))){
            throw new RuntimeException("无法获取下载文件大小");
        }
        if(TextUtils.isEmpty(downLoadInfo.fileName)){
            downLoadInfo.fileName= DownLoadUtil.onbatinFileName(downLoadInfo.downloadUrl);
        }

         if(!DownLoadUtil.createFile(downLoadInfo.downloadPath,downLoadInfo.fileName))
         {
             throw new RuntimeException("无法创建文件");
         }
        downLoadInfo.file=new File(downLoadInfo.downloadPath,downLoadInfo.fileName);
        if(downLoadInfo.file.exists()&&downLoadInfo.file.length()==downLoadInfo.totalsize){
            Log.d(TAG,"已经下载过改文件啦，无需下载咯");
            return;
            //Todo 新增设置选项，手动设置是否下载重名文件
        }
        if(downLoadInfo.hasListener) downLoadInfo.listener.onStart(downLoadInfo.fileName,downLoadInfo.downloadUrl,downLoadInfo.totalsize);
        switch (code){

            case 200:
                //不支持断点下载
                downloadData(connection);
                break;
            case 206:
                if(downLoadInfo.totalsize<=0){
                    downloadData(connection);
                    break;
                }
                if(downLoadInfo.isResume){
                    for(DownThreadInfo threadInfo: downLoadInfo.downThreadInfos){
                        //Todo 加入到线程池下载
                    }
                    break;
                }
                downLoadDispach();
                break;
        }

    }

    private void downLoadDispach() {
        int threadSize;
        int threadLength=DownLoadUtil.LENGTH_PER_THREAD;  //每条线程最大下载的长度
        if(downLoadInfo.totalsize<=DownLoadUtil.LENGTH_PER_THREAD){  //小于10M直接2条线程下载
            threadSize=2;
            threadLength=downLoadInfo.totalsize/threadSize;
        }else {
            threadSize=downLoadInfo.totalsize/DownLoadUtil.LENGTH_PER_THREAD;
        }
        int remainder=downLoadInfo.totalsize%threadLength;
        for(int i=0;i<threadSize;i++){
        int start=i*threadLength;
            int end=start+threadLength-1;
            if(i==threadSize-1){
                end=start+threadLength+remainder;
            }
            DownThreadInfo downThreadInfo=new DownThreadInfo(UUID.randomUUID().toString(),end,start,downLoadInfo.downloadUrl);
            downLoadInfo.addDownloadThread(downThreadInfo);
            //TODO 添加线程信息到数据库
            //TODO 添加新线程下载任务
        }
    }

    private void downloadData(HttpURLConnection connection) throws IOException {
        InputStream inputStream=connection.getInputStream();
        FileOutputStream outputStream=new FileOutputStream(downLoadInfo.file);
        byte bytes[] =new byte[1024];
        int len;
        while (!downLoadInfo.isStop&&(len=inputStream.read(bytes))!=-1){
            outputStream.write(bytes,0,len);
            onProgress(len);
        }
        if(!downLoadInfo.isStop){
            onFinish(null);
        }
        else {
            onStop(null);
        }

        outputStream.close();
        inputStream.close();
    }


}
