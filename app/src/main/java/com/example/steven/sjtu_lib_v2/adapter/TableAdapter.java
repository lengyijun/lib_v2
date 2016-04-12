package com.example.steven.sjtu_lib_v2.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TableRow;
import android.widget.TextView;

import com.example.steven.sjtu_lib_v2.R;

import org.jsoup.nodes.Element;

import java.util.List;

/**
 * Created by steven on 2016/2/15.
 */
public class TableAdapter extends ArrayAdapter<Element> {

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Element element=getItem(position);

        if (convertView==null){
            convertView= LayoutInflater.from(getContext()).inflate(R.layout.detail_table,null);
        }
        TextView location_table= (TextView) convertView.findViewById(R.id.location_table);
        TextView subscribing_table= (TextView) convertView.findViewById(R.id.subscring_table);
        TextView single_status_table= (TextView) convertView.findViewById(R.id.single_status_table);
        TextView return_data_table= (TextView) convertView.findViewById(R.id.return_date_table);

        location_table.setText(element.getElementsByAttributeValue("title", "显示馆藏地详细信息").text());
        subscribing_table.setText(element.getElementsByClass("EXLLocationTableColumn1").text());
        single_status_table.setText(element.getElementsByClass("EXLLocationTableColumn2").text());
        return_data_table.setText(element.getElementsByClass("EXLLocationTableColumn3").text());

        if (! return_data_table.getText().toString().equals("在架上")) {
            TableRow shadow1= (TableRow) convertView.findViewById(R.id.shadow1);
            TableRow shadow2= (TableRow) convertView.findViewById(R.id.shadow2);
            TableRow deep1= (TableRow) convertView.findViewById(R.id.deep1);
            TableRow deep2= (TableRow) convertView.findViewById(R.id.deep2);

            shadow1.setBackgroundColor(android.graphics.Color.parseColor("#fdeee7"));
            shadow2.setBackgroundColor(android.graphics.Color.parseColor("#fdeee7"));
            deep1.setBackgroundColor(android.graphics.Color.parseColor("#fdddd0"));
            deep2.setBackgroundColor(android.graphics.Color.parseColor("#fdddd0"));
        }
        return convertView;
    }

    public TableAdapter(Context context, int resource, List<Element> objects) {
        super(context, resource, objects);
    }
}
