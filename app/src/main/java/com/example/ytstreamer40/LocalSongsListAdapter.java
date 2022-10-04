package com.example.ytstreamer40;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class LocalSongsListAdapter extends BaseAdapter {

    List<HashMap<String,String>> songs;
    Context context;

    public LocalSongsListAdapter(Context context, List<HashMap<String, String>> songs)
    {
        this.songs = songs;
        this.context = context;
    }

    @Override
    public int getCount() {
        return songs.size();
    }

    @Override
    public Object getItem(int position) {
        return songs.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @SuppressLint({"ViewHolder", "InflateParams"})
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        LayoutInflater inflater = LayoutInflater.from(context);
        convertView = inflater.inflate(R.layout.custom_list_local_songs,null);

        TextView title = convertView.findViewById(R.id.title_local_songs);
        TextView artist_path = convertView.findViewById(R.id.artist_path_local_songs);

        title.setSelected(true);

        title.setText(songs.get(position).get("title"));
        artist_path.setText(songs.get(position).get("artist"));

        return convertView;
    }
}

class LocalSongsListCheckedBoxAdapter extends BaseAdapter {

    List<HashMap<String,String>> songs;
    Context context;
    List<String> checked,checked_paths;

    public LocalSongsListCheckedBoxAdapter(Context context, List<HashMap<String, String>> songs)
    {
        this.songs = songs;
        this.context = context;

        checked = new ArrayList<>();
        checked_paths = new ArrayList<>();
    }

    @Override
    public int getCount() {
        return songs.size();
    }

    @Override
    public Object getItem(int position) {
        return songs.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public List<String> getChecked()
    {
        return checked;
    }

    public List<String> getCheckedPaths() {
        return checked_paths;
    }

    @SuppressLint({"ViewHolder", "InflateParams"})
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        LayoutInflater inflater = LayoutInflater.from(context);
        convertView = inflater.inflate(R.layout.custom_list_local_songs_check,null);

        TextView title = convertView.findViewById(R.id.title_local_songs);
        TextView artist_path = convertView.findViewById(R.id.artist_path_local_songs);
        CheckBox check = convertView.findViewById(R.id.check);

        String song_title = songs.get(position).get("title");
        String path = songs.get(position).get("data");

        check.setChecked(checked.contains(song_title));
        check.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked)
                {
                    checked.add(song_title);
                    checked_paths.add(path);
                    Log.e("sanjay_path",path);
                }
                else
                {
                    checked.remove(song_title);
                    checked_paths.remove(path);
                }
            }
        });

        title.setSelected(true);

        title.setText(song_title);
        artist_path.setText(songs.get(position).get("artist"));

        return convertView;
    }
}