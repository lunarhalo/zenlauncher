
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
import com.cooeeui.brand.zenlauncher.category.CategoryData;
import com.cooeeui.brand.zenlauncher.category.CategoryHelper;
import com.cooeeui.brand.zenlauncher.config.GridConfig;
import com.viewpagerindicator.UnderlinePageIndicator;

public class AppListViewGroup extends FrameLayout {
    PageAdapter mAdapters[];
    Context mContext;
    int mTab;

    public AppListViewGroup(Context context, AttributeSet attrs) {
        super(context, attrs);

        mContext = context;
        // TODO: should we read it from preference?
        mTab = 0;

        mAdapters = new PageAdapter[CategoryHelper.COUNT];
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        FragmentActivity activity = (FragmentActivity) mContext;
        for (int i = 0; i < mAdapters.length; i++) {
            mAdapters[i] = new PageAdapter(activity.getSupportFragmentManager(), i);

            ViewPager pager = (ViewPager) findViewById(R.id.pager_0 + 2 * i);
            pager.setAdapter(mAdapters[i]);

            UnderlinePageIndicator indicator = (UnderlinePageIndicator) findViewById(R.id.indicator_0
                    + 2 * i);
            indicator.setViewPager(pager);
        }

        setTab(mTab);
    }

    class PageAdapter extends FragmentPagerAdapter {
        int mCount;
        int mTab;

        public PageAdapter(FragmentManager fm, int tab) {
            super(fm);

            mCount = 1;
            mTab = tab;
        }

        @Override
        public Fragment getItem(int position) {
            return GridFragment.newInstance(mTab, position);
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

        public int getTab() {
            return mTab;
        }
    }

    public void notifyDataSetChanged() {
        Log.v("suyu", "AppListViewGroup notifyDataSetChanged");
        for (int i = 0; i < mAdapters.length; i++) {
            mAdapters[i].notifyDataSetChanged();
            // calculate page count
            int cpp = GridConfig.getCountPerPageOfDrawer();
            if (cpp != 0 && CategoryData.datas != null) {
                int pageCount = (CategoryData.datas.get(i).size() + cpp) / cpp;
                mAdapters[i].setCount(pageCount);
            }
        }
        invalidate();
    }

    public void setTab(int tab) {
        mTab = tab;

        for (int i = 0; i < CategoryHelper.COUNT; i++) {
            if (i != mTab) {
                getChildAt(i).setVisibility(View.INVISIBLE);
            } else {
                getChildAt(i).setVisibility(View.VISIBLE);
            }
        }
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
