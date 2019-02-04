package com.posseggs.motionstream;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;
import android.widget.VideoView;

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

    //The live stream will be displayed in the video view
    String path = "rtmp://184.72.239.149/vod/mp4:bigbuckbunny_1500.mp4";

    public static Video video; //The video obj will store all needed attributes

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try
        {
            video = new Video(path);
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
            Toast.makeText(this, video.getUri().toString(),Toast.LENGTH_LONG).show();
        }
    }

    public void startFeed()
    {

        try {
            //initiate Player
            //Create a default TrackSelector
            BandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
            TrackSelection.Factory videoTrackSelectionFactory = new AdaptiveTrackSelection.Factory(bandwidthMeter);
            TrackSelector trackSelector = new DefaultTrackSelector(videoTrackSelectionFactory);

            //Create the player
            SimpleExoPlayer player = ExoPlayerFactory.newSimpleInstance(this, trackSelector);
            PlayerView playerView = findViewById(R.id.simple_player);
            playerView.setPlayer(player);

            RtmpDataSourceFactory rtmpDataSourceFactory = new RtmpDataSourceFactory();
            // This is the MediaSource representing the media to be played.
            MediaSource videoSource = new ExtractorMediaSource.Factory(rtmpDataSourceFactory)
                    .createMediaSource(video.getUri());

            // Prepare the player with the source.
            player.prepare(videoSource);
            //auto start playing
            player.setPlayWhenReady(true);

        }
        catch (Exception ex)
        {
            Toast.makeText(this, ex.getMessage(),Toast.LENGTH_LONG).show();
        }
    }

    public void stopFeed()
    {

    }

    public void start_onClick(View v)
    {
        startFeed();
        Toast.makeText(this,"Started video feed " + video.getUri().toString() ,Toast.LENGTH_LONG).show();
    }

    public void stop_onClick(View v)
    {
        stopFeed();
    }
}
