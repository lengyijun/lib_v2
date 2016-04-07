package com.example.steven.sjtu_lib_v2.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.example.steven.sjtu_lib_v2.R;
import com.example.steven.sjtu_lib_v2.adapter.DialogAdapter;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import okhttp3.Call;

/**
 * Created by steven on 2016/2/6.
 */
public class BookDetailDialog extends DialogFragment{
    @Bind(R.id.listView) ListView lv;
    @Bind(R.id.call_number)TextView call_num;

    public static String base_url="http://ourex.lib.sjtu.edu.cn/primo_library/libweb/action/";
    String book_name;
    Element element;
    List<Element> loc_sta=new ArrayList<Element>();

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        parse_element();
        LayoutInflater inflater=getActivity().getLayoutInflater();
        View view=inflater.inflate(R.layout.location_info,null);
        ButterKnife.bind(this,view);

        DialogAdapter dialog_adapter=new DialogAdapter(getActivity(),0,loc_sta);
        lv.setAdapter(dialog_adapter);
        AlertDialog.Builder builder=new AlertDialog.Builder(getActivity())
            .setTitle(book_name)
            .setView(view);
        return builder.show();
    }

//    获得馆藏信息的url
    private void parse_element() {
        book_name=element.getElementsByClass("EXLResultTitle").text();
        String link_to_detail=element.getElementsMatchingText("馆藏信息").attr("href");
        link_to_detail=base_url+link_to_detail;
        find_in_out(link_to_detail);
    }

    private void find_in_out(String link_to_detail) {
        OkHttpUtils .get()
                    .url(link_to_detail)
                    .build()
                    .execute(new StringCallback() {
                        @Override
                        public void onError(Call call, Exception e) {

                        }

                        @Override
                        public void onResponse(String response) {
                            Document document = Jsoup.parse(response);
                            Elements elements = document.getElementsByClass("EXLLocationTableColumn3");
//                            表示只在一个图书馆有此书信息
                            if (!elements.isEmpty()) {
                                for (Element i:elements){
                                    loc_sta.add(i.parent());
                                }
                                get_call_number_singlelib(document);
                                lv.invalidateViews();
                            } else {             // 表示在多个图书馆有此书信息
                                List<String> link_list = new ArrayList<String>();
                                Elements link_elm = document.getElementsByClass("EXLLocationsIcon");
                                for (Element i : link_elm) {
                                    String temp_link = i.attr("href");
                                    temp_link = base_url+ temp_link;
                                    link_list.add(temp_link);
                                }
                                get_location_from_linklist(link_list);
                            }
                        }
                    });

    }

    private void get_call_number_singlelib(Document document) {
        Element ele=document.getElementsByClass("EXLLocationTableColumn1").first();
        call_num.setText(ele.text());
    }

    private void get_location_from_linklist(List<String> link_list) {
        for(String link:link_list){
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
                        Document doc=Jsoup.parse(response, "", Parser.xmlParser());
                        String first_modification=doc.getElementsByTag("modification").first().text();
                        Document modi_html=Jsoup.parse(first_modification, "", Parser.htmlParser());
                        Elements fin_eles=modi_html.getElementsByClass("EXLLocationTableColumn3");

                        get_call_number_singlelib(modi_html);
                        for(Element i:fin_eles){
                            loc_sta.add(i.parent());
                        }
                        lv.invalidateViews();

                    }
                });
    }

    public BookDetailDialog(Element element) {
        this.element=element;
    }
}
