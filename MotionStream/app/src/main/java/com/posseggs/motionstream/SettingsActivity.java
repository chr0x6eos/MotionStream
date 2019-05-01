package com.posseggs.motionstream;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

public class SettingsActivity extends AppCompatActivity {

    EditText editUriText;
    Switch editSwitchAuto;
    Switch editSwitchPush;
    Boolean autoplay = true; //Default on true
    Boolean notify = true; //Default on true

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        editUriText = findViewById(R.id.editTextURI);
        editSwitchAuto = findViewById(R.id.switchAutoplay);
        editSwitchPush = findViewById(R.id.switchPushNotification);

        //Set default values
        editSwitchAuto.setChecked(autoplay);
        editSwitchPush.setChecked(notify);

        //Check if there already were settings saved, and set the gui values to these values.
        try
        {
            if (MainActivity.video != null)
            {
                if (MainActivity.video.getAutoplay() != null) {
                    editSwitchAuto.setChecked(MainActivity.video.getAutoplay());
                    autoplay = MainActivity.video.getAutoplay();
                }
                if (MainActivity.video.getNotify() != null)
                {
                    editSwitchPush.setChecked(MainActivity.video.getNotify());
                    notify = MainActivity.video.getNotify();
                }
                if (MainActivity.video.getUri() != null && MainActivity.video.getUri().toString() != "")
                    editUriText.setText(MainActivity.video.getUri().toString());
            }
        }
        catch (Exception ex)
        {
            Toast.makeText(this,"Error! No default settings exists!", Toast.LENGTH_LONG).show();
        }

        //Change Booleans when alternating the switches
        editSwitchAuto.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked)
                    autoplay = true;
                else
                    autoplay = false;
            }
        });
        editSwitchPush.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked)
                    notify = true;
                else
                    notify = false;
            }
        });
    }

    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.menu_settings,menu);
        return true;
    }

    public void apply_onClick(MenuItem menu)
    {
        try
        {
            //Apply settings to the video class and exit the settings tab
            MainActivity.video.setUri(editUriText.getText().toString());
            MainActivity.video.setAutoplay(autoplay);
            MainActivity.video.setNotify(notify);
            Intent i = new Intent();
            setResult(RESULT_OK, i);
            finish();
        }
        catch (Exception ex)
        {
            Toast.makeText(this, ex.getMessage(),Toast.LENGTH_LONG).show();
        }
    }

    public void cancel_onClick(MenuItem menu)
    {
        //Chancel settings and exit
        Intent i = new Intent();
        setResult(RESULT_CANCELED,i);
        finish();
    }
}
