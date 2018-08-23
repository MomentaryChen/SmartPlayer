package com.daniulive.smartplayer;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import java.sql.Time;
import java.util.concurrent.TimeUnit;


public class MyService extends Service
{
    public class LocalBinder extends Binder //宣告一個繼承 Binder 的類別 LocalBinder
    {
        MyService getService()
        {
            return  MyService.this;
        }
    }
    private LocalBinder mLocBin = new LocalBinder();
    @Override
    public IBinder onBind(Intent arg0)
    {
        // TODO Auto-generated method stub
        return mLocBin;
    }

    @Override
    public void onCreate()
    {
        super.onCreate();
        // TODO Auto-generated method stub
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {

        // TODO Auto-generated method stub
        new Thread(new Runnable(){
            public void run() {
                // TODO Auto-generated method stub
                while(true)
                {
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    new SendCode("12",getBaseContext()).start();
                    Log.v("Service-Thread","start");

                    //REST OF CODE HERE//
                }
            }
        }).start();

        return super.onStartCommand(intent, flags, startId);
    }
    @Override
    public boolean onUnbind(Intent intent)
    {
        // TODO Auto-generated method stub
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        // TODO Auto-generated method stub
    }
}
