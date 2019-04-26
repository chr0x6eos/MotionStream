package com.posseggs.motionstream;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.videolan.libvlc.IVLCVout;
import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.Media;

import android.view.Gravity;
import android.view.ViewGroup.LayoutParams;

import org.videolan.libvlc.MediaPlayer;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity
{
    //Codes and identifiers
    public final static String TAG = "MainActivity";
    public static final int REQUEST_CODE_SETTINGS = 12318;
    private static final String KEY_SP = "KEY_SP";
    private static final String KEY_URI = "KEY_URI";
    private static final String KEY_PLAY = "KEY_PLAY";
    private static final String KEY_PUSH = "KEY_PUSH";
    public static final String DEF_URI = "rtmp://172.18.202.202:1935/live/test";

    private SurfaceHolder holder;
    private LibVLC libvlc;
    private MediaPlayer mMediaPlayer = null;
    private int mVideoWidth;
    private int mVideoHeight;

    SurfaceView surface;

    //MqttHelper for managing server-client messaging
    MqttHelper mqttHelper;


    public static Video video; //The video object will store all needed attributes

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        surface = findViewById(R.id.surface);

        holder = surface.getHolder();

        try
        {
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


    //Functions for stream
    public void showStream()
    {
        try
        {
            createPlayer(video.getUri().toString());
        }
        catch (Exception ex)
        {
            Toast.makeText(this, ex.getMessage(),Toast.LENGTH_LONG).show();
        }
    }

    public void startStream()
    {
        //Starting stream by initialising player and then starting video stream
        showStream();
    }

    /*private void setSize(int width, int height) {
        mVideoWidth = width;
        mVideoHeight = height;
        if (mVideoWidth * mVideoHeight <= 1)
            return;

        if (holder == null || surface == null)
            return;

        int w = getWindow().getDecorView().getWidth();
        int h = getWindow().getDecorView().getHeight();
        boolean isPortrait = getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
        if (w > h && isPortrait || w < h && !isPortrait) {
            int i = w;
            w = h;
            h = i;
        }

        float videoAR = (float) mVideoWidth / (float) mVideoHeight;
        float screenAR = (float) w / (float) h;

        if (screenAR < videoAR)
            h = (int) (w / videoAR);
        else
            w = (int) (h * videoAR);

        holder.setFixedSize(mVideoWidth, mVideoHeight);
        LayoutParams lp = surface.getLayoutParams();
        lp.width = w;
        lp.height = h;
        surface.setLayoutParams(lp);
        surface.invalidate();
    }
    */

    private void createPlayer(String media) {
        releasePlayer();
        try {
            if (media.length() > 0) {
                Toast toast = Toast.makeText(this, media, Toast.LENGTH_LONG);
                toast.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0,
                        0);
                toast.show();
            }

            // Create LibVLC
            // TODO: make this more robust, and sync with audio demo
            ArrayList<String> options = new ArrayList<String>();
            //options.add("--subsdec-encoding <encoding>");
            options.add("--aout=opensles");
            options.add("--audio-time-stretch"); // time stretching
            options.add("-vvv"); // verbosity
            libvlc = new LibVLC(this, options);
            holder.setKeepScreenOn(true);

            // Creating media player
            mMediaPlayer = new MediaPlayer(libvlc);
            mMediaPlayer.setEventListener(mPlayerListener);

            // Seting up video output
            final IVLCVout vout = mMediaPlayer.getVLCVout();
            vout.setVideoView(surface);
            //vout.setSubtitlesView(mSurfaceSubtitles);
            //vout.addCallback(this);
            vout.attachViews();

            Media m = new Media(libvlc, Uri.parse(media));
            mMediaPlayer.setMedia(m);
            mMediaPlayer.play();

        } catch (Exception e) {
            Toast.makeText(this, "Error in creating player!", Toast
                    .LENGTH_LONG).show();
        }
    }

    private void releasePlayer() {
        if (libvlc == null)
            return;
        mMediaPlayer.stop();
        final IVLCVout vout = mMediaPlayer.getVLCVout();
        //vout.removeCallback(this);
        vout.detachViews();
        holder = null;
        libvlc.release();
        libvlc = null;

        mVideoWidth = 0;
        mVideoHeight = 0;
    }

    private MediaPlayer.EventListener mPlayerListener = new MyPlayerListener(this);

    /*@Override
    public void onNewLayout(IVLCVout vout, int width, int height, int visibleWidth, int visibleHeight, int sarNum, int sarDen) {
        if (width * height == 0)
            return;

        // store video size
        mVideoWidth = width;
        mVideoHeight = height;
        setSize(mVideoWidth, mVideoHeight);
    }
    */

    private static class MyPlayerListener implements MediaPlayer.EventListener {
        private WeakReference<MainActivity> mOwner;

        public MyPlayerListener(MainActivity owner) {
            mOwner = new WeakReference<MainActivity>(owner);
        }

        @Override
        public void onEvent(MediaPlayer.Event event) {
            MainActivity player = mOwner.get();

            switch (event.type) {
                case MediaPlayer.Event.EndReached:
                    Log.d(TAG, "MediaPlayerEndReached");
                    player.releasePlayer();
                    break;
                case MediaPlayer.Event.Playing:
                case MediaPlayer.Event.Paused:
                case MediaPlayer.Event.Stopped:
                default:
                    break;
            }
        }
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