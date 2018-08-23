package com.daniulive.smartplayer;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.app.NotificationCompat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.UnknownHostException;

public class MainActivity extends AppCompatActivity {
    Button watch, setTime, album, exit ;
    private MyService mMyService = null;
    private String recDir = "/sdcard/daniulive/playrec"; // for recorder path
    //private ServiceConnection mServiceConnection ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        watch = (Button) findViewById(R.id.watch);
        setTime = (Button) findViewById(R.id.setTime);
        album = (Button) findViewById(R.id.album);
        exit = (Button) findViewById(R.id.exit) ;
        setTitle("智慧型寵物寶");
        watch.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setClass(MainActivity.this,SmartPlayer.class);
                startActivity(intent);
            }
        });
        setTime.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setClass(MainActivity.this,SettimeActivity.class);
                startActivity(intent);
            }
        });
        album.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setClass(MainActivity.this, RecorderManager.class);
                intent.putExtra("RecoderDir", recDir);
                startActivity(intent);
            }
        });

        exit.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
                System.exit(0);
            }
        });

        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);

        Intent it = new Intent(MainActivity.this, MyService.class);
        this.startService(it); //開始Service
        //bindService(it, mServiceConnection, BIND_AUTO_CREATE); //綁定Service
    }

   public void onStop() {
        super.onStop();
    }

    public void onDestroy() {
        super.onDestroy();
        Intent it = new Intent(MainActivity.this, MyService.class);
        this.stopService(it); //結束Service
    }


}
