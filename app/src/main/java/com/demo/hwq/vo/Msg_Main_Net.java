package com.demo.hwq.vo;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

/**
 * Created by godfa on 2016/3/21.
 */
public class Msg_Main_Net implements Parcelable {

    private List<MusicInfo> MusicList;
    private MusicInfo CurrentSong;
    private Object [] info = new Object[2];

    public Msg_Main_Net(List<MusicInfo> musicList, MusicInfo currentSong) {
        MusicList = musicList;
        CurrentSong = currentSong;
        info[0] = MusicList;
        info[1] = CurrentSong;
    }

    public List<MusicInfo> getMusicList() {
        return MusicList;
    }

    public void setMusicList(List<MusicInfo> musicList) {
        MusicList = musicList;
    }

    public MusicInfo getCurrentSong() {
        return CurrentSong;
    }

    public void setCurrentSong(MusicInfo currentSong) {
        CurrentSong = currentSong;
    }

    protected Msg_Main_Net(Parcel in) {
        //MusicList = in.readArrayList();
        info = in.readArray(Object.class.getClassLoader());
        MusicList = (List<MusicInfo>) info[0];
        CurrentSong = (MusicInfo) info[1];
    }


    @Override
    public void writeToParcel(Parcel dest, int flags) {
        info[0] = MusicList;
        info[1] = CurrentSong;
        dest.writeArray(info);
    }

    public static final Creator<Msg_Main_Net> CREATOR = new Creator<Msg_Main_Net>() {
        @Override
        public Msg_Main_Net createFromParcel(Parcel in) {
            return new Msg_Main_Net(in);
        }

        @Override
        public Msg_Main_Net[] newArray(int size) {
            return new Msg_Main_Net[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

}
