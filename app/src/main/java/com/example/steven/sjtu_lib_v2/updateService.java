package com.example.steven.sjtu_lib_v2;

import android.app.Service;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.IBinder;

import java.util.Timer;
import java.util.TimerTask;

public class UpdateService extends Service {
    public UpdateService() {
    }
    private Timer timer;
    private TimerTask task;
    private Cursor cursor;
    private SQLiteDatabase dbReader;
    private DbHelper db;
    @Override
    public void onCreate() {
        db=new DbHelper(this.getApplicationContext(),"BorrowDB",null,1);
        dbReader=db.getReadableDatabase();
        super.onCreate();
        timer = new Timer();
        task = new TimerTask() {
            @Override
            public void run() {
                cursor=dbReader.query("BorrowDB",null,null,null,null,null,null,null);
                if (cursor != null) {
                    System.out.println(cursor.getString(0));
                }else {
                    System.out.println("数据库为空");
                }
            }
        };
        timer.schedule(task, 1000, 1000);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        System.out.println("onDestroy");
        super.onDestroy();
    }
}
