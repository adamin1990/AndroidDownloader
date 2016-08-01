package com.adamin.androiddownloader.multithread;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Adam on 2016/7/30.
 */
public class DownLoadService {
    public static final String TAG=DownLoadService.class.getSimpleName();

    private DbHelper dbHelper;
    public int fileSize;
    private int block;
    private File savedFile;
    private String path;
    public boolean isPause;
    private ThreadDownload [] threadDownloads;
    private Map<Integer,Integer> downloadedLength =new ConcurrentHashMap<>();

    public DownLoadService(String target, File destination, int thread_size, Context context) throws Exception {
        dbHelper=new DbHelper(context);
        this.threadDownloads=new ThreadDownload[thread_size];
        this.path=target;
        URL url=new URL(target);
        HttpURLConnection connection= (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(10000);
        if(connection.getResponseCode()!=200){
            throw new RuntimeException("服务器没有响应");
        }
        fileSize=connection.getContentLength();
        if(fileSize<0){
            throw new RuntimeException("文件不正确");
        }
        String fileName=getFileName(connection);
        if(!destination.exists()){
            destination.mkdirs();
        }
        this.savedFile=new File(destination,fileName);
        RandomAccessFile randomAccessFile=new RandomAccessFile(savedFile,"rwd");
        randomAccessFile.setLength(fileSize);
        randomAccessFile.close();
        connection.disconnect();
        this.block=fileSize%thread_size==0?fileSize/thread_size:fileSize/thread_size+1;
        downloadedLength=getDownLoadedLenth(path);
    }


    private String getFileName(HttpURLConnection conn) {
        String fileName = path.substring(path.lastIndexOf("/") + 1, path.length());
        if (fileName == null || "".equals(fileName.trim())) {
            String content_disposition = null;
            for (Map.Entry<String, List<String>> entry : conn.getHeaderFields().entrySet()) {
                if ("content-disposition".equalsIgnoreCase(entry.getKey())) {
                    content_disposition = entry.getValue().toString();
                }
            }
            try {
                Matcher matcher = Pattern.compile(".*filename=(.*)").matcher(content_disposition);
                if (matcher.find()) fileName = matcher.group(1);
            } catch (Exception e) {
                fileName = UUID.randomUUID().toString() + ".tmp"; // 默认名
            }
        }
        return fileName;
    }

    /**
     * 根据传入的下载地址 从数据库中获取下载信息
     * @param path  下载的地址
     * @return
     */
    private Map<Integer ,Integer> getDownLoadedLenth(String path){
        SQLiteDatabase database=dbHelper.getReadableDatabase();
        String sql="SELECT threadId,downloadLength FROM downloadInfo WHERE downPath=?";
    Cursor cursor= database.rawQuery(sql,new String[]{path});
        Map<Integer,Integer> data=new HashMap<>();
        while (cursor.moveToNext()){
            data.put(cursor.getInt(0),cursor.getInt(1));
        }
        database.close();
        return data;
    }

    public void download(DownLoadListener downLoadListener) throws Exception {
        this.deleteDownLoading();
        for(int i=0;i<threadDownloads.length;i++){
            threadDownloads[i]=new ThreadDownload(i,path,downloadedLength.get(i),block,savedFile,this);
            new Thread(threadDownloads[i]).start();
        }
        this.saveDownLoading(threadDownloads);
        while (!isFinish(threadDownloads)){
            Thread.sleep(900);
            if(downLoadListener!=null){
                downLoadListener.onDownload(getDownLoadSize(threadDownloads));
            }
            this.updateDownloading(threadDownloads);
        }
        if (!this.isPause) this.deleteDownLoading();// 完成下载之后删除本次下载记录
    }

    private void updateDownloading(ThreadDownload[] threadDownloads) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        try {
            db.beginTransaction();
            for (ThreadDownload thread : threadDownloads) {
                String sql = "UPDATE downloadInfo SET downloadLength=? WHERE threadId=? AND downPath=?";
                db.execSQL(sql, new String[] { thread.currentDownSize + "", thread.threadId + "", path });
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
            db.close();
        }
    }

    private int getDownLoadSize(ThreadDownload[] threadDownloads) {

        int sum = 0;
        for (int len = threadDownloads.length, i = 0; i < len; i++) {
            sum += threadDownloads[i].currentDownSize;
        }
        return sum;
    }

    private boolean isFinish(ThreadDownload[] threadDownloads) {
        try {
            for (int len = threadDownloads.length, i = 0; i < len; i++) {
                if (!threadDownloads[i].isFinished) {
                    return false;
                }
            }
            return true;
        } catch (Exception e) {
            return false;
        }

}

    private void saveDownLoading(ThreadDownload[] threadDownloads) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        try {
            db.beginTransaction();
            for (ThreadDownload thread : threadDownloads) {
                String sql = "INSERT INTO downloadInfo(downPath,threadId,downloadLength) values(?,?,?)";
                db.execSQL(sql, new Object[] { path, thread.threadId, 0 });
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
            db.close();
        }
    }

    private void deleteDownLoading() {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String sql = "DELETE FROM downloadInfo WHERE downPath=?";
        db.execSQL(sql, new Object[] { path });
        db.close();
    }
}
