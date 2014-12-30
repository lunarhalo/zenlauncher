
package com.cooeeui.brand.zenlauncher.scene.drawer;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.cooeeui.brand.zenlauncher.Launcher;
import com.cooeeui.brand.zenlauncher.R;
import com.cooeeui.brand.zenlauncher.apps.AppInfo;
import com.cooeeui.brand.zenlauncher.category.CategoryData;
import com.cooeeui.brand.zenlauncher.config.GridConfig;
import com.cooeeui.brand.zenlauncher.scenes.ui.ZenGridView;

public class GridFragment extends Fragment {
    private static final String KEY_TAB = "GridFragment:tab";
    private static final String KEY_POSITION = "GridFragment:position";

    int mPosition;
    int mTab;
    BaseAdapter mAdapter;

    public static GridFragment newInstance(int tab, int position) {
        GridFragment fragment = new GridFragment();
        Log.v("suyu", "new grid fragment: " + position);
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
        ZenGridView grid = new ZenGridView(getActivity());
        grid.setHorizontalSpacing(0);
        grid.setVerticalSpacing(0);
        mAdapter = new GridAdapter();
        grid.setAdapter(mAdapter);
        grid.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        Log.v("suyu", "create a new view, position = " + mPosition);

        return grid;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if ((savedInstanceState != null) && savedInstanceState.containsKey(KEY_TAB)
                && savedInstanceState.containsKey(KEY_POSITION)) {
            mTab = savedInstanceState.getInt(KEY_TAB);
            mPosition = savedInstanceState.getInt(KEY_POSITION);
            Log.v("suyu", "get bundle tab: " + mTab + ", position: " + mPosition);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        
        outState.putInt(KEY_TAB, mTab);
        outState.putInt(KEY_POSITION, mPosition);
    }

    public void notifyDataSetChanged() {
        if (mAdapter != null) {
            Log.v("suyu", "GridFragment notifyDataSetChanged");
            mAdapter.notifyDataSetChanged();
        }
    }

    class GridAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            int count = 0;
            int size = CategoryData.getSize(mTab);
            int cpp = GridConfig.getCountPerPageOfDrawer();
            if (size > 0 && cpp > 0) {
                if ((mPosition + 1) * cpp < size) {
                    count = cpp;
                } else {
                    count = size % cpp;
                }
            }
            Log.v("suyu", "count = " + count);
            return count;
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @SuppressLint("NewApi")
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View icon;
            if (convertView == null) {
                icon = View.inflate(GridFragment.this.getActivity(), R.layout.icon_layout, null);
                GridView grid = (GridView) parent;
                icon.setLayoutParams(new GridView.LayoutParams(grid.getColumnWidth(), grid
                        .getColumnWidth()));
                // get app info.
                AppInfo info = CategoryData.datas.get(mTab).get(
                        position + GridConfig.getCountPerPageOfDrawer() * mPosition);
                // set icon image.
                ImageView image = (ImageView) icon.findViewById(R.id.icon_image);
                image.setImageBitmap(info.iconBitmap);
                // set icon text.
                TextView text = (TextView) icon.findViewById(R.id.icon_text);
                text.setText(info.title);
                // set intent
                // icon.setTag(info.intent);
                icon.setTag(info);
                icon.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Intent intent = (Intent) v.getTag();
                        AppInfo i = (AppInfo) v.getTag();
                        // startActivity(i.intent);
                        Launcher l = (Launcher) GridFragment.this.getActivity();
                        l.startActivitySafely(i.intent);
                    }
                });
				//temp for test
                icon.setOnLongClickListener(new OnLongClickListener() {

                    @Override
                    public boolean onLongClick(View v) {
                        AppInfo i = (AppInfo) v.getTag();
                        Launcher l = (Launcher) GridFragment.this.getActivity();
                        l.startApplicationUninstallActivity(i.componentName, i.flags);
                        return true;
                    }
                });
            } else {
                icon = convertView;
            }
            return icon;
        }
    }
}
