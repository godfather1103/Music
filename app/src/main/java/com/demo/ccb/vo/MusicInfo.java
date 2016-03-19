package com.demo.ccb.vo;

/*
 * 音乐的实体类，存放每个音乐文件的信息
 * 
 * */
public class MusicInfo {

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
}
