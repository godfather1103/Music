package com.demo.ccb.util;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.DeflateDecompressingEntity;
import org.apache.http.client.entity.GzipDecompressingEntity;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;

//import sun.misc.BASE64Encoder;

/**
 * Created by godfa on 2016/3/20.
 * 该API使用的是网易云音乐的web API
 * 由https://github.com/darknessomi/musicbox中相应的脚本改写而来
 *wiki:https://github.com/darknessomi/musicbox/wiki
 *
 */
public class API {

    HttpURLConnection connection = null;
    ArrayList<NameValuePair> headerList = new ArrayList<NameValuePair>();
    ArrayList<NameValuePair> cookies = new ArrayList<NameValuePair>();
    int default_timeout = 10;
    String modulus = "00e0b509f6259df8642dbc35662901477df22677ec152b5ff68ace615bb7b725152b3ab17a876aea8a5aa76d2e417629ec4ee341f56135fccf695280104e0312ecbda92557c93870114af6c9d05c4f7f0c3685b7a46bee255932575cce10b424d813cfe4875d3e82047b97ddef52741d546b8e289dc6935b3ece0462db0a22b8e7";
    String nonce = "0CoJUm6Qyw8W8jud";
    String pubKey = "010001";

    public API() {
        headerList.add(new BasicNameValuePair("Accept", "*/*"));
        headerList.add(new BasicNameValuePair("Accept-Language", "zh-CN,zh;q=0.8,gl;q=0.6,zh-TW;q=0.4"));
        headerList.add(new BasicNameValuePair("Connection", "keep-alive"));
        headerList.add(new BasicNameValuePair("Content-Type", "application/x-www-form-urlencoded"));
        headerList.add(new BasicNameValuePair("Host", "music.163.com"));
        headerList.add(new BasicNameValuePair("Referer", "http://music.163.com/search/"));
        headerList.add(new BasicNameValuePair("User-Agent",
                "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_9_2) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/33.0.1750.152 Safari/537.36"));
        cookies.add(new BasicNameValuePair("appver", "1.5.2"));
    }

    // 歌曲加密算法, 基于https://github.com/yanunon/NeteaseCloudMusic脚本修改后实现
    public String encrypted_id(long id) throws Exception {
        MessageDigest md5 = MessageDigest.getInstance("MD5");
       // BASE64Encoder base64en = new BASE64Encoder();
        String result = null;
       byte[] magic = "3go8&$8*3*3h0k(2)2".getBytes();
        byte[] song_id = new byte[(int) id];
       int magic_len = magic.length;
        for (int i = 0; i < song_id.length; i++) {
            song_id[i] = (byte) (song_id[i] ^ magic[i % magic_len]);
        }
       // result = base64en.encode(md5.digest(song_id));
        result = result.replace('/', '_');
        result = result.replace('+', '-');
        return result;
    }

    public JSONObject search(String s, int stype, int offset, String total, int limit) throws Exception {

        String action = "http://music.163.com/api/search/get";
        List<NameValuePair> data = new ArrayList<NameValuePair>();

        data.add(new BasicNameValuePair("s", s));
        data.add(new BasicNameValuePair("type", "" + stype));
        data.add(new BasicNameValuePair("offset", "" + offset));
        data.add(new BasicNameValuePair("total", total));
        data.add(new BasicNameValuePair("limit", "" + limit));

        return httpRequest("POST", action, data, null, null, 0);
    }

    public JSONObject httpRequest(String method, String action, List<NameValuePair> query, String urlencoded,
                                  String callback, int timeout) throws Exception{
        //Gson gson = new Gson();

        String result = null;
        result = rawHttpRequest(method, action, query, urlencoded, callback, timeout);
        return new JSONObject(result);
    }

    public String rawHttpRequest(String method, String action, List<NameValuePair> query, String urlencoded,
                                 String callback, int timeout) throws Exception {
        String result = null;
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
            result = EntityUtils.toString(httpResponse.getEntity(),"UTF-8");
        }
        return result;
    }

    //获取歌曲的详细信息从music_id
    public String song_detail(long music_id){
        String mp3Url = null;
        JSONObject data = null;
        JSONArray songs = null;
        String action = "http://music.163.com/api/song/detail/?id=" + music_id + "&ids=[" + music_id + "]";
        try {
            data = this.httpRequest("GET", action, null, null, null, 0);
            songs = data.getJSONArray("songs");
            mp3Url = ((JSONObject)songs.get(0)).getString("mp3Url");
        } catch (Exception e) {
            // TODO Auto-generated catch block
            mp3Url = null;
            e.printStackTrace();
        }
        return mp3Url;
    }

}