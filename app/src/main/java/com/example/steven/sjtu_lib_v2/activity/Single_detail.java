package com.example.steven.sjtu_lib_v2.activity;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.alirezaafkar.toolbar.Toolbar;
import com.example.steven.sjtu_lib_v2.R;
import com.example.steven.sjtu_lib_v2.adapter.TableAdapter;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import okhttp3.Call;

/**
 * Created by steven on 2016/2/11.
 */
public class Single_detail extends AppCompatActivity {
    @Bind(R.id.detail)TextView tv;
    @Bind(R.id.listview_table) ListView lv_table;
    @Bind(R.id.toolbar) Toolbar toolbar;

    TableAdapter adapter;
    List<Element> table_data=new ArrayList<Element>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.single_info);
        ButterKnife.bind(this);

        getSupportActionBar().hide();
        adapter=new TableAdapter(getApplicationContext(),0,table_data);
        lv_table.setAdapter(adapter);

        final String detail_html=get_html_from_intent();
        final String url=get_url_from_intent();
        tv.setText(Html.fromHtml(detail_html));
        get_table_data(url);

        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()){
                    case R.id.add_to_collection:
                        SQLiteDatabase db=openOrCreateDatabase("collection.db", Context.MODE_PRIVATE,null);
                        db.execSQL("create table if not exists favourite (_id INTEGER PRIMARY KEY AUTOINCREMENT, book_name VARCHAR, url VARCHAR)");

                        ContentValues cv=new ContentValues();
                        cv.put("book_name",detail_html);
                        cv.put("url",url);
                        db.insert("favourite", null, cv);

                        Toast.makeText(getApplicationContext(),"收藏成功",Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.remove_from_collection:
                        Toast.makeText(getApplicationContext(),"remove frem collection",Toast.LENGTH_SHORT).show();
                        break;
                }
                return true;
            }
        });
    }

    private String get_html_from_intent() {
        String detail_html=getIntent().getExtras().getString("detail");
        return detail_html;
    }

    public String get_url_from_intent() {
        String url=getIntent().getExtras().getString("url");
        return url;
    }

    public void get_table_data(String url) {
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
                        Elements EXLLocationTableColumn1_eles=doc.getElementsByClass("EXLLocationTableColumn1");
                        for(Element i:EXLLocationTableColumn1_eles){
                            table_data.add(i.parent());
                        }
                        adapter.notifyDataSetChanged();
                    }
                });
    }
}
