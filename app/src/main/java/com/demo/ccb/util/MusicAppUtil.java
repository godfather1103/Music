package com.demo.ccb.util;

import java.util.ArrayList;
import java.util.List;


import android.content.ContentResolver;
import android.database.Cursor;
import android.provider.MediaStore;

import com.demo.ccb.vo.MusicInfo;

public class MusicAppUtil {
	
	/** 
	* 用于从数据库中查询歌曲的信息，保存在List当中 
	* 
	* @return 
	*/


	public static List<MusicInfo> getMusicListFromDB(ContentResolver cr){
		List<MusicInfo> MusicList = null;
		MusicList = new DBUtil().getMusicList();
		if (MusicList==null){
			MusicList = getMusicListFromSD(cr);
			new DBUtil().setMusicList(MusicList);
		}
		return MusicList;
	}


	public static List<MusicInfo> getMusicListFromSD(ContentResolver cr) {
		List<MusicInfo> MusicList = new ArrayList<MusicInfo>();
		Cursor cursor = cr.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, 
			    null, null, null, MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
		MusicInfo music = null;
		while(cursor.moveToNext()){
			int isMusic = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.IS_MUSIC));//是否为音乐 
			if(isMusic != 0){
				music = new MusicInfo();
				long al = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID));
				music.setMusicID(cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media._ID)));
				music.setMusicTitle(cursor.getString((cursor.getColumnIndex(MediaStore.Audio.Media.TITLE))));
				music.setMusicArtist(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)));
				music.setMusicTime(cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION)));
				music.setMusicSize(cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.SIZE)));
				music.setMusicPath(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA)));
				music.setIco("content://media/external/audio/media/" +music.getMusicID()+ "/albumart");
				MusicList.add(music);
			}
		}
		return MusicList;
	}
	
}
