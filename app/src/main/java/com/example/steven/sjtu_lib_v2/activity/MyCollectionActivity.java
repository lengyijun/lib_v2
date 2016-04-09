package com.example.steven.sjtu_lib_v2.activity;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;

import com.baoyz.swipemenulistview.SwipeMenu;
import com.baoyz.swipemenulistview.SwipeMenuCreator;
import com.baoyz.swipemenulistview.SwipeMenuItem;
import com.baoyz.swipemenulistview.SwipeMenuListView;
import com.example.steven.sjtu_lib_v2.CollectionBook;
import com.example.steven.sjtu_lib_v2.R;
import com.example.steven.sjtu_lib_v2.adapter.CollectionAdapter;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnItemClick;

//收藏界面的activity
public class MyCollectionActivity extends AppCompatActivity {
    @Bind(R.id.listView2)
    SwipeMenuListView lv;
    @Bind(R.id.toolbar)
    Toolbar toolbar;

    SQLiteDatabase db;
    CollectionAdapter adapter;
    ArrayList<CollectionBook> collectionbookList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.collection_drawer);
        ButterKnife.bind(this);

        db = openOrCreateDatabase("collection.db", Context.MODE_PRIVATE, null);

        SwipeMenuCreator creator = new SwipeMenuCreator() {

            @Override
            public void create(SwipeMenu menu) {
                // create "open" item
                SwipeMenuItem openItem = new SwipeMenuItem(
                        getApplicationContext());
                // set item background
                openItem.setBackground(new ColorDrawable(Color.rgb(0xC9, 0xC9,
                        0xCE)));
                // set item width
                openItem.setWidth(dp2px(90));
                // set item title
                openItem.setTitle("Open");
                // set item title fontsize
                openItem.setTitleSize(18);
                // set item title font color
                openItem.setTitleColor(Color.WHITE);
                // add to menu
                menu.addMenuItem(openItem);

                // create "delete" item
                SwipeMenuItem deleteItem = new SwipeMenuItem(
                        getApplicationContext());
                // set item background
                deleteItem.setBackground(new ColorDrawable(Color.rgb(0xF9,
                        0x3F, 0x25)));
                // set item width
                deleteItem.setWidth(dp2px(90));
                // set a icon
                deleteItem.setIcon(R.drawable.ic_delete);
                // add to menu
                menu.addMenuItem(deleteItem);
            }
        };

        lv.setMenuCreator(creator);
        collectionbookList = get_data();
        adapter = new CollectionAdapter(this, 0, collectionbookList);
        lv.setAdapter(adapter);
        lv.setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(int position, SwipeMenu menu, int index) {
                switch (index) {
                    case 0:
                        openSingleActivity(position);
                        break;
                    case 1:
                        // delete
                        rmBook(position);
                        collectionbookList.clear();
                        collectionbookList.addAll(get_data());
                        lv.invalidateViews();
//                        adapter.notifyDataSetChanged();
                        break;
                }
                // false : close the menu; true : not close the menu
                return false;
            }

            private void rmBook(int position) {
                CollectionBook collectionBook = adapter.getItem(position);
                int choosed_id = collectionBook.getId();
                db.delete("favourite", "_id " + "=" + choosed_id, null);
            }
        });

    }

    @OnItemClick(R.id.listView2)
    public void openSingleActivity(int position) {
        CollectionBook collectionBook = adapter.getItem(position);
        int choosed_id = collectionBook.getId();
        Cursor cursor = db.query("favourite", new String[]{"book_name", "url"}, "_id like ?", new String[]{choosed_id + ""}, null, null, null);
        cursor.moveToFirst();
        String name = cursor.getString(cursor.getColumnIndex("book_name"));
        String url = cursor.getString(cursor.getColumnIndex("url"));

        Intent intent = new Intent();
        intent.setClass(MyCollectionActivity.this, SingleDetailActivity.class);
        intent.putExtra("detail", name);
        intent.putExtra("url", url);
        startActivity(intent);
    }

    public ArrayList<CollectionBook> get_data() {
        ArrayList<CollectionBook> book_list = new ArrayList<CollectionBook>();
        int collectionCount=0;

        Cursor cursor = db.rawQuery("select * from favourite", null);
        if (cursor.moveToFirst()) {
            while (cursor.isAfterLast() == false) {
                collectionCount+=1;
                String name = cursor.getString(cursor
                        .getColumnIndex("book_name"));
                String url = cursor.getString(cursor
                        .getColumnIndex("url"));
                int id = cursor.getInt(cursor.getColumnIndex("_id"));

                CollectionBook book = new CollectionBook(id, name, url);
                book_list.add(book);

                cursor.moveToNext();
            }
        }
        toolbar.setTitle("你收藏了"+collectionCount+"本书");
        toolbar.setTitleTextColor(Color.WHITE);
        return book_list;
    }

    private int dp2px(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
                getResources().getDisplayMetrics());
    }

}
