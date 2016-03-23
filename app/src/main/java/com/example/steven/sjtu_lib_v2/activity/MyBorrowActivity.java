package com.example.steven.sjtu_lib_v2.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ListView;

import com.example.steven.sjtu_lib_v2.Login;
import com.example.steven.sjtu_lib_v2.R;
import com.example.steven.sjtu_lib_v2.Refresh_borrow;
import com.example.steven.sjtu_lib_v2.adapter.MyBorrowAdapter;

import org.jsoup.nodes.Element;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by steven on 2016/3/23.
 */
public class MyBorrowActivity extends AppCompatActivity implements Refresh_borrow{
    @Bind(R.id.listView2)ListView listView;
    MyBorrowAdapter myBorrowAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        ButterKnife.bind(this);
        try {
            ArrayList<Element> elementArrayList=new Login(this).execute().get();
            myBorrowAdapter=new MyBorrowAdapter(this,0,elementArrayList);
            listView.setAdapter(myBorrowAdapter);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void ontaskcompleted() {
        myBorrowAdapter.notifyDataSetChanged();
    }
}
