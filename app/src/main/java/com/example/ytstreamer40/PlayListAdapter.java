package com.example.ytstreamer40;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Build;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.RequiresApi;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

public class PlayListAdapter extends BaseAdapter {

    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    Gson gson = new Gson();
    Activity act;
    static String[] playlist_names;
    Map<String,List<String>> default_list = new HashMap<String,List<String>>();
    static Map<String,List<String>> playlists_data;

    PlayListAdapter(Activity act)
    {
        this.act = act;

        sharedPreferences = act.getSharedPreferences("PLAYLISTS", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();

        String[] def_love_songs = new String[]{"munbe vaa","enkeyum kadhal"};
        String[] def_horny_songs = new String[] {"Dollu Dollu","Manogari tamil"};

        default_list.put("Love", Arrays.asList(def_love_songs));
        default_list.put("Horny",Arrays.asList(def_horny_songs));

        playlists_data = gson.fromJson(sharedPreferences.getString("playlists_data",gson.toJson(default_list)),default_list.getClass());
        playlist_names = playlists_data.keySet().toArray(new String[]{});
    }

    public Map<String,List<String>> getPlaylistsData()
    {
        return playlists_data;
    }

    public String[] getPlayListNames()
    {
        return playlist_names;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public String add_playlist(String name, String song)
    {
        boolean found  = Arrays.stream(playlist_names).anyMatch(new Predicate<String>() {
            @Override
            public boolean test(String s) {
                return s.equals(name);
            }
        });

        if(name.isEmpty())
        {
            return "Name cannot be empty";
        }

        else if(found)
        {
            return "Name already found";
        }

        else if(playlists_data!=null)
        {
            if (song.isEmpty()) playlists_data.put(name,new ArrayList<>());
            else playlists_data.put(name,Collections.singletonList(song));
            String data = gson.toJson(playlists_data,Map.class);
            editor.putString("playlists_data",data);
            editor.apply();

            act.recreate();

            return "Playlist added";
        }
        else
        {
            return "Playlist not added";
        }
    }

    public void change_playlist_name(String old_name,String new_name)
    {
        if(playlists_data!=null)
        {
            List<String> old = playlists_data.get(old_name);
            playlists_data.remove(old_name);
            playlists_data.put(new_name,old);
            String data = gson.toJson(playlists_data,Map.class);
            editor.putString("playlists_data",data);
            editor.apply();

            act.recreate();
        }
    }

    public void delete_playList(String name)
    {
        if(playlists_data!=null)
        {
            playlists_data.remove(name);
            String data = gson.toJson(playlists_data,Map.class);
            editor.putString("playlists_data",data);
            editor.apply();

            act.recreate();
        }
    }

    @Override
    public int getCount() {
        return playlists_data.size();
    }

    @Override
    public Object getItem(int position) {
        return playlists_data.get(position);
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
            convertView = inflater.inflate(R.layout.songs_playlist_list,null);

            String playlist_name = playlist_names[position];

            TextView txt = convertView.findViewById(R.id.name);
            txt.setText(playlist_name);

            LinearLayout layout = convertView.findViewById(R.id.drop);
            layout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    PopupMenu menu = new PopupMenu(act,layout, Gravity.BOTTOM);
                    menu.inflate(R.menu.menu_pop_playlist);
                    menu.show();

                    menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            ListView list = act.findViewById(R.id.playlists_list);
                            switch (item.getItemId())
                            {
                                case R.id.change_name:
                                    EditText txt = new EditText(act);
                                    txt.setHint("Enter the new name");
                                    new AlertDialog.Builder(act)
                                            .setIcon(R.drawable.player_playlists)
                                            .setTitle("Playlist Manager")
                                            .setView(txt)
                                            .setPositiveButton("Add", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    String new_name = txt.getText().toString();
                                                    change_playlist_name(playlist_name,new_name);
                                                    list.setAdapter(new PlayListAdapter(act));
                                                }
                                            })
                                            .setNegativeButton("Cancel",null)
                                            .show();
                                    break;

                                case R.id.delete:
                                    delete_playList(playlist_name);
                                    list.setAdapter(new PlayListAdapter(act));
                                    break;
                            }
                            return false;
                        }
                    });
                }
            });
        }
        return convertView;
    }
}
