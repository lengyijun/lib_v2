package com.example.steven.sjtu_lib_v2;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

/**
 * Created by steven on 2016/3/10.
 */
public class CollectionBook {
    int id;
    String name;
    String url;

    public CollectionBook(int id, String name, String url) {
        this.id=id;
        this.name=name;
        this.url=url;
    }

    public String getShortName(){
        Document document= Jsoup.parse(name);
        String result=document.getElementsByClass("EXLResultTitle").text();
        return result;
    }

    public String getName() {
        return name;
    }

    public int getId() {
        return id;
    }
}
