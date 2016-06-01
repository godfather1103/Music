package ccb.demo.com.studio;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaScannerConnection;
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
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.demo.hwq.constant.APPMessage;
import com.demo.hwq.service.PlayService;
import com.demo.hwq.util.DBUtil;
import com.demo.hwq.util.MusicAppUtil;
import com.demo.hwq.vo.MusicInfo;

import java.io.File;
import java.io.FileDescriptor;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private boolean isFirst = true;                    //是否第一次播放

    private boolean isPause = true;                    //暂停状态

    final Intent intent = new Intent();

    //public static MainActivity instance = null;

    ContentResolver cr;

    List<MusicInfo> MusicList;

    ListView MusiclistView;

    //音乐还剩的播放时间
    long MusicTime = 0;

    //当前播放的歌曲
    MusicInfo CurrentSong = new MusicInfo();

    //当前播放的歌曲信息
    TextView CurrentSongTitle;
    TextView CurrentSongTime;
    TextView CurrentSongPosition;
    ImageView CurrentSongIco;
    //是否随机
    Button PlayState;

    //上方工具栏的按钮
    Button PlaySong;
    Button PreviousSong;
    Button NextSong;

    //sd卡的位置
    String sDir = null;

    //是否注册了广播接收器
    boolean isregisterReceiver = false;

    //长时间加载对话框
    ProgressDialog loading = null;
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (MusicList != null)
                setListAdpter(MusicList);
            loading.dismiss();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //建立后台PlayService与前台MainActivity间的通信
        intent.setClass(MainActivity.this, PlayService.class);

        //checkFileAndFolder();
        sDir = MusicAppUtil.checkFileAndFolder();
        init();
        setLisnter();
        back2Main();
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
            if (loading!=null){
                loading.dismiss();
            }
            loading = ProgressDialog.show(this,"扫描","正在扫描歌曲中...");
            new Thread(){
                @Override
                public void run() {
                    if (isregisterReceiver)
                        unregisterReceiver(rec);
                    if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.KITKAT){
/*                        MediaScannerConnection.scanFile(MainActivity.this,new String[]{Environment.getExternalStorageDirectory().toString()},new String[]{"audio*//*"},new MediaScannerConnection.OnScanCompletedListener(){

                            @Override
                            public void onScanCompleted(String path, Uri uri) {
                                Log.i("ExternalStorage", "Scanned " + path + ":");
                                Log.i("ExternalStorage", "-> uri=" + uri);
                            }
                        });*/
                        String ACTION_MEDIA_SCANNER_SCAN_DIR =        "android.intent.action.MEDIA_SCANNER_SCAN_DIR";
                        Intent scanInt = new Intent(ACTION_MEDIA_SCANNER_SCAN_DIR);
                        scanInt.setData(Uri.fromFile(Environment.getExternalStorageDirectory()));
                        sendBroadcast(scanInt);

                    }else{
                        sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri.parse("file://" + Environment.getExternalStorageDirectory())));
                    }
                    try {
                        this.sleep(4000);
                    } catch (InterruptedException e) {
                        Log.e("Exception:",e.getMessage());
                        e.printStackTrace();
                    }
                    if (!isregisterReceiver){
                        registerReceiver(rec,filter);
                        isregisterReceiver = true;
                    }
                    MusicList = MusicAppUtil.getMusicListFromSD(cr);
                    handler.sendEmptyMessage(0);
                }
            }.start();
        } else if (id == R.id.action_openactivity) {
            Intent ac1_ac2 = new Intent();
            ac1_ac2.setClass(MainActivity.this, NetworkActivity.class);
            CurrentSong.setMusicTime(MusicTime);
            int position = Integer.valueOf(CurrentSongPosition.getText().toString());
            Bundle ac1_ac2_bundle = new Bundle();
            ac1_ac2_bundle.putInt("position", position);
            ac1_ac2_bundle.putBoolean("isPlaying", !isPause);
            ac1_ac2_bundle.putParcelable("CurrentSong", CurrentSong);
            ac1_ac2_bundle.putBoolean("isFirst",isFirst);
            ac1_ac2_bundle.putParcelableArrayList("MusicList", (ArrayList<? extends Parcelable>) MusicList);
            ac1_ac2.putExtras(ac1_ac2_bundle);
            startActivity(ac1_ac2);
            this.finish();
        }
        return super.onOptionsItemSelected(item);
    }

    public static Date StartTime;

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            Date CurTime = new Date();
            if (CurTime.getTime() - StartTime.getTime() > 2000) {
                Toast.makeText(this.getApplicationContext(), "再按一次返回键就退出程序！", Toast.LENGTH_SHORT).show();
                StartTime = CurTime;
            } else {
                stopService(intent);
                setCurrentSong(MusicList, Integer.valueOf(CurrentSongPosition.getText().toString()));
                finish();
                System.exit(0);
            }
        }
        return true;
    }

    //设置歌曲列表
    public void setListAdpter(List<MusicInfo> MusicList) {
        List<HashMap<String, String>> Mp3List = new ArrayList<HashMap<String, String>>();
        for (MusicInfo music : MusicList) {
            long songtime = music.getMusicTime();
            String time = songtime / 60000 + ":" + (songtime % 60000) / 1000;
            HashMap<String, String> map = new HashMap<String, String>();
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

        MusiclistView = (ListView) findViewById(R.id.MusicList);
        MusiclistView.setOnItemClickListener(new MusicListItemClickListener());
        MusiclistView.setAdapter(sa);

    }

    //检查文件夹和数据库创建情况
    public void checkFileAndFolder() {
        String status = Environment.getExternalStorageState();

        if (status.equals(Environment.MEDIA_MOUNTED)) {
            sDir = APPMessage.APPPath.ExistSD;
        } else {
            sDir = APPMessage.APPPath.NoExistSD;
        }
        File destDir = new File(sDir);
        if (!destDir.exists()) {
            destDir.mkdirs();
        }

        destDir = new File(sDir + "skin/");
        if (!destDir.exists()) {
            destDir.mkdirs();
        }

        destDir = new File(sDir + "lyric/");
        if (!destDir.exists()) {
            destDir.mkdirs();
        }

        destDir = new File(sDir + "song/");
        if (!destDir.exists()) {
            destDir.mkdirs();
        }

        destDir = new File(sDir + "skin/back.jpg");
        if (!destDir.exists()) {
            Bitmap back = BitmapFactory.decodeResource(this.getResources(), R.drawable.back);
            try {
                MusicAppUtil.saveBitmapToFile(back, sDir + "skin/back.jpg");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


        destDir = new File(APPMessage.APPPath.NoExistSD + "/databases/");
        if (!destDir.exists()) {
            destDir.mkdirs();
        }

        destDir = new File(APPMessage.APPPath.NoExistSD + "/databases/Music.db");
        if (!destDir.exists()) {
            //初始化数据库
            SQLiteDatabase db = SQLiteDatabase.openOrCreateDatabase(
                    APPMessage.APPPath.NoExistSD + "/databases/Music.db",
                    null);
            StringBuilder CurrentSong = new StringBuilder();
            CurrentSong.append("CREATE TABLE IF NOT EXISTS [CurrentSong] (");
            CurrentSong.append("  [position] INTEGER NOT NULL DEFAULT 0, ");
            CurrentSong.append("  [PlayState] VARCHAR NOT NULL DEFAULT 0); ");

            StringBuilder LocalMusicList = new StringBuilder();
            LocalMusicList.append("CREATE TABLE IF NOT EXISTS [LocalMusicList] (");
            LocalMusicList.append("  [MusicID] INTEGER NOT NULL, ");
            LocalMusicList.append("  [MusicTitle] VARCHAR NOT NULL, ");
            LocalMusicList.append("  [MusicArtist] VARCHAR, ");
            LocalMusicList.append("  [MusicTime] INTEGER NOT NULL, ");
            LocalMusicList.append("  [MusicSize] INTEGER NOT NULL, ");
            LocalMusicList.append("  [MusicPath] VARCHAR NOT NULL, ");
            LocalMusicList.append("  [Ico] VARCHAR); ");

            StringBuilder NetMusicList = new StringBuilder();
            NetMusicList.append("CREATE TABLE IF NOT EXISTS [NetMusicList] (");
            NetMusicList.append("  [MusicID] INTEGER NOT NULL, ");
            NetMusicList.append("  [MusicTitle] VARCHAR NOT NULL, ");
            NetMusicList.append("  [MusicArtist] VARCHAR, ");
            NetMusicList.append("  [MusicTime] INTEGER NOT NULL, ");
            NetMusicList.append("  [MusicSize] INTEGER NOT NULL, ");
            NetMusicList.append("  [MusicPath] VARCHAR NOT NULL, ");
            NetMusicList.append("  [Ico] VARCHAR); ");

            db.execSQL(CurrentSong.toString());
            db.execSQL(LocalMusicList.toString());
            db.execSQL(NetMusicList.toString());
            db.close();
        }
    }

    //点击播放列表条目时的监听器
    private class MusicListItemClickListener implements OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
            MusicList = MusicAppUtil.getMusicListFromDB(getContentResolver());
            if (MusicList != null) {
                MusicInfo music = MusicList.get(position);
                intent.putExtra("url", music.getMusicPath());
                intent.putExtra("MSG", APPMessage.PlayMsg.play);
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
                //MusicInfo music;
                //int position = Integer.valueOf(CurrentSongPosition.getText().toString());
                if (MusicList != null) {
                    // music = MusicList.get(position);
                    MusicTime = MusicTime - 1000;
                    String time = MusicTime / 60000 + ":" + (MusicTime % 60000) / 1000;
                    CurrentSongTime.setText(time);
                }
            }else if (msg==APPMessage.NetPlayMsg.downloadFail){
                Toast.makeText(getApplicationContext(),
                        "歌曲下载失败！",
                        Toast.LENGTH_LONG).show();
            }else if (msg==APPMessage.NetPlayMsg.downloadSuccess){
                Toast.makeText(getApplicationContext(),
                        "歌曲下载成功，请重新扫描本地音乐",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    //设置当前歌曲状态
    public void setCurrentSong(List<MusicInfo> MusicList, int position) {
        MusicInfo music = MusicList.get(position);
        CurrentSong = music;
        MusicTime = music.getMusicTime();
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
    protected void init() {
        CurrentSongTitle = (TextView) findViewById(R.id.CurrentSongTitle);
        CurrentSongTime = (TextView) findViewById(R.id.CurrentSongTime);
        CurrentSongPosition = (TextView) findViewById(R.id.CurrentSongPosition);
        CurrentSongIco = (ImageView) findViewById(R.id.CurrentSongIco);
        PlayState = (Button) findViewById(R.id.PlayState);
        PlaySong = (Button) findViewById(R.id.PlaySong);
        PreviousSong = (Button) findViewById(R.id.PreviousSong);
        NextSong = (Button) findViewById(R.id.NextSong);
        cr = getContentResolver();
        StartTime = new Date();
    }


    public void back2Main() {
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
            if(isPause){
                PlaySong.setBackgroundResource(R.drawable.play);
            }else{
                PlaySong.setBackgroundResource(R.drawable.pause);
            }
            if (Current != null) {
                if ("1".equals(Current[1].toString())) {
                    PlayState.setText("1");
                    PlayState.setBackgroundResource(R.drawable.random);
                }
            }
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
    IntentFilter filter = new IntentFilter();
    BroadcastReceive rec = new BroadcastReceive();

    //注册各个组件的监听器
    protected void setLisnter() {
        PlayState.setOnClickListener(new PlayButtonOnClick());
        PlaySong.setOnClickListener(new PlayButtonOnClick());
        PreviousSong.setOnClickListener(new PlayButtonOnClick());
        NextSong.setOnClickListener(new PlayButtonOnClick());
        filter.addAction("com.demo.ccb.service.PlayService");


    }

}
