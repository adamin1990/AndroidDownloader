package com.adamin.androiddownloader;

import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.adamin.androiddownloader.multithread.DownLoadListener;
import com.adamin.androiddownloader.multithread.DownLoadService;
import com.adamin.androiddownloader.threadpoolexcutordemo.PriorityRunnable;

import java.io.File;

public class Main2Activity extends AppCompatActivity {
    private final String music1 = "http://cdn1.mydown.yesky.com/579eca4b/64171bb138738e3a0b3e9945fed34f0a/soft/201604/app-tianji-release-2016-04-13-1.0.9.apk";
    private ProgressBar progressBar;
    private TextView textView;
    private Button button;
    private DownLoadService downLoadService;
        private Handler handler=new UIHandler();
    private boolean ispause=true;
    private DownloadTask downloadTask;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        progressBar = (ProgressBar) findViewById(R.id.progress);
        textView = (TextView) findViewById(R.id.tv_progress);
        button = (Button) findViewById(R.id.btn_dwn);
        downloadTask=new DownloadTask(music1);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(ispause){
                    ispause=false;
                    if(downLoadService!=null){
                        downLoadService.isPause=false;

                    }
                    new Thread(downloadTask).start();
                    button.setText("暂停");
                }else {
                    ispause=true;
                    if(downLoadService!=null){
                        downLoadService.isPause=true;

                    }
                    button.setText("开始");
                }
            }
        });
    }


    class UIHandler extends Handler{
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    int downloaded_size = msg.getData().getInt("size");
                    progressBar.setProgress(downloaded_size);
                    int result = (int) ((float) downloaded_size / progressBar.getMax() * 100);
                    textView.setText(result + "%");
                    if (progressBar.getMax() == progressBar.getProgress()) {
                        Toast.makeText(getApplicationContext(), "下载完成", Toast.LENGTH_LONG).show();
                    }
                    break;
                case 2:
                    int progresslong=msg.arg1;
                    progressBar.setMax(progresslong);
                    break;
            }
        }

    }

    private final class DownloadTask implements Runnable {
        private String url;

        public DownloadTask(String url) {
            this.url = url;
        }

        @Override
        public void run() {
            try {
                File destinaton=Environment.getExternalStorageDirectory();
                downLoadService=new DownLoadService(url,destinaton,3,getApplicationContext());
                Message message=new Message();
                message.what=2;
                message.arg1=downLoadService.fileSize;
                handler.sendMessage(message);
                downLoadService.download(new DownLoadListener() {
                    @Override
                    public void onDownload(int downloaded_size) {
                        Message message = new Message();
                        message.what = 1;
                        message.getData().putInt("size", downloaded_size);
                        handler.sendMessage(message);
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

}
