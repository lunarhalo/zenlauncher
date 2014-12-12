
package com.cooeeui.brand.zenlauncher.scenes.ui;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
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
import com.cooeeui.brand.zenlauncher.scenes.utils.BitmapUtils;

public class ChangeIcon extends Dialog {

    private Context mContext;
    private Launcher mLauncher;
    private ArrayList<Bitmap> mIcons;

    private ArrayList<Bitmap> mAdded;

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
        mIcons = new ArrayList<Bitmap>();
        mAdded = new ArrayList<Bitmap>();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        loadIcon();
        setContentView(R.layout.icon_popup);

        ImageView mImage = (ImageView) findViewById(R.id.title_image);
        TextView mText = (TextView) findViewById(R.id.title_text);
        GridView mGrid = (GridView) findViewById(R.id.gridview);

        mGrid.setAdapter(new ImageAdapter());
        mGrid.setOnItemClickListener(new GridView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mLauncher.getWorkspace().changeIcon(mIcons.get(position), mRes[position]);
                dismiss();
                unLoadIcon(mIcons.get(position));
            }
        });

        mImage.setImageBitmap(mIcons.get(0));
        mText.setText("icon1");
    }

    private void loadIcon() {
        HashMap<Integer, Bitmap> map = mLauncher.getWorkspace().getBuildinIcon();

        for (int i = 0; i < mRes.length; i++) {
            Bitmap b = map.get(mRes[i]);
            if (b == null) {
                b = BitmapUtils.getIcon(mLauncher.getResources(),
                        mRes[i], 144); // modify later
                mAdded.add(b);
            }
            mIcons.add(b);
        }
    }

    private void unLoadIcon(Bitmap b) {
        Bitmap unload;
        for (int i = 0; i < mAdded.size(); i++) {
            unload = mAdded.get(i);
            if (unload != b) {
                unload.recycle();
            }
        }
    }

    private class ImageAdapter extends BaseAdapter {

        public ImageAdapter() {

        }

        @Override
        public int getCount() {
            return mIcons.size();
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

            imageView.setImageBitmap(mIcons.get(position));
            return imageView;
        }

    }
}
