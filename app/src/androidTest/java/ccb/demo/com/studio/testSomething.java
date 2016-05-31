package ccb.demo.com.studio;

import android.test.AndroidTestCase;
import android.util.Log;

import com.demo.hwq.util.MusicAppUtil;

import junit.framework.Assert;

import org.junit.Test;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by godfa on 2016/5/27.
 */
public class testSomething extends AndroidTestCase {

    @Test
    public void testDownload() throws IOException {
        String url = "http://192.168.0.105:8080/img/img1_large.jpg";
        String sdcard = MusicAppUtil.checkFileAndFolder();
        //logger.info(sdcard);
        Log.i("我的测试消息", sdcard);
        //DownLoad("http://192.168.0.105:8080/img/abc.flac", "a.flac", sdcard);
        DownLoad(url, "a.jpg", sdcard);
        String a = "abc";
        Assert.assertEquals("abc", a);

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
