package com.example.steven.sjtu_lib_v2.activity;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioGroup;

import com.alirezaafkar.toolbar.Toolbar;
import com.example.steven.sjtu_lib_v2.R;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.util.ArrayList;
import java.util.Collections;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.Call;

/**
 * Created by steven on 2016/2/7.
 */
public class Search_activity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener{
    @Bind(R.id.book_name)EditText et;
    @Bind(R.id.radio_button)RadioGroup radioGroup;
    @Bind(R.id.toolbar_search)Toolbar toolbar;
    @Bind(R.id.listView2)ListView lv;
    @Bind(R.id.nav_view)NavigationView navigationView;

    SQLiteDatabase db;
    String base_url="http://ourex.lib.sjtu.edu.cn/primo_library/libweb/action/search." +
            "do?fn=search&tab=default_tab&vid=chinese&scp.scps=scope%3A%28SJT%29%2Csc" +
            "ope%3A%28sjtu_metadata%29%2Cscope%3A%28sjtu_sfx%29%2Cscope%3A%28sjtulib" +
            "zw%29%2Cscope%3A%28sjtulibxw%29%2CDuxiuBook&vl%28freeText0%29=";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        getSupportActionBar().hide();

        navigationView.setNavigationItemSelectedListener(this);
        db=openOrCreateDatabase("collection.db", Context.MODE_PRIVATE,null);
        db.execSQL("create table if not exists search_history (_id INTEGER PRIMARY KEY AUTOINCREMENT, name text not null unique)");
        ArrayList<String> list=new ArrayList<String>();
        Cursor cursor = db.rawQuery("select * from search_history", null);
        if (cursor .moveToFirst()) {
            while (cursor.isAfterLast() == false) {
                String name = cursor.getString(cursor .getColumnIndex("name"));
                list.add(name);
                cursor.moveToNext();
            }
        }
        Collections.reverse(list);
        lv.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_expandable_list_item_1,list));
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String ss= (String) lv.getAdapter().getItem(position);
                ContentValues cv=new ContentValues();
                cv.put("name",ss);
                db.insertWithOnConflict("search_history", null, cv,SQLiteDatabase.CONFLICT_REPLACE);
                direct_search(base_url+ss);
            }
        });

        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                Intent intent=new Intent();
                intent.setClass(Search_activity.this,MyCollectionActivity.class);
                startActivity(intent);
                return true;
            }
        });
    }

    @OnClick(R.id.search_button) void jump_to_search(){
        String bookname=et.getText().toString();
        final String url=base_url+bookname;

        ContentValues cv=new ContentValues();
        cv.put("name", bookname);
        db.insertWithOnConflict("search_history", null, cv, SQLiteDatabase.CONFLICT_REPLACE);

        final int choosed_id=radioGroup.getCheckedRadioButtonId();
        if (choosed_id==-1 || choosed_id ==R.id.all_lib){
            direct_search(url);
        }else {
            OkHttpUtils.get()
                    .url(url)
                    .build()
                    .execute(new StringCallback() {
                        @Override
                        public void onError(Call call, Exception e) {

                        }

                        @Override
                        public void onResponse(String response) {
                            Document doc= Jsoup.parse(response);
                            String url_to_intent = null;
                            switch (choosed_id){
                                case R.id.new_lib:
                                     url_to_intent= doc.getElementsMatchingText("主馆图书").attr("href");
                                     break;
                                case R.id.baotu:
                                    url_to_intent= doc.getElementsMatchingText("包玉刚图书馆图书").attr("href");
                                    break;
                                case R.id.subscribing:
                                    url_to_intent= doc.getElementsMatchingText("正在订购").attr("href");
                                    break;
                                case R.id.xuhui:
                                    url_to_intent= doc.getElementsMatchingText("徐汇社科馆").attr("href");
                                    break;
                                case R.id.literature:
                                    url_to_intent= doc.getElementsMatchingText("人文学院分馆").attr("href");
                                    break;

                            }
                            direct_search(url_to_intent);

                        }
                    });
        }
    }

    private void direct_search(String url) {
        Intent intent=new Intent();
        intent.setClass(Search_activity.this,MainActivity.class);
        intent.putExtra("url",url);
        startActivity(intent);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id=item.getItemId();
        if(id==R.id.log_in){
            Intent intent=new Intent();
            intent.setClass(Search_activity.this,MyBorrow.class);
            startActivity(intent);
        }else if(id==R.id.mycollection){
            Intent intent=new Intent();
            intent.setClass(Search_activity.this,MyCollectionActivity.class);
            startActivity(intent);
        }
        return true;
    }
}
