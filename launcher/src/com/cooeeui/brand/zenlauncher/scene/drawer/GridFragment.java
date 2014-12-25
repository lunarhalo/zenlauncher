
package com.cooeeui.brand.zenlauncher.scene.drawer;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.cooeeui.brand.zenlauncher.R;
import com.cooeeui.brand.zenlauncher.apps.AppInfo;
import com.cooeeui.brand.zenlauncher.category.CategoryData;
import com.cooeeui.brand.zenlauncher.scenes.ui.ZenGridView;

public class GridFragment extends Fragment {
    private static final String KEY_POSITION = "GridFragment:position";

    int mPosition;
    BaseAdapter mAdapter;

    public static GridFragment newInstance(int position) {
        GridFragment fragment = new GridFragment();
        Log.v("suyu", "new grid fragment: " + position);
        Bundle args = new Bundle();
        args.putInt(KEY_POSITION, position);
        fragment.setArguments(args);
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

        if ((savedInstanceState != null) && savedInstanceState.containsKey(KEY_POSITION)) {
            mPosition = savedInstanceState.getInt(KEY_POSITION);
            Log.v("suyu", "get bundle position: " + mPosition);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
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
            View view = GridFragment.this.getView();
            Log.v("suyu", "grid get count: " + view);
            if (view == null || !(view instanceof ZenGridView)) {
                return 0;
            } else {
                ZenGridView grid = (ZenGridView) view;
                return grid.getCountPerPage();
            }
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View icon;
            if (convertView == null) {
                icon = View.inflate(GridFragment.this.getActivity(), R.layout.icon_layout, null);
                GridView grid = (GridView) parent;
                icon.setLayoutParams(new GridView.LayoutParams(grid.getColumnWidth(), grid
                        .getColumnWidth()));
                // get app info.
                AppInfo info = CategoryData.datas.get(0).get(position);
                // set icon image.
                ImageView image = (ImageView) icon.findViewById(R.id.icon_image);
                image.setImageBitmap(info.iconBitmap);
                // set icon text.
                TextView text = (TextView) icon.findViewById(R.id.icon_text);
                text.setText(info.title);
            } else {
                icon = convertView;
            }
            return icon;
        }
    }
}
