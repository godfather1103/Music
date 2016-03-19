package com.demo.ccb.util;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.demo.ccb.constant.APPMessage;

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
            sqlite.execSQL("insert into CurrentSong(position,PlayState) values(?,?);",new Object[]{position,PlayState});
            close();
            flag = true;
        }
        return flag;
    }

    public Object[] getCurrentSong(){
        Object[] Current = null;
        if (sqlite != null){
            Current = new Object[2];
            Cursor cr = sqlite.rawQuery("select * from CurrentSong;",new String[]{});
            while (cr.moveToNext()){
                int position = cr.getInt(cr.getColumnIndex("position"));
                String PlayState = cr.getString(cr.getColumnIndex("PlayState"));
                Current[0] = position;
                Current[1] = PlayState;
            }
            close();
        }
        return Current;
    }


    public void close() {
        sqlite.close();
    }
}
