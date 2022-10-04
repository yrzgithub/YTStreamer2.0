package com.example.ytstreamer40;

import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import java.io.IOException;

public class SongPlayer extends MediaPlayer {

    private static final int SKIP = 5000;
    MediaPlayer media_player;
    boolean stream, buffering_completed = false;

    SongPlayer(String source, boolean stream)
    {
        this.stream = stream;

        media_player = new MediaPlayer();
        media_player.setAudioAttributes(new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build());

        setLooping(Settings.PlayerSettings.looping);

        try
        {
            setDataSource(source);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        prepareAsync();
    }

    public void setBufferCompleted(boolean completed)
    {
        buffering_completed = completed;
    }

    public boolean isBufferCompleted()
    {
        return buffering_completed;
    }

    public boolean isStreaming() {
        return this.stream;
    }

    public void start(ImageView play, ImageView thumbnail) throws IllegalStateException {
        if (!isStreaming()) {
            Glide.with(thumbnail).load(R.drawable.local_song_playing).into(thumbnail);
        }
        play.setImageResource(R.drawable.pause);
        try {
            super.start();
        } catch (Exception ignored) {

        }
    }

    public void pause(ImageView play, ImageView thumbnail) {
        if (!isStreaming())
        {
            thumbnail.setImageResource(R.drawable.local_song_playing);
        }

        play.setImageResource(R.drawable.play);

        try
        {
            super.pause();
        }
        catch (Exception ignored)
        {

        }
    }

    @Override
    public void setOnPreparedListener(OnPreparedListener listener) {
        super.setOnPreparedListener(listener);
    }

    @Override
    public void setOnCompletionListener(OnCompletionListener listener) {
        super.setOnCompletionListener(listener);
    }

    public int getDuration() {
        return media_player.getDuration();
    }

    public void setDataSource(String source) throws IOException, IllegalArgumentException, IllegalStateException, SecurityException {
        super.setDataSource(source);
    }

    @Override
    public void setOnErrorListener(OnErrorListener listener) {
        OnErrorListener error = new OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                mp.reset();
                return false;
            }
        };
        super.setOnErrorListener(error);
    }

    @Override
    public int getCurrentPosition() throws IllegalStateException{
        try
        {
            return super.getCurrentPosition();
        }

        catch (IllegalStateException e)
        {
            return 0;
        }
    }

    @Override
    public void setOnBufferingUpdateListener(OnBufferingUpdateListener listener) {
        super.setOnBufferingUpdateListener(listener);
    }

    @Override
    public void seekTo(int msec) throws IllegalStateException {
        try
        {
            super.seekTo(msec);
        }
        catch (Exception ignored)
        {

        }
    }

    public void forward()
    {
        try
        {
            seekTo(super.getCurrentPosition()+SKIP);
        }
        catch (Exception ignored)
        {

        }
    }

    public void backward()
    {
        try
        {
            seekTo(super.getCurrentPosition()-SKIP);
        }
        catch (Exception ignored)
        {

        }
    }

    @Override
    public boolean isPlaying() {
        return super.isPlaying();
    }

    @Override
    public void stop() throws IllegalStateException {
        super.stop();
    }

    public void destroy_player() throws NullPointerException
    {
        try
        {
            stop();
            reset();
            release();
        }

        catch (NullPointerException | IllegalStateException ignored)
        {

        }
    }
}
