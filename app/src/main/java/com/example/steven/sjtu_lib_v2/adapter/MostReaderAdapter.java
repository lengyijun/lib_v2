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
public class MostReaderAdapter extends ArrayAdapter<Element> {

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Element complete_info=getItem(position);
        Elements elements_td1=complete_info.getElementsByTag("td");
        String rank=elements_td1.get(0).text();
        String name=elements_td1.get(1).text();
        String times=elements_td1.get(2).text();

        if(convertView==null){
            convertView= LayoutInflater.from(getContext()).inflate(R.layout.mostpersonborrowing,null);
        }
        TextView tv_rank = (TextView) convertView.findViewById(R.id.rank);
        TextView tv_name= (TextView) convertView.findViewById(R.id.name);
        TextView tv_times= (TextView) convertView.findViewById(R.id.times);

        tv_rank.setText(rank);
        tv_name.setText(name);
        tv_times.setText(times);

        return convertView;
    }

    public MostReaderAdapter(Context context, int resource, List<Element> objects) {
        super(context, resource, objects);
    }
}
