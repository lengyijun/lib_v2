package com.example.steven.sjtu_lib_v2;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;

/**
 * Created by zcw on 16/4/5.
 */
public class DbHelper extends SQLiteOpenHelper{
    public static final String TABLE_NAME = "BorrowDB";
    public static final String BOOKNAME = "bookname";
    public static final String ID = "_id";
    public static final String RETURNDATE = "returndate";


    public DbHelper(Context mContext, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(mContext,name,factory,version);

    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        sqLiteDatabase.execSQL("CREATE TABLE " + TABLE_NAME +
                "(" + ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                BOOKNAME + " TEXT NOT NULL," +
                RETURNDATE + " TEXT NOT NULL)");
    }


    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }


    public Cursor query(String tableName){
            SQLiteDatabase db = this.getReadableDatabase();
            Cursor cursor = db
                    .query(tableName, null, null, null, null, null, null);
            return cursor;
    }

    public void insert(ArrayList<Element> elementArrayList){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cValue = new ContentValues();
        for(int i =0;i<elementArrayList.size();i++){
                Elements elements_td1=elementArrayList.get(i).getElementsByClass("td1");
                String bookname = elements_td1.get(3).text();
                String return_day = elements_td1.get(5).text();
                cValue.put("bookname", bookname);
                cValue.put("returndate", return_day);
                db.insert("BorrowDB", null, cValue);
            }
    }
}
