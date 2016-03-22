package com.example.steven.sjtu_lib_v2.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.android.volley.toolbox.Volley;
import com.example.steven.sjtu_lib_v2.R;

import org.jsoup.nodes.Element;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by steven on 2016/2/9.
 */
public class BookItemAdapter extends ArrayAdapter<Element> {
    ImageLoader mImageloader;
    String book_cover_base_url="http://pds.cceu.org.cn/cgi-bin/isbn_cover.cgi?isbn=";

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Element complete_info=getItem(position);
        String cover_image_url=get_cover_image_url(complete_info);
        String book_name=get_book_name(complete_info);

        if(convertView==null){
            convertView= LayoutInflater.from(getContext()).inflate(R.layout.item,null);
        }
        TextView tv= (TextView) convertView.findViewById(R.id.textView2);
        NetworkImageView book_cover= (NetworkImageView) convertView.findViewById(R.id.book_cover);

        tv.setText(book_name);
        book_cover.setImageUrl(cover_image_url,mImageloader);

        return convertView;
    }

    private String get_book_name(Element complete_info) {
        return complete_info.getElementsByClass("EXLResultTitle").text();
    }

    private String get_cover_image_url(Element complete_info) {
        Pattern pattern= Pattern.compile("(?<=isbn=)\\d*");
        Matcher matcher=pattern.matcher(complete_info.toString());
        if(matcher.find()){
           return(book_cover_base_url+matcher.group());
        }else{
            return null;
        }
    }

    public BookItemAdapter(Context context, int resource, List<Element> objects) {
        super(context, resource, objects);
        RequestQueue quene= Volley.newRequestQueue(context);
        mImageloader=new ImageLoader(quene,new BitmapCache());
    }

    private class BitmapCache implements ImageLoader.ImageCache {

        private LruCache<String,Bitmap> mCache;

        public BitmapCache(){
            int maxMemory= (int) Runtime.getRuntime().maxMemory();
            int cacheSize=maxMemory/8;
            mCache=new LruCache<String,Bitmap>(cacheSize){
                @Override
                protected int sizeOf(String key, Bitmap value) {
                    return value.getRowBytes()*value.getHeight();
                }
            };
        }
        @Override
        public Bitmap getBitmap(String url) {
            return mCache.get(url);
        }

        @Override
        public void putBitmap(String url, Bitmap bitmap) {
            mCache.put(url,bitmap);
        }
    }
}
