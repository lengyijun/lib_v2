package com.example.steven.sjtu_lib_v2.activity;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.steven.sjtu_lib_v2.CollectionBook;
import com.example.steven.sjtu_lib_v2.R;
import com.example.steven.sjtu_lib_v2.adapter.CollectionAdapter;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;

//收藏界面的activity
public class Main2Activity extends AppCompatActivity {
    @Bind(R.id.listView2)ListView lv;
    SQLiteDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        ButterKnife.bind(this);

        db=openOrCreateDatabase("collection.db", Context.MODE_PRIVATE,null);

        final CollectionAdapter adapter=new CollectionAdapter(this,0,get_data());
        lv.setAdapter(adapter);

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                CollectionBook collectionBook=adapter.getItem(position);
                int choosed_id=collectionBook.getId();
                Cursor cursor=db.query("favourite", new String[]{"book_name", "url"}, "_id like ?", new String[]{choosed_id + ""}, null, null, null);
                cursor.moveToFirst();
                String name=cursor.getString(cursor.getColumnIndex("book_name"));
                String url=cursor.getString(cursor.getColumnIndex("url"));

                Intent intent=new Intent();
                intent.setClass(Main2Activity.this,Single_detail.class);
                intent.putExtra("detail",name);
                intent.putExtra("url",url);
                startActivity(intent);
            }
        });
    }

    public ArrayList<CollectionBook> get_data() {
        ArrayList<CollectionBook> book_list=new ArrayList<CollectionBook>();

        Cursor cursor = db.rawQuery("select * from favourite", null);
        if (cursor .moveToFirst()) {

        while (cursor.isAfterLast() == false) {
            String name = cursor.getString(cursor
                    .getColumnIndex("book_name"));
            String url= cursor.getString(cursor
                    .getColumnIndex("url"));
            int id=cursor.getInt(cursor.getColumnIndex("_id"));

            CollectionBook book=new CollectionBook(id,name,url);
            book_list.add(book);

            cursor.moveToNext();
            }
        }
        return book_list;
    }
}
