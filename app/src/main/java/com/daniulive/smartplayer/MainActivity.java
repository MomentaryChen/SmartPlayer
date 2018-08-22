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

    /*private ServiceConnection mServiceConnection = new ServiceConnection()
    {
        @Override
        public void onServiceConnected(ComponentName name, IBinder serviceBinder)
        {
            // TODO Auto-generated method stub
            mMyService = ((MyService.LocalBinder)serviceBinder).getService();
        }

        public void onServiceDisconnected(ComponentName name)
        {
            // TODO Auto-generated method stub
            Log.d("Service", "onServiceDisconnected()" + name.getClassName());
        }
    };*/

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


        /*Intent it = new Intent(MainActivity.this, MyService.class);
        startService(it); //開始Service


        it = new Intent(MainActivity.this, MyService.class);
        bindService(it, mServiceConnection, BIND_AUTO_CREATE); //綁定Service

        if (mMyService != null)
            mMyService.myMethod(); //透過bindService()可以使用Service中的方法*/
    /*
        watch.setWidth(metrics.widthPixels/5);
        watch.setHeight(metrics.heightPixels/5);*/
    }

   /* public void onStop() {
        super.onStop();


        unbindService(mServiceConnection); //解除綁定Service
        mMyService = null;
        Intent it = new Intent(MainActivity.this, MyService.class);
        stopService(it); //結束Service
    }

    public void onDestroy() {
        super.onDestroy();
    }*/


}
