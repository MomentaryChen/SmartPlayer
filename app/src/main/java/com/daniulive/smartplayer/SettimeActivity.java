package com.daniulive.smartplayer;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class SettimeActivity extends AppCompatActivity {
    Button add_time;
    ListView listview;
    private final static String createTable = "CREATE TABLE tableTime(_id interger not null ,hours int ,minutes int)";
    private SQLiteDatabase db=null;
    String code;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_time);
        setTitle("餵食時間設定");
        db =this.openOrCreateDatabase("Time.db", MODE_PRIVATE, null);
        //db.execSQL("drop table tableTime");
        try{
            db.execSQL(createTable);
            db.execSQL("INSERT INTO tableTime(_id,hours,minutes) values (0,0,10)");
            db.execSQL("INSERT INTO tableTime(_id,hours,minutes) values (1,0,10)");
        }catch (Exception e){
            //getActivity().setTitle("already insert ");
        }

        add_time = (Button) findViewById(R.id.btnAddTime);
        add_time.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {

                final EditText et_hours = new EditText(SettimeActivity.this);
                et_hours.setHint("0500");
                new AlertDialog.Builder(SettimeActivity.this).setTitle("輸入時間 EX:1800")
                        .setIcon(android.R.drawable.ic_dialog_info)
                        .setView(et_hours)
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                String input = et_hours.getText().toString();
                                if (input.equals("")) {
                                    Toast.makeText(getApplicationContext(), "搜索内容不能為空！" + input, Toast.LENGTH_LONG).show();
                                }
                                else {
                                    if(Integer.valueOf(input.substring(0,2)) >24 || Integer.valueOf(input.substring(2,4))> 60 ){

                                        Toast.makeText(getApplicationContext(), "時間輸入錯誤！" + input, Toast.LENGTH_LONG).show();
                                    }else{
                                        Log.v("H",input.substring(0,2));
                                        Log.v("M",input.substring(2,4));
                                        insertTime(Integer.valueOf(input.substring(0,2)),Integer.valueOf(input.substring(2,4)));
                                    }
                                }
                            }
                        })
                        .setNegativeButton("取消", null)
                        .show();
            }
        });

        listview = (ListView) findViewById(R.id.listview);
        //ListView 要顯示的內容
        //android.R.layout.simple_list_item_1 為內建樣式，還有其他樣式可自行研究
        upDate();
        listview.setOnItemClickListener(listviewDoListener);

    }
    private ListView.OnItemClickListener listviewDoListener=new ListView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view,int i, final long l) {
            new AlertDialog.Builder(SettimeActivity.this).setTitle("是否要刪除")
                    .setIcon(android.R.drawable.ic_dialog_info)
                    .setPositiveButton("確認刪除", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            deleteTime((int) l);
                        }
                    })
                    .setNegativeButton("取消", null)
                    .show();
        }
    };

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    void upDate(){
        Cursor cursor;
        cursor=getAll("tableTime");
        if (cursor != null && cursor.getCount() >= 0) {                             //資料庫載入Listview
            SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, R.layout.display_time, cursor, new String[]{"hours","minutes"
            }, new int[]{R.id.hours,R.id.minutes}, 0);
            listview.setAdapter(adapter);
        }
    }
    void insertTime(int hours, int minutes){
        Cursor cursor;
        cursor=getAll("tableTime");
        int id=0;
        if(cursor.getCount() > 0 || cursor.getCount()<=4){
            cursor.moveToLast();
            id = cursor.getInt(2);
            db.execSQL("INSERT INTO tableTime(_id,hours,minutes) values ("+ id +","+hours+","+minutes+")");
        }
        upDate();
    }
    void deleteTime(int _id){
        db.delete("tableTime", "_id = " + _id , null);
        upDate();
    }
    public Cursor getAll(String tableName) {
        Cursor cursor = db.rawQuery("SELECT * FROM "+tableName, null);
        return cursor;
    }
    void Destroy(){
        db.close();
    }
    public void onStop() {
        super.onStop();
        Cursor cursor;                                          //拿取時間   第一個字餵食 第二個位元幾筆資料
        cursor=getAll("tableTime");
        code ="12";
        code += Integer.toString(cursor.getCount());

        cursor.moveToFirst();
        for(int i=0;i<cursor.getCount();i++){
            code += checkTime(cursor.getInt(1),cursor.getInt(2));
            cursor.moveToNext();
        }
        new SendCode(code).start();
        Log.v("Code:",code);
    }

    public String checkTime(int hour,int minute){
        String str= "" ;
        if(hour<10){
            str +="0" + Integer.toString(hour);
        }else {
            str += Integer.toString(hour);
        }

        if(minute<10){
            str+="0" + Integer.toString(minute);
        }else{
            str += Integer.toString(minute);
        }

        return str;
    }

}