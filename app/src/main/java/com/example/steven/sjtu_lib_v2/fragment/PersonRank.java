package com.example.steven.sjtu_lib_v2.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.example.steven.sjtu_lib_v2.R;
import com.example.steven.sjtu_lib_v2.adapter.MostReaderAdapter;
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
public class PersonRank extends Fragment{
    ListView listView;
    ArrayList<Element> data;
    MostReaderAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment,container,false);
        listView= (ListView) view.findViewById(R.id.listView3);
        adapter= new MostReaderAdapter(getActivity(),0,data);
        listView.setAdapter(adapter);
//        listView.setAdapter(new ArrayAdapter<String>(getActivity(),android.R.layout.simple_expandable_list_item_1,data));
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
                        System.out.print("nothing2");
                    }

                    @Override
                    public void onResponse(String response) {
                        Pattern pattern= Pattern.compile("http:.*phb\\.html");
                        Matcher matcher=pattern.matcher(response);
                        if (matcher.find()) {
                            String url=matcher.group(0);
                            url=url.replace("phb","phb-user");
                            System.out.println(url);
                            OkHttpUtils.get()
                                    .url(url)
                                    .build()
                                    .execute(new StringCallback() {
                                        @Override
                                        public void onError(Call call, Exception e) {
                                            System.out.print("nothing1");
                                        }

                                        @Override
                                        public void onResponse(String response) {
                                            Document document= Jsoup.parse(response);
                                            Elements elements=document.getElementsByClass("text1");
                                            for (Element i : elements) {
                                                if (i.tagName().equals("tr")) {
                                                    data.add(i);
                                                    System.out.print(i.toString());
                                                }
                                            }
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
