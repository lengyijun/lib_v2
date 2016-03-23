package com.example.steven.sjtu_lib_v2.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.example.steven.sjtu_lib_v2.fragment.BookRank;
import com.example.steven.sjtu_lib_v2.fragment.PersonRank;

/**
 * Created by steven on 2016/3/23.
 */
public class MyPagerAdapter extends FragmentStatePagerAdapter{
    int nNumOfTabs;

    public MyPagerAdapter(FragmentManager fm,int nNumOfTabs) {
        super(fm);
        this.nNumOfTabs=nNumOfTabs;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position){
            case 0:
                PersonRank fragment1= new PersonRank();
                return fragment1;
            case 1:
                BookRank fragment2=new BookRank();
                return fragment2;

        }
        return null;
    }

    @Override
    public int getCount() {
        return nNumOfTabs;
    }
}
