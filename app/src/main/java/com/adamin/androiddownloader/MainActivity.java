package com.adamin.androiddownloader;

import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.adamin.androiddownloader.threadpoolexcutordemo.DefaultExecutorSupplier;

import org.jsoup.Connection;
import org.jsoup.Jsoup;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.ThreadPoolExecutor;

public class MainActivity extends AppCompatActivity {
    private ProgressBar progressBar1, progressBar2;

    private TextView tv1, tv2;

    private String music1 = "http://yuedu.fm/static/file/pod/a5aa751914ff8b20b0ad25af23ca62ad.mp3";
    private String music2 = "http://yuedu.fm/static/file/pod/f39095f0f63e1569386ee539762bf502.mp3";

    private String root = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator;

    private int hasRead = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        progressBar1 = (ProgressBar) findViewById(R.id.progress1);
        progressBar2 = (ProgressBar) findViewById(R.id.progress2);
        tv1 = (TextView) findViewById(R.id.tv1);
        tv2 = (TextView) findViewById(R.id.tv2);
        DefaultExecutorSupplier.getInstance().forBackgroundTasks()
                .execute(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Connection.Response res = Jsoup.connect("http://yuedu.fm/")
                                    .method(Connection.Method.GET)
                                    .execute();

                            String xsrf = res.cookie("_xsrf");
                            tv1.setText(xsrf + "");
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });

//        download(music1, root, progressBar1, tv1);
//        download(music2, root, progressBar2, tv2);
    }

    private void download(String music1, String root, ProgressBar progressBar1, TextView tv1) {
        DownLoadThread downLoadThread = new DownLoadThread(music1, root, progressBar1, tv1);
        downLoadThread.start();
    }

    public class DownLoadThread extends Thread {

        private String url, path;
        private ProgressBar progressBar;
        private TextView textView;
        private MyHandler handler;
        private int len = -1;
        private byte buffler[] = new byte[4 * 1024];
        private int size = 0;
        private int rate = 1;
        private int hasDownload = 0;
        private Message message;
        /**
         * 当前时间
         */
        private long curTime;


        public DownLoadThread(String url, String path, ProgressBar progressBar, TextView textView) {
            this.url = url;
            this.path = path;
            this.progressBar = progressBar;
            this.textView = textView;
            handler = new MyHandler(this.progressBar, this.textView);

        }

        @Override
        public void run() {

            String targetFileName = this.path + this.url.substring(url.lastIndexOf("/") + 1, url.length());
            File downloadFile = new File(targetFileName);
            if (!downloadFile.exists()) {
                try {
                    downloadFile.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            try {
                URL fileUrl = new URL(url);
                HttpURLConnection connection = (HttpURLConnection) fileUrl.openConnection();
                size = connection.getContentLength();

                InputStream is = connection.getInputStream();
                OutputStream outputStream = new FileOutputStream(targetFileName);
                long start = System.nanoTime();   //开始时间
                long totalRead = 0;  //总共下载了多少
                final double NANOS_PER_SECOND = 1000000000.0;  //1秒=10亿nanoseconds
                final double BYTES_PER_MIB = 1024 * 1024;    //1M=1024*1024byte
                while (((len = is.read(buffler, 0, 1024)) > 0)) {
                    totalRead += len;
                    double speed = NANOS_PER_SECOND / BYTES_PER_MIB * totalRead / (System.nanoTime() - start + 1);

                    outputStream.write(buffler);
                    Log.e("长度", "长度：" + len + "\n总长度： " + totalRead);
                    hasDownload += len;
                    rate = (hasDownload * 100 / size);
                    message = new Message();
                    message.arg1 = rate;
                    message.obj = speed;
                    handler.sendMessage(message);


                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    public class MyHandler extends Handler {
        private ProgressBar progressBar;
        private TextView textView;

        public MyHandler(ProgressBar progressBar, TextView textView) {
            this.progressBar = progressBar;
            this.textView = textView;
        }

        @Override
        public void handleMessage(Message msg) {
            double speed = (double) msg.obj;
            this.progressBar.setProgress(msg.arg1);
            this.textView.setText(msg.arg1 + "%" + "     " + "速度|：" + speed * 1024 + "kb/S");
//            Log.e("时间","时间是："+usetime);
            super.handleMessage(msg);
        }
    }
}
