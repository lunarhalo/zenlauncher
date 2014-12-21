
package com.cooeeui.brand.zenlauncher.unittest.layout;

import com.cooeeui.brand.zenlauncher.unittest.R;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;

public class IconDialog extends Dialog {

    GridView grid;

    public IconDialog(Context context) {
        super(context, R.style.DialogStyle);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.icon_dialog_layout);

        findViewById(R.id.icon_dialog_base).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                IconDialog.this.dismiss();
            }
        });
        findViewById(R.id.icon_dialog_content).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // do nothing.
            }
        });

        grid = (GridView) findViewById(R.id.gridview);
        grid.setAdapter(new MyAdapter());
    }

    class MyAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return 20;
        }

        @Override
        public Object getItem(int arg0) {
            return null;
        }

        @Override
        public long getItemId(int arg0) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View icon;
            if (convertView == null) {
                icon = View.inflate(IconDialog.this.getContext(), R.layout.icon_layout, null);
                int width = IconDialog.this.grid.getColumnWidth();
                icon.setLayoutParams(new GridView.LayoutParams(width, width));
            } else {
                icon = convertView;
            }
            return icon;
        }
    }
}
