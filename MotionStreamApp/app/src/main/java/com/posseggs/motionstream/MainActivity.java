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
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;

public class MainActivity extends AppCompatActivity
{

    public static final int REQUEST_CODE_SETTINGS = 12318;

    //The live stream will be displayed in the video view
    VideoView streamFeed;

    //String link = "http://www.ted.com/talks/download/video/8584/talk/761";
    String link = "https://archive.org/download/ksnn_compilation_master_the_internet/ksnn_compilation_master_the_internet_512kb.mp4";

    public static Video video; //The video obj will store all needed attributes

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        streamFeed = findViewById(R.id.videoFeed);

        try
        {
            video = new Video(link);
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
        try
        {
            streamFeed.setVideoURI(Uri.parse("http://www.pocketjourney.com/downloads/pj/video/famous.3gp"));
            MediaController mediaController = new MediaController(this);
            streamFeed.setMediaController(mediaController);
            streamFeed.requestFocus();
            streamFeed.start();
        }
        catch (Exception ex)
        {
            Toast.makeText(this, ex.getMessage(),Toast.LENGTH_LONG).show();
        }
    }

    public void stopFeed()
    {
        streamFeed.stopPlayback();
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
