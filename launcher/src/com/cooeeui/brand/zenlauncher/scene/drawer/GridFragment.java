
package com.cooeeui.brand.zenlauncher.scene.drawer;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;

import com.cooeeui.brand.zenlauncher.category.CategoryData;
import com.cooeeui.brand.zenlauncher.scenes.ui.ZenGridView;

public class GridFragment extends Fragment {
    private static final String KEY_TAB = "GridFragment:tab";
    private static final String KEY_POSITION = "GridFragment:position";
    private ZenGridView mGridView = null;
    int mPosition;
    int mTab;

    public static GridFragment newInstance(int tab, int position) {
        GridFragment fragment = new GridFragment();
        Log.e("suyu", "create a new view grid fragment: " + position +
                " tab is " + tab);
        Bundle args = new Bundle();
        args.putInt(KEY_TAB, tab);
        args.putInt(KEY_POSITION, position);
        fragment.setArguments(args);
        fragment.mTab = tab;
        fragment.mPosition = position;
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (mGridView == null) {
            mGridView = getmGridView(ZenGridViewUtil.mLauncher, mTab, mPosition);
        }
        return mGridView;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if ((savedInstanceState != null) && savedInstanceState.containsKey(KEY_TAB)
                && savedInstanceState.containsKey(KEY_POSITION)) {
            mTab = savedInstanceState.getInt(KEY_TAB);
            mPosition = savedInstanceState.getInt(KEY_POSITION);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putInt(KEY_TAB, mTab);
        outState.putInt(KEY_POSITION, mPosition);
    }

    public void notifyDataSetChanged() {
        if (CategoryData.getSize(mTab) > 0) {
            if (mGridView == null) {
                mGridView = getmGridView(ZenGridViewUtil.mLauncher, mTab, mPosition);
            }
            if (mGridView.getChildCount() == 0) {
                mGridView.addChildView();
            }
        }

    }

    private ZenGridView getmGridView(Activity mLauncher, int tab, int position) {
        ZenGridView gridView = new ZenGridView(mLauncher, tab, position);
        gridView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT));
        return gridView;
    }

}
