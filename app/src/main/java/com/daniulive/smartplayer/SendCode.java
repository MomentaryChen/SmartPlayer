package com.daniulive.smartplayer;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
public class SendCode extends Thread {
    String host = "192.168.43.207";
    int port = 6666;
    Socket socket = null;
    //覆寫Thread方法run()
    private  String code;
    Context context =null;
    public SendCode(String code){
        this.code = code;
    }

    public SendCode(String code, Context context)  {
        try {
            Log.v("SendCode = ",code);
            this.code = new String(code.getBytes("UTF-8"));
            this.context = context;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }
    public void call() {
        if(context==null) {
            Log.v("Context is ","null");
            return;
        }
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        //Intent intent = new Intent(context, XXXActivity.class);//将要跳转的界面
        Intent intent = new Intent();//只显示通知，无页面跳转
        builder.setAutoCancel(true);//点击后消失
        builder.setSmallIcon(R.drawable.cat);//设置通知栏消息标题的头像
        builder.setDefaults(NotificationCompat.DEFAULT_SOUND);//设置通知铃声
        builder.setTicker("餓了");
        builder.setContentTitle("寵物好餓");
        builder.setContentText("快來餵食寵物喔");
        //利用PendingIntent来包装我们的intent对象,使其延迟跳转
        PendingIntent intentPend = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        builder.setContentIntent(intentPend);
        NotificationManager manager = (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);
        manager.notify(0, builder.build());
    }
    public void run() {
        try {
            socket = new Socket(host, port);
            DataInputStream input = null;
            DataOutputStream output = null;
            input = new DataInputStream(socket.getInputStream());
            output = new DataOutputStream(socket.getOutputStream());
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            output.writeUTF(code);
            //output.writeUTF(code);
            this.sleep(1000);
            String getCode = in.readLine();
            if(!(getCode==null || getCode =="")){
                Log.v("接收訊息", getCode);
            }
            if (getCode.equals("111")) {
                this.call();
            }
            output.flush();
            output.close();

            if (input != null)
                input.close();
            if (output != null)
                output.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}