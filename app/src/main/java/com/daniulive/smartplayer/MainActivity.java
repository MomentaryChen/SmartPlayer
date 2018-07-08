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
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        watch = (Button) findViewById(R.id.watch);
        setTime = (Button) findViewById(R.id.setTime);
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

        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
    /*
        watch.setWidth(metrics.widthPixels/5);
        watch.setHeight(metrics.heightPixels/5);*/

    }
}
