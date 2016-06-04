package com.demo.hwq.util;

import android.util.Log;

import com.demo.hwq.constant.APPMessage;
import com.demo.hwq.vo.LrcContent;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by godfa on 2016/6/3.
 * 歌词处理的工具类
 */
public class LrcUtil {
    private List<LrcContent> lrcList; //List集合存放歌词内容对象
    private LrcContent mLrcContent;     //声明一个歌词内容对象
    /**
     * 无参构造函数用来实例化对象
     */
    public LrcUtil() {
        lrcList = new ArrayList<LrcContent>();
    }


    /**
     * 读取歌词
     * @param path
     * @return
     */
    public int readLRC(String path){
        File f = new File(path);
        BufferedReader br = null;
        try {
                br = new BufferedReader(new FileReader(f));
                String line = "";
                if ((line=br.readLine())!=null){
                    JSONObject lrcFile = new JSONObject(line);
                    JSONObject lrc = lrcFile.getJSONObject("lrc");
                    String lyric = lrc.getString("lyric").replace("[","");
                    String[] lyricItems = lyric.split("\n");
                    for (int i = 0;i<lyricItems.length;i++){
                        mLrcContent = new LrcContent();
                        String lyricItem = lyricItems[i];
                        String [] s = lyricItem.split("]");
                        if (s.length>1){
                            mLrcContent.setLrcTime(Str2time(s[0]));
                            mLrcContent.setLrcStr(s[1]);
                            lrcList.add(mLrcContent);
                        }
                    }
                }
        } catch (FileNotFoundException e) {
                Log.e("LrcUtil Exception", e.getMessage());
                return APPMessage.LrcMsg.LrcNotFind;
            } catch (IOException e) {
                Log.e("LrcUtil Exception", e.getMessage());
                return APPMessage.LrcMsg.LrcNotFind;
        } catch (JSONException e) {
            Log.e("LrcUtil Exception", e.getMessage());
            return APPMessage.LrcMsg.LrcNotFind;
        }finally {
            try {
                if (br!=null)
                br.close();
            } catch (IOException e) {
                Log.e("LrcUtil Exception", e.getMessage());
            }
        }
        return APPMessage.LrcMsg.LrcReadSuccess;
    }

    /**
     * 解析歌词时间
     * 歌词内容格式如下：
     * [00:02.032]陈奕迅
     * [00:03.043]好久不见
     * [00:05.022]歌词制作  王涛
     * @param Strtime
     * @return
     */
    public int Str2time(String Strtime) {
        Strtime = Strtime.replace(":", ".");
        Strtime = Strtime.replace(".", "@");

        String timeData[] = Strtime.split("@"); //将时间分隔成字符串数组

        //分离出分、秒并转换为整型
        int minute = Integer.parseInt(timeData[0]);
        int second = Integer.parseInt(timeData[1]);
        int millisecond = Integer.parseInt(timeData[2]);

        //计算上一行与下一行的时间转换为毫秒数
        int currentTime = (minute * 60 + second) * 1000 + millisecond;
        return currentTime;
    }


    public List<LrcContent> getLrcList() {
        return lrcList;
    }
}
