package com.example.steven.sjtu_lib_v2.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.steven.sjtu_lib_v2.R;
import com.example.steven.sjtu_lib_v2.activity.MainActivity;
import com.example.steven.sjtu_lib_v2.adapter.MostBorrowedBookAdapter;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.Call;

/**
 * Created by steven on 2016/3/23.
 */
public class BookRank extends Fragment{
    ListView listView;
    ArrayList<Element> data;
    MostBorrowedBookAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment,container,false);
        listView= (ListView) view.findViewById(R.id.listView3);
        adapter=new MostBorrowedBookAdapter(getActivity(),0,data);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                TextView tv_bookname= (TextView) view.findViewById(R.id.book_name);
                String bookName=tv_bookname.getText().toString();
                Intent intent=new Intent();
                intent.setClass(getActivity(), MainActivity.class);
                String base_url="http://ourex.lib.sjtu.edu.cn/primo_library/libweb/action/search." +
                        "do?fn=search&tab=default_tab&vid=chinese&scp.scps=scope%3A%28SJT%29%2Csc" +
                        "ope%3A%28sjtu_metadata%29%2Cscope%3A%28sjtu_sfx%29%2Cscope%3A%28sjtulib" +
                        "zw%29%2Cscope%3A%28sjtulibxw%29%2CDuxiuBook&vl%28freeText0%29=";
                intent.putExtra("url",base_url+bookName);
                startActivity(intent);
            }
        });
        return view;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        data=new ArrayList<Element>();
        OkHttpUtils.get()
                .url("http://opac.lib.sjtu.edu.cn/F")
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e) {

                    }

                    @Override
                    public void onResponse(String response) {
                        Pattern pattern= Pattern.compile("http:.*phb\\.html");
                        Matcher matcher=pattern.matcher(response);
                        if (matcher.find()) {
                            String url=matcher.group(0);
                            System.out.println(url);
                            OkHttpUtils.get()
                                    .url(url)
                                    .build()
                                    .execute(new StringCallback() {
                                        @Override
                                        public void onError(Call call, Exception e) {

                                        }

                                        @Override
                                        public void onResponse(String response) {
                                            Document document= Jsoup.parse(response);
                                            Elements elements=document.getElementsByClass("text1");
                                            System.out.println(elements.size());
                                            for (Element i : elements) {
                                                if(i.tagName().equals("tr")){
                                                    data.add(i);
                                                    System.out.print(i.toString());
                                                }
                                            }
                                            System.out.println(data.size());
                                            adapter.notifyDataSetChanged();
                                        }
                                    });
                        }else {
                            System.out.print("not found");
                        }
                    }
                });

    }
}
