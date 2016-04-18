package com.demo.hwq.util;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


import android.content.ContentResolver;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.provider.MediaStore;

import com.demo.hwq.vo.MusicInfo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MusicAppUtil {

    public static final API api = new API();

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
        List<MusicInfo> MusicList = new ArrayList<MusicInfo>();
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
        return MusicList;
    }

    /**
     * 用于从网络中查询歌曲的信息，保存在List当中
     *
     * @return List
     */
    public static List<MusicInfo> getMusicListFromNet(String key) {
        List<MusicInfo> MusicList = new ArrayList<MusicInfo>();
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

    public static MusicInfo MusicJSON2Object(JSONObject song) {
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

}
