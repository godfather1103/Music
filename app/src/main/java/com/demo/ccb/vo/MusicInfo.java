package com.demo.ccb.vo;

import android.os.Parcel;
import android.os.Parcelable;

/*
 * 音乐的实体类，存放每个音乐文件的信息
 * 
 * */
public class MusicInfo implements Parcelable, Cloneable {

    /*
     * 音乐的
     * ID
     * 标题
     * 艺术家
     * 音乐时间长度
     * 音乐大小
     * 音乐路径
     * 图标
     * * */
    private long MusicID;
    private String MusicTitle;
    private String MusicArtist;
    private long MusicTime;
    private long MusicSize;
    private String MusicPath;
    private String ico;

    public MusicInfo() {
        super();
    }

    public MusicInfo(long musicID, String musicTitle, String musicArtist, long musicTime, long musicSize,
                     String musicPath) {
        super();
        MusicID = musicID;
        MusicTitle = musicTitle;
        MusicArtist = musicArtist;
        MusicTime = musicTime;
        MusicSize = musicSize;
        MusicPath = musicPath;
    }

    private MusicInfo(Parcel in) {
        MusicID = in.readLong();
        MusicTitle = in.readString();
        MusicArtist = in.readString();
        MusicTime = in.readLong();
        MusicSize = in.readLong();
        MusicPath = in.readString();
        ico = in.readString();
    }

    public static final Creator<MusicInfo> CREATOR = new Creator<MusicInfo>() {
        @Override
        public MusicInfo createFromParcel(Parcel in) {
            return new MusicInfo(in);
        }

        @Override
        public MusicInfo[] newArray(int size) {
            return new MusicInfo[size];
        }
    };

    public long getMusicID() {
        return MusicID;
    }

    public void setMusicID(long musicID) {
        MusicID = musicID;
    }

    public String getMusicTitle() {
        return MusicTitle;
    }

    public void setMusicTitle(String musicTitle) {
        MusicTitle = musicTitle;
    }

    public String getMusicArtist() {
        return MusicArtist;
    }

    public void setMusicArtist(String musicArtist) {
        MusicArtist = musicArtist;
    }

    public long getMusicTime() {
        return MusicTime;
    }

    public void setMusicTime(long musicTime) {
        MusicTime = musicTime;
    }

    public long getMusicSize() {
        return MusicSize;
    }

    public void setMusicSize(long musicSize) {
        MusicSize = musicSize;
    }

    public String getMusicPath() {
        return MusicPath;
    }

    public void setMusicPath(String musicPath) {
        MusicPath = musicPath;
    }

    public String getIco() {
        return ico;
    }

    public void setIco(String ico) {
        this.ico = ico;
    }

    @Override
    public String toString() {
        return "MusicInfo{" +
                "MusicID=" + MusicID +
                ", MusicTitle='" + MusicTitle + '\'' +
                ", MusicArtist='" + MusicArtist + '\'' +
                ", MusicTime=" + MusicTime +
                ", MusicSize=" + MusicSize +
                ", MusicPath='" + MusicPath + '\'' +
                ", ico='" + ico + '\'' +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(MusicID);
        dest.writeString(MusicTitle);
        dest.writeString(MusicArtist);
        dest.writeLong(MusicTime);
        dest.writeLong(MusicSize);
        dest.writeString(MusicPath);
        dest.writeString(ico);
    }

    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
