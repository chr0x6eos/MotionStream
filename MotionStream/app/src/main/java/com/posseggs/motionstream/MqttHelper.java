package com.posseggs.motionstream;

import android.content.Context;
import android.util.Log;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.DisconnectedBufferOptions;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.util.ArrayList;

//Class taken from Darius Hasslauer and modified for this purpose
public class MqttHelper
{
        public MqttAndroidClient mqttAndroidClient;

        private static String TAG = "Mqtt";

        //ToDO:change stuff here
        final String serverUri = "tcp://172.18.202.202";

        final String clientId = "clientMsg";
        final String subscriptionTopic = "stream";
        final String archiveTopic = "archive";
        final String publishTopic = "app";

        final String username = "admin";
        final String password = "password";

        public MqttHelper(final Context context)
        {
            if (mqttAndroidClient == null)
            {
                mqttAndroidClient = new MqttAndroidClient(context, serverUri, clientId);
                mqttAndroidClient.setCallback(new MqttCallbackExtended() {
                    @Override
                    public void connectComplete(boolean b, String s) {
                        Log.w(TAG, s);
                    }

                    @Override
                    public void connectionLost(Throwable throwable) {
                        Log.w(TAG, throwable.getMessage());
                    }

                    @Override
                    public void messageArrived(String topic, MqttMessage mqttMessage) {
                        String message = mqttMessage.toString();
                        Log.w(TAG, message);
                    }

                    @Override
                    public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
                    }
                });
                connect();
            }
        }

        public String returnNotification(String message)
        {
             return message;
        }

        /*public ArrayList<String> returnArchive(String message)
        {
            //Concat with separator
            ArrayList<String> list = new ArrayList<>();
            String[] dates = message.split(",");

            for (String s: dates)
            {
                if (!list.contains(s))
                    list.add(s);
            }
            return list;
        }
        */

        public MqttAndroidClient getMqttAndroidClient()
        {
            return mqttAndroidClient;
        }

        public void setCallback(MqttCallbackExtended callback)
        {
            mqttAndroidClient.setCallback(callback);
        }

        private void connect()
        {
            MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
            mqttConnectOptions.setAutomaticReconnect(true);
            mqttConnectOptions.setCleanSession(false);
            mqttConnectOptions.setUserName(username);
            mqttConnectOptions.setPassword(password.toCharArray());

            try
            {

                mqttAndroidClient.connect(mqttConnectOptions, null, new IMqttActionListener()
                {
                    @Override
                    public void onSuccess(IMqttToken asyncActionToken)
                    {

                        DisconnectedBufferOptions disconnectedBufferOptions = new DisconnectedBufferOptions();
                        disconnectedBufferOptions.setBufferEnabled(true);
                        disconnectedBufferOptions.setBufferSize(100);
                        disconnectedBufferOptions.setPersistBuffer(false);
                        disconnectedBufferOptions.setDeleteOldestMessages(false);
                        mqttAndroidClient.setBufferOpts(disconnectedBufferOptions);
                        subscribeToTopic();
                    }

                    @Override
                    public void onFailure(IMqttToken asyncActionToken, Throwable exception)
                    {
                        Log.w(TAG, "Failed to connect to: " + serverUri  + " " + exception.toString());
                    }
                });


            }
            catch (MqttException ex)
            {
                ex.printStackTrace();
            }
        }

        public void publish(String payload)
        {
            try
            {
                //Send input-string to server with the topic publishTopic
                MqttMessage message = new MqttMessage(payload.getBytes());
                mqttAndroidClient.publish(publishTopic, message);
            }
            catch (Exception ex)
            {
                Log.w(TAG,"Failed to publish message: " + payload + " to the topic: " + publishTopic + ".");
            }
        }


        private void subscribeToTopic()
        {
            try
            {
                mqttAndroidClient.subscribe(subscriptionTopic, 0, null, new IMqttActionListener()
                {
                    @Override
                    public void onSuccess(IMqttToken asyncActionToken)
                    {
                        Log.w(TAG,"Successfully subscribed!");
                    }

                    @Override
                    public void onFailure(IMqttToken asyncActionToken, Throwable exception)
                    {
                        Log.w(TAG, "Subscribing failed!");
                    }
                });

                mqttAndroidClient.subscribe(archiveTopic, 0, null, new IMqttActionListener()
                {
                    @Override
                    public void onSuccess(IMqttToken asyncActionToken)
                    {
                        Log.w(TAG,"Successfully subscribed!");
                    }

                    @Override
                    public void onFailure(IMqttToken asyncActionToken, Throwable exception)
                    {
                        Log.w(TAG, "Subscribing failed!");
                    }
                });

            }
            catch (MqttException ex)
            {
                System.err.println("Exception whilst subscribing");
                ex.printStackTrace();
            }
        }
}
