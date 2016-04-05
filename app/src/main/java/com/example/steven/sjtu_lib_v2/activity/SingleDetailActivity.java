package com.example.steven.sjtu_lib_v2.activity;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.steven.sjtu_lib_v2.R;
import com.example.steven.sjtu_lib_v2.adapter.TableAdapter;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.BitmapCallback;
import com.zhy.http.okhttp.callback.StringCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.Call;

/**
 * Created by steven on 2016/2/11.
 */
public class SingleDetailActivity extends AppCompatActivity {
    @Bind(R.id.listview_table)
    ListView lv_table;
    //    @Bind(R.id.toolbar)
//    Toolbar toolbar;
    @Bind(R.id.tv_book_author)
    TextView tvBookAuthor;
    @Bind(R.id.tv_book_time)
    TextView tvBookTime;
    @Bind(R.id.tv_book_page)
    TextView tvBookPage;
    @Bind(R.id.tv_book_publicer)
    TextView tvBookPublicer;
    @Bind(R.id.tv_book_isbn)
    TextView tvBookIsbn;
    @Bind(R.id.tv_book_price)
    TextView tvBookPrice;
    @Bind(R.id.tv_book_score)
    TextView tvBookScore;
    @Bind(R.id.tv_book_tag)
    TextView tvBookTag;
    @Bind(R.id.iv_book_icon)
    ImageView ivBookIcon;
    @Bind(R.id.toolbar)
    Toolbar toolbar;

    TableAdapter adapter;
    List<Element> table_data = new ArrayList<Element>();
    String bookInfo;
    String authorInfo;
    String url = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.single_drawer);
        ButterKnife.bind(this);

        adapter = new TableAdapter(getApplicationContext(), 0, table_data);
        lv_table.setAdapter(adapter);

        final String detail_html = get_html_from_intent();
        url= get_url_from_intent();
        get_table_data(url);

        toolbar.inflateMenu(R.menu.menu_main);
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                SQLiteDatabase db = openOrCreateDatabase("collection.db", Context.MODE_PRIVATE, null);
                db.execSQL("create table if not exists favourite (_id INTEGER PRIMARY KEY AUTOINCREMENT, book_name VARCHAR, url VARCHAR unique)");
                switch (item.getItemId()) {
                    case R.id.add_to_collection:
                        ContentValues cv = new ContentValues();
                        cv.put("book_name", detail_html);
                        cv.put("url", url);
                        db.insert("favourite", null, cv);

                        Toast.makeText(getApplicationContext(), "收藏成功", Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.remove_from_collection:
                        if (db.delete("favourite", "url=?", new String[]{url}) > 0) {
                            Toast.makeText(getApplicationContext(), "取消收藏成功", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getApplicationContext(), "你尚未收藏此书", Toast.LENGTH_SHORT).show();
                        }
                        break;
                    case R.id.share:
                        Intent intent=new Intent();
                        intent.setAction(Intent.ACTION_SEND);
                        intent.putExtra(Intent.EXTRA_TEXT,url);
                        intent.setType("text/plain");
                        startActivity(intent);
                        break;
                }
                return true;
            }
        });

    }

    private String get_html_from_intent() {
        String detail_html = getIntent().getExtras().getString("detail");
        Document docuement = Jsoup.parse(detail_html);
        tvBookAuthor.setText(docuement.getElementsByClass("EXLResultAuthor").text());
        tvBookPublicer.setText(docuement.getElementsByClass("EXLResultFourthLine").text());
        return detail_html;
    }

    public String get_url_from_intent() {
        String url = getIntent().getExtras().getString("url");
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
                        Pattern pattern = Pattern.compile("(?<=rft\\.isbn=)\\d+");
                        Matcher matcher = pattern.matcher(response);
                        if (matcher.find()) {
                            tvBookIsbn.setText(matcher.group(0));
                            getDoubanInfo(matcher.group(0));
                        } else {
                            System.out.print("not found");
                        }

                        Document doc = Jsoup.parse(response);
                        Elements EXLLocationTableColumn1_eles = doc.getElementsByClass("EXLLocationTableColumn1");
                        for (Element i : EXLLocationTableColumn1_eles) {
                            table_data.add(i.parent());
                        }
                        adapter.notifyDataSetChanged();
                    }

                    private void getDoubanInfo(String isbn) {
                        OkHttpUtils.get()
                                .url("https://api.douban.com/v2/book/isbn/" + isbn)
                                .build()
                                .execute(new StringCallback() {
                                    @Override
                                    public void onError(Call call, Exception e) {
                                        Toast.makeText(getApplicationContext(), "加载豆瓣数据失败", Toast.LENGTH_SHORT).show();
                                    }

                                    @Override
                                    public void onResponse(String response) {
                                        try {
                                            JSONObject jsonobect = new JSONObject(response);
                                            tvBookPage.setText(jsonobect.getString("pages"));
                                            tvBookPrice.setText(jsonobect.getString("price"));
                                            tvBookAuthor.setText(jsonobect.getString("author"));
                                            tvBookPublicer.setText(jsonobect.getString("publisher"));
                                            tvBookTime.setText(jsonobect.getString("pubdate"));
                                            tvBookScore.setText(jsonobect.getJSONObject("rating").getString("average"));
                                            tvBookPage.setText(jsonobect.getString("pages"));
                                            bookInfo = jsonobect.getString("summary");
                                            authorInfo = jsonobect.getString("author_intro");

                                            String imageUrl = jsonobect.getString("image");
                                            OkHttpUtils.get()
                                                    .url(imageUrl)
                                                    .build()
                                                    .execute(new BitmapCallback() {
                                                        @Override
                                                        public void onError(Call call, Exception e) {
                                                            Toast.makeText(getApplicationContext(), "加载图片失败", Toast.LENGTH_SHORT).show();
                                                        }

                                                        @Override
                                                        public void onResponse(Bitmap response) {
                                                            ivBookIcon.setImageBitmap(response);
                                                        }
                                                    });
                                            JSONArray jsonarray = jsonobect.getJSONArray("tags");
                                            String tagContent = "";
                                            for (int i = 0; i < jsonarray.length(); i++) {
                                                JSONObject object = jsonarray.getJSONObject(i);
                                                tagContent += object.getString("title");
                                                tagContent += ",";
                                            }
                                            tvBookTag.setText(tagContent);
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                });
                    }
                });
    }

    @OnClick(R.id.iv_book_icon)
    public void showBookInfo() {
        if (bookInfo == null) {
            new AlertDialog.Builder(SingleDetailActivity.this)
                    .setTitle("图书信息")
                    .setMessage("请稍等。。")
                    .setPositiveButton("确认", null)
                    .create()
                    .show();
        } else {
            new AlertDialog.Builder(SingleDetailActivity.this)
                    .setTitle("图书信息")
                    .setMessage("请稍等。。")
                    .setMessage(bookInfo)
                    .setPositiveButton("确认", null)
                    .create()
                    .show();
        }
    }

    @OnClick(R.id.tv_book_author)
    public void showAuthorInfo() {
        if (authorInfo == null) {
            new AlertDialog.Builder(SingleDetailActivity.this)
                    .setTitle("作者信息")
                    .setMessage("请稍等。。。")
                    .setPositiveButton("确认", null)
                    .create()
                    .show();
        } else {
            new AlertDialog.Builder(SingleDetailActivity.this)
                    .setMessage(authorInfo)
                    .setTitle("作者信息")
                    .setPositiveButton("确认", null)
                    .create()
                    .show();
        }
    }
}
