package com.example.ytstreamer40;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class DownloadsAct extends AppCompatActivity {

    static List<Downloader> downloading_list = new ArrayList<Downloader>();
    ImageView none;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_downloads);

        ListView downloads = findViewById(R.id.downloads);
        none = findViewById(R.id.none);

        BaseAdapter adapter = new BaseAdapter() {
            @Override
            public int getCount() {
                return downloading_list.size();
            }

            @Override
            public Object getItem(int position) {
                return null;
            }

            @Override
            public long getItemId(int position) {
                return 0;
            }

            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {

                if (convertView == null) {
                    LayoutInflater inflater = DownloadsAct.this.getLayoutInflater();
                    convertView = inflater.inflate(R.layout.custom_list_downloads, null);

                    Downloader current = downloading_list.get(position);

                    String file_name = current.file_name;

                    TextView title = convertView.findViewById(R.id.download_title);
                    title.setText(file_name);

                    ProgressBar progress = convertView.findViewById(R.id.progress);
                    progress.setMax(100);

                    TextView progress_txt_view = convertView.findViewById(R.id.progress_txt);

                    Handler handler = new Handler();
                    new Runnable() {
                        @RequiresApi(api = Build.VERSION_CODES.O)
                        @SuppressLint({"DefaultLocale", "SetTextI18n"})
                        @Override
                        public void run() {
                            int p;
                            p = current.getProgress();
                            progress_txt_view.setText(String.format("%d %%", p));
                            progress.setProgress(p);
                            if(p<100 && current.error==null) handler.postDelayed(this, 1000);

                            else if(current.error!=null)
                            {
                                progress.setVisibility(View.INVISIBLE);
                                progress_txt_view.setTextColor(Color.RED);
                                progress_txt_view.setText(current.error);

                                if(!current.is_notified())
                                {
                                    new ShowNotification(DownloadsAct.this,"Download Manager",String.format("Error occurred\n%s",file_name),position).show();
                                    current.set_notified(true);
                                }

                            }
                            else if(p==100)
                            {
                                progress.setVisibility(View.INVISIBLE);
                                progress_txt_view.setTextColor(Color.GREEN);
                                progress_txt_view.setText("Download Completed");

                                if(!current.is_notified())
                                {
                                    if(Settings.DownloadSettings.each_song)
                                    {
                                        new ShowNotification(DownloadsAct.this, "Download Manager", String.format("Download Completed\n%s", file_name),position).show();
                                    }

                                    boolean allfiles = DownloadsAct.downloading_list.stream().noneMatch(new Predicate<Downloader>() {
                                        @Override
                                        public boolean test(Downloader downloader) {
                                            return downloader.getProgress()<100;
                                        }
                                    });

                                    if(Settings.DownloadSettings.complete_list && allfiles)
                                    {
                                        new ShowNotification(DownloadsAct.this,"Download Manager","All files downloaded",200).show();
                                    }

                                    current.set_notified(true);
                                }

                            }
                        }
                    }.run();
                }
                return convertView;
            }
        };
        if(adapter.isEmpty())
        {
            none.setImageResource(R.drawable.noresultfound);
            downloads.setVisibility(View.GONE);
        }
        else
        {
            none.setVisibility(View.GONE);
            downloads.setAdapter(adapter);
        }
    }
}