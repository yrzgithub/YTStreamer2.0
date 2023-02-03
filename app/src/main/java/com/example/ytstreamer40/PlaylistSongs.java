package com.example.ytstreamer40;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.Image;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.chaquo.python.PyException;
import com.chaquo.python.PyObject;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class PlaylistSongs extends AppCompatActivity {

    PlayListSongsAdapter adapter;
    String name;
    ListView songs = null;

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.songs_list_view);

        Intent intent = getIntent();
        name = intent.getStringExtra("playlist_name");

        adapter = new PlayListSongsAdapter(this,name);

        songs = findViewById(R.id.sgn_list);
        songs.setAdapter(adapter);

        songs.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                List<String> sngs = adapter.getSongs();
                String song = sngs.get(position);
                Intent intent = new Intent(PlaylistSongs.this,MainActivity.class);
                intent.setAction("Playlist");
                intent.putExtra("title",song);
                intent.putExtra("playlist_name",name);
                startActivity(intent);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.playlists_songs_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId())
        {
            case R.id.add_song:
                EditText txt = new EditText(this);
                txt.setHint("Enter the song");
                new AlertDialog.Builder(this)
                        .setIcon(R.drawable.player_playlists)
                        .setTitle("Playlist Manager")
                        .setView(txt)
                        .setPositiveButton("Add", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String song = txt.getText().toString();
                                if(!song.isEmpty())
                                {
                                    Snackbar snack = Snackbar.make(songs,"Searching local songs list",Snackbar.LENGTH_INDEFINITE);
                                    snack.show();
                                    new Thread()
                                    {
                                        @Override
                                        public void run() {
                                            String msg = adapter.add_song_to_playlist(song);
                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    snack.dismiss();
                                                    Toast.makeText(PlaylistSongs.this,msg,Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                            super.run();
                                        }
                                    }.start();
                                }
                                else
                                {
                                    Toast.makeText(PlaylistSongs.this,"Song not added",Toast.LENGTH_SHORT).show();
                                }
                            }
                        })
                        .setNegativeButton("Cancel",null)
                        .show();
                break;

            case R.id.add_local:
                Intent intent  = new Intent(PlaylistSongs.this,LocalSongs.class);
                intent.setAction("choose");
                intent.putExtra("playlist_name",name);
                startActivity(intent);
                break;

            case R.id.shuffle:
                adapter.shuffle();
                break;

            case R.id.alpha:
                adapter.alpha();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(PlaylistSongs.this,PlayListsAct.class));
    }
}

class PlayListSongsAdapter extends BaseAdapter {

    List<String> songs;
    Activity act;
    String name;
    Gson gson = new Gson();
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    Map<String,List<String>> playlists_data;
    static String download_path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString();

    @RequiresApi(api = Build.VERSION_CODES.N)
    PlayListSongsAdapter(Activity act, String name)
    {
        this.act = act;
        this.name = name;

        sharedPreferences = act.getSharedPreferences("PLAYLISTS", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();

        playlists_data = new PlayListAdapter(act).getPlaylistsData();
        songs = playlists_data.get(this.name);

        if(Settings.PlayListSettings.shuffle) shuffle();
    }

    void change_playlist_name(String name)
    {
        songs = playlists_data.get(name);
    }

    String add_song_to_playlist(String song)
    {
        String generated_title = null;
        if(songs!=null && !songs.contains(song))
        {
            try
            {
                Map<PyObject,PyObject> search_data = MainActivity.main.callAttr("get_stream_and_thumb",song).asMap();
                generated_title = search_data.get(PyObject.fromJava("title")).toString();
                String path = Downloader.getSavedFilePath(generated_title);
                File file = new File(path);
                if(file.exists())
                {
                    songs.add(path);
                }
                else
                {
                    songs.add(song);
                }
            }

            catch(Exception e) {
                songs.add(song);
            }

            PlayListAdapter.playlists_data.put(name,songs);
            String data = gson.toJson(PlayListAdapter.playlists_data,Map.class);
            editor.putString("playlists_data",data);
            editor.apply();

            act.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    act.recreate();
                }
            });

            return "Song added";
        }
        return "Name already found";
    }

    void shuffle()
    {
        if(songs!=null)
        {
            Collections.shuffle(songs);
            PlayListAdapter.playlists_data.put(name,songs);
            String data = gson.toJson(PlayListAdapter.playlists_data,Map.class);
            editor.putString("playlists_data",data);
            editor.apply();

            act.recreate();
        }
    }

    void alpha()
    {
        if(songs!=null)
        {
            Collections.sort(songs);
            PlayListAdapter.playlists_data.put(name,songs);
            String data = gson.toJson(PlayListAdapter.playlists_data,Map.class);
            editor.putString("playlists_data",data);
            editor.apply();

            act.recreate();
        }
    }

    @SuppressLint("DefaultLocale")
    @RequiresApi(api = Build.VERSION_CODES.N)
    String add_songs_to_playlist(List<String> songs_list)
    {
        int size = songs_list.size();
        if(songs!=null && size>0)
        {
            songs.addAll(songs_list);
            songs = songs.stream().distinct().collect(Collectors.toList());
            PlayListAdapter.playlists_data.put(name,songs);
            String data = gson.toJson(PlayListAdapter.playlists_data,Map.class);
            editor.putString("playlists_data",data);
            editor.apply();

            if(size==1) return "1 song added";
            else return String.format("%d songs added",size);
        }
        return "No song selected";
    }

    void delete(String song)
    {
        if(songs!=null)
        {
            songs.remove(song);
            PlayListAdapter.playlists_data.put(name,songs);
            String data = gson.toJson(PlayListAdapter.playlists_data,Map.class);
            editor.putString("playlists_data",data);
            editor.apply();

            act.recreate();
        }
    }

    public List<String> getSongs()
    {
        return songs;
    }

    @Override
    public int getCount() {
        Log.e("sanjay", name);
        return songs.size();
    }

    @Override
    public Object getItem(int position) {
        return songs.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @SuppressLint("ViewHolder")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(act);
        convertView = inflater.inflate(R.layout.playlist_adapter,null);

        LinearLayout src = convertView.findViewById(R.id.drop);
        ImageView source  = convertView.findViewById(R.id.source);
        TextView title = convertView.findViewById(R.id.title);

        String name = songs.get(position);

        if(name.startsWith(download_path))
        {
            String name_show = name.replaceFirst(download_path,"").replaceFirst(File.separator,"");
            source.setImageResource(R.drawable.local_song_playing);
            title.setText(name_show);
        }
        else
        {
            source.setImageResource(R.drawable.youtube);
            title.setText(name);
        }

        src.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu menu = new PopupMenu(act,src);
                menu.inflate(R.menu.playlists_songs_menu_pop_up);
                menu.show();

                menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId())
                        {
                            case R.id.delete:
                                delete(name);
                                break;
                        }
                        return false;
                    }
                });
            }
        });
        return convertView;
    }
}