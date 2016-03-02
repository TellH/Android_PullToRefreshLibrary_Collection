package com.tellh.android_pulltorefreshlibrary_collection;


import android.annotation.TargetApi;
import android.os.Build;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.tellh.android_pulltorefreshlibrary_collection.PullToRefreshLayoutTellH.PullToRefreshLayout;

public class PullToRefreshLayoutActivity extends BaseActivity {


    private PullToRefreshLayout mRefreshLayout;
    private ProgressBar progressBar;
    private TextView textView;
    private ImageView imageView;
    private ImageView imgDone;

    @Override
    public int getLayoutId() {
        return R.layout.activity_pull_to_refresh_tellh;
    }

    @Override
    public void initView() {
        mRefreshLayout = (PullToRefreshLayout) findViewById(R.id.refresh_widget);
        createRefreshView();
        mRefreshLayout.setOnRefreshListener(new PullToRefreshLayout.OnRefreshListenerAdapter() {
            @Override
            public void onRefresh() {
                textView.setText("正在刷新");
                imgDone.setVisibility(View.GONE);
                imageView.setVisibility(View.INVISIBLE);
                progressBar.setVisibility(View.VISIBLE);
                mRefreshLayout.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mRefreshLayout.setRefreshing(false);
                    }
                }, 2000);
            }

            @TargetApi(Build.VERSION_CODES.HONEYCOMB)
            @Override
            public void onDragDistanceChange(float distance, float percent, float offset) {
                if (percent >= 1.0f) {
                    textView.setText("松开刷新");
                    imgDone.setVisibility(View.GONE);
                    imageView.setVisibility(View.VISIBLE);
                    imageView.setRotation(180);
                } else {
                    textView.setText("下拉刷新");
                    imgDone.setVisibility(View.GONE);
                    imageView.setVisibility(View.VISIBLE);
                    imageView.setRotation(0);
                }
            }

            @Override
            public void onFinish() {
                textView.setText("刷新成功");
                imageView.setVisibility(View.GONE);
                progressBar.setVisibility(View.GONE);
                imgDone.setVisibility(View.VISIBLE);
            }
        });
        mRefreshLayout.setFinishRefreshToPauseDuration(800);
        mRefreshLayout.setRefreshing(true);
    }

    private View createRefreshView() {
        View headerView=mRefreshLayout.setRefreshView(R.layout.layout_refresh_view);
        progressBar = (ProgressBar) headerView.findViewById(R.id.pb_view);
        textView = (TextView) headerView.findViewById(R.id.text_view);
        textView.setText("下拉刷新");
        imageView = (ImageView) headerView.findViewById(R.id.image_view);
        imageView.setVisibility(View.VISIBLE);
        imageView.setImageResource(R.drawable.down_arrow);
        imgDone=(ImageView)headerView.findViewById(R.id.img_done);
        imgDone.setImageResource(R.drawable.ic_check_circle_black);
        imgDone.setVisibility(View.GONE);
        progressBar.setVisibility(View.GONE);
        return headerView;
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
