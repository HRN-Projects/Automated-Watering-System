package com.example.harsh.materialui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

public class SubActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sub);
        Toolbar tb1 = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(tb1);

        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu item)
    {
        getMenuInflater().inflate(R.menu.sub_main, item);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem mi)
    {
        int id = mi.getItemId();

        switch (id)
        {
            case R.id.action_settings:
            {
                Toast.makeText(getApplicationContext(),"This is 'settings'...",Toast.LENGTH_SHORT).show();
                return true;
            }
            case android.R.id.home:
            {
                NavUtils.navigateUpFromSameTask(this);
                return true;
            }
            default:
            {
                return onOptionsItemSelected(mi);
            }
        }
    }
}
