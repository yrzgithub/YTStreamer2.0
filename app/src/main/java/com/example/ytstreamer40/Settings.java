package com.example.ytstreamer40;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.Nullable;

import java.util.Map;
import java.util.Set;

public class Settings implements SharedPreferences,SharedPreferences.Editor {

    SharedPreferences settings_data;
    SharedPreferences.Editor editor;

    Settings(Context context)
    {
        settings_data = context.getSharedPreferences("settings_data",Context.MODE_PRIVATE);
        editor = settings_data.edit();

        PlayerSettings.looping = getBoolean("looping",true);
        PlayerSettings.show_video_list = getBoolean("show_video_list",true);

        LyricsSettings.web_sites_list = getBoolean("web_sites_list",true);
        LyricsSettings.lyrics_in = getString("lyrics_in","Browser");
        LyricsSettings.custom_keywords = getString("custom_keywords","lyrics in tamil");

        SearchSettings.local_songs = getBoolean("local_songs",true);
        SearchSettings.yt_songs = getBoolean("yt_songs",true);

        DownloadSettings.each_song = getBoolean("each_song",false);
        DownloadSettings.auto_download = getBoolean("auto_download",false);
        DownloadSettings.auto_generate_title = getBoolean("auto_generate_title",false);
        DownloadSettings.complete_list = getBoolean("complete_list",false);

        PlayListSettings.shuffle = getBoolean("shuffle",false);
    }

    public void setAutoShuffle(boolean shuffle)
    {
        PlayListSettings.shuffle = shuffle;
        putBoolean("shuffle",shuffle);
    }

    public void set_complete_list_notification(boolean c)
    {
        DownloadSettings.complete_list = c;
        putBoolean("complete_list",c);
    }

    public void set_auto_generate_title(boolean t)
    {
        DownloadSettings.auto_generate_title = t;
        putBoolean("auto_generate_title",t);
    }

    public void set_auto_download(boolean d)
    {
        DownloadSettings.auto_download = d;
        putBoolean("auto_download",d);
        if(d)
        {
            set_auto_generate_title(true);
        }
    }

    public void set_looping(boolean loop)
    {
        putBoolean("looping",loop);
    }

    public void show_video_list(boolean show)
    {
        PlayerSettings.show_video_list = show;
        putBoolean("show_video_list",show);
    }

    public void show_websites_list(boolean show)
    {
        LyricsSettings.web_sites_list = show;
        putBoolean("web_sites_list",show);
    }

    public void set_custom_keywords(String key)
    {
        LyricsSettings.custom_keywords = key;
        putString("custom_keywords",key);
    }

    public void show_lyrics_in(String in)
    {
        LyricsSettings.lyrics_in = in;
        putString("lyrics_in",in);
    }

    public void notify_after_each_song(boolean notify)
    {
        DownloadSettings.each_song = notify;
        putBoolean("each_song",notify);
    }

    @Override
    public Map<String, ?> getAll() {
        return settings_data.getAll();
    }

    @Nullable
    @Override
    public String getString(String key, @Nullable String defValue) {
        return settings_data.getString(key,defValue);
    }

    @Nullable
    @Override
    public Set<String> getStringSet(String key, @Nullable Set<String> defValues) {
        return settings_data.getStringSet(key,defValues);
    }

    @Override
    public int getInt(String key, int defValue) {
        return settings_data.getInt(key, defValue);
    }

    @Override
    public long getLong(String key, long defValue) {
        return settings_data.getLong(key,defValue);
    }

    @Override
    public float getFloat(String key, float defValue) {
        return settings_data.getFloat(key, defValue);
    }

    @Override
    public boolean getBoolean(String key, boolean defValue) {
        return settings_data.getBoolean(key, defValue);
    }

    @Override
    public boolean contains(String key) {
        return settings_data.contains(key);
    }

    @Override
    public Editor edit() {
        return editor;
    }

    @Override
    public void registerOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener listener) {

    }

    @Override
    public void unregisterOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener listener) {

    }

    @Override
    public Editor putString(String key, @Nullable String value) {
        editor.putString(key, value).commit();
        return null;
    }

    @Override
    public Editor putStringSet(String key, @Nullable Set<String> values) {
        editor.putStringSet(key, values).commit();
        return null;
    }

    @Override
    public Editor putInt(String key, int value) {
        editor.putInt(key, value).commit();
        return null;
    }

    @Override
    public Editor putLong(String key, long value) {
        editor.putLong(key, value).commit();
        return null;
    }

    @Override
    public Editor putFloat(String key, float value) {
        editor.putFloat(key, value).commit();
        return null;
    }

    @Override
    public Editor putBoolean(String key, boolean value) {
        editor.putBoolean(key, value).commit();
        return null;
    }

    @Override
    public Editor remove(String key) {
        editor.remove(key);
        return null;
    }

    @Override
    public Editor clear() {
        editor.clear();
        return null;
    }

    @Override
    public boolean commit() {
        return editor.commit();
    }

    @Override
    public void apply() {
        editor.apply();
    }

    static class PlayerSettings
    {
        static boolean looping,show_video_list;
    }

    static class LyricsSettings
    {
        static boolean web_sites_list;
        static String lyrics_in;
        static String custom_keywords;
    }

    static class SearchSettings
    {
        static boolean local_songs,yt_songs;
    }

    static class DownloadSettings
    {
        static boolean each_song,auto_download,auto_generate_title,complete_list;
    }

    static class PlayListSettings
    {
        static boolean shuffle;
    }

}