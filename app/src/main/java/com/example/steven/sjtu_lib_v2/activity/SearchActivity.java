package com.example.steven.sjtu_lib_v2.activity;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.steven.sjtu_lib_v2.R;
import com.example.steven.sjtu_lib_v2.adapter.TextTagsAdapter;
import com.example.steven.sjtu_lib_v2.identicons.Identicon;
import com.lapism.searchview.adapter.SearchAdapter;
import com.lapism.searchview.adapter.SearchItem;
import com.lapism.searchview.view.SearchCodes;
import com.lapism.searchview.view.SearchView;
import com.moxun.tagcloudlib.view.TagCloudView;
import com.snappydb.DB;
import com.snappydb.DBFactory;
import com.snappydb.SnappydbException;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import okhttp3.Call;

/**
 * Created by steven on 2016/2/7.
 */
public class SearchActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    @Bind(R.id.nav_view)
    NavigationView navigationView;
    @Bind(R.id.drawer_layout)
    DrawerLayout drawerLayout;
    @Bind(R.id.searchView)
    SearchView searchView;
    @Bind(R.id.tag_cloud)
    TagCloudView tagCloud;

    Identicon identicon;
    TextView tvNaviTitle;
    TextView tvNaviSubTitle;

    private List<SearchItem> mSuggestionList;
    private int mTheme = SearchCodes.THEME_LIGHT;
    SQLiteDatabase db;
    String base_url = "http://ourex.lib.sjtu.edu.cn/primo_library/libweb/action/search." +
            "do?fn=search&tab=default_tab&vid=chinese&scp.scps=scope%3A%28SJT%29%2Csc" +
            "ope%3A%28sjtu_metadata%29%2Cscope%3A%28sjtu_sfx%29%2Cscope%3A%28sjtulib" +
            "zw%29%2Cscope%3A%28sjtulibxw%29%2CDuxiuBook&vl%28freeText0%29=";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_drawer);
        ButterKnife.bind(this);
        Toolbar toolbar1 = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar1);
        View view = navigationView.inflateHeaderView(R.layout.nav_header_main);
        tvNaviTitle = (TextView) view.findViewById(R.id.navi_title);
        tvNaviSubTitle = (TextView) view.findViewById(R.id.navi_subtitle);
        identicon = (Identicon) view.findViewById(R.id.identicon);
        identicon.show(System.currentTimeMillis());

        File file = new File(Environment.getExternalStorageDirectory() + "/tessdata", "eng.traineddata");
        if (!file.exists()) {
            push_tranedeng_tosdcard();
        }

        navigationView.setNavigationItemSelectedListener(this);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar1, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        db = openOrCreateDatabase("collection.db", Context.MODE_PRIVATE, null);
        db.execSQL("create table if not exists search_history (_id INTEGER PRIMARY KEY AUTOINCREMENT, name text not null unique)");
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                ContentValues cv = new ContentValues();
                cv.put("name", query);
                db.insertWithOnConflict("search_history", null, cv, SQLiteDatabase.CONFLICT_REPLACE);
                direct_search(base_url + query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        searchView.setOnSearchMenuListener(new SearchView.SearchMenuListener() {
            @Override
            public void onMenuClick() {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });
        OkHttpUtils.get()
                .url("https://read.douban.com/topic/326/")
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e) {
                        String [] data={
                            "docker",
                            "机器学习",
                            "haskell",
                            "scala",
                            "kotlin",
                            "lisp",
                            "node.js",
                            "hadoop",
                            "bootstrap",
                            "angularjs",
                            "go",
                        };
                        TextTagsAdapter textTagsAdapter = new TextTagsAdapter(data);
                        tagCloud.setAdapter(textTagsAdapter);
                    }

                    @Override
                    public void onResponse(String response) {
                        Document document = Jsoup.parse(response);
                        Elements elements = document.getElementsByClass("title");
                        String[] data = new String[elements.size()];
                        for (int i = 0; i < elements.size(); i++) {
                            String temp=elements.get(i).text();
                            temp=temp.replaceAll("（.*）","");
                            data[i]=temp;
                        }
                        TextTagsAdapter textTagsAdapter = new TextTagsAdapter(data);
                        tagCloud.setAdapter(textTagsAdapter);
                    }
                });
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            DB snappydb = DBFactory.open(getApplication(), "notvital");
            String jaccountName = snappydb.get("name");
            if (jaccountName.length() != 0) {
                tvNaviSubTitle.setText(jaccountName);
                identicon.show(jaccountName);
            }
            String realname = snappydb.get("realname");
            if (realname.length() != 0) {
                tvNaviTitle.setText(realname);
            }
        } catch (SnappydbException e) {
            e.printStackTrace();
        }
    }

    private void push_tranedeng_tosdcard() {
        File folder = new File(Environment.getExternalStorageDirectory() + "/tessdata");
        if (!folder.exists()) {
            folder.mkdir();
        }
        AssetManager assetManager = getAssets();
        String[] files = null;
        try {
            files = assetManager.list("");
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (files != null) {
            for (String filename : files) {
                InputStream in = null;
                OutputStream out = null;
                try {
                    in = assetManager.open(filename);
                    String path = Environment.getExternalStorageDirectory() + "/tessdata";
                    File outfile = new File(path, filename);
                    out = new FileOutputStream(outfile);
                    copyFile(in, out);
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if (in != null) {
                        try {
                            in.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    if (out != null) {
                        try {
                            out.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }

    }

    private void copyFile(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while ((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
    }

    public void direct_search(String url) {
        Intent intent = new Intent();
        intent.setClass(SearchActivity.this, MainActivity.class);
        intent.putExtra("url", url);
        startActivity(intent);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.myborrow) {
            Intent intent = new Intent();
            intent.setClass(SearchActivity.this, MyBorrowActivity.class);
            startActivity(intent);
        } else if (id == R.id.mycollection) {
            if (isFavouriteExist()) {
                Intent intent = new Intent();
                intent.setClass(SearchActivity.this, MyCollectionActivity.class);
                startActivity(intent);
            } else {
                Toast.makeText(getApplicationContext(), "你尚未收藏任何一本书", Toast.LENGTH_SHORT).show();
            }
        } else if (id == R.id.borrowrank) {
            Intent intent = new Intent();
            intent.setClass(SearchActivity.this, RankActivity.class);
            startActivity(intent);
        } else if (id == R.id.history) {
            Intent intent = new Intent();

            intent.setClass(SearchActivity.this, MyHistoryActivity.class);
            startActivity(intent);
        } else if (id == R.id.log_in) {
            Intent intent = new Intent();
            intent.setClass(SearchActivity.this, LoginActivity.class);
            startActivity(intent);
        } else if (id == R.id.log_out) {
            try {
                DB snappydb = DBFactory.open(getApplication(), "notvital");
                snappydb.del("name");
                snappydb.del("pass");
                snappydb.close();
                Toast.makeText(getApplicationContext(), "退出成功", Toast.LENGTH_SHORT).show();
            } catch (SnappydbException e) {
                e.printStackTrace();
            }
        }
        return true;
    }

    private boolean isFavouriteExist() {
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM sqlite_master WHERE type = ? AND name = ?", new String[]{"table", "favourite"});
        if (!cursor.moveToFirst()) {
            return false;
        }
        int count = cursor.getInt(0);
        cursor.close();
        return count > 0;
    }

    @Override
    protected void onStart() {
        List<SearchItem> list = new ArrayList<>();
        Cursor cursor = db.rawQuery("select * from search_history", null);
        if (cursor.moveToFirst()) {
            while (cursor.isAfterLast() == false) {
                String name = cursor.getString(cursor.getColumnIndex("name"));
                list.add(new SearchItem(name));
                cursor.moveToNext();
            }
        }
        mSuggestionList = new ArrayList<>();
        mSuggestionList.addAll(list);
        List<SearchItem> mReasultList = new ArrayList<>();
        SearchAdapter mSearchAdapter = new SearchAdapter(this, mReasultList, mSuggestionList, mTheme);
        mSearchAdapter.setOnItemClickListener(new SearchAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                TextView textview = (TextView) view.findViewById(R.id.textView_item_text);
                String name = textview.getText().toString();
                direct_search(base_url + name);
            }
        });
        super.onStart();
        searchView.setAdapter(mSearchAdapter);
    }

}
