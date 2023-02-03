package com.example.ytstreamer40;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Environment;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import java.net.URL;
import java.net.URLConnection;

@SuppressWarnings("ALL")
@SuppressLint("StaticFieldLeak")
public class Downloader extends AsyncTask {

    final int BUFFER_SIZE = 100;
    String stream_url,error,file_name;
    static String dir = Environment.getExternalStorageDirectory() +"/" + Environment.DIRECTORY_DOWNLOADS;
    File data;
    static String YT_default = " - YTStreamer";
    int total,read=0,count,progress;
    private String saved_file_name;
    boolean notified = false;

    Downloader(String file_name, String stream_url)
    {
        this.saved_file_name = file_name + YT_default + ".mp3";
        this.file_name = file_name;
        this.stream_url = stream_url;
    }

    static String getSavedFilePath(String file_name)
    {
        file_name = file_name.replaceAll("[^A-Za-z 0-9]","")  + YT_default + ".mp3";
        return new File(dir,file_name).getPath();
    }

    public static boolean new_file(String file_name)
    {
        file_name+= YT_default + ".mp3";
        File data = new File(dir,file_name);

        return !data.exists();
    }

    public boolean is_downloading()
    {
        return getProgress()!=100;
    }

    public String getTitle()
    {
        return file_name;
    }

    @Override
    protected void onProgressUpdate(Object[] values) {
        super.onProgressUpdate(values);
    }

    @Override
    protected Object doInBackground(Object[] objects) {
        download(stream_url);
        return null;
    }

    @Override
    protected void onPostExecute(Object o) {
        if(getProgress()!=100 && error==null)
        {
            data.delete();
        }
        super.onPostExecute(o);
    }

    public void start()
    {
        new Thread(){
            @Override
            public void run() {
                doInBackground(null);
                super.run();
            }
        }.start();
    }

    public boolean download(String website) {

        File data = new File(dir,saved_file_name);

        try
        {
            URL url = new URL(website);
            URLConnection connection = url.openConnection();
            connection.connect();
            InputStream stream = url.openStream();
            OutputStream out_stream = new FileOutputStream(data.getPath());
            byte[] bytes = new byte[BUFFER_SIZE];
            total = connection.getContentLength();
            while ((count = stream.read(bytes))!=-1)
            {
                read+=count;
                progress = (read*100)/total;
                out_stream.write(bytes,0,count);
            }
            out_stream.flush();
            out_stream.close();
            stream.close();
        }

        catch (IOException e)
        {
            error = "Error occured";
            data.delete();
            return false;
        }
        return true;
    }

    public int getProgress()
    {
        return progress;
    }

    public boolean is_notified()
    {
        return notified;
    }

    public void set_notified(boolean notified)
    {
        this.notified = notified;
    }
}
