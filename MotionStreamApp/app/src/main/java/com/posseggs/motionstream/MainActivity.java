package com.posseggs.motionstream;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;

public class MainActivity extends AppCompatActivity
{

    //The live stream will be displayed here
    VideoView streamFeed;
    Video v;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        streamFeed = findViewById(R.id.videoFeed);

        try
        {
            v = new Video("https://archive.org/download/ksnn_compilation_master_the_internet/ksnn_compilation_master_the_internet_512kb.mp4");
        }
        catch (Exception ex)
        {
            Toast.makeText(this, ex.getMessage(),Toast.LENGTH_LONG).show();
        }
    }

    public void startFeed()
    {
        try
        {
            streamFeed.setVideoURI(v.getUri());
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
        Toast.makeText(this,"Started video feed",Toast.LENGTH_LONG).show();
    }

    public void stop_onClick(View v)
    {
        stopFeed();
    }
}
