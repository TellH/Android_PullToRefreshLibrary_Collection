package com.tellh.android_pulltorefreshlibrary_collection;


import com.yalantis.phoenix.PullToRefreshView;

public class PullToRefreshViewPhoenixActivity extends BaseActivity {


    private PullToRefreshView mRefreshLayout;

    @Override
    public int getLayoutId() {
        return R.layout.activity_pull_to_refresh_phoenix;
    }

    @Override
    public void initView() {
        mRefreshLayout = (PullToRefreshView) findViewById(R.id.refresh_widget);
        mRefreshLayout.setRefreshing(true);
        mRefreshLayout.postDelayed(new Runnable() {
            @Override
            public void run() {
                mRefreshLayout.setRefreshing(false);
            }
        }, 3000);
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
