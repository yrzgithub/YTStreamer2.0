package com.example.ytstreamer40;

import android.app.Activity;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import androidx.annotation.RequiresApi;

import java.util.ArrayList;
import java.util.List;

public class PlayListChooseAdapter extends BaseAdapter {

    PlayListSongsAdapter adapter;
    PlayListAdapter playlist_adapter;
    String[] playlist_names;
    List<String> checked = new ArrayList<>();
    Activity act;

    PlayListChooseAdapter(Activity act)
    {
        this.act = act;
        playlist_adapter = new PlayListAdapter(act);
        playlist_names = playlist_adapter.getPlayListNames();
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    void add_song_to_playlists(String song)
    {
        adapter = new PlayListSongsAdapter(act,checked.get(0));
        adapter.add_song_to_playlist(song);

        String playlist_name;
        for(int playlist=1;playlist<checked.size();++playlist)
        {
            playlist_name = checked.get(playlist);
            adapter.change_playlist_name(playlist_name);
            adapter.add_song_to_playlist(song);
        }
    }

    @Override
    public int getCount() {
        return playlist_names.length;
    }

    @Override
    public Object getItem(int position) {
        return playlist_names[position];
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(convertView==null)
        {
            LayoutInflater inflater = LayoutInflater.from(act);
            convertView = inflater.inflate(R.layout.playlist_choose_adapter,null);

            TextView name = convertView.findViewById(R.id.name);
            CheckBox check = convertView.findViewById(R.id.check);

            String playlist_name = playlist_names[position];

            name.setText(playlist_name);

            check.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if(isChecked)
                    {
                        checked.add(playlist_name);
                    }
                    else
                    {
                        checked.remove(playlist_name);
                    }
                }
            });
        }

        return convertView;
    }
}
