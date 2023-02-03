package com.example.ytstreamer40;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.chaquo.python.PyException;
import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.example.ytstreamer40.R.id;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    TextView title,start_time,end_time;
    ImageButton play,forward,backward;
    ImageView thumbnail;
    SeekBar seek;
    BroadcastReceiver receiver;
    DrawerLayout drawer;
    ActionBarDrawerToggle toggle;
    String action,playlist,playlist_title_intent;
    LinearLayout player_nav,songs_list,downloads,settings,about,developer_contact,playlist_nav;
    MenuItem download_item;
    SharedPreferences saved_data;
    Settings setting;
    int previous_headset_value = -1,current_playlist_song_index = 0;
    String[] sngs;
    String name,update_link;
    float latest_version,version;
    IntentFilter filter;

    Handler seek_handler = new Handler();
    Runnable seek_runnable = new Runnable() {
        @Override
        public void run() {
            doSeek(songPlayer.getCurrentPosition());
            seek_handler.postDelayed(this,1000);
        }
    };
    MediaMetadataRetriever retriever = new MediaMetadataRetriever();

    static String title_str;
    static int duration;
    static Drawable img;
    static SeekBar.OnSeekBarChangeListener seek_listener;
    static SongPlayer songPlayer;
    static YTData yt;
    static MediaPlayer.OnPreparedListener media_on_prepared;
    static MediaPlayer.OnCompletionListener on_complete_listener,play_list_on_completion_listener;
    static MediaPlayer.OnBufferingUpdateListener buffer_update_listener;
    static SharedPreferences.Editor saved_data_editor;
    static String git_url = "https://www.github.com/yrzgithub";
    static String data_base_name = "latest_version",link="link";
    static PyObject main = Python.getInstance().getModule("main");

    @SuppressLint("SetTextI18n")
    @RequiresApi(api = Build.VERSION_CODES.S)
    @Override
    protected void onCreate(Bundle savedInstanceState) throws NullPointerException {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setting = new Settings(this);

        saved_data = getSharedPreferences("data", MODE_PRIVATE);
        saved_data_editor = saved_data.edit();

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference reference = database.getReference(data_base_name);

        reference.addValueEventListener(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Map<String,String> data = snapshot.getValue(new GenericTypeIndicator<Map<String, String>>() {});
                name = data.get("name");
                version = Float.parseFloat(data.get("version"));
                update_link = data.get(link);

                if(version>getCurrentVersion() && !getPopShown())
                {
                    latest_version = version;
                    setPopShown(false);
                    show_update_pop();
                    setPopShown(true);
                }
                else
                {
                    latest_version = getCurrentVersion();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("sanjay",error.toString());
            }
        });

        if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.MANAGE_EXTERNAL_STORAGE
            }, 1001);
        }

        drawer = findViewById(id.drawer);
        toggle = new ActionBarDrawerToggle(this, drawer, R.string.open_nav, R.string.close_nav);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        //Drawer actions
        player_nav = drawer.findViewById(R.id.player_nav);
        playlist_nav = drawer.findViewById(id.player_playlist);
        songs_list = drawer.findViewById(R.id.songs_list_nav);
        downloads = drawer.findViewById(R.id.downloads_nav);
        settings = drawer.findViewById(R.id.settings_nav);
        about = drawer.findViewById(R.id.about_nav);
        developer_contact = drawer.findViewById(R.id.developer_contact_nav);

        player_nav.setOnClickListener(this);
        playlist_nav.setOnClickListener(this);
        songs_list.setOnClickListener(this);
        downloads.setOnClickListener(this);
        settings.setOnClickListener(this);
        about.setOnClickListener(this);
        developer_contact.setOnClickListener(this);

        ActionBar bar = getSupportActionBar();
        if (bar != null) bar.setDisplayHomeAsUpEnabled(true);

        //TextView
        title = findViewById(R.id.title);
        title.setSelected(true);

        //Image Button
        play = findViewById(R.id.play);
        forward = findViewById(R.id.forward);
        backward = findViewById(R.id.backward);

        thumbnail = findViewById(R.id.thumb);

        seek = findViewById(R.id.seek);

        start_time = findViewById(R.id.start);
        end_time = findViewById(R.id.end);

        //onClickListener
        play.setOnClickListener(this);
        forward.setOnClickListener(this);
        backward.setOnClickListener(this);

        Snackbar offline = Snackbar.make(player_nav, "You're offline", BaseTransientBottomBar.LENGTH_INDEFINITE);

        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                switch (intent.getAction()) {
                    case ConnectivityManager.CONNECTIVITY_ACTION:
                        if ((!check_internet() && songPlayer != null && songPlayer.isStreaming())) {
                            offline.show();
                        } else {
                            offline.dismiss();
                        }
                        break;

                    case Intent.ACTION_HEADSET_PLUG:
                        int headset_val = intent.getIntExtra("state", previous_headset_value);
                        switch (headset_val) {
                            case 0:
                                try {
                                    if (songPlayer != null && songPlayer.isPlaying() && previous_headset_value != headset_val && previous_headset_value != -1) {
                                        songPlayer.pause(play, thumbnail);
                                    }
                                } catch (Exception ignored) {

                                }
                                break;

                            case 1:
                                break;
                        }
                        previous_headset_value = headset_val;
                        break;
                }
            }
        };

        filter = new IntentFilter();
        filter.addAction(Intent.ACTION_HEADSET_PLUG);
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);

        seek.setSecondaryProgressTintMode(PorterDuff.Mode.DARKEN);
        buffer_update_listener = new MediaPlayer.OnBufferingUpdateListener() {
            @Override
            public void onBufferingUpdate(MediaPlayer mp, int percent) {
                if (percent >= 99) songPlayer.setBufferCompleted(true);
                int secondary_progress = (percent * duration) / 100;
                seek.setSecondaryProgress(secondary_progress);
            }
        };

        seek_listener = new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    songPlayer.seekTo(progress * 1000);
                }
            }

            @RequiresApi(api = Build.VERSION_CODES.Q)
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                songPlayer.pause(play, thumbnail);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                songPlayer.start(play, thumbnail);
                if (!seek_handler.hasCallbacks(seek_runnable)) seek_runnable.run();
            }
        };

        media_on_prepared = new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                if (!title.getText().toString().equals(title_str)) title.setText(title_str);
                thumbnail.setImageDrawable(img);
                songPlayer.start(play, thumbnail);
                end_time.setText(current_time(duration));
                seek.setMax(duration);
                seek_runnable.run();
                if (!play.isEnabled()) play.setEnabled(true);
            }
        };

        MediaPlayer.OnPreparedListener media_on_prepared_local = new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {

                seek.setSecondaryProgress(0);

                songPlayer.start(play, thumbnail);

                end_time.setText(current_time(duration));
                seek.setMax(duration);
                seek_runnable.run();

            }
        };

        on_complete_listener = new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                if (!Settings.PlayerSettings.looping) {
                    seek_handler.removeCallbacks(seek_runnable);
                    start_time.setText(start_time.getText());
                }
                play.setImageResource(R.drawable.play);
            }
        };


        play_list_on_completion_listener = new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {

                start_time.setText(end_time.getText());

                destroy_player();

                if (songPlayer.isStreaming()) {
                    seek.setSecondaryProgress(100);
                }

                List<String> songs_data = PlayListAdapter.playlists_data.get(playlist);
                sngs = songs_data.toArray(new String[]{});
                if (current_playlist_song_index >= sngs.length - 1) {
                    current_playlist_song_index = 0;
                } else {
                    ++current_playlist_song_index;
                }

                String title_song = sngs[current_playlist_song_index];

                try {
                    Glide.with(MainActivity.this).load(R.drawable.loading_pink_list).into(thumbnail);
                } catch (Exception ignored) {

                }

                start_time.setText(getString(R.string._0_0));
                end_time.setText(getString(R.string._0_0));
                title.setText("");

                if (title_song.startsWith(PlayListSongsAdapter.download_path)) {
                    Glide.with(MainActivity.this).load(R.drawable.yt).into(thumbnail);

                    try {
                        retriever.setDataSource(title_song);
                        title_str = title_song.replaceFirst(PlayListSongsAdapter.download_path, "").replaceFirst(File.separator, "");
                        duration = Integer.parseInt(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)) / 1000;

                        Log.e("sanjay",title_song);
                        title.setText(title_str);
                        songPlayer = new SongPlayer(title_song, false);
                        songPlayer.setOnPreparedListener(media_on_prepared_local);
                        songPlayer.setOnCompletionListener(this);
                        seek.setSecondaryProgressTintMode(PorterDuff.Mode.DARKEN);
                        seek.setOnSeekBarChangeListener(seek_listener);
                    } catch (Exception e) {
                        Snackbar.make(player_nav, "This file is not found", BaseTransientBottomBar.LENGTH_LONG)
                                .setAction("remove", new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        new PlayListSongsAdapter(MainActivity.this, playlist).delete(title_song);
                                    }
                                })
                                .show();
                    }
                } else {
                    from_link(title_song, true, this);
                }
            }
        };

        Intent intent = getIntent();
        action = intent.getAction();

        if (action.equals("reload") && songPlayer == null) {
            title.setText("Hello Cutie!");
            Glide.with(this).load(R.drawable.yt).into(thumbnail);
        }

        if (action.equals("reload") && songPlayer != null) {
            title.setText(title_str);
            end_time.setText(current_time(duration));
            seek.setMax(duration);
            seek_runnable.run();
            seek.setOnSeekBarChangeListener(seek_listener);
            if (songPlayer.isBufferCompleted()) {
                seek.setSecondaryProgress(duration);
            } else {
                songPlayer.setOnBufferingUpdateListener(buffer_update_listener);
            }

            songPlayer.setOnCompletionListener(on_complete_listener);

            boolean playing;
            try {
                playing = songPlayer.isPlaying();
            } catch (Exception e) {
                playing = false;
            }

            if (end_time.getText().equals(start_time.getText())) {
                play.setImageResource(R.drawable.play);
            } else if (!playing) {
                play.setImageResource(R.drawable.play);
            } else {
                play.setImageResource(R.drawable.pause);
            }

            if (songPlayer.isStreaming()) {
                thumbnail.setImageDrawable(img);
                songPlayer.setOnPreparedListener(media_on_prepared);
            } else {
                Glide.with(this).load(R.drawable.local_song_playing).into(thumbnail);
                songPlayer.setOnPreparedListener(media_on_prepared_local);
            }
        } else if (action.equals("play")) {
            if (intent.hasExtra("data_bundle")) {
                loading();

                Bundle bundle = intent.getBundleExtra("data_bundle");

                title_str = bundle.getString("title");
                String url = bundle.getString("url");
                String thumb_url = bundle.getString("thumb");
                duration = bundle.getInt("duration");

                title.setText(title_str);

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        yt = new YTData(url, thumb_url);
                        img = yt.getThumb();
                        songPlayer = new SongPlayer(yt.getAudioStreamLink(), true);
                        songPlayer.setOnPreparedListener(media_on_prepared);
                        songPlayer.setOnCompletionListener(on_complete_listener);
                        songPlayer.setOnBufferingUpdateListener(buffer_update_listener);

                        seek.setOnSeekBarChangeListener(seek_listener);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                download_item.setVisible(true);
                                if (Settings.DownloadSettings.auto_download) {
                                    download(true);
                                }
                            }
                        });
                    }
                }).start();
            } else if (intent.hasExtra("local_songs")) {
                Glide.with(this).load(R.drawable.local_song_playing).into(thumbnail);

                Bundle bundle = intent.getBundleExtra("local_songs");

                title_str = bundle.getString("title");
                String path = bundle.getString("data");

                title.setText(title_str);

                retriever.setDataSource(path);
                duration = Integer.parseInt(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)) / 1000;

                songPlayer = new SongPlayer(path, false);
                Log.e("sanjay_dur0", String.valueOf(songPlayer.getDuration()));
                Log.e("sanjay_dur1", String.valueOf(duration));
                songPlayer.setOnPreparedListener(media_on_prepared_local);
                songPlayer.setOnCompletionListener(on_complete_listener);
                seek.setOnSeekBarChangeListener(seek_listener);
            }
        } else if (action.equals("Playlist")) {
            destroy_player();

            playlist = intent.getStringExtra("playlist_name");
            List<String> songs_data = PlayListAdapter.playlists_data.get(playlist);
            if (songs_data != null) {
                sngs = songs_data.toArray(new String[]{});
                playlist_title_intent = intent.getStringExtra("title");
                current_playlist_song_index = Arrays.asList(sngs).indexOf(playlist_title_intent);
                Glide.with(MainActivity.this).load(R.drawable.loading_pink_list).into(thumbnail);
                start_time.setText(getString(R.string._0_0));
                end_time.setText(getString(R.string._0_0));
                title.setText("");

                if (playlist_title_intent.startsWith(PlayListSongsAdapter.download_path)) {
                    Glide.with(this).load(R.drawable.local_song_playing).into(thumbnail);
                    if (!playlist_title_intent.endsWith(".mp3")) {
                        playlist_title_intent += ".mp3";
                    }

                    try {
                        retriever.setDataSource(playlist_title_intent);
                        title_str = playlist_title_intent.replaceFirst(PlayListSongsAdapter.download_path, "").replaceFirst(File.separator, "");
                        duration = Integer.parseInt(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)) / 1000;

                        Log.e("sanjay_title", title_str);
                        title.setText(title_str);

                        songPlayer = new SongPlayer(playlist_title_intent, false);
                        songPlayer.setOnPreparedListener(media_on_prepared_local);
                        songPlayer.setOnCompletionListener(play_list_on_completion_listener);
                        seek.setOnSeekBarChangeListener(seek_listener);
                    } catch (Exception e) {
                        Snackbar.make(player_nav, "This file is not found", BaseTransientBottomBar.LENGTH_LONG)
                                .setAction("remove", new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        Log.e("sanjay_tit", title_str);
                                        new PlayListSongsAdapter(MainActivity.this, playlist).delete(playlist_title_intent);
                                    }
                                })
                                .show();
                    }
                } else {
                    from_link(playlist_title_intent, true, play_list_on_completion_listener);
                }
            } else {
                Snackbar.make(player_nav, "Something went wrong", BaseTransientBottomBar.LENGTH_LONG).show();
            }
        }

        if (action != null) {
            if (action.equals(Intent.ACTION_MAIN)) {
                Glide.with(this).load(R.drawable.yt).into(thumbnail);
                title.setText("Hello Cutie!");

            } else if (action.equals(Intent.ACTION_SEND)) {
                loading();
                String url = intent.getStringExtra(Intent.EXTRA_TEXT);
                from_link(url, false, on_complete_listener);
            }
        }
    }

    public void loading()
    {
        Glide.with(this).load(R.drawable.loading).into(thumbnail);
    }

    @Override
    protected void onRestart() {

        super.onRestart();
    }

    boolean check_internet()
    {
        try {
            ConnectivityManager cm = (ConnectivityManager) this.getSystemService(CONNECTIVITY_SERVICE);
            NetworkInfo[] info = cm.getAllNetworkInfo();
            for (NetworkInfo networkInfo : info) {
                if (networkInfo.isConnected()) return true;
            }
            return false;
        }

        catch (NullPointerException e)
        {
            return false;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        registerReceiver(receiver,filter);
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        int id = v.getId();

        drawer.close();

        switch (id)
        {
            case R.id.player_nav:
                startActivity(new Intent(MainActivity.this,LocalSongs.class));
                break;

            case R.id.player_playlist:
                startActivity(new Intent(this,PlayListsAct.class));
                break;

            case R.id.songs_list_nav:
                String last_query = getLastQuery();
                if(songPlayer!=null)
                {
                    if(songPlayer.isStreaming())
                    {
                        Intent intent = new Intent(MainActivity.this,VideoList.class);
                        intent.putExtra("search",last_query);
                        startActivity(intent);
                    }
                    else
                    {
                        startActivity(new Intent(MainActivity.this,LocalSongs.class));
                    }
                }
                else 
                {
                    Intent intent = new Intent(MainActivity.this,VideoList.class);
                    intent.putExtra("search",last_query);
                    startActivity(intent);
                }
                break;

            case R.id.downloads_nav:
                Intent intent = new Intent(MainActivity.this,DownloadsAct.class);
                startActivity(intent);
                break;

            case R.id.settings_nav:
                startActivity(new Intent(this,SettingsActivity.class));
                break;

            case R.id.about_nav:
                if(latest_version>getCurrentVersion())
                {
                    show_update_pop();
                }
                else
                {
                    new android.app.AlertDialog.Builder(this)
                            .setIcon(R.mipmap.ic_launcher)
                            .setTitle("YTStreamer")
                            .setMessage("Already in the latest version")
                            .setNegativeButton("ok",null)
                            .show();
                }
                break;

            case R.id.developer_contact_nav:
                startActivity(new Intent(MainActivity.this,DeveloperContact.class));
                break;
        }

        if(songPlayer!=null)
        {
            switch (id)
            {
                case R.id.play:
                    try
                    {
                        if(!seek_handler.hasCallbacks(seek_runnable)) seek_runnable.run();
                        if(songPlayer.isPlaying())
                        {
                            songPlayer.pause(play,thumbnail);
                        }
                        else
                        {
                            songPlayer.start(play,thumbnail);
                        }
                    }
                    catch (Exception ignored)
                    {

                    }
                   break;

                case R.id.forward:
                    songPlayer.forward();
                    break;

                case R.id.backward:
                    songPlayer.backward();
                    break;
            }
        }
    }

    @SuppressLint("ResourceType")
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu,menu);

        download_item = menu.findItem(id.download);
        download_item.setVisible(songPlayer != null && songPlayer.isStreaming());

        SearchView search = (SearchView) menu.findItem(R.id.search).getActionView();

        AutoCompleteTextView auto = (AutoCompleteTextView) search.findViewById(search.getContext().getResources().getIdentifier("android:id/search_src_text",null,null));
        auto.setHint("Search YouTube");
        auto.setDropDownBackgroundResource(R.color.white);
        auto.setValidator(new AutoCompleteTextView.Validator() {
            @Override
            public boolean isValid(CharSequence text) {
                return true;
            }

            @Override
            public CharSequence fixText(CharSequence invalidText) {
                return invalidText;
            }
        });

        search.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public boolean onQueryTextSubmit(String query) {

                search.clearFocus();
                search.onActionViewCollapsed();

                setLastQuery(query);

                if(Settings.PlayerSettings.show_video_list)
                {
                    Intent intent = new Intent(MainActivity.this,VideoList.class);
                    intent.putExtra("search",query);
                    startActivity(intent);
                }

                else
                {
                    download_item.setVisible(false);
                    Glide.with(MainActivity.this).load(R.drawable.loading_pink_list).into(thumbnail);
                    start_time.setText(getString(R.string._0_0));
                    end_time.setText(getString(R.string._0_0));
                    title.setText("");
                    if(songPlayer!=null)
                    {
                        songPlayer.pause(play,thumbnail);
                    }
                    from_link(query,true,on_complete_listener);
                }
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        String[] suggestions;
                        try
                        {
                            suggestions = main.callAttr("suggest",newText).toJava(String[].class);
                            Log.e("sanjay", Arrays.toString(suggestions));
                            if(suggestions.length>0)
                            {
                                ArrayAdapter<String> suggestions_list = new ArrayAdapter<String>(MainActivity.this, androidx.appcompat.R.layout.support_simple_spinner_dropdown_item,suggestions);

                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        auto.setAdapter(suggestions_list);
                                        if(!auto.isPopupShowing()) auto.showDropDown();
                                    }
                                });
                            }
                        }

                        catch (PyException ignored)
                        {

                        }
                    }
                }).start();

                return false;
            }
        });

        auto.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String query = parent.getItemAtPosition(position).toString();
                search.setQuery(query,false);
            }
        });
        return true;
    }

    public void show_update_pop()
    {
        new android.app.AlertDialog.Builder(this)
                .setIcon(R.mipmap.ic_launcher)
                .setTitle("YTStreamer")
                .setMessage("Update Available")
                .setPositiveButton("update", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Intent intent = new Intent();
                        intent.setData(Uri.parse(update_link));
                        startActivity(intent);
                    }
                })
                .setNegativeButton("cancel",null)
                .show();
    }

    public void setCurrentVersion(String version)
    {
        saved_data_editor.putFloat("current_version",Float.parseFloat(version));
        saved_data_editor.commit();
    }

    public float getCurrentVersion()
    {
        return saved_data.getFloat("current_version",0);
    }

    public boolean getPopShown()
    {
        return saved_data.getBoolean("pop_shown",false);
    }

    public void setPopShown(boolean shown)
    {
        saved_data_editor.putBoolean("pop_shown",shown);
        saved_data_editor.commit();
    }

    public static void setLastQuery(String query)
    {
        saved_data_editor.putString("last_query",query);
        saved_data_editor.commit();
    }

    public String getLastQuery()
    {
        return saved_data.getString("last_query","anbe en anbe");
    }

    public void from_link(String source,boolean query,MediaPlayer.OnCompletionListener complete_listener)
    {
        new Thread(new Runnable() {
            @SuppressLint("UseCompatLoadingForDrawables")
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void run() {
                PyObject map_obj;
                try
                {
                    map_obj = MainActivity.main.callAttr("get_stream_and_thumb",source,query);
                    if(query)
                    {
                        setLastQuery(source);
                    }
                }
                catch (PyException e)
                {
                    map_obj = null;
                }

                if(map_obj!=null)
                {
                    Map<PyObject,PyObject> map = map_obj.asMap();
                    String thumb = Objects.requireNonNull(map.get(PyObject.fromJava("thumb"))).toString();
                    title_str = Objects.requireNonNull(map.get(PyObject.fromJava("title"))).toString();
                    duration = getDuration(Objects.requireNonNull(map.get(PyObject.fromJava("duration"))).toString());
                    String url = Objects.requireNonNull(map.get(PyObject.fromJava("url"))).toString();
                    String stream_url = Objects.requireNonNull(map.get(PyObject.fromJava("stream"))).toString();

                    try
                    {
                        yt = new YTData(url,thumb);
                        img = yt.getThumb();

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                download_item.setVisible(true);
                                if(Settings.DownloadSettings.auto_download)
                                {
                                    download(true);
                                }

                                title.setText(title_str);
                                thumbnail.setImageDrawable(img);
                                play.setEnabled(false);

                                try
                                {
                                    Glide.with(play).load(R.drawable.buffering).into(play);
                                }
                                catch (Exception ignored)
                                {

                                }

                                songPlayer = new SongPlayer(stream_url,true);
                                songPlayer.setOnPreparedListener(media_on_prepared);
                                songPlayer.setOnCompletionListener(complete_listener);
                                songPlayer.setOnBufferingUpdateListener(buffer_update_listener);
                                seek.setOnSeekBarChangeListener(seek_listener);
                            }
                        });
                    }

                    catch (PyException p)
                    {
                        Glide.with(MainActivity.this).load(R.drawable.noresultfound).into(thumbnail);
                    }

                }

                else
                {
                    Snackbar.make(player_nav,"Invalid YT link",BaseTransientBottomBar.LENGTH_LONG).show();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Glide.with(MainActivity.this).load(R.drawable.noresultfound).into(thumbnail);
                        }
                    });
                }
            }
        }).start();
    }

    public void doSeek(int current)
    {
        current = Math.round(((float)current)/1000);
        seek.setProgress(current);
        start_time.setText(current_time(current));
    }

    @SuppressLint("DefaultLocale")
    public String current_time(int current)
    {
        int hours = current/60;
        int seconds = current%60;
        return String.format("%d:%02d",hours,seconds);
    }

    static Intent browse(String url)
    {
        Intent intent = new Intent();
        intent.setData(Uri.parse(url));
        return intent;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public boolean download(boolean auto)
    {

        title_str = title_str.replaceAll("[^A-Za-z 0-9]","");

        boolean auto_generate_title = Settings.DownloadSettings.auto_generate_title;
        boolean new_file = Downloader.new_file(title_str);
        boolean downloading;

        if(DownloadsAct.downloading_list.size()==0)
        {
            downloading = false;
        }

        else
        {
            downloading = DownloadsAct.downloading_list.stream().anyMatch(new Predicate<Downloader>() {
                @Override
                public boolean test(Downloader downloader) {
                    Log.d("sanjay_down",downloader.getTitle());
                    Log.d("sanjay_down",title_str);
                    return downloader.getTitle().equals(title_str);
                }
            });
        }

        if(yt==null)
        {
            Toast.makeText(this, "YT link not found", Toast.LENGTH_SHORT).show();
            return false;
        }

        if(auto)
        {
            if(new_file && !downloading)
            {
                Downloader downloader = new Downloader(title_str,yt.getAudioStreamLink());
                start_download(downloader);
            }
            return true;
        }

        if(auto_generate_title)
        {
            if(downloading)
            {
                Snackbar.make(player_nav,"Found in download que",BaseTransientBottomBar.LENGTH_LONG)
                        .setAction("Force download", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                start_download(new Downloader(title_str,yt.getAudioStreamLink()));
                            }
                        })
                        .show();
            }

            else if(!new_file)
            {
                Snackbar.make(player_nav,"A file with same name already exists",BaseTransientBottomBar.LENGTH_LONG)
                        .show();
            }

            else
            {
                start_download(new Downloader(title_str,yt.getAudioStreamLink()));
            }
        }

        else
        {
            String msg = "Enter the title";

            if(downloading)
            {
                msg = "A file with same name already found in download que";
            }

            else if(!new_file)
            {
                msg = "A file with same name already exists";
            }

            EditText tittle = new EditText(this);
            tittle.setMaxLines(3);
            tittle.setText(title_str);

            new AlertDialog.Builder(this)
                    .setIcon(R.mipmap.ic_launcher)
                    .setTitle("Download Manager")
                    .setMessage(msg)
                    .setView(tittle)
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Downloader downloader = new Downloader(tittle.getText().toString(),yt.getAudioStreamLink());
                            start_download(downloader);
                        }
                    })
                    .setNegativeButton("Cancel", null)
                    .create().show();
        }
        return true;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if(toggle.onOptionsItemSelected(item))
        {
            return true;
        }

        switch (item.getItemId())
        {
            case R.id.git:
                startActivity(browse(git_url));
                break;

            case id.lyrics:
                String query = getLastQuery();
                if(query==null)
                {
                    break;
                }
                query+= Settings.LyricsSettings.custom_keywords;

                if(songPlayer!=null && !songPlayer.isStreaming())
                {
                    Toast.makeText(this,"Not available at the moment",Toast.LENGTH_SHORT).show();
                    break;
                }

                if(Settings.LyricsSettings.web_sites_list)
                {
                    Intent intent = new Intent(MainActivity.this,Lyrics.class);
                    intent.putExtra("query",query);
                    startActivity(intent);
                }
                else
                {
                    Snackbar.make(player_nav,"Processing",BaseTransientBottomBar.LENGTH_LONG).show();

                    boolean browser = Settings.LyricsSettings.lyrics_in.equals("Browser");

                    String finalQuery = query;
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            List<PyObject> links = MainActivity.main.callAttr("lyrics", finalQuery).asList();
                            String link = links.get(0).toString();
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Intent intent;
                                    if(browser)
                                    {
                                        intent = new Intent();
                                        intent.setData(Uri.parse(link));
                                    }
                                    else
                                    {
                                        intent = new Intent(MainActivity.this,WebBrowserAct.class);
                                        intent.putExtra("url",link);
                                    }
                                    startActivity(intent);
                                }
                            });
                        }
                    }).start();
                }
                break;

            case id.download:
                download(false);
                break;

            case id.open_in_yt:
                if(yt==null)
                {
                    Toast.makeText(this,"YT link not found",Toast.LENGTH_LONG).show();
                }
                else
                {
                    startActivity(browse(yt.url));
                }
                break;

            case  id.add_to_playlist:
                Intent intent = new Intent(MainActivity.this,PlayListsAct.class);
                intent.setAction("choose");

                if(songPlayer!=null && songPlayer.isStreaming())
                {
                    intent.putExtra("song",getLastQuery());
                }
                else
                {
                    String path = PlayListSongsAdapter.download_path+File.separator+title_str;
                    intent.putExtra("song",path);
                }
                startActivity(intent);
                break;

            default:

        }
        return super.onOptionsItemSelected(item);
    }

    public void start_download(Downloader downloader)
    {
        downloader.start();
        DownloadsAct.downloading_list.add(downloader);

        Snackbar.make(player_nav,"Added to download que", BaseTransientBottomBar.LENGTH_LONG)
                .setAction("Downloads", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startActivity(new Intent(MainActivity.this,DownloadsAct.class));
                    }
                })
                .show();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        if(receiver!=null)
        {
            unregisterReceiver(receiver);
        }
        super.onDestroy();
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public int getDuration(String duration)
    {
        int[] duration_array = Arrays.stream(duration.split(":")).mapToInt(Integer::parseInt).toArray();
        return duration_array[1] * 60 + duration_array[2];
    }

    public static void destroy_player()
    {
        if(MainActivity.songPlayer!=null)
        {
            songPlayer.destroy_player();
        }
    }
}