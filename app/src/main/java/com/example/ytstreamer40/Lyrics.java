package com.example.ytstreamer40;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.chaquo.python.PyObject;

import java.util.List;

public class Lyrics extends AppCompatActivity {

    ListView lyrics;
    ImageView loading;
    List<PyObject> links;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lyrics);

        lyrics = findViewById(R.id.lyrics_list);
        loading = findViewById(R.id.loading);

        lyrics.setVisibility(View.GONE);
        loading.setVisibility(View.VISIBLE);

        lyrics.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String url = links.get(position).toString();
                boolean browser = Settings.LyricsSettings.lyrics_in.equals("Browser");

                Intent intent;
                if(browser)
                {
                    intent = new Intent();
                    intent.setData(Uri.parse(url));
                }

                else
                {
                    intent = new Intent(Lyrics.this,WebBrowserAct.class);
                    intent.putExtra("url",url);
                }
                startActivity(intent);
            }
        });

        Intent intent = getIntent();
        String song = intent.getStringExtra("query");

        Glide.with(this).load(R.drawable.loading_pink_list).into(loading);

        new Thread(new Runnable() {
            @Override
            public void run() {

                links = MainActivity.main.callAttr("lyrics",song).asList();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        if(links.size()<3)
                        {
                            loading.setImageResource(R.drawable.noresultfound);
                        }

                        else
                        {
                            lyrics.setVisibility(View.VISIBLE);
                            loading.setVisibility(View.GONE);

                            lyrics.setAdapter(new BaseAdapter() {
                                @Override
                                public int getCount() {
                                    return links.size();
                                }

                                @Override
                                public Object getItem(int position) {
                                    return links.get(position).toString();
                                }

                                @Override
                                public long getItemId(int position) {
                                    return 0;
                                }

                                @SuppressLint("InflateParams")
                                @Override
                                public View getView(int position, View convertView, ViewGroup parent) {
                                    if(convertView==null)
                                    {
                                        String url = links.get(position).toString();

                                        LayoutInflater inflater = getLayoutInflater();
                                        convertView = inflater.inflate(R.layout.custom_lyrics_list,null);

                                        TextView lyrics = convertView.findViewById(R.id.lyrics);
                                        url = url.replace("https://","").replaceFirst("http://","");
                                        if(!url.startsWith("www."))
                                        {
                                            url = "www." + url;
                                        }
                                        lyrics.setText(url.split("/")[0]);

                                    }
                                    return convertView;
                                }
                            });

                        }
                    }
                });
            }
        }).start();
    }
}