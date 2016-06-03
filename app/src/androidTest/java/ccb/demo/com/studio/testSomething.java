package ccb.demo.com.studio;

import android.test.AndroidTestCase;
import android.util.Log;

import com.demo.hwq.util.API;
import com.demo.hwq.util.DBUtil;
import com.demo.hwq.util.MusicAppUtil;
import com.demo.hwq.vo.MusicInfo;

import junit.framework.Assert;

import org.junit.Test;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

/**
 * Created by godfa on 2016/5/27.
 */
public class testSomething extends AndroidTestCase {

    @Test
    public void testDownload() throws Exception {
/*        List<MusicInfo> list = new DBUtil().getMusicListInNetTable();
        MusicInfo musicInfo = list.get(0);
        boolean f = MusicAppUtil.downloadLrcFile(musicInfo);
        Assert.assertEquals(f,true);*/
        String url = "http://192.168.0.105:8080/img/img1_large.jpg";
        url = "http://music.163.com/api/song/lyric?os=osx&id=30837&lv=-1&kv=-1&tv=-1";
        String sdcard = MusicAppUtil.checkFileAndFolder();
        Log.i("我的测试消息", sdcard);
        API api = new API();
        String res = api.rawHttpRequest("GET",url,null);
        Log.i("我的测试消息", res);
        File f = new File(sdcard+"a.gc");
        if (f.exists()){
            f.delete();
        }
        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(f));
        byte[] bytes = res.getBytes();
        bos.write(bytes,0,bytes.length);
        bos.flush();
        bos.close();
        //DownLoad(url, "a.jpg", sdcard);
    }


    public void DownLoad(String urlStr, String fileName, String savePath) throws IOException {

        URL url = new URL(urlStr);
        URLConnection conn = url.openConnection();
        //设置超时间为3秒
        conn.setConnectTimeout(3 * 1000);
        //防止屏蔽程序抓取而返回403错误
        conn.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)");

        //得到输入流
        BufferedInputStream bis = new BufferedInputStream(conn.getInputStream());;// 定义一个带缓冲的输入流 。

        //得到输出流
        File f = new File(savePath + fileName);
        if (f.exists()){
            f.delete();
        }
        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(f)); // 定义一个带缓冲的输出流。

        byte[] buffer = new byte[4*1024];
        int len=-1;
        while((len = bis.read(buffer)) != -1) {
            bos.write(buffer,0,len);
        }
        bos.flush();
        //bis.close();
        if (bis != null)
            bis.close();
        if (bos != null)
            bos.close();
    }
}
