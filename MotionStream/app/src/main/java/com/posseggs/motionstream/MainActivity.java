package com.posseggs.motionstream;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.ext.rtmp.RtmpDataSourceFactory;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.extractor.ExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class MainActivity extends AppCompatActivity
{
    //Codes and identifiers
    public static final int REQUEST_CODE_SETTINGS = 12318;
    private static final String KEY_SP = "KEY_SP";
    private static final String KEY_URI = "KEY_URI";
    private static final String KEY_PLAY = "KEY_PLAY";
    private static final String KEY_PUSH = "KEY_PUSH";
    public static final String DEF_URI = "rtmp://172.18.202.202:1935/live/test";

    //The live stream will be displayed in the video view
    PlayerView playerView;

    //MqttHelper for managing server-client messaging
    MqttHelper mqttHelper;

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
            startMqtt();
            startStream(); //Start stream if autoplay is enabled
        }
        catch (Exception ex)
        {
            Toast.makeText(this, ex.getMessage(),Toast.LENGTH_LONG).show();
        }
    }

    private void startMqtt() {
        mqttHelper = new MqttHelper(getApplicationContext());
        mqttHelper.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean b, String s) {

            }

            @Override
            public void connectionLost(Throwable throwable) {

            }

            @Override
            public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {
                Log.w("Debug", mqttMessage.toString());
                //For now not needed
                String notificationMessage = mqttMessage.toString();
                //if (notificationMessage == "Stream1")
                showNotification("Attention: Motion has been detected!","Press here to access the stream!" +
                        " MQTT message: " + notificationMessage);
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {

            }
        });
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
        video.setUri(sp.getString(KEY_URI, DEF_URI));
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
        //Release player if one exists currently
        if (player != null)
        {
            player.release();
        }
        player = null;

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
            //MediaSource videoSource = new ExtractorMediaSource.Factory(rtmpDataSourceFactory).createMediaSource(video.getUri());

            //Other possible solution
            ExtractorsFactory extractorsFactory = new DefaultExtractorsFactory();
            MediaSource videoSource = new ExtractorMediaSource(Uri.parse(video.getUri().toString()+ " live=1 buffer=1000"),rtmpDataSourceFactory,extractorsFactory,null,null);

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
    public void showNotification(String title, String content)
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
                    .setSmallIcon(R.mipmap.ic_launcher) // Notification icon
                    .setContentTitle(title) // Title for notification
                    .setContentText(content)// Message for notification
                    .setAutoCancel(true); // Clear notification after click

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