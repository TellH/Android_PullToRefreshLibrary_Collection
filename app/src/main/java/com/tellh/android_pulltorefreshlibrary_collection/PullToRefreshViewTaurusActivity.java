package com.tellh.android_pulltorefreshlibrary_collection;



public class PullToRefreshViewTaurusActivity extends BaseActivity {


    private com.yalantis.taurus.PullToRefreshView mRefreshLayout;

    @Override
    public int getLayoutId() {
        return R.layout.activity_pull_to_refresh_taurus_fly_plane;
    }

    @Override
    public void initView() {
        mRefreshLayout = (com.yalantis.taurus.PullToRefreshView) findViewById(R.id.refresh_widget);
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
