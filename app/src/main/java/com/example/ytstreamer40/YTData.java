package com.example.ytstreamer40;

import android.graphics.drawable.Drawable;

import com.chaquo.python.PyObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class YTData {

    PyObject pyObject;
    String stream_url,thumb_url,url;

    public YTData(String url,String thumb_url)
    {
        pyObject = MainActivity.main;
        this.stream_url =  pyObject.callAttr("stream",url).toString();

        this.url = url;
        this.thumb_url = thumb_url;
    }

    public YTData(String url)
    {
        pyObject = MainActivity.main;
        YTData.this.stream_url =  pyObject.callAttr("stream",url).toString();
        YTData.this.url = url;
    }

    public Drawable getThumb()
    {
        Drawable img = null;
        try {
            InputStream ipStream = new URL(thumb_url).openStream();
            img = Drawable.createFromStream(ipStream,"youTube_Thumbnail");
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return img;
    }

    public String getAudioStreamLink()
    {
        return stream_url;
    }
}
