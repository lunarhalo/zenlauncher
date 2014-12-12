
package com.cooeeui.brand.zenlauncher.scenes.ui;

import java.util.ArrayList;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.cooeeui.brand.zenlauncher.Launcher;
import com.cooeeui.brand.zenlauncher.R;
import com.cooeeui.brand.zenlauncher.apps.AppInfo;

public class PopupDialog extends Dialog {

    public static final int ADD_VIEW = 0;
    public static final int CHANGE_VIEW = 1;
    private int mState;

    private Context mContext;
    private Launcher mLauncher;
    private ArrayList<AppInfo> mApps;

    public PopupDialog(Context context, int state) {
        super(context, R.style.PopupStyle);
        mContext = context;
        mState = state;
        mLauncher = (Launcher) context;
        mApps = mLauncher.getApps();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.icon_popup);

        ImageView mImage = (ImageView) findViewById(R.id.title_image);
        TextView mText = (TextView) findViewById(R.id.title_text);
        GridView mGrid = (GridView) findViewById(R.id.gridview);

        mGrid.setAdapter(new ImageAdapter());
        mGrid.setOnItemClickListener(new GridView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (mState == ADD_VIEW) {
                    mLauncher.getWorkspace().addBubbleView(mApps.get(position).makeShortcut());
                    mLauncher.getWorkspace().update();
                } else {
                    mLauncher.getWorkspace().changeBubbleView(mApps.get(position).makeShortcut());
                }

                dismiss();
            }
        });

        mImage.setImageBitmap(mApps.get(0).iconBitmap);
        mText.setText("icon1");
    }

    private class ImageAdapter extends BaseAdapter {

        public ImageAdapter() {

        }

        @Override
        public int getCount() {
            return mApps.size();
        }

        @Override
        public Object getItem(int position) {
            return position;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ImageView imageView;
            if (convertView == null)
            {
                imageView = new ImageView(mContext);
                imageView.setLayoutParams(new GridView.LayoutParams(144, 144)); //
                imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
            }
            else
            {
                imageView = (ImageView) convertView;
            }

            imageView.setImageBitmap(mApps.get(position).iconBitmap);
            return imageView;
        }

    }
}
