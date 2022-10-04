package com.example.ytstreamer40;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;

import com.chaquo.python.PyObject;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.squareup.picasso.Picasso;

import java.util.Map;

public class ListAdapterClass extends BaseAdapter {

    Context context;
    PyObject[] videos;
    LayoutInflater inflater;

    @RequiresApi(api = Build.VERSION_CODES.N)
    public ListAdapterClass(Context context,PyObject[] videos)
    {

        this.videos = videos;
        this.context = context;
    }

    @Override
    public int getCount() {
        return videos.length;
    }

    @Override
    public Object getItem(int position) {
        return videos[position];
    }

    @Override
    public long getItemId(int position) {
        return videos[position].id();
    }

    String getString(Map<PyObject,PyObject> video_data,String key)
    {
        PyObject object_key  = PyObject.fromJava(key);
        PyObject res = video_data.get(object_key);
        if(res==null) return "Not Found";
        else return res.toString();
    }

    @SuppressLint({"ViewHolder", "InflateParams"})
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        inflater = LayoutInflater.from(context);
        convertView = inflater.inflate(R.layout.coustom_list_view,null);

        ImageView thumb  = convertView.findViewById(R.id.thumb_search);
        ImageView uploader_image = convertView.findViewById(R.id.uploader_icon);
        ImageView menu = convertView.findViewById(R.id.menu_popup);

        PopupMenu popup = new PopupMenu(menu.getContext(), menu);
        popup.getMenuInflater().inflate(R.menu.list_video_menu,popup.getMenu());

        menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popup.show();
            }
        });

        TextView title_view = convertView.findViewById(R.id.title_search);
        TextView channel = convertView.findViewById(R.id.channel);
        TextView views = convertView.findViewById(R.id.views);
        TextView time_ago = convertView.findViewById(R.id.time);

        Map<PyObject,PyObject> video_data = videos[position].asMap();

        Picasso picasso = Picasso.get();

        String thumbnail,channel_thumb,title,channel_name,video_views,published_time,url;

        thumbnail = getString(video_data,"thumb");
        channel_thumb = getString(video_data,"uploader_icon");
        title = getString(video_data,"title");
        channel_name = getString(video_data,"uploader");
        video_views = getString(video_data,"views");
        published_time = getString(video_data,"published_time");
        url = getString(video_data,"url");

        final String title_final = title;
        View finalConvertView = convertView;
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @SuppressLint("NonConstantResourceId")
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId())
                {
                    case R.id.download:
                        String tit = title_final.replaceAll("[^A-Za-z 0-9]",""),msg="Enter the title";

                        EditText text = new EditText(context);
                        text.setText(tit);
                        text.setMaxLines(3);

                        if(!Downloader.new_file(tit))
                        {
                            msg = "A file with same name already exists";
                        }

                        new AlertDialog.Builder(context)
                                .setIcon(R.mipmap.ic_launcher)
                                .setTitle("Download Manager")
                                .setMessage(msg)
                                .setView(text)
                                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        new Thread(new Runnable() {
                                            @Override
                                            public void run() {
                                                Snackbar.make(finalConvertView,"Processing",BaseTransientBottomBar.LENGTH_SHORT)
                                                        .show();
                                                YTData yt = new YTData(url);
                                                ((Activity)context).runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {

                                                        Downloader downloader = new Downloader(text.getText().toString(),yt.getAudioStreamLink());
                                                        downloader.start();
                                                        DownloadsAct.downloading_list.add(downloader);

                                                        Snackbar.make(finalConvertView,"Added to download que", BaseTransientBottomBar.LENGTH_LONG)
                                                                .setAction("Download", new View.OnClickListener() {
                                                                    @Override
                                                                    public void onClick(View v) {
                                                                        context.startActivity(new Intent(context,DownloadsAct.class));
                                                                    }
                                                                }).show();
                                                    }
                                                });
                                            }
                                        }).start();
                                    }
                                })
                                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                    }
                                })
                                .create().show();


                        break;

                    case R.id.open_in_yt:
                        Intent intent = new Intent();
                        intent.setData(Uri.parse(url));
                        context.startActivity(intent);
                        break;
                }
                return false;
            }
        });

        picasso.load(thumbnail).resize(360,180).into(thumb);
        picasso.load(channel_thumb).into(uploader_image);

        title_view.setText(title);
        channel.setText(channel_name);
        views.setText(video_views);
        time_ago.setText(published_time);

        return convertView;
    }

}
