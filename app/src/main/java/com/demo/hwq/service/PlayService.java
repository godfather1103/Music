package com.demo.hwq.service;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.media.MediaPlayer.OnCompletionListener;
import android.util.Log;
import android.view.animation.AnimationUtils;

import com.demo.hwq.constant.APPMessage;
import com.demo.hwq.util.LrcUtil;
import com.demo.hwq.util.MusicAppUtil;
import com.demo.hwq.vo.LrcContent;
import com.demo.hwq.vo.MusicInfo;

import java.util.ArrayList;
import java.util.List;

import ccb.demo.com.studio.MainActivity;
import ccb.demo.com.studio.R;


/**
 * Created by godfa on 2016/3/18.
 */
public class PlayService extends Service {
    private Thread thread = null;
    private final MediaPlayer mediaPlayer = new MediaPlayer();       //媒体播放器对象
    private String path;                        //音乐文件路径
    private boolean isPause;                    //暂停状态
    private final Intent intent = new Intent();
    private List<MusicInfo> mp3list = null;
    private MusicInfo music;

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
    public void onDestroy() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        path = intent.getStringExtra("url");
        int msg = intent.getIntExtra("MSG", -1);
        music = intent.getParcelableExtra("music");
        if (msg == APPMessage.PlayMsg.play) {
            play(0);
        } else if (msg == APPMessage.PlayMsg.pause) {
            pause();
        } else if (msg == APPMessage.PlayMsg.stop) {
            stop();
        } else if (msg == APPMessage.PlayMsg.replay) {
            replay();
        } else if (msg == APPMessage.PlayMsg.beginSearch) {
            String key = intent.getStringExtra("key");
            searchNetSong(key);
        } else if (msg == APPMessage.NetPlayMsg.download) {
            String title = intent.getStringExtra("title");
            String artist = intent.getStringExtra("artist");
            MusicInfo music = intent.getParcelableExtra("music");
            NetWork net = new NetWork();
            net.doFlag = APPMessage.NetPlayMsg.download;
            net.title = artist + "-" + title;
            net.music = music;
            new Thread(net).start();
        } else if (msg == APPMessage.PlayMsg.seekToHead && mediaPlayer.isPlaying()) {
            int currentTime = mediaPlayer.getCurrentPosition();
            currentTime += APPMessage.PlayMsg.Offset;
            play(currentTime);
        } else if (msg == APPMessage.PlayMsg.seekToBack && mediaPlayer.isPlaying()) {
            int currentTime = mediaPlayer.getCurrentPosition();
            currentTime -= APPMessage.PlayMsg.Offset;
            play(currentTime);
        }
        if (music != null) {
            initLrc();
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
                Log.e("Exception", e.getMessage());
            }
            try {
                mediaPlayer.prepare(); // 在调用stop后如果需要再次通过start进行播放,需要之前调用prepare函数
            } catch (Exception e) {
                Log.e("Exception", e.getMessage());
            }
        }
    }

    /**
     * 实现一个OnPrepareLister接口,当音乐准备好的时候开始播放
     */
    private final class PreparedListener implements OnPreparedListener {
        private final int positon;

        public PreparedListener(int positon) {
            this.positon = positon;
        }

        @Override
        public void onPrepared(MediaPlayer mp) {
            mediaPlayer.start();    //开始播放
            if (positon <= 0) {
                mediaPlayer.seekTo(0);
            } else if (positon > 0 && positon < mediaPlayer.getDuration()) {    //如果音乐不是从头播放
                mediaPlayer.seekTo(positon);
            } else {
                mediaPlayer.seekTo(mediaPlayer.getDuration());
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


    private void searchNetSong(String key) {
        NetWork net = new NetWork();
        net.key = key;
        net.doFlag = APPMessage.PlayMsg.beginSearch;
        new Thread(net).start();
    }

    private final Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Bundle data = msg.getData();
            int flag = data.getInt("result");
            String title = data.getString("title");
            intent.putExtra("OverMsg", flag);
            if (title != null)
                intent.putExtra("title", title);
            intent.setAction("com.demo.ccb.service.PlayService");
            sendBroadcast(intent);
        }
    };

    private class NetWork implements Runnable {
        public String key;//搜索歌曲的关键词
        public int doFlag;//应该采用的动作标志
        public String title;//下载的歌曲名
        public MusicInfo music;//歌曲的bean

        @Override
        public void run() {

            Message msg = new Message();
            Bundle data = new Bundle();
            if (doFlag == APPMessage.PlayMsg.beginSearch) {
                mp3list = MusicAppUtil.getMusicListFromNet(key);
                if (mp3list != null && mp3list.size() > 0) {
                    data.putInt("result", APPMessage.PlayMsg.searchSuccess);
                } else {
                    data.putInt("result", APPMessage.PlayMsg.searchFail);
                }
            } else if (doFlag == APPMessage.NetPlayMsg.download) {
                if (MusicAppUtil.downloadFile(path, title)) {
                    MusicAppUtil.downloadLrcFile(music);
                    data.putInt("result", APPMessage.NetPlayMsg.downloadSuccess);
                    String prefix = path.substring(path.lastIndexOf(".") + 1);
                    data.putString("title", title + "." + prefix);
                } else {
                    data.putInt("result", APPMessage.NetPlayMsg.downloadFail);
                }
            }

            msg.setData(data);
            handler.sendMessage(msg);
        }
    }


    /*
    *以下代码用于歌词显示
    * */
    private List<LrcContent> lrcList = new ArrayList<>(); //存放歌词列表对象
    private int index = 0;          //歌词检索值
    private LrcUtil lrcUtil;

    private void initLrc() {
        lrcUtil = new LrcUtil();

        StringBuilder path = new StringBuilder();
        path.append(MusicAppUtil.checkFileAndFolder() + "lyric/");
        path.append(music.getMusicTitle());
        //读取歌词文件
        if (lrcUtil.readLRC(path.toString()) == APPMessage.LrcMsg.LrcReadSuccess) {
            lrcList = lrcUtil.getLrcList();
            MainActivity.lrcShowViewMain.setmLrcList(lrcList);
            MainActivity.lrcShowViewMain.setAnimation(AnimationUtils.loadAnimation(PlayService.this, R.anim.alpha_z));
            handler.post(mRunnable);
        } else {
            lrcList = null;
            MainActivity.lrcShowViewMain.setmLrcList(lrcList);
            MainActivity.lrcShowViewMain.setAnimation(AnimationUtils.loadAnimation(PlayService.this, R.anim.alpha_z));
        }
    }

    private final Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            MainActivity.lrcShowViewMain.setIndex(lrcIndex());
            MainActivity.lrcShowViewMain.invalidate();
            handler.postDelayed(mRunnable, 50);
        }
    };

    //获取歌词索引
    private int lrcIndex() {
        if (lrcList == null || lrcList.size() < 0) {
            return 0;
        }
        int index = 0;
        if (mediaPlayer.isPlaying()) {
            int currentTime = mediaPlayer.getCurrentPosition();
            int duration = mediaPlayer.getDuration();
            if (currentTime <= duration) {
                for (int i = 0; i < lrcList.size(); i++) {
                    if (i < lrcList.size() - 1) {
                        if (currentTime < lrcList.get(i).getLrcTime() && i == 0) {
                            index = i;
                        } else if (currentTime >= lrcList.get(i).getLrcTime()
                                && currentTime < lrcList.get(i + 1).getLrcTime()) {
                            index = i;
                        }
                    } else if (i == lrcList.size() - 1
                            && currentTime >= lrcList.get(i).getLrcTime()) {
                        index = i;
                    }
                }
            }
        }

        return index;
    }
}