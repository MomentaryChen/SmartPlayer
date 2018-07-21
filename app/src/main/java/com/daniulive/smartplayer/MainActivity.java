package com.daniulive.smartplayer;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {
    Button watch ;
    Button setTime ;
    Button album ;
    Button exit ;
    private String recDir = "/sdcard/daniulive/playrec"; // for recorder path
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
    /*
        watch.setWidth(metrics.widthPixels/5);
        watch.setHeight(metrics.heightPixels/5);*/

    }
}
