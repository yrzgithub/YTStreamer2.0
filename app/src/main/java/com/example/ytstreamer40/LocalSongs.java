package com.example.ytstreamer40;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;

import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class LocalSongs extends AppCompatActivity {

    List<HashMap<String, String>> songs, current_list;
    ListView songs_list;
    Cursor cursor;
    LocalSongsListCheckedBoxAdapter localSongsListCheckedBoxAdapter;
    String playlist_name;
    List<String> checked_songs;
    boolean show_check_boxes=false;

    @RequiresApi(api = Build.VERSION_CODES.S)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_local_songs);

        Intent intent = getIntent();
        String action = intent.getAction();
        playlist_name = intent.getStringExtra("playlist_name");

        songs_list = findViewById(R.id.songs_list_local);

        songs_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                MainActivity.destroy_player();

                HashMap<String, String> current = current_list.get(position);

                Bundle bundle = new Bundle();
                bundle.putString("title", current.get("title"));
                bundle.putString("duration", current.get("duration"));
                bundle.putString("data", current.get("data"));

                Intent intent = new Intent(LocalSongs.this, MainActivity.class);
                intent.setAction("play");
                intent.putExtra("local_songs", bundle);
                startActivity(intent);
            }
        });

        if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
        {
            Snackbar.make(this,songs_list,"Permission not granted", BaseTransientBottomBar.LENGTH_LONG).show();
        }

        else
        {
            songs = getSongs();
            current_list = songs;
        }

        show_check_boxes = action!=null && action.equals("choose");

        if (songs != null)
        {
            if(show_check_boxes)
            {
                localSongsListCheckedBoxAdapter = new LocalSongsListCheckedBoxAdapter(this, songs);
                songs_list.setAdapter(localSongsListCheckedBoxAdapter);
            }
            else
            {
                songs_list.setAdapter(new LocalSongsListAdapter(this, songs));
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.S)
    public ArrayList<HashMap<String, String>> getSongs()
    {
        ArrayList<HashMap<String, String>> songs = new ArrayList<>();
        String[] projection = {MediaStore.Audio.Media._ID, MediaStore.Audio.Media.TITLE, MediaStore.Audio.Media.DURATION, MediaStore.Audio.Media.DATA, MediaStore.Audio.Media.RELATIVE_PATH, MediaStore.Audio.Media.ALBUM_ARTIST};

        cursor = getApplicationContext().getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projection,
                "is_download", null, "title ASC");

        while (cursor.moveToNext()) {

            songs.add(
                    new HashMap<String, String>() {
                        {
                            put("id", cursor.getString(0));
                            put("title", cursor.getString(1));
                            put("duration", cursor.getString(2));
                            put("data", cursor.getString(3));
                            put("path", cursor.getString(4));
                            put("artist", cursor.getString(5));
                        }
                    }
            );
        }
        return songs;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.git:
                startActivity(MainActivity.browse(MainActivity.git_url));
                break;

            case R.id.add:
                PlayListSongsAdapter  adapter = new PlayListSongsAdapter(this,playlist_name);
                checked_songs = localSongsListCheckedBoxAdapter.getCheckedPaths();
                String msg = adapter.add_songs_to_playlist(checked_songs);
                Toast.makeText(this,msg,Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(this,PlaylistSongs.class);
                intent.putExtra("playlist_name",playlist_name);
                startActivity(intent);
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_local_songs, menu);

        MenuItem add = menu.findItem(R.id.add);
        MenuItem search_item = menu.findItem(R.id.search);

        add.setVisible(show_check_boxes);
        search_item.setVisible(!show_check_boxes);

        SearchView search = (SearchView) menu.findItem(R.id.search).getActionView();
        search.setQueryHint("Search LocalSongs");
        search.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public boolean onQueryTextChange(String newText) {

                if (newText.equals(""))
                {
                    current_list = songs;
                    songs_list.setAdapter(new LocalSongsListAdapter(LocalSongs.this, current_list));
                    return true;
                }

                current_list = songs.stream().filter(new Predicate<HashMap<String, String>>() {
                    @Override
                    public boolean test(HashMap<String, String> stringStringHashMap) {
                        String title = stringStringHashMap.get("title");
                        assert title != null;
                        title = title.toLowerCase();
                        return title.contains(newText.toLowerCase());
                    }
                }).collect(Collectors.<HashMap<String, String>>toList());

                songs_list.setAdapter(new LocalSongsListAdapter(LocalSongs.this, current_list));
                return true;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onDestroy() {
        if (cursor != null) cursor.close();
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        Intent intent  = new Intent(this,MainActivity.class);
        intent.setAction("reload");
        startActivity(intent);
    }
}
