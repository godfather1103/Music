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
            if (cr.moveToNext()) {
                int position = cr.getInt(cr.getColumnIndex("position"));
                String PlayState = cr.getString(cr.getColumnIndex("PlayState"));
                Current[0] = position;
                Current[1] = PlayState;
            } else {
                Current = null;
            }
            cr.close();
            close();
        }
        return Current;
    }

    //从数据库中获取本地歌曲列表
    public List<MusicInfo> getMusicList() {
        List<MusicInfo> MusicList = null;
        if (sqlite != null) {
            MusicList = new ArrayList<MusicInfo>();
            Cursor cr = sqlite.rawQuery("select * from LocalMusicList;", new String[]{});
            while (cr.moveToNext()) {
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

    //插入本地歌曲列表到本地歌曲数据库中
    public void setMusicList(List<MusicInfo> MusicList) {
        for (MusicInfo music : MusicList) {
            if (existMusic(music.getMusicID())) {
                deleteMusic(music.getMusicID());
            }
            insertMusic(music);
        }
        close();
    }

    //插入本地歌曲信息到本地歌曲数据库中
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

    //判断本地歌曲数据库中是否存在相应的歌曲信息
    public boolean existMusic(long MusicID) {
        boolean flag = false;
        Cursor cr = sqlite.rawQuery("select * from LocalMusicList where MusicID = ?;", new String[]{String.valueOf(MusicID)});
        flag = cr.moveToNext();
        cr.close();
        return flag;
    }

    //删除本地歌曲数据库中相应的歌曲
    public void deleteMusic(long MusicID) {
        sqlite.execSQL("delete from LocalMusicList where MusicID = ?;", new Object[]{MusicID});
    }


    //从数据库中获取网络歌曲列表
    public List<MusicInfo> getMusicListInNetTable() {
        List<MusicInfo> MusicList = null;
        if (sqlite != null) {
            MusicList = new ArrayList<MusicInfo>();
            Cursor cr = sqlite.rawQuery("select * from NetMusicList;", new String[]{});
            while (cr.moveToNext()) {
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


    //插入网络歌曲信息到网络歌曲数据库中
    public void setMusicListInNetTable(List<MusicInfo> MusicList) {
        deleMusicInNetTable();
        for (MusicInfo music : MusicList) {
            if (existMusicInNetTable(music.getMusicID())) {
                deleMusicInNetTable(music.getMusicID());
            }
            insertMusicInNetTable(music);
        }
        close();
    }


    //插入网络歌曲信息到网络歌曲数据库中
    public void insertMusicInNetTable(MusicInfo music) {
        sqlite.execSQL("insert into " +
                        "NetMusicList(" +
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

    //判断网络歌曲数据库中是否存在相应的歌曲信息
    public boolean existMusicInNetTable(long MusicID) {
        boolean flag = false;
        Cursor cr = sqlite.rawQuery("select * from NetMusicList where MusicID = ?;", new String[]{String.valueOf(MusicID)});
        flag = cr.moveToNext();
        cr.close();
        return flag;
    }

    //删除网络歌曲数据库中相应的歌曲
    public void deleMusicInNetTable(long MusicID) {
        sqlite.execSQL("delete from NetMusicList where MusicID = ?;", new Object[]{MusicID});
    }

    //删除网络歌曲列表数据库中的数据
    public void deleMusicInNetTable() {
        sqlite.execSQL("delete from NetMusicList;");
    }

    public void close() {
        sqlite.close();
    }
}
