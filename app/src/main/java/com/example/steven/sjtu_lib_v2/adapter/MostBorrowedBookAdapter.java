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
public class MostBorrowedBookAdapter extends ArrayAdapter<Element> {

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Element complete_info=getItem(position);
        Elements elements_td1=complete_info.getElementsByTag("td");
        String rank=elements_td1.get(0).text();
        String book_name=elements_td1.get(1).text();
        String author=elements_td1.get(2).text();
        String company=elements_td1.get(3).text();
        String year=elements_td1.get(4).text();
        String call_number=elements_td1.get(5).text();
        String times=elements_td1.get(6).text();

        if(convertView==null){
            convertView= LayoutInflater.from(getContext()).inflate(R.layout.mostborrowedbookitem,null);
        }
        TextView tv_rank = (TextView) convertView.findViewById(R.id.rank);
        TextView tv_name= (TextView) convertView.findViewById(R.id.book_name);
        TextView tv_author= (TextView) convertView.findViewById(R.id.author);
        TextView tv_company= (TextView) convertView.findViewById(R.id.company);
        TextView tv_year= (TextView) convertView.findViewById(R.id.year);
        TextView tv_callnumber= (TextView) convertView.findViewById(R.id.call_number);
        TextView tv_times= (TextView) convertView.findViewById(R.id.times);

        tv_rank.setText(rank);
        tv_name.setText(book_name);
        tv_author.setText(author);
        tv_company.setText(company);
        tv_year.setText(year);
        tv_callnumber.setText(call_number);
        tv_times.setText(times);

        return convertView;
    }

    public MostBorrowedBookAdapter(Context context, int resource, List<Element> objects) {
        super(context, resource, objects);
    }
}
