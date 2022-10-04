package com.example.ytstreamer40;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

@RequiresApi(api = Build.VERSION_CODES.M)
public class SettingsActivity extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener, AdapterView.OnItemSelectedListener {

    SwitchCompat switch_set_looping,switch_show_websites,switch_video_list,auto_download,auto_generate_title,auto_shuffle;
    CheckBox check_each_song,complete_list;
    Spinner show_lyrics_in;
    Settings settings;
    String spinner_txt;
    TextView spinner_txt_v,custom_keywords,custom_keywords_txt;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);

        settings = new Settings(this);

        switch_set_looping = findViewById(R.id.set_looping);
        switch_video_list = findViewById(R.id.show_video_list);

        switch_show_websites = findViewById(R.id.web_sites_list);
        show_lyrics_in = findViewById(R.id.show_lyrics_in);
        custom_keywords = findViewById(R.id.set_custom_keyword_txt);
        custom_keywords_txt = findViewById(R.id.custom_keywords);

        check_each_song = findViewById(R.id.each_song);
        complete_list = findViewById(R.id.complete_list);
        auto_download = findViewById(R.id.auto_download);
        auto_generate_title = findViewById(R.id.auto_generate_title);

        auto_shuffle = findViewById(R.id.auto_shuffle);

        switch_set_looping.setOnCheckedChangeListener(this);
        switch_show_websites.setOnCheckedChangeListener(this);
        check_each_song.setOnCheckedChangeListener(this);
        complete_list.setOnCheckedChangeListener(this);
        auto_download.setOnCheckedChangeListener(this);
        auto_generate_title.setOnCheckedChangeListener(this);
        switch_video_list.setOnCheckedChangeListener(this);
        auto_shuffle.setOnCheckedChangeListener(this);
        show_lyrics_in.setOnItemSelectedListener(this);

        custom_keywords.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText edit = new EditText(SettingsActivity.this);
                edit.setText(Settings.LyricsSettings.custom_keywords);

                new AlertDialog.Builder(SettingsActivity.this)
                        .setTitle("Custom keywords")
                        .setView(edit)
                        .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String text = edit.getText().toString();
                                settings.set_custom_keywords(text);
                                custom_keywords_txt.setText(text);
                            }
                        })
                        .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        })
                        .show();
            }
        });

        switch_set_looping.setChecked(Settings.PlayerSettings.looping);
        switch_video_list.setChecked(Settings.PlayerSettings.show_video_list);

        spinner_txt = getResources().getString(R.string.show_lyrics_in);
        spinner_txt_v = findViewById(R.id.spin);
        String[] drop = new String[] {"Select","App","Browser"};
        show_lyrics_in.setAdapter(new ArrayAdapter<String>(this, androidx.appcompat.R.layout.support_simple_spinner_dropdown_item,drop ));
        spinner_txt_v.setText(spinner_txt+" "+ Settings.LyricsSettings.lyrics_in);
        switch_show_websites.setChecked(Settings.LyricsSettings.web_sites_list);
        custom_keywords_txt.setText(Settings.LyricsSettings.custom_keywords);

        check_each_song.setChecked(Settings.DownloadSettings.each_song);
        complete_list.setChecked(Settings.DownloadSettings.complete_list);
        auto_download.setChecked(Settings.DownloadSettings.auto_download);
        auto_generate_title.setChecked(Settings.DownloadSettings.auto_generate_title);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

        switch(buttonView.getId())
        {

            case R.id.set_looping:
                settings.set_looping(isChecked);
                break;


            case R.id.show_video_list:
                settings.show_video_list(isChecked);
                break;

            case R.id.web_sites_list:
                settings.show_websites_list(isChecked);
                break;

            case R.id.each_song:
                settings.notify_after_each_song(isChecked);
                break;

            case R.id.complete_list:
                settings.set_complete_list_notification(isChecked);
                break;

            case R.id.auto_download:
                settings.set_auto_download(isChecked);
                if(isChecked)
                {
                    settings.set_auto_generate_title(true);
                    auto_generate_title.setChecked(true);
                }
                break;

            case R.id.auto_generate_title:
                if(Settings.DownloadSettings.auto_download && !isChecked)
                {
                    settings.set_auto_download(false);
                    auto_download.setChecked(false);
                }
                settings.set_auto_generate_title(isChecked);
                break;

            case R.id.auto_shuffle:
                settings.setAutoShuffle(isChecked);
                break;

        }
    }

    @Override
    protected void onDestroy() {
        settings.commit();
        super.onDestroy();
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        String item = parent.getItemAtPosition(position).toString();
        if(!item.equals("Select"))
        {
            settings.show_lyrics_in(item);
            spinner_txt_v.setText(spinner_txt+" "+item);
        }
    }
    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}