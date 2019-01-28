package com.posseggs.motionstream;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

public class SettingsActivity extends AppCompatActivity {

    EditText editUriText;
    String uri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        editUriText = findViewById(R.id.editTextURI);
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
}
