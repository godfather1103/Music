package com.demo.hwq.util;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


import android.content.ContentResolver;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import com.demo.hwq.constant.APPMessage;
import com.demo.hwq.vo.MusicInfo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MusicAppUtil {

    private static final API api = new API();

    //内存卡的路径
    private static String sDir = null;

    /**
     * 用于从数据库中查询歌曲的信息，保存在List当中
     *
     * @return List
     */

    public static List<MusicInfo> getMusicListFromDB(ContentResolver cr) {
        List<MusicInfo> MusicList;
        MusicList = new DBUtil().getMusicList();
        if (MusicList == null) {
            MusicList = getMusicListFromSD(cr);
            new DBUtil().setMusicList(MusicList);
        }
        return MusicList;
    }

    /**
     * 用于从SD中查询歌曲的信息，保存在List当中
     *
     * @return List
     */

    public static List<MusicInfo> getMusicListFromSD(ContentResolver cr) {
        List<MusicInfo> MusicList = new ArrayList<>();
        Cursor cursor = cr.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                null, null, null, MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
        MusicInfo music;
        while (cursor != null && cursor.moveToNext()) {
            int isMusic = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.IS_MUSIC));//是否为音乐
            if (isMusic != 0) {
                music = new MusicInfo();
                //long al = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID));
                music.setMusicID(cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media._ID)));
                music.setMusicTitle(cursor.getString((cursor.getColumnIndex(MediaStore.Audio.Media.TITLE))));
                music.setMusicArtist(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)));
                music.setMusicTime(cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION)));
                music.setMusicSize(cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.SIZE)));
                music.setMusicPath(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA)));
                music.setIco("content://media/external/audio/media/" + music.getMusicID() + "/albumart");
                MusicList.add(music);
            }
        }
        if (cursor != null)
            cursor.close();
        new DBUtil().setMusicList(MusicList);
        return MusicList;
    }

    /**
     * 用于从网络中查询歌曲的信息，保存在List当中
     *
     * @return List
     */
    public static List<MusicInfo> getMusicListFromNet(String key) {
        List<MusicInfo> MusicList = new ArrayList<>();
        JSONArray songs;
        JSONObject result;
        JSONObject msg;
        JSONObject song;

        try {
            msg = api.search(key, 1, 0, "true", 20);
            int code = msg.getInt("code");
            if (code == 200) {
                result = msg.getJSONObject("result");
                songs = result.getJSONArray("songs");
                for (int i = 0; i < songs.length(); i++) {
                    song = (JSONObject) songs.get(i);
                    MusicList.add(MusicJSON2Object(song));
                }

            } else {
                MusicList = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            MusicList = null;
        }
        if (MusicList != null)
            new DBUtil().setMusicListInNetTable(MusicList);
        return MusicList;
    }

    /*
    *从网络中下载歌曲，并返回相应标志
    *
    *@return boolean
    * */
    public static boolean downloadFile(String url, String title) {
        boolean flag;
        String sdcard = MusicAppUtil.checkFileAndFolder();
        String prefix = null;
        if (url != null) {
            prefix = url.substring(url.lastIndexOf(".") + 1);
        }
        try {
            api.DownLoad(url, title + "." + prefix, sdcard + "song/");
            flag = true;
        } catch (IOException e) {
            Log.e("Exception", e.getMessage());
            flag = false;
        }
        return flag;
    }


    /*
    *从网络中下载歌词，并返回相应标志
    *
    *@return boolean
    * */
    public static boolean downloadLrcFile(MusicInfo music) {
        boolean flag;
        String sdcard = MusicAppUtil.checkFileAndFolder();
        try {
            api.DownLoadLrc(String.valueOf(music.getMusicID()), music.getMusicTitle(), sdcard + "lyric/");
            flag = true;
        } catch (Exception e) {
            Log.e("Exception", e.getMessage());
            flag = false;
        }
        return flag;
    }


    /*
    * 把歌曲的json对象转换成MusicInfo对象
    *
    * */
    private static MusicInfo MusicJSON2Object(JSONObject song) {
        MusicInfo musicInfo = new MusicInfo();
        try {
            musicInfo.setMusicID(song.getLong("id"));
            musicInfo.setMusicTitle(song.getString("name"));
            musicInfo.setMusicArtist(((JSONObject) song.getJSONArray("artists").get(0)).getString("name"));
            musicInfo.setMusicTime(song.getLong("duration"));
            musicInfo.setMusicSize(song.getLong("copyrightId"));
            musicInfo.setMusicPath(api.song_detail(musicInfo.getMusicID()));
            musicInfo.setIco(((JSONObject) song.getJSONArray("artists").get(0)).getString("img1v1Url"));
        } catch (JSONException e) {
            e.printStackTrace();
            musicInfo = null;
        }
        return musicInfo;
    }

    //保存图片到SD卡
    public static void saveBitmapToFile(Bitmap bitmap, String _file)
            throws IOException {
        BufferedOutputStream os = null;
        try {
            File file = new File(_file);

            int end = _file.lastIndexOf(File.separator);
            String _filePath = _file.substring(0, end);
            File filePath = new File(_filePath);
            if (!filePath.exists()) {
                filePath.mkdirs();
            }
            file.createNewFile();
            os = new BufferedOutputStream(new FileOutputStream(file));
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, os);
        } finally {
            if (os != null) {
                try {
                    os.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    //保存图片到SD卡
    public static void saveJPGToFile(Bitmap bitmap, String _file)
            throws IOException {
        BufferedOutputStream os = null;
        try {
            File file = new File(_file);

            int end = _file.lastIndexOf(File.separator);
            String _filePath = _file.substring(0, end);
            File filePath = new File(_filePath);
            if (!filePath.exists()) {
                filePath.mkdirs();
            }
            file.createNewFile();
            os = new BufferedOutputStream(new FileOutputStream(file));
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, os);
        } finally {
            if (os != null) {
                try {
                    os.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    //检查文件夹存在情况
    public static String checkFileAndFolder() {
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
        return sDir;
    }


    //扫描文件夹下所有文件
    public static Set showAllFiles(File dir) {
        Set<String> File_Set = new HashSet<>();
        File[] files = dir.listFiles();
        for (File f : files) {
            if (f.isDirectory()) {
                File_Set.addAll(showAllFiles(f));
            } else {
                File_Set.add(f.getAbsolutePath());
            }
        }
        return File_Set;
    }
}
