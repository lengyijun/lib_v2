package com.example.steven.sjtu_lib_v2.activity;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
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
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import me.next.tagview.TagCloudView;
import okhttp3.Call;

/**
 * Created by steven on 2016/2/11.
 */
public class SingleDetailActivity extends AppCompatActivity {
    @Bind(R.id.listview_table)
    ListView lv_table;
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
    @Bind(R.id.iv_book_icon)
    ImageView ivBookIcon;
    @Bind(R.id.toolbar)
    Toolbar toolbar;
    @Bind(R.id.tag_cloud_view)
    TagCloudView tagCloudView;
    @Bind(R.id.bookinfo)
    TextView tvBookInfo;

    TextView titleTextView = null;

    TableAdapter adapter;
    List<Element> table_data = new ArrayList<Element>();
    String bookInfo;
    String authorInfo;
    String url = null;
    public static String base_url = "http://ourex.lib.sjtu.edu.cn/primo_library/libweb/action/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.single_drawer);
        ButterKnife.bind(this);

        adapter = new TableAdapter(getApplicationContext(), 0, table_data);
        lv_table.setAdapter(adapter);

        final String detail_html = get_html_from_intent();
        url = get_url_from_intent();
        get_table_data(url);

        try {
            Field f = toolbar.getClass().getDeclaredField("mTitleTextView");
            f.setAccessible(true);
            titleTextView = (TextView) f.get(toolbar);

            titleTextView.setEllipsize(TextUtils.TruncateAt.MARQUEE);
            titleTextView.setFocusable(true);
            titleTextView.setFocusableInTouchMode(true);
            titleTextView.requestFocus();
            titleTextView.setSingleLine(true);
            titleTextView.setSelected(true);
            titleTextView.setMarqueeRepeatLimit(-1);
            titleTextView.setTextColor(Color.WHITE);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
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
                        Intent intent = new Intent();
                        intent.setAction(Intent.ACTION_SEND);
                        intent.putExtra(Intent.EXTRA_TEXT, url);
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
        toolbar.setTitle(docuement.getElementsByClass("EXLResultTitle").text());
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
                            Toast.makeText(getApplicationContext(), "未能找到isbn，无法加载豆瓣数据", Toast.LENGTH_SHORT).show();
                        }

                        Document doc = Jsoup.parse(response);
                        Elements EXLLocationTableColumn1_eles = doc.getElementsByClass("EXLLocationTableColumn1");
                        if (!EXLLocationTableColumn1_eles.isEmpty()) {
                            for (Element i : EXLLocationTableColumn1_eles) {
                                table_data.add(i.parent());
                            }
                            adapter.notifyDataSetChanged();
                        } else {
                            List<String> link_list = new ArrayList<String>();
                            Elements link_elm = doc.getElementsByClass("EXLLocationsIcon");
                            for (Element i : link_elm) {
                                String temp_link = i.attr("href");
                                temp_link = base_url + temp_link;
                                link_list.add(temp_link);
                            }
                            get_location_from_linklist(link_list);

                        }
                    }

                    private void get_location_from_linklist(List<String> link_list) {
                        for (String link : link_list) {
                            get_location_from_link(link);
                        }
                    }

                    private void get_location_from_link(String link) {
                        OkHttpUtils.get()
                                .url(link)
                                .build()
                                .execute(new StringCallback() {
                                    @Override
                                    public void onError(Call call, Exception e) {

                                    }

                                    @Override
                                    public void onResponse(String response) {
                                        Document doc = Jsoup.parse(response, "", Parser.xmlParser());
                                        String first_modification = doc.getElementsByTag("modification").first().text();
                                        Document modi_html = Jsoup.parse(first_modification, "", Parser.htmlParser());
                                        Elements fin_eles = modi_html.getElementsByClass("EXLLocationTableColumn3");

                                        for (Element i : fin_eles) {
                                            table_data.add(i.parent());
                                        }
                                        adapter.notifyDataSetChanged();
                                    }
                                });
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
                                            tvBookInfo.setText(jsonobect.getString("summary"));
                                            authorInfo = jsonobect.getString("author_intro");
                                            final String doubanlink=jsonobect.getString("alt");
                                            makeTextViewResizable(tvBookInfo, 3, "View More", true);

                                            ivBookIcon.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(doubanlink));
                                                    startActivity(browserIntent);
                                                }
                                            });

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
                                            List<String > tag=new ArrayList<String>();
                                            for (int i = 0; i < jsonarray.length(); i++) {
                                                JSONObject object = jsonarray.getJSONObject(i);
                                                tag.add(object.getString("title"));
                                            }
                                            tagCloudView.setTags(tag);
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                });
                    }
                });
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

    public static void makeTextViewResizable(final TextView tv, final int maxLine, final String expandText, final boolean viewMore) {

        if (tv.getTag() == null) {
            tv.setTag(tv.getText());
        }
        ViewTreeObserver vto = tv.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {

            @SuppressWarnings("deprecation")
            @Override
            public void onGlobalLayout() {

                ViewTreeObserver obs = tv.getViewTreeObserver();
                obs.removeGlobalOnLayoutListener(this);
                if (maxLine == 0) {
                    int lineEndIndex = tv.getLayout().getLineEnd(0);
                    String text = tv.getText().subSequence(0, lineEndIndex - expandText.length() + 1) + " " + expandText;
                    tv.setText(text);
                    tv.setMovementMethod(LinkMovementMethod.getInstance());
                    tv.setText(
                            addClickablePartTextViewResizable(Html.fromHtml(tv.getText().toString()), tv, maxLine, expandText,
                                    viewMore), TextView.BufferType.SPANNABLE);
                } else if (maxLine > 0 && tv.getLineCount() >= maxLine) {
                    int lineEndIndex = tv.getLayout().getLineEnd(maxLine - 1);
                    String text = tv.getText().subSequence(0, lineEndIndex - expandText.length() + 1) + " " + expandText;
                    tv.setText(text);
                    tv.setMovementMethod(LinkMovementMethod.getInstance());
                    tv.setText(
                            addClickablePartTextViewResizable(Html.fromHtml(tv.getText().toString()), tv, maxLine, expandText,
                                    viewMore), TextView.BufferType.SPANNABLE);
                } else {
                    int lineEndIndex = tv.getLayout().getLineEnd(tv.getLayout().getLineCount() - 1);
                    String text = tv.getText().subSequence(0, lineEndIndex) + " " + expandText;
                    tv.setText(text);
                    tv.setMovementMethod(LinkMovementMethod.getInstance());
                    tv.setText(
                            addClickablePartTextViewResizable(Html.fromHtml(tv.getText().toString()), tv, lineEndIndex, expandText,
                                    viewMore), TextView.BufferType.SPANNABLE);
                }
            }
        });

    }

    private static SpannableStringBuilder addClickablePartTextViewResizable(final Spanned strSpanned, final TextView tv,
                                                                            final int maxLine, final String spanableText, final boolean viewMore) {
        String str = strSpanned.toString();
        SpannableStringBuilder ssb = new SpannableStringBuilder(strSpanned);

        if (str.contains(spanableText)) {
            ssb.setSpan(new ClickableSpan() {

                @Override
                public void onClick(View widget) {

                    if (viewMore) {
                        tv.setLayoutParams(tv.getLayoutParams());
                        tv.setText(tv.getTag().toString(), TextView.BufferType.SPANNABLE);
                        tv.invalidate();
                        makeTextViewResizable(tv, -1, "View Less", false);
                    } else {
                        tv.setLayoutParams(tv.getLayoutParams());
                        tv.setText(tv.getTag().toString(), TextView.BufferType.SPANNABLE);
                        tv.invalidate();
                        makeTextViewResizable(tv, 3, "View More", true);
                    }

                }
            }, str.indexOf(spanableText), str.indexOf(spanableText) + spanableText.length(), 0);

        }
        return ssb;

    }

}
