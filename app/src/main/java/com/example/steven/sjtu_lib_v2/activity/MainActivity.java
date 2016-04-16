package com.example.steven.sjtu_lib_v2.activity;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.baoyz.swipemenulistview.SwipeMenu;
import com.baoyz.swipemenulistview.SwipeMenuCreator;
import com.baoyz.swipemenulistview.SwipeMenuItem;
import com.baoyz.swipemenulistview.SwipeMenuListView;
import com.example.steven.sjtu_lib_v2.R;
import com.example.steven.sjtu_lib_v2.adapter.BookItemAdapter;
import com.example.steven.sjtu_lib_v2.dialog.BookDetailDialog;
import com.example.steven.sjtu_lib_v2.dialog.LoadingDialog;
import com.example.steven.sjtu_lib_v2.view.SuperSwipeRefreshLayout;
import com.lapism.searchview.adapter.SearchAdapter;
import com.lapism.searchview.adapter.SearchItem;
import com.lapism.searchview.view.SearchCodes;
import com.lapism.searchview.view.SearchView;
import com.yolanda.multiasynctask.MultiAsynctask;
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
import butterknife.OnClick;
import butterknife.OnItemClick;
import okhttp3.Call;
public class MainActivity extends AppCompatActivity {
    @Bind(R.id.swipe_refresh)
    SuperSwipeRefreshLayout superSwipeRefreshLayout;
    @Bind(R.id.listView)
    SwipeMenuListView plistview;
    @Bind(R.id.toolbar)
    Toolbar toolbar;
    @Bind(R.id.searchView)
    SearchView searchView;

    //    footerview
    ProgressBar footerProgressBar;
    ImageView footerImageView;
    TextView footerTextView;
<<<<<<< Updated upstream

=======
    String url;
    String NextUrls;
>>>>>>> Stashed changes
    public List<Element> book_elements = new ArrayList<Element>();
    private List<SearchItem> mSuggestionList;
    BookItemAdapter bookItemAdapter;
    SQLiteDatabase db;
    LoadingDialog dialog;

    String url;
    String NextUrls;
    String base_url = "http://ourex.lib.sjtu.edu.cn/primo_library/libweb/action/search." +
            "do?fn=search&tab=default_tab&vid=chinese&scp.scps=scope%3A%28SJT%29%2Csc" +
            "ope%3A%28sjtu_metadata%29%2Cscope%3A%28sjtu_sfx%29%2Cscope%3A%28sjtulib" +
            "zw%29%2Cscope%3A%28sjtulibxw%29%2CDuxiuBook&vl%28freeText0%29=";

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.result_drawer);
        ButterKnife.bind(this);
<<<<<<< Updated upstream

        toolbar.inflateMenu(R.menu.menu_result);
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                searchView.setVisibility(View.VISIBLE);
                return false;
            }
        });
        dialog=new LoadingDialog(MainActivity.this);
        dialog.show();
=======
>>>>>>> Stashed changes
        get_intent_extra();
        plistview_init();
        superSwipelayout_init();
        get_list_from_url(url);
<<<<<<< Updated upstream
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
    }

    @Override
    protected void onStart() {
        db = openOrCreateDatabase("collection.db", Context.MODE_PRIVATE, null);
        List<SearchItem> list = new ArrayList<>();
        int mTheme= SearchCodes.THEME_LIGHT;

        Cursor cursor = db.rawQuery("select * from search_history", null);
        if (cursor.moveToFirst()) {
            while (cursor.isAfterLast() == false) {
                String name = cursor.getString(cursor.getColumnIndex("name"));
                list.add(new SearchItem(name));
                cursor.moveToNext();
            }
        }
        mSuggestionList=new ArrayList<>();
        mSuggestionList.addAll(list);
        List<SearchItem> mReasultList=new ArrayList<>();
        SearchAdapter mSearchAdapter=new SearchAdapter(this,mReasultList,mSuggestionList,mTheme);
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
=======
//        final Intent intent = new Intent(MainActivity.this, updateService.class);
//        startService(intent);
//        Toast.makeText(MainActivity.this, "Service启动成功", Toast.LENGTH_SHORT).show();
>>>>>>> Stashed changes
    }

    private void superSwipelayout_init() {
        superSwipeRefreshLayout.setTargetScrollWithLayout(false);
        superSwipeRefreshLayout.setHeaderView(null);
        superSwipeRefreshLayout.setFooterView(createFootview());
        superSwipeRefreshLayout.setOnPushLoadMoreListener(new SuperSwipeRefreshLayout.OnPushLoadMoreListener() {
            @Override
            public void onLoadMore() {
                if (NextUrls.equals("no more")) {
                    Toast.makeText(getApplicationContext(), "已经是最后一页了", Toast.LENGTH_SHORT).show();
                    superSwipeRefreshLayout.setLoadMore(false);
                } else {
                    new NextAsyncTask(MainActivity.this).execute();
                    footerTextView.setText("正在加载...");
                    footerImageView.setVisibility(View.GONE);
                    footerProgressBar.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onPushDistance(int distance) {

            }

            @Override
            public void onPushEnable(boolean enable) {
                footerTextView.setText(enable ? "松开加载" : "上拉加载");
                footerImageView.setVisibility(View.VISIBLE);
            }
        });
        superSwipeRefreshLayout.setOnPullRefreshListener(new SuperSwipeRefreshLayout.OnPullRefreshListener() {
            @Override
            public void onRefresh() {
            }

            @Override
            public void onPullDistance(int distance) {
                superSwipeRefreshLayout.setRefreshing(false);
            }

            @Override
            public void onPullEnable(boolean enable) {

            }
        });
    }

    private void plistview_init() {
        SwipeMenuCreator creator = new SwipeMenuCreator() {
            @Override
            public void create(SwipeMenu menu) {
                SwipeMenuItem more_info = new SwipeMenuItem(getApplicationContext());
                more_info.setBackground(new ColorDrawable(Color.rgb(0xC9, 0xC9, 0xCE)));
                more_info.setWidth(dp2px(90));
                more_info.setTitle("详细信息");
                more_info.setTitleSize(18);
                more_info.setTitleColor(Color.WHITE);
                menu.addMenuItem(more_info);
            }
        };
        plistview.setMenuCreator(creator);
        bookItemAdapter = new BookItemAdapter(this, 0, book_elements);
        plistview.setAdapter(bookItemAdapter);
        plistview.setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(int position, SwipeMenu menu, int index) {
                switch (index) {
                    case 0:
                        show_detail_info(position);
                }
                return false;
            }
        });
    }

    private void show_detail_info(int position) {
        Element doc = book_elements.get(position);
//       多版本的处理
        Elements MultipleLink = doc.getElementsByClass("EXLBriefResultsDisplayMultipleLink");
        Intent intent = new Intent();

        if (MultipleLink.isEmpty()) {
            Element tosend = doc.getElementsByClass("EXLSummaryContainer").first();
            tosend.getElementsByTag("script").remove();
            tosend.getElementsByTag("noscript").remove();
            tosend.getElementsByClass("EXLResultAvailability").remove();

            String url = BookDetailDialog.base_url + doc.getElementsMatchingText("馆藏信息").attr("href");
            intent.setClass(MainActivity.this, SingleDetailActivity.class);
            intent.putExtra("detail", tosend.toString());
            intent.putExtra("url", url);
            startActivity(intent);
        } else {
            intent.setClass(MainActivity.this, MainActivity.class);
            intent.putExtra("url", MultipleLink.attr("href"));
            startActivity(intent);
        }
    }

    private void get_intent_extra() {
        String url_intent = getIntent().getExtras().getString("url");
        this.url = url_intent;
    }

    private View createFootview() {
        View footerView = LayoutInflater.from(superSwipeRefreshLayout.getContext())
                .inflate(R.layout.layout_footer, null);
        footerProgressBar = (ProgressBar) footerView
                .findViewById(R.id.footer_pb_view);
        footerImageView = (ImageView) footerView
                .findViewById(R.id.footer_image_view);
        footerTextView = (TextView) footerView
                .findViewById(R.id.footer_text_view);
        footerProgressBar.setVisibility(View.GONE);
        footerImageView.setVisibility(View.VISIBLE);
        footerImageView.setImageResource(R.drawable.down_arrow);
        footerTextView.setText("上拉加载更多...");
        return footerView;
    }

    private void get_list_from_url(String url) {
        OkHttpUtils.get()
                .url(url)
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e) {
                        Toast.makeText(getApplicationContext(), "fail to connect", Toast.LENGTH_SHORT).show();
                        superSwipeRefreshLayout.setLoadMore(false);
                        if (dialog != null) {
                            dialog.dismiss();
                        }
                    }

                    @Override
                    public void onResponse(String response) {
                        Document doc = Jsoup.parse(response);

                        new Refrsh_next_url().execute(doc);
                        Elements elements = doc.getElementsByClass("EXLSummary");
                        for (Element i : elements) {
                            if (!i.getElementsMatchingText("馆藏信息").isEmpty()) {
                                book_elements.add(i);
                            }
                        }
                        toolbar.setTitle("已加载了" + book_elements.size() + "本书");
                        superSwipeRefreshLayout.setLoadMore(false);
                        if (dialog != null) {
                            dialog.dismiss();
                        }
                    }
                });
    }

    @OnItemClick(R.id.listView)
    void onItemSelected(int position) {
        if (searchView.getVisibility() == View.VISIBLE) {
            searchView.setVisibility(View.INVISIBLE);
        } else {
            BookDetailDialog bookDetail = new BookDetailDialog(book_elements.get(position));
            bookDetail.show(getFragmentManager(), "book");
        }
    }

    @OnClick(R.id.toolbar)
    void scroolToTop() {
        plistview.setSelectionAfterHeaderView();
    }

    public class NextAsyncTask extends MultiAsynctask<Void, Void, Integer> {
        MainActivity activity;
        int saved_postion;

        public NextAsyncTask(MainActivity mainActivity) {
            this.activity = mainActivity;
            dialog = new LoadingDialog(mainActivity);
        }

        @Override
        public void onResult(Integer integer) {
            bookItemAdapter.notifyDataSetChanged();
        }

        @Override
        public Integer onTask(Void... params) {
            while (NextUrls.length() == 0 || book_elements.size() == 0) {
                try {
                    Thread.sleep(10);
                    System.out.println(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            get_list_from_url(NextUrls);

            return 1;
        }

        @Override
        public void onPrepare() {
            saved_postion = plistview.getFirstVisiblePosition();
            dialog.show();
        }
    }

    private class Refrsh_next_url extends MultiAsynctask<Object, Void, Elements> {

        @Override
        public Elements onTask(Object... objects) {
            Element come_in = (Element) objects[0];
            Elements elements = come_in.getElementsByAttributeValue("title", "下一页");
            return elements;
        }

        @Override
        public void onResult(Elements elements) {
            bookItemAdapter.notifyDataSetChanged();
            if (elements.size() != 0) {
                NextUrls = elements.first().attr("href");
            } else { //没有下一页了
                NextUrls="no more";
            }
        }

    }

    private int dp2px(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
                getResources().getDisplayMetrics());
    }

    private void direct_search(String url) {
        Intent intent = new Intent();
        intent.setClass(MainActivity.this, MainActivity.class);
        intent.putExtra("url", url);
        startActivity(intent);
    }

}
