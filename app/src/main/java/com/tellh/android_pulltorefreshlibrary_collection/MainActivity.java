package com.tellh.android_pulltorefreshlibrary_collection;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_swipe_to_refresh_layout_google:
                startActivity(new Intent(MainActivity.this, SwipeToRefreshActivity.class));
                break;
            case R.id.btn_pull_to_refresh_view_phoenix:
                startActivity(new Intent(MainActivity.this, PullToRefreshViewPhoenixActivity.class));
                break;
            case R.id.btn_pull_to_refresh_view_tellh:
                startActivity(new Intent(MainActivity.this, PullToRefreshLayoutActivity.class));
                break;
            case R.id.btn_xlistview:
                startActivity(new Intent(MainActivity.this, XListViewActivity.class));
                break;
            case R.id.btn_pull_to_refresh_view_taurus_fly_plane:
                startActivity(new Intent(MainActivity.this, PullToRefreshViewTaurusActivity.class));
                break;
            case R.id.btn_jelly_refresh:
                startActivity(new Intent(MainActivity.this, JellyRefreshActivity.class));
                break;
            case R.id.btn_circle_refresh:
                startActivity(new Intent(MainActivity.this, CircleRefreshLayoutActivity.class));
                break;
            case R.id.btn_water_drop_listview:
                startActivity(new Intent(MainActivity.this, WaterDropListViewActivity.class));
                break;
//            case R.id.btn_ultra_refresh_layout:
//                startActivity(new Intent(MainActivity.this, UltraPullToRefreshActivity.class));
//                break;
        }
    }

}
