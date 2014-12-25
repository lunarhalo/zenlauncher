
package com.cooeeui.brand.zenlauncher.scene.drawer;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.cooeeui.brand.zenlauncher.R;
import com.viewpagerindicator.PageIndicator;
import com.viewpagerindicator.UnderlinePageIndicator;

public class AppListViewGroup extends FrameLayout {
    PageAdapter mAdapter;
    ViewPager mPager;
    PageIndicator mIndicator;
    Context mContext;

    public AppListViewGroup(Context context, AttributeSet attrs) {
        super(context, attrs);

        mContext = context;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        FragmentActivity activity = (FragmentActivity) mContext;
        mAdapter = new PageAdapter(activity.getSupportFragmentManager());

        mPager = (ViewPager) findViewById(R.id.pager);
        mPager.setAdapter(mAdapter);

        mIndicator = (UnderlinePageIndicator) findViewById(R.id.indicator);
        mIndicator.setViewPager(mPager);
    }

    class PageAdapter extends FragmentPagerAdapter {
        int mCount = 2;

        public PageAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return GridFragment.newInstance(position);
        }

        @Override
        public int getCount() {
            return mCount;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            Log.v("suyu", "instantiateItem: " + position);
            GridFragment fragment = (GridFragment) super.instantiateItem(container, position);
            if (fragment != null) {
                fragment.notifyDataSetChanged();
            }
            return fragment;
        }

        @Override
        public int getItemPosition(Object object) {
            Log.v("suyu", "getItemPosition: " + object);
            GridFragment fragment = (GridFragment) object;
            if (fragment != null) {
                fragment.notifyDataSetChanged();
            }
            return super.getItemPosition(object);
        }

        public void setCount(int count) {
            if (count > 0) {
                mCount = count;
                notifyDataSetChanged();
            }
        }
    }

    public void notifyDataSetChanged() {
        Log.v("suyu", "AppListViewGroup notifyDataSetChanged");
        mAdapter.notifyDataSetChanged();
    }

    public void changeTextView(String name) {
        // TODO: under constructing.
        Log.v("suyu", "name = " + name);
    }

    /**
     * 重新分类
     */
    public void classifyApp() {
        // TODO Auto-generated method stub
        Log.v("", "popmenu classifyApp");
    }

    /**
     * 卸载应用
     */
    public void unloadApp() {
        // TODO Auto-generated method stub
        Log.v("", "popmenu unloadApp");
    }

    /**
     * 隐藏图标
     */
    public void hideIcon() {
        // TODO Auto-generated method stub
        Log.v("", "popmenu hideIcon");
    }

    /**
     * 开启Zen设置
     */
    public void ZenSettings() {
        // TODO Auto-generated method stub
        Log.v("", "popmenu ZenSettings");
    }
}
