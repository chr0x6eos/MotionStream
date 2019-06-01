package com.posseggs.motionstream;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.util.ArrayList;

import static com.posseggs.motionstream.MainActivity.mqttHelper;

public class ArchiveActivity extends AppCompatActivity {

   ListView archive;
   ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_archive);
        archive = findViewById(R.id.listViewArchive);
        startMqtt();
        mqttHelper.publish("ls"); //Send ls to server
    }

    private void startMqtt() {
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
                else if (notificationMessage.contains(".flv"))
                {
                    //Concat with separator
                    ArrayList<String> list = new ArrayList<>();
                    String[] dates = notificationMessage.split(",");

                    for (String s: dates)
                    {
                        if (!list.contains(s))
                            list.add(s);
                    }

                    adapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_2, android.R.id.text1,list) {

                        @Override
                        public View getView(int position, View convertView, ViewGroup parent) {
                            // the position is the index if the item in the ListView
                            // the view v is the layout of the item
                            View v = super.getView(position, convertView, parent);

                            TextView textViewName = v.findViewById(android.R.id.text1);
                            String s = adapter.getItem(position);
                            textViewName.setText("Stream from: " + s);
                            //Return the prepared view
                            return v;
                        }
                    };
                    archive.setAdapter(adapter);

                    archive.setOnItemClickListener((parent, view, position, id) -> {
                        mqttHelper.publish(list.get(position));
                        ArchiveActivity.this.finish();
                    });
                }
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {

            }
        });
    }

    // Suppress Lint because of android studio bug: https://stackoverflow.com/questions/48131068/warning-must-be-one-of-notificationmanager-importance
    @SuppressLint("WrongConstant")
    //Showing push notifications
    public void showNotification(String title, String content)
    {
        //If notifications are enabled
        if (MainActivity.video.getNotify())
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
}
