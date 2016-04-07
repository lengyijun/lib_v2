package com.example.steven.sjtu_lib_v2.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ListView;
import android.widget.Toast;

import com.example.steven.sjtu_lib_v2.asynctask.MyBorrowAsy;
import com.example.steven.sjtu_lib_v2.R;
import com.example.steven.sjtu_lib_v2.RefreshBorrowInterface;
import com.example.steven.sjtu_lib_v2.adapter.MyBorrowAdapter;
import com.snappydb.DB;
import com.snappydb.DBFactory;
import com.snappydb.SnappydbException;

import org.jsoup.nodes.Element;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by steven on 2016/3/23.
 */
public class MyBorrowActivity extends AppCompatActivity implements RefreshBorrowInterface {
    @Bind(R.id.listView2)ListView listView;
    MyBorrowAdapter myBorrowAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.myborrow_drawer);
        ButterKnife.bind(this);
        try {
            DB snappydb= DBFactory.open(getApplication(), "notvital");
            String name=snappydb.get("name");
            String pass=snappydb.get("pass");
            if (name.length() == 0 || pass.length() == 0) {
                Toast.makeText(getApplicationContext(),"尚未登陆",Toast.LENGTH_SHORT).show();
            }else {
                ArrayList<Element> elementArrayList=new MyBorrowAsy(this,name,pass,getApplicationContext()).execute().get();
                myBorrowAdapter=new MyBorrowAdapter(this,0,elementArrayList);
                listView.setAdapter(myBorrowAdapter);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (SnappydbException e) {
            Toast.makeText(getApplicationContext(),"尚未登陆",Toast.LENGTH_SHORT).show();
            finish();
            e.printStackTrace();
        }
    }

    @Override
    public void ontaskcompleted() {
        myBorrowAdapter.notifyDataSetChanged();
    }
}
