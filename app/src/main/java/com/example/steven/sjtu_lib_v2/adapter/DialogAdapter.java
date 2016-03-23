package com.example.steven.sjtu_lib_v2.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.steven.sjtu_lib_v2.R;

import org.jsoup.nodes.Element;

import java.util.List;

/**
 * Created by steven on 2016/2/13.
 */
public class DialogAdapter extends ArrayAdapter<Element>{
    List<Element> total_element;

    public DialogAdapter(Context context, int resource, List<Element> objects) {
        super(context, resource, objects);
        this.total_element=objects;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        
        convertView= LayoutInflater.from(getContext()).inflate(R.layout.dialog,null);
        TextView location= (TextView) convertView.findViewById(R.id.location);
        TextView status= (TextView) convertView.findViewById(R.id.status);
        
        status.setText(getStatus(position));
        location.setText(getLocation(position));
        return convertView;
    }

    private String getStatus(int position) {
        Element ele=total_element.get(position);
        return ele.getElementsByClass("EXLLocationTableColumn3").text();
    }

    private String getLocation(int position) {
        Element ele=total_element.get(position);
        return ele.getElementsByAttributeValue("title","显示馆藏地详细信息").text();
    }
}
