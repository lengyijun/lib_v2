package com.example.steven.sjtu_lib_v2.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.steven.sjtu_lib_v2.R;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.List;

/**
 * Created by steven on 2016/2/9.
 */
public class MyBorrowAdapter extends ArrayAdapter<Element> {

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Element complete_info=getItem(position);
        Elements elements_td1=complete_info.getElementsByClass("td1");
        String book_name=elements_td1.get(3).text();
        String author=elements_td1.get(2).text();
        String return_day=elements_td1.get(5).text();
        String return_time=elements_td1.get(6).text();
        String call_number=elements_td1.get(9).text();

        if(convertView==null){
            convertView= LayoutInflater.from(getContext()).inflate(R.layout.borrowitem,null);
        }
        TextView tv_name= (TextView) convertView.findViewById(R.id.book_name);
        TextView tv_author= (TextView) convertView.findViewById(R.id.author);
        TextView tv_return_day_time= (TextView) convertView.findViewById(R.id.return_day_time);
        TextView tv_callnumber= (TextView) convertView.findViewById(R.id.call_number);

        tv_name.setText(book_name);
        tv_author.setText(author);
        tv_return_day_time.setText(return_day+"   "+return_time);
        tv_callnumber.setText(call_number);

        return convertView;
    }

    public MyBorrowAdapter(Context context, int resource, List<Element> objects) {
        super(context, resource, objects);
    }
}
