package com.posseggs.motionstream;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.renderscript.RenderScript;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
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

import static android.app.NotificationManager.EXTRA_NOTIFICATION_CHANNEL_ID;
import static android.app.NotificationManager.IMPORTANCE_HIGH;

public class MainActivity extends AppCompatActivity
{
    //Codes and identifiers
    public static final int REQUEST_CODE_SETTINGS = 12318;
    private static final String KEY_SP = "KEY_SP";
    private static final String KEY_URI = "KEY_URI";
    private static final String KEY_PLAY = "KEY_PLAY";
    private static final String KEY_PUSH = "KEY_PUSH";

    //The live stream will be displayed in the video view
    PlayerView playerView;

    //The player that manages the video stream
    SimpleExoPlayer player;

    public static Video video; //The video object will store all needed attributes

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
            startStream(); //Start stream if autoplay is enabled
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
        //Open settings activity
        Intent i = new Intent(this, SettingsActivity.class);
        startActivityForResult(i, REQUEST_CODE_SETTINGS);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == REQUEST_CODE_SETTINGS)
        {
            if (resultCode == RESULT_OK)
            {
                savePreferences();
                startStream();
                //Toast.makeText(this, video.getUri().toString(),Toast.LENGTH_LONG).show();
            }
            else
            {
                Toast.makeText(this, "Applying settings was canceled!", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void loadPreferences()
    {
        SharedPreferences sp = getSharedPreferences(KEY_SP,MODE_PRIVATE);

        //Set addresses from the shared Preferences
        video.setAutoplay(sp.getBoolean(KEY_PLAY,true));
        video.setNotify(sp.getBoolean(KEY_PUSH,true));
        video.setUri(sp.getString(KEY_URI, "rtmp://184.72.239.149/vod/mp4:bigbuckbunny_1500.mp4"));
    }

    //Save the settings to the shared preferences
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
            sp_editor.putBoolean(KEY_PUSH,video.getNotify());
            //Apply changes
            sp_editor.apply();
        }
    }

    private SimpleExoPlayer initStream()
    {
        //Initiate Player
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
            //Give the video view the player
            playerView.setPlayer(player);

            RtmpDataSourceFactory rtmpDataSourceFactory = new RtmpDataSourceFactory();
            // This is the MediaSource representing the media to be played.
            MediaSource videoSource = new ExtractorMediaSource.Factory(rtmpDataSourceFactory)
                    .createMediaSource(video.getUri());

            // Prepare the player with the source.
            player.prepare(videoSource);
            //Auto start playing
            player.setPlayWhenReady(video.getAutoplay());
        }
        catch (Exception ex)
        {
            Toast.makeText(this, ex.getMessage(),Toast.LENGTH_LONG).show();
        }
    }

    public void startStream()
    {
        //Starting stream by initialising player and then starting video stream
        player = initStream();
        showStream();
    }


    // Suppress Lint because of android studio bug:
    // https://stackoverflow.com/questions/48131068/warning-must-be-one-of-notificationmanager-importance
    @SuppressLint("WrongConstant")
    //Showing push notifications
    private void showNotification(String title, String content)
    {
        //If notifications are enabled
        if (video.getNotify())
        {
            NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                NotificationChannel channel = new NotificationChannel("default",
                        "NOTIFICATION_CHANNEL",
                        NotificationManager.IMPORTANCE_HIGH);
                channel.setDescription("SOME_CHANNEL_DESCRIPTION");
                mNotificationManager.createNotificationChannel(channel);
            }

            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(getApplicationContext(), "default")
                    .setSmallIcon(R.mipmap.ic_launcher) // notification icon
                    .setContentTitle(title) // title for notification
                    .setContentText(content)// message for notification
                    .setAutoCancel(true); // clear notification after click

            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            PendingIntent pi = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            mBuilder.setContentIntent(pi);
            mNotificationManager.notify(0, mBuilder.build());
        }
    }

    public void notify_OnClick(MenuItem menuItem)
    {
        showNotification("Attention: Motion has been detected!","Press here to access the stream!");
    }
}