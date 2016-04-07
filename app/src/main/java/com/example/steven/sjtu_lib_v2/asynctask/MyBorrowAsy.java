package com.example.steven.sjtu_lib_v2.asynctask;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;

import com.example.steven.sjtu_lib_v2.RefreshBorrowInterface;
import com.googlecode.tesseract.android.TessBaseAPI;
import com.snappydb.DB;
import com.snappydb.DBFactory;
import com.snappydb.SnappydbException;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.BitmapCallback;
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
 * Created by steven on 2016/3/21.
 */
public class MyBorrowAsy extends AsyncTask<Void,Void,ArrayList<Element>> {
    Elements elements;
    private RefreshBorrowInterface listener;
    private String name;
    private String pass;
    private Context context;

    public MyBorrowAsy(RefreshBorrowInterface listener, String name, String pass, Context context){
        this.listener=listener;
        this.name=name;
        this.pass=pass;
        this.context=context;
    }

    @Override
    protected ArrayList<Element> doInBackground(Void... params) {
        final ArrayList<Element> result=new ArrayList<Element>();
        OkHttpUtils.get()
                .url("http://opac.lib.sjtu.edu.cn:8118/sjt-local/opac-login.jsp")
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e) {

                    }

                    @Override
                    public void onResponse(String response) {
                        System.out.println(response.length());
                        final Document doc = Jsoup.parse(response);
                        final String sid = doc.getElementsByAttributeValueMatching("name", "sid").first().attr("value").toString();
                        final String returl = doc.getElementsByAttributeValueMatching("name", "returl").first().attr("value").toString();
                        final String se = doc.getElementsByAttributeValueMatching("name", "se").first().attr("value").toString();
                        final String v = doc.getElementsByAttributeValueMatching("name", "v").first().attr("value").toString();
                        OkHttpUtils.get()
                                .url("https://jaccount.sjtu.edu.cn/jaccount/captcha")
                                .addHeader("User-Agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/43.0.2357.65 Safari/537.36")
                                .build()
                                .execute(new BitmapCallback() {
                                    @Override
                                    public void onError(Call call, Exception e) {

                                    }

                                    @Override
                                    public void onResponse(Bitmap response) {
                                        final TessBaseAPI baseAPI = new TessBaseAPI();
                                        baseAPI.init("/sdcard/", "eng");
                                        baseAPI.setImage(response);
                                        String captcha_text = baseAPI.getUTF8Text().toString();
                                        System.out.println(captcha_text);

                                        OkHttpUtils.post()
                                                .url("https://jaccount.sjtu.edu.cn/jaccount/ulogin")
                                                .addHeader("User-Agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/43.0.2357.65 Safari/537.36")
                                                .addParams("sid", sid)
                                                .addParams("returl", returl)
                                                .addParams("se", se)
                                                .addParams("v", v)
                                                .addParams("captcha", captcha_text)
                                                .addParams("user", name)
                                                .addParams("pass", pass)
                                                .addParams("imageField.x", "55")
                                                .addParams("imageField.y", "3")
                                                .build()
                                                .execute(new StringCallback() {
                                                    @Override
                                                    public void onError(Call call, Exception e) {

                                                    }

                                                    @Override
                                                    public void onResponse(String response) {
                                                        System.out.println("hello");
                                                        int index = response.indexOf("loginfail");
                                                        if (index == -1) {
                                                            //                                没有找到loginfail，所以可以继续访问下去
                                                            final String line = "\'https.*\'";
                                                            final Pattern p = Pattern.compile(line);
                                                            Matcher m = p.matcher(response);
                                                            if (m.find()) {
                                                                String next_url = m.group(0);
                                                                next_url = next_url.substring(1, next_url.length() - 1);
                                                                OkHttpUtils.get()
                                                                        .url(next_url)
                                                                        .build()
                                                                        .execute(new StringCallback() {
                                                                            @Override
                                                                            public void onError(Call call, Exception e) {

                                                                            }

                                                                            @Override
                                                                            public void onResponse(String response) {
                                                                                System.out.println(response);
                                                                                Pattern pattern = Pattern.compile("(?<=href=\").*func=bor-info");
                                                                                Matcher matcher = pattern.matcher(response);
                                                                                if (matcher.find()) {
                                                                                    String url = matcher.group(0);
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
                                                                                                    System.out.println(response);
                                                                                                    Pattern pattern = Pattern.compile("(?<=javascript:replacePage\\(').*func=bor-loan&adm_library=SJT50");
                                                                                                    final Matcher matcher = pattern.matcher(response);
                                                                                                    if (matcher.find()) {
                                                                                                        String url = matcher.group(0);
                                                                                                        OkHttpUtils.get()
                                                                                                                .url(url)
                                                                                                                .build()
                                                                                                                .execute(new StringCallback() {
                                                                                                                    @Override
                                                                                                                    public void onError(Call call, Exception e) {

                                                                                                                    }

                                                                                                                    @Override
                                                                                                                    public void onResponse(String response) {
                                                                                                                        System.out.println(response);
//                                                                                                                        .不能匹配换行符
                                                                                                                        Pattern pattern1=Pattern.compile("(?<=在借书籍情况：).*");
                                                                                                                        Matcher matcher1=pattern1.matcher(response);
                                                                                                                        if(matcher1.find()){
                                                                                                                            try {
                                                                                                                                DB snappydb= DBFactory.open(context, "notvital");
                                                                                                                                snappydb.put("realname",matcher1.group(0));
                                                                                                                                System.out.print("<<<<........");
                                                                                                                                System.out.print(matcher1.group(0));
                                                                                                                            } catch (SnappydbException e) {
                                                                                                                                e.printStackTrace();
                                                                                                                            }
                                                                                                                        }else {
                                                                                                                            System.out.println("not found real name");
                                                                                                                        }
                                                                                                                        Document document = Jsoup.parse(response);
                                                                                                                        elements = document.getElementsByAttributeValue("id", "centered");
                                                                                                                        System.out.println(elements.size());
                                                                                                                        for (Element ele : elements) {
                                                                                                                            result.add(ele.parent());
                                                                                                                            listener.ontaskcompleted();
                                                                                                                        }
                                                                                                                    }
                                                                                                                });
                                                                                                    }

                                                                                                }
                                                                                            });
                                                                                }
                                                                            }
                                                                        });

                                                            } else {
                                                                System.out.print("not found");
                                                            }
                                                        } else {
                                                            System.out.print("you should retry");
                                                        }
                                                    }
                                                });

                                    }
                                });
                    }
                });

        return result;
    }

}
