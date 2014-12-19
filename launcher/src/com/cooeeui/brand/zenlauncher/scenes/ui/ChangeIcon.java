
package com.cooeeui.brand.zenlauncher.scenes.ui;

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

public class ChangeIcon extends Dialog {

    private Context mContext;
    private Launcher mLauncher;

    private int mSelect = -1;

    private int[] mRes = {
            R.raw.camera,
            R.raw.contacts,
            R.raw.setting,
            R.raw.dial,
            R.raw.sms,
            R.raw.browser,
    };

    public ChangeIcon(Context context) {
        super(context, R.style.PopupStyle);
        mContext = context;
        mLauncher = (Launcher) context;
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
                mImage.setImageResource(mRes[position]);

            }
        });

        mImage.setImageResource(mRes[0]);
        mText.setText("Change Icon");

        btn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (mSelect != -1) {
                    mLauncher.getSpeedDial().changeIcon(mRes[mSelect]);
                }
                ChangeIcon.this.dismiss();
            }
        });
    }

    private class ImageAdapter extends BaseAdapter {

        public ImageAdapter() {

        }

        @Override
        public int getCount() {
            return mRes.length;
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

            imageView.setImageResource(mRes[position]);

            return imageView;
        }

    }
}
