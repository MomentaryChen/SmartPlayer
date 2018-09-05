package com.daniulive.smartplayer;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Debug;
import android.os.HandlerThread;
import android.os.IBinder;
import android.util.Log;

import java.sql.Time;
import java.util.concurrent.TimeUnit;


public class MyService extends Service
{
    private  Thread thread;
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
        Log.v("Sevice:","start  " + startId);
        // TODO Auto-generated method stub
        thread = new MyThread(getBaseContext());
        thread.start();
        return super.onStartCommand(intent, flags, startId);
    }
    @Override
    public boolean onUnbind(Intent intent)
    {
        // TODO Auto-generated method stub
        return super.onUnbind(intent);
    }
    public boolean stop(Intent name){
        thread.interrupt();
        Log.v("Service","stop");
        return true;
    }

    @Override
    public boolean stopService(Intent name) {
        return super.stopService(name);
    }

    public void onDestroy()
    {
        Log.v("Service","stop");
        if(thread!=null) thread.interrupt();
        this.stopSelf();
        super.onDestroy();
        // TODO Auto-generated method stub
    }

}

class MyThread extends Thread{
    private  Context context;
    public MyThread(Context context){
        this.context =context;
    }

    public void run(){
        while (!this.isInterrupted()){
            new SendCode("10",this.context).start();
            try {
                this.sleep(5000);
            } catch (InterruptedException e) {
                Log.v("Thread","exit");
                e.printStackTrace();
                break;
            }

        }
    }
}