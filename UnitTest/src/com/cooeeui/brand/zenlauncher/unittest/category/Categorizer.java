
package com.cooeeui.brand.zenlauncher.unittest.category;

import java.util.ArrayList;

import com.cooeeui.brand.zenlauncher.unittest.R;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TabHost;
import android.widget.TabWidget;

public class Categorizer extends FragmentActivity {
    TabHost mTabHost;
    ViewPager mViewPager;
    TabsAdapter mTabsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.categorizer);
        mTabHost = (TabHost) findViewById(android.R.id.tabhost);
        mTabHost.setup();

        mViewPager = (ViewPager) findViewById(R.id.pager);

        mTabsAdapter = new TabsAdapter(this, mTabHost, mViewPager);

        // the first tab: all application
        Bundle allBundle = new Bundle();
        allBundle.putInt("categoryId", 0);
        mTabsAdapter.addTab(mTabHost.newTabSpec("All Application").setIndicator("All"),
                AppListFragment.class, allBundle);

        // the second tab: game
        Bundle gameBundle = new Bundle();
        gameBundle.putInt("categoryId", 801);
        mTabsAdapter.addTab(mTabHost.newTabSpec("Game Application").setIndicator("Game"),
                AppListFragment.class, gameBundle);

        if (savedInstanceState != null) {
            mTabHost.setCurrentTabByTag(savedInstanceState.getString("tab"));
        }

        // the third tab: life
        Bundle lifeBundle = new Bundle();
        lifeBundle.putInt("categoryId", 0);
        mTabsAdapter.addTab(mTabHost.newTabSpec("Life Application").setIndicator("Life"),
                AppListFragment.class, lifeBundle);

        if (savedInstanceState != null) {
            mTabHost.setCurrentTabByTag(savedInstanceState.getString("tab"));
        }

        // the fourth tab: social
        Bundle socialBundle = new Bundle();
        socialBundle.putInt("categoryId", 0);
        mTabsAdapter.addTab(mTabHost.newTabSpec("Social Application").setIndicator("Social"),
                AppListFragment.class, socialBundle);

        if (savedInstanceState != null) {
            mTabHost.setCurrentTabByTag(savedInstanceState.getString("tab"));
        }

        // the fifth tab: tool
        Bundle toolBundle = new Bundle();
        toolBundle.putInt("categoryId", 0);
        mTabsAdapter.addTab(mTabHost.newTabSpec("Tool Application").setIndicator("Tool"),
                AppListFragment.class, toolBundle);

        if (savedInstanceState != null) {
            mTabHost.setCurrentTabByTag(savedInstanceState.getString("tab"));
        }
        
        // the sixth tab: setting
        Bundle settingBundle = new Bundle();
        settingBundle.putInt("categoryId", 0);
        mTabsAdapter.addTab(mTabHost.newTabSpec("Setting Application").setIndicator("Setting"),
                AppListFragment.class, settingBundle);

        if (savedInstanceState != null) {
            mTabHost.setCurrentTabByTag(savedInstanceState.getString("tab"));
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("tab", mTabHost.getCurrentTabTag());
    }

    public static class TabsAdapter extends FragmentPagerAdapter
            implements TabHost.OnTabChangeListener, ViewPager.OnPageChangeListener {
        private final Context mContext;
        private final TabHost mTabHost;
        private final ViewPager mViewPager;
        private final ArrayList<TabInfo> mTabs = new ArrayList<TabInfo>();

        static final class TabInfo {
            private final String tag;
            private final Class<?> clss;
            private final Bundle args;

            TabInfo(String _tag, Class<?> _class, Bundle _args) {
                tag = _tag;
                clss = _class;
                args = _args;
            }
        }

        static class DummyTabFactory implements TabHost.TabContentFactory {
            private final Context mContext;

            public DummyTabFactory(Context context) {
                mContext = context;
            }

            @Override
            public View createTabContent(String tag) {
                View v = new View(mContext);
                v.setMinimumWidth(0);
                v.setMinimumHeight(0);
                return v;
            }
        }

        public TabsAdapter(FragmentActivity activity, TabHost tabHost, ViewPager pager) {
            super(activity.getSupportFragmentManager());
            mContext = activity;
            mTabHost = tabHost;
            mViewPager = pager;
            mTabHost.setOnTabChangedListener(this);
            mViewPager.setAdapter(this);
            mViewPager.setOnPageChangeListener(this);
        }

        public void addTab(TabHost.TabSpec tabSpec, Class<?> clss, Bundle args) {
            tabSpec.setContent(new DummyTabFactory(mContext));
            String tag = tabSpec.getTag();

            TabInfo info = new TabInfo(tag, clss, args);
            mTabs.add(info);
            mTabHost.addTab(tabSpec);
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return mTabs.size();
        }

        @Override
        public Fragment getItem(int position) {
            TabInfo info = mTabs.get(position);
            return Fragment.instantiate(mContext, info.clss.getName(), info.args);
        }

        @Override
        public void onTabChanged(String tabId) {
            int position = mTabHost.getCurrentTab();
            mViewPager.setCurrentItem(position);
        }

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        }

        @Override
        public void onPageSelected(int position) {
            // Unfortunately when TabHost changes the current tab, it kindly
            // also takes care of putting focus on it when not in touch mode.
            // The jerk.
            // This hack tries to prevent this from pulling focus out of our
            // ViewPager.
            TabWidget widget = mTabHost.getTabWidget();
            int oldFocusability = widget.getDescendantFocusability();
            widget.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
            mTabHost.setCurrentTab(position);
            widget.setDescendantFocusability(oldFocusability);
        }

        @Override
        public void onPageScrollStateChanged(int state) {
        }
    }
}
