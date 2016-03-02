package com.tellh.android_pulltorefreshlibrary_collection;


import com.tellh.android_pulltorefreshlibrary_collection.util.Logger;
import com.tellh.swipetorefreshlayoutgoogle.SwipeRefreshTestLayout;

public class SwipeToRefreshActivity extends BaseActivity {


    private SwipeRefreshTestLayout mRefreshLayout;

    @Override
    public int getLayoutId() {
        Logger.d();
        return R.layout.activity_swipe_to_refresh_google;
    }

    @Override
    public void initView() {
        mRefreshLayout = (SwipeRefreshTestLayout) findViewById(R.id.refresh_widget);
        mRefreshLayout.setEnableSpinnerScale(true);
        mRefreshLayout.setRefreshing(true);
//        new Handler().postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                mRefreshLayout.setRefreshing(true);
//            }
//        },100);

//        mRefreshLayout.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                mRefreshLayout.setRefreshing(false);
//            }
//        }, 3000);
    }


    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        com.tellh.swipetorefreshlayoutgoogle.util.Logger.d("mRefreshLayout.isActivated();" + mRefreshLayout.isShown());
//        mRefreshLayout.setRefreshing(true);
    }

    @Override
    public void onBackPressed() {
        if (mRefreshLayout.isRefreshing()) {
            mRefreshLayout.setRefreshing(false);
            return;
        }
        super.onBackPressed();
    }
}
