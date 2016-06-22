package com.demo.ccb.util;

import android.util.Log;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;



/**
 * Created by godfa on 2016/3/20.
 * 该API使用的是网易云音乐的web API
 * 由https://github.com/darknessomi/musicbox中相应的脚本改写而来
 * wiki:https://github.com/darknessomi/musicbox/wiki
 */
public class API {

    private final ArrayList<NameValuePair> headerList = new ArrayList<>();

    public API() {
        headerList.add(new BasicNameValuePair("Accept", "*/*"));
        headerList.add(new BasicNameValuePair("Accept-Language", "zh-CN,zh;q=0.8,gl;q=0.6,zh-TW;q=0.4"));
        headerList.add(new BasicNameValuePair("Connection", "keep-alive"));
        headerList.add(new BasicNameValuePair("Content-Type", "application/x-www-form-urlencoded"));
        headerList.add(new BasicNameValuePair("User-Agent",
                "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_9_2) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/33.0.1750.152 Safari/537.36"));
        headerList.add(new BasicNameValuePair("Host", "music.163.com"));
        headerList.add(new BasicNameValuePair("Referer", "http://music.163.com/search/"));
    }


    public JSONObject search(String s, int type, int offset, String total, int limit) throws Exception {

        String action = "http://music.163.com/api/search/get";
        List<NameValuePair> data = new ArrayList<>();

        data.add(new BasicNameValuePair("s", s));
        data.add(new BasicNameValuePair("type", "" + type));
        data.add(new BasicNameValuePair("offset", "" + offset));
        data.add(new BasicNameValuePair("total", total));
        data.add(new BasicNameValuePair("limit", "" + limit));

        return httpRequest("POST", action, data);
    }

    /*
    *把HTTP请求返回的数据封装成json对象
    *
    * */
    private JSONObject httpRequest(String method, String action, List<NameValuePair> query) throws Exception {
        String result;
        result = rawHttpRequest(method, action, query);
        return new JSONObject(result);
    }

    /*
    * 发送HTTP请求，并返回结果
    *
    * */
    public String rawHttpRequest(String method, String action, List<NameValuePair> query) throws Exception {
 /*       String result = null;
        HttpClient httpClient = new DefaultHttpClient();
        HttpResponse httpResponse = null;
        if ("GET".equals(method)) {
            String url = action;
            if (query != null)
                url = action + "?" + query.toString();
            HttpGet connection = new HttpGet(url);
            for (int i = 0; i < headerList.size(); i++) {
                connection.addHeader(headerList.get(i).getName(), headerList.get(i).getValue());
            }
            httpResponse = httpClient.execute(connection);
        } else if ("POST".equals(method)) {
            HttpPost connection = new HttpPost(action);
            for (int i = 0; i < headerList.size(); i++) {
                connection.addHeader(headerList.get(i).getName(), headerList.get(i).getValue());
            }
            connection.setEntity(new UrlEncodedFormEntity(query, HTTP.UTF_8));
            httpResponse = httpClient.execute(connection);
        }
        if (httpResponse != null && httpResponse.getStatusLine().getStatusCode() == 200) {
            result = EntityUtils.toString(httpResponse.getEntity(), "UTF-8");
        }*/


        String result = null;
        StringBuffer Params = new StringBuffer();
        byte[] buffer = new byte[4 * 1024];
        int len = -1;
        StringBuffer stringBuffer = new StringBuffer();

        URLConnection conn = new URL(action).openConnection();
        conn.setReadTimeout(4000);
        for (int i = 0; i < headerList.size(); i++) {
            conn.setRequestProperty(headerList.get(i).getName(), headerList.get(i).getValue());
        }

        //得到输入流
        BufferedInputStream bis = null;

        if ("GET".equals(method)){
            bis = new BufferedInputStream(conn.getInputStream());
            while ((len = bis.read(buffer)) != -1) {
                stringBuffer.append(new String(buffer,0,len));
            }
            result = stringBuffer.toString();
        }else if ("POST".equals(method)&&query!=null){

            Params.append("1").append("=").append("1");

            for (NameValuePair param:
                    query) {
                Params.append("&").append(param.getName()).append("=").append(param.getValue());
            }

            // 发送POST请求必须设置如下两行
            conn.setDoOutput(true);
            conn.setDoInput(true);

            //发送参数
            PrintWriter out = null;
            out = new PrintWriter(conn.getOutputStream());
            out.print(Params.toString());
            out.flush();


            bis = new BufferedInputStream(conn.getInputStream());
            while ((len = bis.read(buffer)) != -1) {
                stringBuffer.append(new String(buffer,0,len));
            }
            result = stringBuffer.toString();
        }
        return result;
    }

    //获取歌曲的详细信息从music_id
    public String song_detail(long music_id) {
        String mp3Url;
        JSONObject data;
        JSONArray songs;
        String action = "http://music.163.com/api/song/detail/?id=" + music_id + "&ids=[" + music_id + "]";
        try {
            data = this.httpRequest("GET", action, null);
            songs = data.getJSONArray("songs");
            mp3Url = ((JSONObject) songs.get(0)).getString("mp3Url");
        } catch (Exception e) {
            // TODO Auto-generated catch block
            mp3Url = null;
            e.printStackTrace();
        }
        return mp3Url;
    }

    //从网络上下载文件
    public void DownLoad(String urlStr, String fileName, String savePath) throws IOException {

        URL url = new URL(urlStr);
        URLConnection conn = url.openConnection();
        //设置超时间为4秒
        conn.setConnectTimeout(4 * 1000);
        //防止屏蔽程序抓取而返回403错误
        for (int i = 0; i < 5; i++) {
            conn.setRequestProperty(headerList.get(i).getName(), headerList.get(i).getValue());
        }

        //得到输入流
        BufferedInputStream bis = new BufferedInputStream(conn.getInputStream());
        // 定义一个带缓冲的输入流 。

        //得到输出流
        File f = new File(savePath + fileName);
        if (f.exists()) {
            f.delete();
        }
        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(f)); // 定义一个带缓冲的输出流。

        byte[] buffer = new byte[4 * 1024];
        int len = -1;
        while ((len = bis.read(buffer)) != -1) {
            bos.write(buffer, 0, len);
        }
        bos.flush();
        if (bis != null)
            bis.close();
        if (bos != null)
            bos.close();
    }

    //从网络上下载歌词
    public void DownLoadLrc(String musicid, String fileName, String savePath) throws Exception {
        String url = "http://music.163.com/api/song/lyric?os=osx&id=$$&lv=-1&kv=-1&tv=-1";
        url = url.replace("$$", musicid);
        String res = rawHttpRequest("GET", url, null);
        Log.i("下载歌词", res);
        File f = new File(savePath + fileName);
        Log.i("下载歌词", savePath + fileName);
        if (f.exists()) {
            f.delete();
        }
        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(f));
        byte[] bytes = res.getBytes();
        bos.write(bytes, 0, bytes.length);
        bos.flush();
        bos.close();
    }
}
