package ccb.demo.com.studio;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.ParcelFileDescriptor;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.demo.ccb.constant.APPMessage;
import com.demo.ccb.service.PlayService;
import com.demo.ccb.util.DBUtil;
import com.demo.ccb.util.MusicAppUtil;
import com.demo.ccb.vo.MusicInfo;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

import java.io.File;
import java.io.FileDescriptor;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private boolean isFirst = true;                    //是否第一次播放

    private boolean isPause = true;                    //暂停状态

    private final Intent intent;

    private ContentResolver cr;

    private List<MusicInfo> MusicList;

    //音乐列表view
    private ListView MusiclistView;
    //歌词列表view
    public static LrcView lrcShowViewMain;
    private boolean showLrc = false;

    //音乐当前栏目
    private ViewGroup MusicItem;

    //音乐还剩的播放时间
    private long MusicTime = 0;

    //当前播放的歌曲
    private MusicInfo CurrentSong = new MusicInfo();

    //当前播放的歌曲信息
    private TextView CurrentSongTitle;
    private TextView CurrentSongTime;
    private TextView CurrentSongPosition;
    private ImageView CurrentSongIco;
    //是否随机
    private Button PlayState;

    //上方工具栏的按钮
    private Button PlaySong;
    private Button PreviousSong;
    private Button NextSong;
    private Button seekToHead;
    private Button seekToBack;

    //sd卡的位置
    private String sDir = null;

    //是否注册了广播接收器
    private boolean isregisterReceiver = false;

    //用于控制音量
    private AudioManager audioManager = null; //音频

    //长时间加载对话框
    private ProgressDialog loading = null;
    private final Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (MusicList != null)
                setListAdpter(MusicList);
            loading.dismiss();
        }
    };
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    public MainActivity() {
        intent = new Intent();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //建立后台PlayService与前台MainActivity间的通信
        intent.setClass(MainActivity.this, PlayService.class);

        sDir = MusicAppUtil.checkFileAndFolder();
        init();
        setLisnter();
        back2Main();
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        } else if (id == R.id.action_exit) {
            stopService(intent);
            setCurrentSong(MusicList, Integer.valueOf(CurrentSongPosition.getText().toString()));
            finish();
            System.exit(0);

        } else if (id == R.id.action_find) {
            MusicList = null;
            if (loading != null) {
                loading.dismiss();
            }
            loading = ProgressDialog.show(this, "扫描", "正在扫描歌曲中...");

            new Thread() {
                @Override
                public void run() {
                    if (isregisterReceiver) {
                        unregisterReceiver(rec);
                        isregisterReceiver = false;
                    }

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                        String sdir = MusicAppUtil.checkFileAndFolder();
                        Set<String> set = MusicAppUtil.showAllFiles(new File(sdir));
                        Intent scanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                        for (String a :
                                set) {
                            if (a.contains(".")) {
                                Log.i("扫描动作的日志", a);
                                scanIntent.setData(Uri.fromFile(new File(a)));
                                sendBroadcast(scanIntent);
                            }
                        }
                    } else {
                        sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri.parse("file://" + Environment.getExternalStorageDirectory() + "/")));
                    }
                    if (!isregisterReceiver) {
                        registerReceiver(rec, filter);
                        isregisterReceiver = true;
                    }
                    try {
                        this.sleep(2000);
                    } catch (InterruptedException e) {
                        Log.e("Exception", e.getMessage());
                    }
                    MusicList = MusicAppUtil.getMusicListFromSD(cr);
                    handler.sendEmptyMessage(0);
                }
            }.start();
        } else if (id == R.id.action_openactivity) {
            if (isregisterReceiver)
                unregisterReceiver(rec);
            Intent ac1_ac2 = new Intent();
            ac1_ac2.setClass(MainActivity.this, NetworkActivity.class);
            CurrentSong.setMusicTime(MusicTime);
            int position = Integer.valueOf(CurrentSongPosition.getText().toString());
            Bundle ac1_ac2_bundle = new Bundle();
            ac1_ac2_bundle.putInt("position", position);
            ac1_ac2_bundle.putBoolean("isPlaying", !isPause);
            ac1_ac2_bundle.putParcelable("CurrentSong", CurrentSong);
            ac1_ac2_bundle.putBoolean("isFirst", isFirst);
            ac1_ac2_bundle.putParcelableArrayList("MusicList", (ArrayList<? extends Parcelable>) MusicList);
            ac1_ac2.putExtras(ac1_ac2_bundle);
            startActivity(ac1_ac2);
            this.finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setMessage("是否退出应用？");
            builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    stopService(intent);
                    setCurrentSong(MusicList, Integer.valueOf(CurrentSongPosition.getText().toString()));
                    finish();
                    System.exit(0);
                }
            });
            builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            builder.create().show();
        } else if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC,
                    AudioManager.ADJUST_RAISE,
                    AudioManager.FLAG_SHOW_UI);  //调高声音
        } else if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC,
                    AudioManager.ADJUST_LOWER,
                    AudioManager.FLAG_SHOW_UI);//调低声音
        }
        return true;
    }

    //设置歌曲列表
    private void setListAdpter(List<MusicInfo> MusicList) {
        List<HashMap<String, String>> Mp3List = new ArrayList<>();
        for (MusicInfo music : MusicList) {
            long songtime = music.getMusicTime();
            String time = songtime / 60000 + ":" + (songtime % 60000) / 1000;
            HashMap<String, String> map = new HashMap<>();
            map.put("title", music.getMusicTitle());
            map.put("artist", music.getMusicArtist());
            map.put("duration", time);
            map.put("size", String.valueOf(music.getMusicSize()));
            map.put("url", music.getMusicPath());
            Mp3List.add(map);
        }

        SimpleAdapter sa = new SimpleAdapter(this, Mp3List,
                R.layout.musiclist,
                new String[]{"title", "artist", "duration"},
                new int[]{R.id.SongTitle, R.id.Singer, R.id.SongTime});

        MusiclistView.setOnItemClickListener(new MusicListItemClickListener());
        MusiclistView.setAdapter(sa);

    }

    //点击播放列表条目时的监听器
    private class MusicListItemClickListener implements OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
            MusicList = MusicAppUtil.getMusicListFromDB(getContentResolver());
            if (MusicList != null) {
                MusicInfo music = MusicList.get(position);
                intent.putExtra("music", music);
                intent.putExtra("url", music.getMusicPath());
                intent.putExtra("MSG", APPMessage.PlayMsg.play);

                if (isregisterReceiver)
                    unregisterReceiver(rec);
                registerReceiver(rec, filter);
                isregisterReceiver = true;

                startService(intent);
                setCurrentSong(MusicList, position);
                PlaySong.setBackgroundResource(R.drawable.pause);
                isPause = false;
            }
        }


    }

    //播放按钮的监听器
    private class PlayButtonOnClick implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            MusicInfo music;
            int id = view.getId();
            TextView CurrentSongPosition = (TextView) findViewById(R.id.CurrentSongPosition);
            if (CurrentSongPosition == null)
                return;
            int position = Integer.valueOf(CurrentSongPosition.getText().toString());
            if (id == R.id.PlayState) {
                if (isRandom()) {
                    PlayState.setText("0");
                    PlayState.setBackgroundResource(R.drawable.loop);
                } else {
                    PlayState.setText("1");
                    PlayState.setBackgroundResource(R.drawable.random);
                }
            } else if (id == R.id.seekToHead || id == R.id.seekToBack) {
                if (!isPause) {
                    if (id == R.id.seekToHead) {
                        intent.putExtra("MSG", APPMessage.PlayMsg.seekToHead);
                        MusicTime = MusicTime + APPMessage.PlayMsg.Offset;
                    } else if (id == R.id.seekToBack) {
                        intent.putExtra("MSG", APPMessage.PlayMsg.seekToBack);
                        MusicTime = MusicTime - APPMessage.PlayMsg.Offset;
                        MusicTime = MusicTime > 0 ? MusicTime : 0;
                    }

                    if (MusicList != null) {
                        String time = MusicTime / 60000 + ":" + (MusicTime % 60000) / 1000;
                        CurrentSongTime.setText(time);
                        music = MusicList.get(position);
                        intent.putExtra("url", music.getMusicPath());
                        intent.putExtra("music", music);
                        startService(intent);
                    }
                }
            } else {
                if (id == R.id.PlaySong) {
                    if (isFirst) {
                        intent.putExtra("MSG", APPMessage.PlayMsg.play);
                        PlaySong.setBackgroundResource(R.drawable.pause);
                        isPause = false;
                        if (isregisterReceiver)
                            unregisterReceiver(rec);
                        registerReceiver(rec, filter);
                        isregisterReceiver = true;
                    } else {
                        if (isPause) {
                            intent.putExtra("MSG", APPMessage.PlayMsg.replay);
                            PlaySong.setBackgroundResource(R.drawable.pause);
                            isPause = false;
                            if (isregisterReceiver)
                                unregisterReceiver(rec);
                            registerReceiver(rec, filter);
                            isregisterReceiver = true;
                        } else {
                            intent.putExtra("MSG", APPMessage.PlayMsg.pause);
                            PlaySong.setBackgroundResource(R.drawable.play);
                            isPause = true;
                            unregisterReceiver(rec);
                            isregisterReceiver = false;
                        }
                    }
                } else {
                    if (id == R.id.PreviousSong) {
                        position = (position - 1 + MusicList.size()) % MusicList.size();
                    } else if (id == R.id.NextSong) {
                        if (!isRandom()) {
                            position = (position + 1) % MusicList.size();
                        } else {
                            int random = 1 + ((int) (Math.random() * MusicList.size())) % (MusicList.size() - 1);
                            position = (position + random) % MusicList.size();
                        }
                    }
                    if (MusicList != null) {
                        intent.putExtra("MSG", APPMessage.PlayMsg.play);
                        setCurrentSong(MusicList, position);
                        PlaySong.setBackgroundResource(R.drawable.pause);
                        isPause = false;
                    }
                    if (isregisterReceiver)
                        unregisterReceiver(rec);
                    registerReceiver(rec, filter);
                    isregisterReceiver = true;
                }
                music = MusicList.get(position);
                intent.putExtra("url", music.getMusicPath());
                intent.putExtra("music", music);
                startService(intent);
                isFirst = false;
            }
        }

        //是否随机播放
        public boolean isRandom() {
            int state = Integer.valueOf(PlayState.getText().toString());
            return state != 0;
        }
    }

    //处理PlayService发送来的广播
    private class BroadcastReceive extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            int msg = intent.getIntExtra("OverMsg", 0);
            if (msg == APPMessage.PlayMsg.playover) {
                NextSong.performClick();
            } else if (msg == APPMessage.PlayMsg.playtime) {
                if (MusicList != null) {
                    MusicTime = MusicTime + 1000;
                    String time = MusicTime / 60000 + ":" + (MusicTime % 60000) / 1000;
                    CurrentSongTime.setText(time);
                }
            } else if (msg == APPMessage.NetPlayMsg.downloadFail) {
                Toast.makeText(getApplicationContext(),
                        "歌曲下载失败！",
                        Toast.LENGTH_LONG).show();
            } else if (msg == APPMessage.NetPlayMsg.downloadSuccess) {
                String title = intent.getStringExtra("title");
                Intent scanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                scanIntent.setData(Uri.fromFile(new File(MusicAppUtil.checkFileAndFolder() + "song/" + title)));
                sendBroadcast(scanIntent);
                Toast.makeText(getApplicationContext(),
                        "歌曲下载成功，请重新扫描本地音乐",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    //设置当前歌曲状态
    private void setCurrentSong(List<MusicInfo> MusicList, int position) {
        MusicInfo music = MusicList.get(position);
        CurrentSong = music;
        MusicTime = 0;
        String time = MusicTime / 60000 + ":" + (MusicTime % 60000) / 1000;
        CurrentSongTitle.setText(music.getMusicTitle());
        CurrentSongTime.setText(time);
        CurrentSongPosition.setText(String.valueOf(position));

        new DBUtil().insertCurrentSong(position, PlayState.getText().toString());

        //设置内嵌图标
        Uri uri = Uri.parse(music.getIco());
        ParcelFileDescriptor pfd = null;
        Bitmap bm = null;
        FileDescriptor fd;
        try {
            pfd = cr.openFileDescriptor(uri, "r");
            if (pfd != null) {
                fd = pfd.getFileDescriptor();
                bm = BitmapFactory.decodeFileDescriptor(fd);
            }
            if (bm != null) {
                CurrentSongIco.setImageBitmap(bm);
            } else {
                throw new Exception();
            }
        } catch (Exception e) {
            CurrentSongIco.setImageResource(R.mipmap.ico);
        } finally {
            if (pfd != null) {
                try {
                    pfd.close();
                } catch (IOException e) {
                    Log.i("aaa", e.getMessage());
                }
            }
        }
        isFirst = false;
    }

    //初始化各个组件
    private void init() {
        CurrentSongTitle = (TextView) findViewById(R.id.CurrentSongTitle);
        CurrentSongTime = (TextView) findViewById(R.id.CurrentSongTime);
        CurrentSongPosition = (TextView) findViewById(R.id.CurrentSongPosition);
        CurrentSongIco = (ImageView) findViewById(R.id.CurrentSongIco);
        PlayState = (Button) findViewById(R.id.PlayState);
        PlaySong = (Button) findViewById(R.id.PlaySong);
        PreviousSong = (Button) findViewById(R.id.PreviousSong);
        NextSong = (Button) findViewById(R.id.NextSong);
        seekToHead = (Button) findViewById(R.id.seekToHead);
        seekToBack = (Button) findViewById(R.id.seekToBack);

        MusicItem = (ViewGroup) findViewById(R.id.MusicItem);
        lrcShowViewMain = (LrcView) findViewById(R.id.lrcShowViewMain);
        MusiclistView = (ListView) findViewById(R.id.MusicList);
        cr = getContentResolver();

        audioManager = (AudioManager) getSystemService(Service.AUDIO_SERVICE);
    }


    private void back2Main() {
        int position;
        Object[] Current = new DBUtil().getCurrentSong();
        Bundle bundle = getIntent().getExtras();
        try {
            position = bundle.getInt("position");
            isPause = bundle.getBoolean("isPause");
            CurrentSong = bundle.getParcelable("CurrentSong");
            MusicList = bundle.getParcelableArrayList("MusicList");
            setCurrentSong(MusicList, position);
            MusicTime = CurrentSong.getMusicTime();
            String time = MusicTime / 60000 + ":" + (MusicTime % 60000) / 1000;
            CurrentSongTime.setText(time);
            if (isPause) {
                PlaySong.setBackgroundResource(R.drawable.play);
            } else {
                PlaySong.setBackgroundResource(R.drawable.pause);
                if (isregisterReceiver)
                    unregisterReceiver(rec);
                registerReceiver(rec, filter);
                isregisterReceiver = true;
            }
            if (Current != null) {
                if ("1".equals(Current[1].toString())) {
                    PlayState.setText("1");
                    PlayState.setBackgroundResource(R.drawable.random);
                }
            }


            MusicInfo music = MusicList.get(position);
            intent.putExtra("url", music.getMusicPath());
            intent.putExtra("music", music);
            intent.putExtra("MSG", APPMessage.LrcMsg.showLrc);
            startService(intent);

        } catch (Exception e) {

            MusicList = MusicAppUtil.getMusicListFromDB(cr);
            if (Current != null) {
                setCurrentSong(MusicList, (int) Current[0]);
                if ("1".equals(Current[1].toString())) {
                    PlayState.setText("1");
                    PlayState.setBackgroundResource(R.drawable.random);
                }
            } else {
                if (MusicList != null && MusicList.size() > 0)
                    setCurrentSong(MusicList, 0);
            }
            PlaySong.setBackgroundResource(R.drawable.play);
            isFirst = true;
        }

        if (MusicAppUtil.getMusicListFromDB(cr) != null) {
            setListAdpter(MusicAppUtil.getMusicListFromDB(cr));
        }

    }

    //广播的Filter
    private final IntentFilter filter = new IntentFilter();
    private final BroadcastReceive rec = new BroadcastReceive();


    //注册各个组件的监听器
    private void setLisnter() {
        PlayState.setOnClickListener(new PlayButtonOnClick());
        PlaySong.setOnClickListener(new PlayButtonOnClick());
        PreviousSong.setOnClickListener(new PlayButtonOnClick());
        NextSong.setOnClickListener(new PlayButtonOnClick());
        seekToHead.setOnClickListener(new PlayButtonOnClick());
        seekToBack.setOnClickListener(new PlayButtonOnClick());


        filter.addAction("com.demo.ccb.service.PlayService");


        //当前音乐栏目的点击事件
        MusicItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (showLrc) {
                    MusiclistView.setVisibility(View.VISIBLE);
                    lrcShowViewMain.setVisibility(View.GONE);
                    showLrc = false;
                } else {
                    MusiclistView.setVisibility(View.GONE);
                    lrcShowViewMain.setVisibility(View.VISIBLE);
                    showLrc = true;
                }
            }
        });

        //歌词显示页面的点击事件
        lrcShowViewMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (showLrc) {
                    MusiclistView.setVisibility(View.VISIBLE);
                    lrcShowViewMain.setVisibility(View.GONE);
                    showLrc = false;
                } else {
                    MusiclistView.setVisibility(View.GONE);
                    lrcShowViewMain.setVisibility(View.VISIBLE);
                    showLrc = true;
                }
            }
        });

    }

}
