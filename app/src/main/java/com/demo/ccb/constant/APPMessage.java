package com.demo.ccb.constant;

/**
 * Created by godfa on 2016/3/18.
 */
public final class APPMessage {

    public static final class PlayMsg {
        public static final int play = 0;
        public static final int pause = 1;
        public static final int stop = 2;
        public static final int replay = 3;
        public static final int playover = 4;
        public static final int playtime = 5;
    }

    public static final class APPPath{
        public static final String ExistSD ="/sdcard/MusicApp/";
        public static final String NoExistSD ="/data/data/ccb.demo.com.studio/";
    }
}
