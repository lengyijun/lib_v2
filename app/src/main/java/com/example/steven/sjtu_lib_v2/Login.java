package com.example.steven.sjtu_lib_v2;

import android.graphics.Bitmap;
import android.os.AsyncTask;

import com.googlecode.tesseract.android.TessBaseAPI;
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
public class Login extends AsyncTask<Void,Void,ArrayList<Element>> {
    Elements elements;
    private Refresh_borrow listener;

    public Login(Refresh_borrow listener){
        this.listener=listener;
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
                                                .addParams("user", "maundercurfew")
                                                .addParams("pass", "stodgy1")
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
                                                                                Document document=Jsoup.parse(response);
                                                                                Pattern pattern=Pattern.compile("(?<=href=\").*func=bor-info");
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
                                                                                                    System.out.println(response);
                                                                                                    Pattern pattern=Pattern.compile("(?<=javascript:replacePage\\(').*func=bor-loan&adm_library=SJT50");
                                                                                                    Matcher matcher=pattern.matcher(response);
                                                                                                    if (matcher.find()) {
                                                                                                        String url=matcher.group(0);
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
                                                                                                                        Document document=Jsoup.parse(response);
                                                                                                                        elements=document.getElementsByAttributeValue("id", "centered");
                                                                                                                        System.out.println(elements.size());
                                                                                                                        for (Element ele : elements) {
                                                                                                                            result.add(ele.parent());
                                                                                                                            System.out.println(ele.parent().toString());
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

                                                            }else {
                                                                System.out.print("not found");
                                                            }
                                                        }else {
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
