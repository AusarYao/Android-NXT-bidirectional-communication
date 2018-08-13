package com.example.auser.nxtcontroller;

import android.app.ListActivity;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;

public class DynamicListView {

    private ArrayList<String> mListData = new ArrayList<>();
    private ArrayAdapter<String> mAdapter;
    private ListView mListView;

    public void initialize(ListView listView, Context context){
        mListView = listView;
        mAdapter = new ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, mListData );
        mListView.setAdapter(mAdapter);
    }

    public void updataData(int data){
        mAdapter.add(Integer.toString(data));

    }

}
