package com.example.steven.sjtu_lib_v2.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.steven.sjtu_lib_v2.CollectionBook;
import com.example.steven.sjtu_lib_v2.R;

import java.util.ArrayList;

/**
 * Created by steven on 2016/3/10.
 */
public class CollectionAdapter extends ArrayAdapter<CollectionBook>{

    public CollectionAdapter(Context context, int resource, ArrayList<CollectionBook> objects) {
        super(context, resource, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        CollectionBook book=getItem(position);

        if(convertView==null){
            convertView= LayoutInflater.from(getContext()).inflate(R.layout.collectionitem,null);
        }
        TextView tv_col_name= (TextView) convertView.findViewById(R.id.col_name);
        TextView tv_col_author= (TextView) convertView.findViewById(R.id.col_author);
        TextView tv_col_detail= (TextView) convertView.findViewById(R.id.col_details);
        TextView tv_forth= (TextView) convertView.findViewById(R.id.fourthline);

        tv_col_name.setText(book.getShortName());
        tv_col_author.setText(book.getAuthor());
        tv_col_detail.setText(book.getDetail());
        tv_forth.setText(book.getForth());
        return convertView;
    }

}
