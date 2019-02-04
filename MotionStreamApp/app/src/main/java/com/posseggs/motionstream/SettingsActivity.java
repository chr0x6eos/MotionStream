package com.posseggs.motionstream;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

public class SettingsActivity extends AppCompatActivity {

    EditText editUriText;
    Switch editSwitch;
    String uri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        editUriText = findViewById(R.id.editTextURI);
        editSwitch = findViewById(R.id.switchAutoplay);

        try
        {
            if (MainActivity.video != null)
            {
                if (MainActivity.video.getAutoplay() != null)
                    editSwitch.setChecked(MainActivity.video.getAutoplay());
                if (MainActivity.video.getUri() != null && MainActivity.video.getUri().toString() != "")
                    editUriText.setText(MainActivity.video.getUri().toString());
            }
        }
        catch (Exception ex)
        {
            Toast.makeText(this,"Error! No default settings exists!",Toast.LENGTH_LONG).show();
        }
        editSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked)
                    MainActivity.video.setAutoplay(true);
                 else
                    MainActivity.video.setAutoplay(false);
            }
        });
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_settings,menu);
        return true;
    }

    public void apply_onClick(MenuItem menu)
    {
        try
        {
            MainActivity.video.setUri(editUriText.getText().toString());
            Intent i = new Intent();
            setResult(RESULT_OK,i);
            finish();
        }
        catch (Exception ex)
        {
            Toast.makeText(this, ex.getMessage(),Toast.LENGTH_LONG).show();
        }
    }

    public void cancel_onClick(MenuItem menu)
    {
        Intent i = new Intent();
        setResult(RESULT_CANCELED);
        finish();
    }
}
