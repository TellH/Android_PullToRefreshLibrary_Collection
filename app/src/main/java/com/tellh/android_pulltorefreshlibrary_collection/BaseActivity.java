package com.tellh.android_pulltorefreshlibrary_collection;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by tlh on 2016/3/1.
 */
public abstract class BaseActivity extends AppCompatActivity {
    public List<String> mList;
    public ListView listView;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutId());
        mList=new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            mList.add(String.valueOf(i));
        }
        listView= (ListView) findViewById(R.id.list_view);
        listView.setAdapter(new SampleAdapter(this, R.layout.list_item, mList));

        initView();
    }

    public void initView() {

    }

    abstract public int getLayoutId();

    class SampleAdapter extends ArrayAdapter<String> {

        private final LayoutInflater mInflater;
        private final List<String> mData;

        public SampleAdapter(Context context, int layoutResourceId, List<String> data) {
            super(context, layoutResourceId, data);
            mData = data;
            mInflater = LayoutInflater.from(context);
        }

        @Override
        public View getView(final int position, View convertView, @NonNull ViewGroup parent) {
            final ViewHolder viewHolder;
            if (convertView == null) {
                viewHolder = new ViewHolder();
                convertView = mInflater.inflate(R.layout.list_item, parent, false);
                viewHolder.tvNum= (TextView) convertView.findViewById(R.id.tv_num);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            viewHolder.tvNum.setText(mData.get(position));
            return convertView;
        }

        class ViewHolder {
            TextView tvNum;
        }

    }
}
