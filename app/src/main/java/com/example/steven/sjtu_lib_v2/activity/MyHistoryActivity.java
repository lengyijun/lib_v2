package com.example.steven.sjtu_lib_v2.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ListView;
import android.widget.Toast;

import com.example.steven.sjtu_lib_v2.asynctask.MyHistoryAsy;
import com.example.steven.sjtu_lib_v2.R;
import com.example.steven.sjtu_lib_v2.RefreshBorrowInterface;
import com.example.steven.sjtu_lib_v2.adapter.HistoryAdapter;
import com.snappydb.DB;
import com.snappydb.DBFactory;
import com.snappydb.SnappydbException;

import org.jsoup.nodes.Element;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by zcw on 16/4/1.
 */
public class MyHistoryActivity extends AppCompatActivity implements RefreshBorrowInterface {
    @Bind(R.id.listview_history)ListView listView;
    HistoryAdapter HistoryAdapter;
    ArrayList<Element> data;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.history_drawer);
        ButterKnife.bind(this);
        try {
            DB snappydb = DBFactory.open(getApplication(), "notvital");
            String name = snappydb.get("name");
            String pass = snappydb.get("pass");
            if (name.length() == 0 || pass.length() == 0) {
                Toast.makeText(getApplicationContext(), "尚未登陆", Toast.LENGTH_SHORT).show();
            } else {
                ArrayList<Element> elementArrayList = new MyHistoryAsy(this, name, pass, getApplicationContext()).execute().get();
                HistoryAdapter = new HistoryAdapter(this, 0, elementArrayList);
                listView.setAdapter(HistoryAdapter);
            }
        } catch (SnappydbException e) {
            Toast.makeText(getApplicationContext(),"尚未登陆",Toast.LENGTH_SHORT).show();
            finish();
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void ontaskcompleted() {
        HistoryAdapter.notifyDataSetChanged();
    }
}
