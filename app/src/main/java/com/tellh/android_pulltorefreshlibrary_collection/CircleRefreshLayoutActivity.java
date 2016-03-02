package com.tellh.android_pulltorefreshlibrary_collection;


import com.tuesda.walker.circlerefresh.CircleRefreshLayout;

public class CircleRefreshLayoutActivity extends BaseActivity {


    private CircleRefreshLayout mRefreshLayout;

    @Override
    public int getLayoutId() {
        return R.layout.activity_circle_refresh;
    }

    @Override
    public void initView() {
        mRefreshLayout = (CircleRefreshLayout) findViewById(R.id.refresh_widget);
        mRefreshLayout.setOnRefreshListener(new CircleRefreshLayout.OnCircleRefreshListener() {
            @Override
            public void completeRefresh() {

            }

            @Override
            public void refreshing() {
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
