
package com.cooeeui.brand.zenlauncher.scenes.ui;

import java.util.ArrayList;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
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

    private int mSelect = -1;

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

        final ImageView mImage = (ImageView) findViewById(R.id.title_image);
        TextView mText = (TextView) findViewById(R.id.title_text);
        GridView mGrid = (GridView) findViewById(R.id.gridview);

        Button btn = (Button) findViewById(R.id.ok);

        mGrid.setAdapter(new ImageAdapter());
        mGrid.setOnItemClickListener(new GridView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mSelect = position;
                mImage.setImageBitmap(mApps.get(position).iconBitmap);
            }
        });

        mImage.setImageBitmap(mApps.get(0).iconBitmap);
        if (mState == ADD_VIEW) {
            mText.setText("Add");
        } else {
            mText.setText("Change");
        }

        btn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (mSelect != -1) {
                    if (mState == ADD_VIEW) {
                        mLauncher.getWorkspace().addBubbleView(mApps.get(mSelect).makeShortcut());
                        mLauncher.getWorkspace().update();
                    } else {
                        mLauncher.getWorkspace().changeBubbleView(
                                mApps.get(mSelect).makeShortcut());
                    }
                }
                PopupDialog.this.dismiss();
            }
        });

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
