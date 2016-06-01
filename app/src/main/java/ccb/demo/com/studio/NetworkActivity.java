package ccb.demo.com.studio;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
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
import java.util.HashMap;
import java.util.List;

/**
 * Created by godfa on 2016/3/19.
 */
public class NetworkActivity extends AppCompatActivity {
    NetworkActivity activity = this;
    List<MusicInfo> MusicList;
    ListView MusiclistView;
    final Intent intent = new Intent();
    ContentResolver cr;
    //是否第一次播放
    private boolean isFirst;

    EditText input_key;

    //音乐还剩的播放时间
    long MusicTime;

    //当前播放的歌曲信息
    TextView CurrentSongTitle;
    TextView CurrentSongTime;
    TextView CurrentSongPosition;
    ImageView CurrentSongIco;


    //当前播放的歌曲
    MusicInfo CurrentSong;

    //播放按钮
    Button PlaySong;
    boolean isPlaying = false;

    String sDir = null;

    //是否是随机播放
    int isRandom = 0;

    //实现下载等待窗口
    private ProgressDialog loading = null;
    //AlertDialog.Builder loading = new AlertDialog.Builder(NetworkActivity.this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_network);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        init();
        Bundle bundle = getIntent().getExtras();
        int position;
        try {
            position = bundle.getInt("position");
            isPlaying = bundle.getBoolean("isPlaying");
            isFirst = bundle.getBoolean("isFirst");
            CurrentSong = bundle.getParcelable("CurrentSong");
            MusicList = bundle.getParcelableArrayList("MusicList");
            setCurrentSong(MusicList, position);

            MusicTime = CurrentSong.getMusicTime();
            String time = MusicTime / 60000 + ":" + (MusicTime % 60000) / 1000;
            CurrentSongTime.setText(time);

            if (!isPlaying) {
                PlaySong.setBackgroundResource(R.drawable.play);
            } else {
                PlaySong.setBackgroundResource(R.drawable.pause);
            }

        } catch (Exception e) {
            MusicList = MusicAppUtil.getMusicListFromDB(getContentResolver());
        }


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_network, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        } else if (id == R.id.action_exit) {
            this.finish();
            stopService(intent);
            System.exit(0);
        }
//        else if (id == R.id.action_openactivity) {
//            back2Main();
//
//        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            back2Main();
        }
        return true;
    }

    public void back2Main() {
        Intent ac2_ac1 = new Intent();
        ac2_ac1.setClass(this, MainActivity.class);
        CurrentSong.setMusicTime(MusicTime);
        int position = Integer.valueOf(CurrentSongPosition.getText().toString());
        Bundle ac2_ac1_bundle = new Bundle();
        ac2_ac1_bundle.putInt("position", position);
        ac2_ac1_bundle.putBoolean("isPause", !isPlaying);
        ac2_ac1_bundle.putBoolean("isFirst", isFirst);
        ac2_ac1_bundle.putParcelable("CurrentSong", CurrentSong);
        ac2_ac1_bundle.putParcelableArrayList("MusicList", (ArrayList<? extends Parcelable>) MusicList);
        ac2_ac1.putExtras(ac2_ac1_bundle);
        startActivity(ac2_ac1);
        this.finish();
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
                    //pfd = null;
                } catch (IOException e) {
                    //pfd = null;
                }
            }
        }
    }

    //初始化各个组件
    public void init() {

        CurrentSongTitle = (TextView) findViewById(R.id.CurrentSongTitle);
        CurrentSongTime = (TextView) findViewById(R.id.CurrentSongTime);
        CurrentSongPosition = (TextView) findViewById(R.id.CurrentSongPosition);
        CurrentSongIco = (ImageView) findViewById(R.id.CurrentSongIco);

        PlaySong = (Button) findViewById(R.id.PlayControl);
        PlaySong.setOnClickListener(new PlayButtonOnClick());

        intent.setClass(this, PlayService.class);
        cr = getContentResolver();
        input_key = (EditText) findViewById(R.id.input_key);
        loading = null;
        input_key.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                String key = v.getText().toString().trim();
                if (actionId == EditorInfo.IME_ACTION_SEND || (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                    if (key.length() <= 0) {
                        Toast.makeText(getApplicationContext(), "输入不能为空", Toast.LENGTH_SHORT).show();
                    } else {
                        if (loading!=null){
                            loading.dismiss();
                        }
                        intent.putExtra("key", key);
                        intent.putExtra("MSG", APPMessage.PlayMsg.beginSearch);
                        loading = ProgressDialog.show(NetworkActivity.this,"搜索","正在搜索...");
                        startService(intent);
                    }
                    return true;
                }
                return false;
            }
        });

        Object[] o = (new DBUtil().getCurrentSong());
        isRandom = Integer.valueOf(o[1].toString());

        IntentFilter filter = new IntentFilter();
        filter.addAction("com.demo.ccb.service.PlayService");
        registerReceiver(new BroadcastReceive(), filter);

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
            map.put("ID",String.valueOf(music.getMusicID()));
            Mp3List.add(map);
        }

        SimpleAdapter sa = new SimpleAdapter(this, Mp3List,
                R.layout.musiclist,
                new String[]{"title", "artist", "duration"},
                new int[]{R.id.SongTitle, R.id.Singer, R.id.SongTime});
        MusiclistView = (ListView) findViewById(R.id.MusicList);
        MusiclistView.setOnItemClickListener(new MusicListItemClickListener());
        MusiclistView.setOnItemLongClickListener(new MusicListItemLongClickListenter());
        MusiclistView.setAdapter(sa);
    }

    //处理PlayService发送来的广播
    private class BroadcastReceive extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            int msg = intent.getIntExtra("OverMsg", 0);

            if (msg == APPMessage.PlayMsg.playtime) {
                MusicTime = MusicTime - 1000;
                String time = MusicTime / 60000 + ":" + (MusicTime % 60000) / 1000;
                CurrentSongTime.setText(time);
            }else if (msg==APPMessage.PlayMsg.searchSuccess){
                loading.cancel();
                stopService(intent);
                MusicList = new DBUtil().getMusicListInNetTable();
                setListAdpter(MusicList);
            }else if(msg==APPMessage.PlayMsg.searchFail){
                loading.dismiss();
                Toast.makeText(getApplicationContext(),
                        "没有查找到相应歌曲信息请重新输入搜索关键词",
                        Toast.LENGTH_LONG).show();
            }else if (msg==APPMessage.NetPlayMsg.downloadSuccess){
                String title = intent.getStringExtra("title");
                Toast.makeText(getApplicationContext(),
                        "歌曲下载成功，请重新扫描本地音乐",
                        Toast.LENGTH_SHORT).show();
                        Intent scanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                        scanIntent.setData(Uri.fromFile(new File(MusicAppUtil.checkFileAndFolder()+"song/"+title)));
                        sendBroadcast(scanIntent);
            }else if (msg==APPMessage.NetPlayMsg.downloadFail){
                Toast.makeText(getApplicationContext(),
                        "歌曲下载失败！",
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    //点击播放列表条目时的监听器
    private class MusicListItemClickListener implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
            if (MusicList != null) {
                MusicInfo music = MusicList.get(position);
                intent.putExtra("url", music.getMusicPath());
                intent.putExtra("MSG", APPMessage.PlayMsg.play);
                startService(intent);
                setCurrentSong(MusicList, position);
                isPlaying=true;
                isFirst = false;
                PlaySong.setBackgroundResource(R.drawable.pause);
            }
        }
    }

    //长按播放条目时的监听器
    public int p;//记录下载的歌曲的位置
    private class MusicListItemLongClickListenter implements AdapterView.OnItemLongClickListener {

        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
            p = position;
            AlertDialog.Builder builder = new AlertDialog.Builder(NetworkActivity.this);
            builder.setMessage("是否下载此音乐？");
            builder.setTitle("下载提示");
            builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    if (MusicList != null) {
                        MusicInfo music = MusicList.get(p);
                        intent.putExtra("url", music.getMusicPath());
                        intent.putExtra("title",music.getMusicTitle());
                        intent.putExtra("artist",music.getMusicArtist());
                        intent.putExtra("MSG", APPMessage.NetPlayMsg.download);
                        startService(intent);
                        Toast.makeText(getApplicationContext(),
                                "歌曲下载任务已经提交到后台",
                                Toast.LENGTH_SHORT).show();
                    }
                }
            });
            builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            builder.create().show();
            return true;
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
                if (id == R.id.PlayControl) {
                    if (isFirst) {
                        intent.putExtra("MSG", APPMessage.PlayMsg.play);
                        PlaySong.setBackgroundResource(R.drawable.pause);
                        isPlaying = true;
                    } else {
                        if (!isPlaying) {
                            intent.putExtra("MSG", APPMessage.PlayMsg.replay);
                            PlaySong.setBackgroundResource(R.drawable.pause);
                            isPlaying = true;
                        } else {
                            intent.putExtra("MSG", APPMessage.PlayMsg.pause);
                            PlaySong.setBackgroundResource(R.drawable.play);
                            isPlaying = false;
                        }
                    }
                }
                music = MusicList.get(position);
                intent.putExtra("url", music.getMusicPath());
                startService(intent);
                isFirst = false;

        }

        //是否随机播放
        public boolean isRandom() {

            return isRandom != 0;
        }
    }
}
