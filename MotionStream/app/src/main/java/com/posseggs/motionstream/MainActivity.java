package com.posseggs.motionstream;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.videolan.libvlc.IVLCVout;
import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.Media;
import org.videolan.libvlc.MediaPlayer;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity
{
    //Codes and identifiers
    public final static String TAG = "MainActivity";
    public static final int REQUEST_CODE_SETTINGS = 12318;
    public static final int REQUEST_CODE_ARCHIVE = 21813;
    private static final String KEY_SP = "KEY_SP";
    private static final String KEY_URI = "KEY_URI";
    private static final String KEY_PLAY = "KEY_PLAY";
    private static final String KEY_PUSH = "KEY_PUSH";
    public static final String DEF_URI = "rtmp://172.18.202.202:1935/live/test"; //Default path

    //MqttHelper for managing server-client messaging
    static MqttHelper mqttHelper;

    public static Video video; //The video object will store all needed attributes

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try
        {
            video = new Video();
            loadPreferences(); //Load from previous settings
            mqttHelper = new MqttHelper(this);
            startMqtt();
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

                if (notificationMessage.contains("Motion detected")) {
                    showNotification("Attention: Motion has been detected!", "Press here to access the stream!"//;
                            + " MQTT message: " + notificationMessage);
                }
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

    //When pressed send msg to server to invoke stream
    public void invokeStream_OnClick(MenuItem menu)
    {
        mqttHelper.publish("Start Stream");
    }

    public void archive_OnClick(MenuItem menu)
    {
        Intent i = new Intent(this, ArchiveActivity.class);
        startActivityForResult(i, REQUEST_CODE_ARCHIVE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == REQUEST_CODE_SETTINGS)
        {
            if (resultCode == RESULT_OK)
            {
                savePreferences();

                //Toast.makeText(this, video.getUri().toString(),Toast.LENGTH_LONG).show();
            }
            else
            {
                Toast.makeText(this, "Applying settings was canceled!", Toast.LENGTH_LONG).show();
            }
        }
    }

    public void accessStream(View v)
    {
        //Stream stream = (Stream) getFragmentManager().findFragmentById(R.id.stream);
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


    /*private void setOrientation()
    {
        //https://stackoverflow.com/questions/14858214/how-to-rotate-video-mp4-in-surfaceview

        DisplayMetrics displaymetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        int h = displaymetrics.heightPixels;
        int w = displaymetrics.widthPixels;

        holder = surface.getHolder();

        if (w > h) {
            holder.setFixedSize(w,h);
        } else {
            holder.setFixedSize(h,w);
        }
    }*/

    // Suppress Lint because of android studio bug: https://stackoverflow.com/questions/48131068/warning-must-be-one-of-notificationmanager-importance
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

    /* Testing notifications
    public void notify_OnClick(MenuItem menuItem)
    {
        showNotification("Attention: Motion has been detected!","Press here to access the stream!");
    }
    */
}