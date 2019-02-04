package com.posseggs.motionstream;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.ext.rtmp.RtmpDataSourceFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;

public class MainActivity extends AppCompatActivity
{
    public static final int REQUEST_CODE_SETTINGS = 12318;
    public static final String KEY_URI = "KEY_URI";
    public static final String KEY_PLAY = "KEY_PLAY";
    private static final String KEY_SP = "KEY_SP";

    //The live stream will be displayed in the video view
    PlayerView playerView;

    //The player that manages the video stream
    SimpleExoPlayer player;

    public static Video video; //The video obj will store all needed attributes

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        try
        {
            playerView = findViewById(R.id.player);
            video = new Video();
            loadPreferences(); //Load from previous settings
            startStream();
        }
        catch (Exception ex)
        {
            Toast.makeText(this, ex.getMessage(),Toast.LENGTH_LONG).show();
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main,menu);
        return true;
    }

    public void settings_OnClick(MenuItem menu)
    {
        Intent i = new Intent(this, SettingsActivity.class);
        startActivityForResult(i, REQUEST_CODE_SETTINGS);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == REQUEST_CODE_SETTINGS && resultCode == RESULT_OK)
        {
            savePreferences();
            startStream();
            //Toast.makeText(this, video.getUri().toString(),Toast.LENGTH_LONG).show();
        }
    }

    private void loadPreferences()
    {
        SharedPreferences sp = getSharedPreferences(KEY_SP,MODE_PRIVATE);

        //Set addresses from the shared Preferences
        video.setAutoplay(sp.getBoolean(KEY_PLAY,true));
        video.setUri(sp.getString(KEY_URI, "rtmp://184.72.239.149/vod/mp4:bigbuckbunny_1500.mp4"));
    }

    //Save the addresses to the shared preferences
    public void savePreferences()
    {
        //Setup sp
        SharedPreferences sp = getSharedPreferences(KEY_SP,MODE_PRIVATE);
        SharedPreferences.Editor sp_editor = sp.edit();

        //Save to editor
        if (video != null)
        {
            sp_editor.putString(KEY_URI, video.getUri().toString());
            sp_editor.putBoolean(KEY_PLAY,video.getAutoplay());
            //Apply changes
            sp_editor.apply();
        }
    }

    private SimpleExoPlayer initStream()
    {
        //initiate Player
        //Create a default TrackSelector
        BandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
        TrackSelection.Factory videoTrackSelectionFactory = new AdaptiveTrackSelection.Factory(bandwidthMeter);
        TrackSelector trackSelector = new DefaultTrackSelector(videoTrackSelectionFactory);

        //Create the player
        return player = ExoPlayerFactory.newSimpleInstance(this, trackSelector);
    }

    //Functions for stream
    public void showStream()
    {
        try
        {
            playerView.setPlayer(player);

            RtmpDataSourceFactory rtmpDataSourceFactory = new RtmpDataSourceFactory();
            // This is the MediaSource representing the media to be played.
            MediaSource videoSource = new ExtractorMediaSource.Factory(rtmpDataSourceFactory)
                    .createMediaSource(video.getUri());

            // Prepare the player with the source.
            player.prepare(videoSource);
            //auto start playing
            player.setPlayWhenReady(video.getAutoplay());
        }
        catch (Exception ex)
        {
            Toast.makeText(this, ex.getMessage(),Toast.LENGTH_LONG).show();
        }
    }

    public void startStream()
    {
        player = initStream();
        showStream();
    }
}
