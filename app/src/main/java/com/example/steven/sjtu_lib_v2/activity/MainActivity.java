package com.example.steven.sjtu_lib_v2.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.TypedValue;
import android.view.LayoutInflater;
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
import butterknife.OnItemClick;
import okhttp3.Call;

public class MainActivity extends AppCompatActivity {
    @Bind(R.id.swipe_refresh)SuperSwipeRefreshLayout superSwipeRefreshLayout;
    @Bind(R.id.listView)SwipeMenuListView plistiview;

//    footerview
    ProgressBar footerProgressBar;
    ImageView footerImageView;
    TextView footerTextView;
    String url;

    String NextUrls;
    public List<Element> book_elements=new ArrayList<Element>();
    BookItemAdapter bookItemAdapter;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.result);
        ButterKnife.bind(this);

        get_intent_extra();
        plistview_init();
        superSwipelayout_init();

        get_list_from_url(url);
    }

    private void superSwipelayout_init() {
        superSwipeRefreshLayout.setTargetScrollWithLayout(false);
        superSwipeRefreshLayout.setHeaderView(null);
        superSwipeRefreshLayout.setFooterView(createFootview());
        superSwipeRefreshLayout.setOnPushLoadMoreListener(new SuperSwipeRefreshLayout.OnPushLoadMoreListener() {
            @Override
            public void onLoadMore() {
                footerTextView.setText("正在加载...");
                footerImageView.setVisibility(View.GONE);
                footerProgressBar.setVisibility(View.VISIBLE);
                new NextAsyncTask(MainActivity.this).execute();
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
        SwipeMenuCreator creator=new SwipeMenuCreator() {
            @Override
            public void create(SwipeMenu menu) {
                SwipeMenuItem more_info=new SwipeMenuItem(getApplicationContext());
                more_info.setBackground(new ColorDrawable(Color.rgb(0xC9, 0xC9, 0xCE)));
                more_info.setWidth(dp2px(90));
                more_info.setTitle("详细信息");
                more_info.setTitleSize(18);
                more_info.setTitleColor(Color.WHITE);
                menu.addMenuItem(more_info);
            }
        };
        plistiview.setMenuCreator(creator);
        bookItemAdapter=new BookItemAdapter(this, 0, book_elements);
        plistiview.setAdapter(bookItemAdapter);
        plistiview.setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {
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
        Element doc=book_elements.get(position);
//       多版本的处理
        Elements MultipleLink=doc.getElementsByClass("EXLBriefResultsDisplayMultipleLink");
        Intent intent=new Intent();

        if(MultipleLink.isEmpty()){
            Element tosend=doc.getElementsByClass("EXLSummaryContainer").first();
            tosend.getElementsByTag("script").remove();
            tosend.getElementsByTag("noscript").remove();
            tosend.getElementsByClass("EXLResultAvailability").remove();

            String url= BookDetailDialog.base_url+doc.getElementsMatchingText("馆藏信息").attr("href");
            intent.setClass(MainActivity.this, SingleDetailActivity.class);
            intent.putExtra("detail", tosend.toString());
            intent.putExtra("url",url);
            startActivity(intent);
        }else {
            intent.setClass(MainActivity.this,MainActivity.class);
            intent.putExtra("url",MultipleLink.attr("href"));
            startActivity(intent);
        }
    }

    private void get_intent_extra() {
        String url_intent=getIntent().getExtras().getString("url");
        this.url=url_intent;
    }

    private View createFootview() {
        View footerView= LayoutInflater.from(superSwipeRefreshLayout.getContext())
            .inflate(R.layout.layout_footer,null);
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
        OkHttpUtils .get()
                .url(url)
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e) {
                        Toast.makeText(getApplicationContext(), "fail to connect", Toast.LENGTH_SHORT).show();
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
                        MainActivity.this.setTitle("已加载了" + book_elements.size() + "本书");
                    }
                });
    }

    @OnItemClick(R.id.listView) void onItemSelected(int position){
        BookDetailDialog bookDetail=new BookDetailDialog(book_elements.get(position));
        bookDetail.show(getFragmentManager(), "book");
    }

    public class NextAsyncTask extends MultiAsynctask<Void,Void,Void> {
        MainActivity activity;
        int saved_postion;
        LoadingDialog dialog;
        Context context;

        public NextAsyncTask(MainActivity mainActivity) {
            this.activity=mainActivity;
            this.context=activity;
            dialog=new LoadingDialog(mainActivity);
        }

        @Override
        public void onResult(Void Void) {
            bookItemAdapter.notifyDataSetChanged();
            dialog.dismiss();
            superSwipeRefreshLayout.setLoadMore(false);
        }

        @Override
        public Void onTask(Void... params) {
            synchronized (this){
                while (NextUrls.length() == 0 || book_elements.size() == 0) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
            get_list_from_url(NextUrls);

            return null;
        }

        @Override
        public void onPrepare() {
            saved_postion=plistiview.getFirstVisiblePosition();
            dialog.show();
        }
    }

    private class Refrsh_next_url extends MultiAsynctask<Object,Void,Elements>{

        @Override
        public Elements onTask(Object... objects) {
            Element come_in= (Element) objects[0];
            Elements elements=come_in .getElementsByAttributeValue("title", "下一页");
            return elements;
        }

        @Override
        public void  onResult(Elements elements) {
            bookItemAdapter.notifyDataSetChanged();
            if(elements.size()!=0){
                NextUrls=elements.first().attr("href");
            }
        }

    }

    private int dp2px(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
                getResources().getDisplayMetrics());
    }
}
