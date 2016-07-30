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
    private final String music1 = "http://yuedu.fm/static/file/pod/a5aa751914ff8b20b0ad25af23ca62ad.mp3";
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
            }
        }

    }

    private final class DownloadTask implements Runnable {

        public DownloadTask(final String target) throws Exception {
            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                final File destination = Environment.getExternalStorageDirectory();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            downLoadService = new DownLoadService(target, destination, 3, getApplicationContext());
                            progressBar.setMax(downLoadService.fileSize);

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }).start();

            } else {
                Toast.makeText(getApplicationContext(), "SD卡不存在或写保护!", Toast.LENGTH_LONG).show();
            }
        }

        @Override
        public void run() {
            try {
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
