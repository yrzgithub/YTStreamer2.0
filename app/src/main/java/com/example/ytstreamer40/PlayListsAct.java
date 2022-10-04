package com.example.ytstreamer40;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import java.util.List;
import java.util.Map;

public class PlayListsAct extends AppCompatActivity {

    PlayListAdapter adapter;
    PlayListChooseAdapter choose_adapter;
    ListView playlists;
    String song_add;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_lists);

        playlists = findViewById(R.id.playlists_list);

        Intent intent = getIntent();
        String action = intent.getAction();
        song_add = intent.getStringExtra("song");

        if(action==null || !action.equals("choose"))
        {
            adapter = new PlayListAdapter(this);
            playlists.setAdapter(adapter);

            Map<String, List<String>> playlist_data = adapter.getPlaylistsData();

            playlists.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @RequiresApi(api = Build.VERSION_CODES.N)
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                    String[] names = {};
                    names = playlist_data.keySet().toArray(names);
                    String name = names[position];
                    Intent intent = new Intent(PlayListsAct.this,PlaylistSongs.class);
                    intent.putExtra("playlist_name",name);
                    startActivity(intent);
                }
            });
        }
        else
        {
            choose_adapter = new PlayListChooseAdapter(this);
            playlists.setAdapter(choose_adapter);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.play_list_add_menu,menu);

        MenuItem add = menu.findItem(R.id.add);
        MenuItem add_playlist = menu.findItem(R.id.add_playlist);

        if(adapter==null) //choose = false
        {
            add_playlist.setVisible(false);
            add.setVisible(true);
        }
        else
        {
            add_playlist.setVisible(true);
            add.setVisible(false);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId())
        {
            case R.id.add_playlist:
                LinearLayout layout = new LinearLayout(this);
                layout.setOrientation(LinearLayout.VERTICAL);

                EditText title = new EditText(this);
                EditText song = new EditText(this);

                layout.addView(title);
                layout.addView(song);

                title.setHint("Enter the title");
                song.setHint("Enter the first song");

                new AlertDialog.Builder(this)
                        .setIcon(R.drawable.player_playlists)
                        .setTitle("Playlist Manager")
                        .setView(layout)
                        .setPositiveButton("Add", new DialogInterface.OnClickListener() {
                            @RequiresApi(api = Build.VERSION_CODES.N)
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String tittle = title.getText().toString();
                                String s = song.getText().toString();
                                String msg = adapter.add_playlist(tittle,s);
                                if(msg.equals("Playlist added"))
                                {
                                    adapter = new PlayListAdapter(PlayListsAct.this);
                                    playlists.setAdapter(adapter);
                                }
                                Toast.makeText(PlayListsAct.this,msg,Toast.LENGTH_LONG).show();
                            }
                        })
                        .setNegativeButton("Cancel",null)
                        .show();
                break;

            case R.id.add:
                choose_adapter.add_song_to_playlists(song_add);
                Toast.makeText(this,"Song added",Toast.LENGTH_LONG).show();
                onBackPressed();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this,MainActivity.class);
        intent.setAction("reload");
        startActivity(intent);
    }
}