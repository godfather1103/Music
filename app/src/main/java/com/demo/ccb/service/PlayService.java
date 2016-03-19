package com.demo.ccb.service;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.media.MediaPlayer.OnCompletionListener;

import com.demo.ccb.constant.APPMessage;

import java.io.IOException;

/**
 * Created by godfa on 2016/3/18.
 */
public class PlayService extends Service {
    private Thread thread = null;
    private MediaPlayer mediaPlayer = new MediaPlayer();       //媒体播放器对象
    private String path;                        //音乐文件路径
    private boolean isPause;                    //暂停状态
    Intent intent = new Intent();


    @Override
    public void onCreate() {
        super.onCreate();
        thread = new Thread(new Thread() {
            @Override
            public void run() {
                while (thread.isAlive()) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if (mediaPlayer.isPlaying()) {
                        intent.putExtra("OverMsg", APPMessage.PlayMsg.playtime);
                        intent.setAction("com.demo.ccb.service.PlayService");
                        sendBroadcast(intent);
                    }
                }
            }
        });
        thread.start();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        path = intent.getStringExtra("url");
        int msg = intent.getIntExtra("MSG", 0);
        if (msg == APPMessage.PlayMsg.play) {
            play(0);
        } else if (msg == APPMessage.PlayMsg.pause) {
            pause();
        } else if (msg == APPMessage.PlayMsg.stop) {
            stop();
        } else if (msg == APPMessage.PlayMsg.replay) {
            replay();
        }

        return super.onStartCommand(intent, flags, startId);
    }


    /**
     * 播放音乐
     *
     * @param position
     */
    private void play(int position) {
        try {
            mediaPlayer.reset();//把各项参数恢复到初始状态
            mediaPlayer.setDataSource(path);
            mediaPlayer.prepare();  //进行缓冲
            mediaPlayer.setOnPreparedListener(new PreparedListener(position));//注册一个监听器监听进度快进事件
            mediaPlayer.setOnCompletionListener(new CompletionListener());    //注册一个监听器监听播放完成事件
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 暂停音乐
     */
    private void pause() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            isPause = true;
        }
    }

    private void replay() {
        mediaPlayer.start();    //开始播放
    }

    /**
     * 停止音乐
     */
    private void stop() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            try {
                mediaPlayer.prepare(); // 在调用stop后如果需要再次通过start进行播放,需要之前调用prepare函数
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    @Override
    public void onDestroy() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
        }
    }


    /**
     * 实现一个OnPrepareLister接口,当音乐准备好的时候开始播放
     */
    private final class PreparedListener implements OnPreparedListener {
        private int positon;

        public PreparedListener(int positon) {
            this.positon = positon;
        }

        @Override
        public void onPrepared(MediaPlayer mp) {
            mediaPlayer.start();    //开始播放
            if (positon > 0) {    //如果音乐不是从头播放
                mediaPlayer.seekTo(positon);
            }
        }
    }

    /*
    * 实现一个OnCompletionListener接口，当音乐播放完毕时进行处理
    *
    * */

    private final class CompletionListener implements OnCompletionListener {

        @Override
        public void onCompletion(MediaPlayer mp) {

            intent.putExtra("OverMsg", APPMessage.PlayMsg.playover);
            intent.setAction("com.demo.ccb.service.PlayService");
            sendBroadcast(intent);
        }
    }

}
