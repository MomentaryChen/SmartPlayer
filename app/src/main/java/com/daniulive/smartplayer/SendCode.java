package com.daniulive.smartplayer;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
public class SendCode extends Thread {
    String host = "192.168.43.207";
    int port = 6666;
    Socket socket = null;
    //覆寫Thread方法run()
    private  String code;
    public SendCode(String code){
        this.code = code ;
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
            Log.v("接收訊息",in.readLine());
            output.flush();
            output.close();

            if (input != null)
                input.close();
            if (output != null)
                output.close();
        } catch (IOException e) {
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