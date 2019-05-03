package com.example.harsh.materialui;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.NumberPicker;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import static android.widget.Toast.LENGTH_LONG;
import static android.widget.Toast.LENGTH_SHORT;
import static android.widget.Toast.makeText;

public class MainActivity extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener {

    public Handler handler;
    private TextView minView, secView;
    private Switch sw;
    private String minutes, seconds, temp_min, temp_sec, temp_Time;
    private NumberPicker secPick, MinPick;
    private int min, sec, mts, TtMill, total;
    private boolean Soe = true, valid = true;
    private NavigationView mNavView;
    private DrawerLayout mDrawerLayout;
    private Cursor cursor;

    private Toolbar tb1;

    SQLiteDatabase myDB;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tb1 = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(tb1);
        getSupportActionBar().setDisplayShowHomeEnabled(true);


        mainDrawerFrag dr = (mainDrawerFrag) getSupportFragmentManager().findFragmentById(R.id.fragment_main_drawer);
        dr.setUp(R.id.fragment_main_drawer, (DrawerLayout) findViewById(R.id.dr_layout), tb1);


        // Getting Values from all components on this activity
        minView = (TextView) findViewById(R.id.MinView);
        secView = (TextView) findViewById(R.id.SecView);

        secPick = (NumberPicker) findViewById(R.id.SecPick);
        MinPick = (NumberPicker) findViewById(R.id.MinPick);

        secPick.setMaxValue(60);
        secPick.setMinValue(0);
        MinPick.setMaxValue(180);
        MinPick.setMinValue(0);

        secPick.setWrapSelectorWheel(false);
        MinPick.setWrapSelectorWheel(false);

        sw = (Switch) findViewById(R.id.switch1);
        sw.setOnCheckedChangeListener(this);

        {

            //Database creation
            myDB = openOrCreateDatabase("iedc_AWS_DB.db", MODE_PRIVATE, null);
        }


        // Table creation
        myDB.execSQL("CREATE TABLE IF NOT EXISTS aws_logbook (id INTEGER PRIMARY KEY,duraMin INTEGER, duraSec INTEGER,sTime DATETIME);");

        mNavView = (NavigationView) findViewById(R.id.navMenuView);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.dr_layout);

        mNavView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                        int mid = menuItem.getItemId();


                        // Log book SELECT query
                        switch (mid) {
                            case R.id.navDrawerLog: {
                                String[] args = new String[]{};
                                try {
                                    Cursor cursor = myDB.rawQuery("SELECT duraMin, duraSec, sTime FROM aws_logbook ORDER BY id DESC LIMIT 5 ;", args);
                                    if (cursor.moveToFirst()) {
                                        temp_min = cursor.getString(cursor.getColumnIndex(" duraMin "));
                                        temp_sec = cursor.getString(cursor.getColumnIndex(" duraSec "));
                                        temp_Time = cursor.getString(cursor.getColumnIndex(" sTime "));

                                        showLogData();
                                    }
                                } catch (Exception e) {
                                    Toast.makeText(getApplicationContext(), "Error : " + e, Toast.LENGTH_SHORT).show();
                                } finally {
                                }

                                Toast.makeText(getApplicationContext(), "This is drawer item 1.", Toast.LENGTH_SHORT).show();
                                return true;
                            }
                            case R.id.navDrawerPWS: {
                                Toast.makeText(getApplicationContext(), "This is navigation drawer item 2.", Toast.LENGTH_SHORT).show();
                                return true;
                            }
                            default: {
                                return false;
                            }
                        }
                    }
                }
        );
    }


    // Function for programmatically hanging up ongoing call
    public void endCall()
    {
        try {

            //String serviceManagerName = "android.os.IServiceManager";
            String serviceManagerName = "android.os.ServiceManager";
            String serviceManagerNativeName = "android.os.ServiceManagerNative";
            String telephonyName = "com.android.internal.telephony.ITelephony";

            Class telephonyClass;
            Class telephonyStubClass;
            Class serviceManagerClass;
            Class serviceManagerStubClass;
            Class serviceManagerNativeClass;
            Class serviceManagerNativeStubClass;

            Method telephonyCall;
            Method telephonyEndCall;
            Method telephonyAnswerCall;
            Method getDefault;

            Method[] temps;
            Constructor[] serviceManagerConstructor;

            // Method getService;
            Object telephonyObject;
            Object serviceManagerObject;

            telephonyClass = Class.forName(telephonyName);
            telephonyStubClass = telephonyClass.getClasses()[0];
            serviceManagerClass = Class.forName(serviceManagerName);
            serviceManagerNativeClass = Class.forName(serviceManagerNativeName);

            Method getService = // getDefaults[29];
                    serviceManagerClass.getMethod("getService", String.class);

            Method tempInterfaceMethod = serviceManagerNativeClass.getMethod(
                    "asInterface", IBinder.class);

            Binder tmpBinder = new Binder();
            tmpBinder.attachInterface(null, "fake");

            serviceManagerObject = tempInterfaceMethod.invoke(null, tmpBinder);
            IBinder retbinder = (IBinder) getService.invoke(serviceManagerObject, "phone");
            Method serviceMethod = telephonyStubClass.getMethod("asInterface", IBinder.class);

            telephonyObject = serviceMethod.invoke(null, retbinder);
            //telephonyCall = telephonyClass.getMethod("call", String.class);
            telephonyEndCall = telephonyClass.getMethod("endCall");
            //telephonyAnswerCall = telephonyClass.getMethod("answerRingingCall");

            telephonyEndCall.invoke(telephonyObject);

        } catch (Exception e) {
            e.printStackTrace();
            Log.v(String.valueOf(MainActivity.this),
                    "FATAL ERROR: could not connect to telephony subsystem");
            Log.v(String.valueOf(MainActivity.this), "Exception object: " + e);
        }
    }


    // Function for showing alertbox having watering log data
    public void showLogData() {
        int lid = 1;
        AlertDialog.Builder lb_show = new AlertDialog.Builder(this);
        lb_show.setTitle("Last 5 Logs -")
                .setMessage("id - " + lid + "Duration - " + temp_min + ":" + temp_sec + "Started on - " + temp_Time)
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(MainActivity.this, "Set time by scrolling UP / DOWN.", Toast.LENGTH_LONG).show();
                    }
                });
        AlertDialog TimeSet = lb_show.create();
    }


    //Function for starting timer for 30 seconds
    public void setFor30()
    {
        secPick.setValue(30);
        MinPick.setValue(00);
        sw.toggle();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu m) {
        getMenuInflater().inflate(R.menu.main, m);
        return true;
    }


    // Option selection and their actions in Action bar
    @Override
    public boolean onOptionsItemSelected(MenuItem mi) {
        int id = mi.getItemId();

        switch (id) {
            case R.id.action_settings: {
                Toast.makeText(getApplicationContext(), "Watering started for 30 seconds...", Toast.LENGTH_SHORT).show();
                setFor30();
                return true;
            }
            /*case R.id.action_navigate:
            {
                startActivity(new Intent(this, SubActivity.class));
                return true;
            }*/
            default: {
                return onOptionsItemSelected(mi);
            }
        }
    }


    // Function for checking if both of the timer value are Zero
    public void ifZero() {
        if ((sec == 0) && (min == 0)) {
            AlertDialog.Builder alertTime = new AlertDialog.Builder(this);
            alertTime.setTitle("Time not set!")
                    .setMessage("Please set valid time.")
                    .setCancelable(false)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Toast.makeText(MainActivity.this, "Set time by scrolling UP / DOWN.", Toast.LENGTH_LONG).show();
                        }
                    });
            AlertDialog TimeSet = alertTime.create();
            TimeSet.show();

            valid = false;
        } else {
            return;
        }
    }


    // Function for sending SMS to the defined number
    public void SmsSender() {
        String PhoneNo = "96xxxxxxxx";

        if (Soe == true) {
            try {
                myDB.execSQL("INSERT INTO aws_logbook (rowid, duraMin, duraSec, sTime) VALUES( null , "+ min + "," + sec + ",datetime());");
                SmsManager smsManager = SmsManager.getDefault();
                smsManager.sendTextMessage(PhoneNo, null, "Watering started.\n\t Time Set for :\n" + min + " minutes " + sec + " seconds.", null, null);
                Toast.makeText(getApplicationContext(), "SMS Sent!", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Toast.makeText(getApplicationContext(), "SMS failed, please try again later!", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        } else {
            try {
                SmsManager smsManager = SmsManager.getDefault();
                smsManager.sendTextMessage(PhoneNo, null, "Watering Added.", null, null);
                Toast.makeText(getApplicationContext(), "SMS Sent!", Toast.LENGTH_SHORT).show();
                Soe = true;
            } catch (Exception e) {
                Toast.makeText(getApplicationContext(), "SMS failed, please try again later!", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }
    }


    // Function for displaying running timer time.
    public void dispTimer() {
        sec = sec - 1;
        if ((sec < 0) && (min > 0)) {
            sec = 59;
            min = min - 1;
            seconds = String.valueOf(sec);
            minutes = String.valueOf(min);
        }
        seconds = String.valueOf(sec);
        minutes = String.valueOf(min);

        secView.setText(seconds);
        minView.setText(minutes);
    }


    // Function for checking if the switch is Checked(ON)
    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        try {

            Runnable sprinkler = null;
            if (isChecked) {

                sec = secPick.getValue();
                min = MinPick.getValue();

                ifZero();

                if (valid == false) {
                    valid = true;
                    throw new Exception();
                }


                // Intent for starting a call
                Intent callIntent = new Intent(Intent.ACTION_CALL);
                callIntent.setData(Uri.parse("tel:96xxxxxxxx"));
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                startActivity(callIntent);

                mts = min * 60;
                total = mts + sec;
                TtMill = (total) * 1000;

                Soe = true;
                SmsSender();

                minutes = String.valueOf(min);
                seconds = String.valueOf(sec);

                minView.setText(minutes);
                secView.setText(seconds);

                handler = new Handler();
                sprinkler = new Runnable() {
                    @Override
                    public void run() {
                        TtMill = TtMill - 1000;
                        dispTimer();
                        if (TtMill > 0)
                        {
                            handler.postDelayed(this, 1000);
                        }
                        else
                        {
                            Soe = false;
                            SmsSender();
                            secPick.setValue(0);
                            MinPick.setValue(0);
                            sw.toggle();

                            endCall();
                        }
                    }
                };

                handler.postDelayed(sprinkler, 1000);

                makeText(this, "TImer status : ON\nTime set : " + min + " min " + sec + " sec.", LENGTH_LONG).show();
            }
            else
            {
                handler.removeCallbacksAndMessages(sprinkler);
                makeText(this, "Timer status : OFF", LENGTH_SHORT).show();
                secPick.setValue(0);
                MinPick.setValue(0);
                minView.setText("00");
                secView.setText("00");
                Soe = false;
                SmsSender();
            }
        }
        catch (Exception e)
        {
            sw.toggle();
            makeText(this, "Error : Try again later.", LENGTH_SHORT).show();
            valid = true;
        }
        cursor.close();
        myDB.close();
    }
}
