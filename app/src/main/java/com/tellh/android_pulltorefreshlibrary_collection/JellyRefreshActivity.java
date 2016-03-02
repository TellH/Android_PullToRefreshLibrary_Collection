package com.tellh.android_pulltorefreshlibrary_collection;


import uk.co.imallan.jellyrefresh.JellyRefreshLayout;

public class JellyRefreshActivity extends BaseActivity {


    private JellyRefreshLayout mRefreshLayout;

    @Override
    public int getLayoutId() {
        return R.layout.activity_jelly_refresh;
    }

    @Override
    public void initView() {
        mRefreshLayout = (JellyRefreshLayout) findViewById(R.id.refresh_widget);
        mRefreshLayout.setRefreshListener(new JellyRefreshLayout.JellyRefreshListener() {
            @Override
            public void onRefresh(JellyRefreshLayout jellyRefreshLayout) {
                mRefreshLayout.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mRefreshLayout.finishRefreshing();
                    }
                }, 3000);
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (mRefreshLayout.isRefreshing()) {
            mRefreshLayout.finishRefreshing();
            return;
        }
        super.onBackPressed();
    }
}
