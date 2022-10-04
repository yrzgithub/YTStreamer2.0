package com.example.ytstreamer40;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SearchView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.chaquo.python.PyException;
import com.chaquo.python.PyObject;

import java.util.Arrays;
import java.util.Map;

public class VideoList extends AppCompatActivity {

    ListAdapterClass adapter;
    PyObject main;
    PyObject[] videos;
    Map<PyObject,PyObject> video;
    SwipeRefreshLayout swipe;
    ImageView loading;
    String search;

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_list);

        this.swipe = findViewById(R.id.swipe);

        Intent intent = getIntent();
        this.search = intent.getStringExtra("search");

        loading = findViewById(R.id.loading);
        Glide.with(this).load(R.drawable.loading_pink_list).into(loading);

        ListView songs_list = findViewById(R.id.vid_list);
        songs_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                if(MainActivity.songPlayer!=null) MainActivity.destroy_player();

                Intent intent = new Intent(VideoList.this,MainActivity.class);

                VideoList.this.video = videos[position].asMap();

                Bundle bundle = new Bundle();
                bundle.putString("thumb",getString("thumb"));
                bundle.putString("title",getString("title"));
                bundle.putString("url",getString("url"));
                bundle.putInt("duration",getDuration());

                intent.setAction("play");
                intent.putExtra("data_bundle",bundle);
                startActivity(intent);
            }
        });

        list_videos(search,loading,songs_list);
        swipe.setEnabled(true);

        swipe.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                list_videos(search,loading,songs_list);
                swipe.setRefreshing(false);
            }
        });

    }

    void list_videos(String search,ImageView loading,ListView songs_list)
    {
        new Thread()
        {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void run() {
                super.run();

                main = MainActivity.main;
                try
                {
                    videos = main.callAttr("search",search).toJava(PyObject[].class);
                }
                catch (PyException ignored)
                {

                }

                if(videos!=null && videos.length!=0)
                {
                    VideoList.this.adapter = new ListAdapterClass(VideoList.this,videos);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            loading.setVisibility(View.GONE);
                            songs_list.setAdapter(adapter);
                        }
                    });
                }

                else
                {
                   runOnUiThread(new Runnable() {
                       @Override
                       public void run() {
                           Glide.with(VideoList.this).load(R.drawable.noresultfound).into(VideoList.this.loading);
                       }
                   });
                }
            }
        }.start();
    }

    public String getString(String key)
    {
        PyObject object_key  = PyObject.fromJava(key);
        PyObject res = video.get(object_key);
        if(res==null) return "Not Found";
        else return res.toString();
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public int getDuration()
    {
        int[] duration_array = Arrays.stream(getString("duration").split(":")).mapToInt(Integer::parseInt).toArray();
        return duration_array[0] * 60 + duration_array[1];
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_video_list,menu);

        SearchView search = (SearchView) menu.findItem(R.id.search_video_list_menu).getActionView();

        AutoCompleteTextView auto = (AutoCompleteTextView) search.findViewById(search.getContext().getResources().getIdentifier("android:id/search_src_text",null,null));
        auto.setThreshold(1);
        auto.setDropDownBackgroundResource(R.color.white);

        search.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                Intent intent  = new Intent(VideoList.this,VideoList.class);
                intent.putExtra("search",query);
                startActivity(intent);
                finishAndRemoveTask();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        String[]suggestions;
                        try {
                            suggestions = main.callAttr("suggest",newText).toJava(String[].class);
                            ArrayAdapter<String> suggestions_list = new ArrayAdapter<String>(VideoList.this, androidx.appcompat.R.layout.support_simple_spinner_dropdown_item,suggestions);

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    auto.setAdapter(suggestions_list);
                                }
                            });
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

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId())
        {
            case R.id.git:
                startActivity(MainActivity.browse(MainActivity.git_url));
                break;

            case R.id.search_in_yt_menu:
                String url = "https://www.youtube.com/search?q=" + search;
                startActivity(MainActivity.browse(url));
                break;

            default:
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        Intent intent  = new Intent(this,MainActivity.class);
        intent.setAction("reload");
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}