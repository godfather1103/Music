package com.demo.hwq.constant;

import android.os.Environment;

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
        public static final int searchSuccess = 6;
        public static final int beginSearch = 7;
        public static final int searchFail = 8;
    }

    public static final class APPPath{
        public static final String ExistSD = Environment.getExternalStorageDirectory().getPath() + "/MusicApp/";
        public static final String NoExistSD ="/data/data/ccb.demo.com.studio/";
    }

    public static final class NetPlayMsg{
        public static final int download = 9;
        public static final int downloadSuccess = 10;
        public static final int downloadFail = 11;


    }
}
