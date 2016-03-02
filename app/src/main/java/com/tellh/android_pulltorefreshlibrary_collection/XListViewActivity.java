package com.tellh.android_pulltorefreshlibrary_collection;


import com.tellh.android_pulltorefreshlibrary_collection.XListView.XListView;

public class XListViewActivity extends BaseActivity {


    @Override
    public int getLayoutId() {
        return R.layout.activity_xlistview;
    }

    @Override
    public void initView() {
    }


    @Override
    public void onBackPressed() {
        if (((XListView)listView).isPullRefreshing()){
            ((XListView)listView).stopRefresh();
            return;
        }
        super.onBackPressed();
    }
}
