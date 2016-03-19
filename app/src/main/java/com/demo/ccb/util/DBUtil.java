package com.demo.ccb.util;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.demo.ccb.constant.APPMessage;
import com.demo.ccb.vo.MusicInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by godfa on 2016/3/19.
 */
public class DBUtil {

    private SQLiteDatabase sqlite = null;

    public DBUtil() {
        sqlite = SQLiteDatabase.openOrCreateDatabase(
                APPMessage.APPPath.NoExistSD + "/databases/Music.db",
                null);
    }

    public SQLiteDatabase getSQLiteDatabase() {
        return sqlite;
    }

    public boolean insertCurrentSong(int position, String PlayState) {
        boolean flag = false;
        if (sqlite != null) {
            sqlite.execSQL("delete from CurrentSong;");
            sqlite.execSQL("insert into CurrentSong(position,PlayState) values(?,?);", new Object[]{position, PlayState});
            close();
            flag = true;
        }
        return flag;
    }

    public Object[] getCurrentSong() {
        Object[] Current = null;
        if (sqlite != null) {
            Current = new Object[2];
            Cursor cr = sqlite.rawQuery("select * from CurrentSong;", new String[]{});
            while (cr.moveToNext()) {
                int position = cr.getInt(cr.getColumnIndex("position"));
                String PlayState = cr.getString(cr.getColumnIndex("PlayState"));
                Current[0] = position;
                Current[1] = PlayState;
            }
            cr.close();
            close();
        }
        return Current;
    }


    public List<MusicInfo> getMusicList() {
        List<MusicInfo> MusicList = null;
        if (sqlite != null) {
            MusicList = new ArrayList<MusicInfo>();
            Cursor cr = sqlite.rawQuery("select * from LocalMusicList;", new String[]{});
            while(cr.moveToNext()){
                MusicInfo music = new MusicInfo();
                music.setMusicID(cr.getLong(cr.getColumnIndex("MusicID")));
                music.setMusicTitle(cr.getString(cr.getColumnIndex("MusicTitle")));
                music.setMusicArtist(cr.getString(cr.getColumnIndex("MusicArtist")));
                music.setMusicTime(cr.getLong(cr.getColumnIndex("MusicTime")));
                music.setMusicSize(cr.getLong(cr.getColumnIndex("MusicSize")));
                music.setMusicPath(cr.getString(cr.getColumnIndex("MusicPath")));
                music.setIco(cr.getString(cr.getColumnIndex("Ico")));
                MusicList.add(music);
            }
            close();
        }
        if (MusicList.size() < 1) {
            MusicList = null;
        }
        return MusicList;
    }

    public void setMusicList(List<MusicInfo> MusicList) {
        for (MusicInfo music : MusicList) {
            if (existMusic(music.getMusicID())) {
                deleteMusic(music.getMusicID());
            }
            insertMusic(music);
        }
        close();
    }

    //插入本地歌曲信息到数据库中
    public void insertMusic(MusicInfo music) {
        sqlite.execSQL("insert into " +
                        "LocalMusicList(" +
                        "MusicID," +
                        "MusicTitle," +
                        "MusicArtist," +
                        "MusicTime," +
                        "MusicSize," +
                        "MusicPath," +
                        "ico) " +
                        "values(?,?,?,?,?,?,?);",
                new Object[]{
                        music.getMusicID(),
                        music.getMusicTitle(),
                        music.getMusicArtist(),
                        music.getMusicTime(),
                        music.getMusicSize(),
                        music.getMusicPath(),
                        music.getIco()});
    }

    //判断数据库中是否存在相应的歌曲信息
    public boolean existMusic(long MusicID) {
        boolean flag = false;
        Cursor cr = sqlite.rawQuery("select * from LocalMusicList where MusicID = ?;", new String[]{String.valueOf(MusicID)});
        flag = cr.moveToNext();
        cr.close();
        return flag;
    }

    //删除数据库中相应的歌曲
    public void deleteMusic(long MusicID) {
        sqlite.execSQL("delete from LocalMusicList where MusicID = ?;", new Object[]{MusicID});
    }

    public void close() {
        sqlite.close();
    }
}
