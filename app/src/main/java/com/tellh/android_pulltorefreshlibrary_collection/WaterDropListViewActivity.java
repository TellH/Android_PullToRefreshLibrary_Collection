package com.tellh.android_pulltorefreshlibrary_collection;


import com.tellh.android_pulltorefreshlibrary_collection.WaterDropListView.WaterDropListView;

public class WaterDropListViewActivity extends BaseActivity {


    @Override
    public int getLayoutId() {
        return R.layout.activity_water_drop_listview;
    }

    @Override
    public void initView() {
    }


    @Override
    public void onBackPressed() {
        if (((WaterDropListView)listView).isRefreshing()){
            ((WaterDropListView)listView).stopRefresh();
            return;
        }
        super.onBackPressed();
    }
}
