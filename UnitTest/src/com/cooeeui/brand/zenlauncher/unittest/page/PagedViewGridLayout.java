
package com.cooeeui.brand.zenlauncher.unittest.page;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.widget.GridLayout;
import android.widget.ImageView;

public class PagedViewGridLayout extends GridLayout {

    private Context mContext;
    private int count = 16;
    private int xCount = 4;

    private static int num;

    public PagedViewGridLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        // TODO Auto-generated constructor stub
        mContext = context;
    }

    public PagedViewGridLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        // TODO Auto-generated constructor stub
        mContext = context;
    }

    public PagedViewGridLayout(Context context) {
        super(context);
        // TODO Auto-generated constructor stub
        mContext = context;
        init();
    }

    private void init() {
        Log.e("launcher123", "- init -");

        int[] c = {
                Color.RED, Color.BLUE, Color.GREEN
        };

        num = (num + 1) % 3;

        for (int i = 0; i < count; i++) {
            ImageView imageView = new ImageView(mContext);
            // imageView.setImageResource(R.drawable.icon);
            imageView.setBackgroundColor(c[num]);
            int ix = i % xCount;
            int iy = i / xCount;
            GridLayout.LayoutParams lp = new GridLayout.LayoutParams(
                    GridLayout.spec(iy, GridLayout.LEFT),
                    GridLayout.spec(ix, GridLayout.TOP));
            lp.width = 144;
            lp.height = 144;
            lp.setGravity(Gravity.TOP | Gravity.START);
            if (ix > 0)
                lp.leftMargin = 30;
            if (iy > 0)
                lp.topMargin = 30;
            addView(imageView, lp);
        }
    }
}
