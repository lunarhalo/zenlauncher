
package com.cooeeui.brand.zenlauncher.scene.drawer;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cooeeui.brand.zenlauncher.R;
import com.cooeeui.brand.zenlauncher.apps.AppInfo;

public class AppIcon extends RelativeLayout {
    private int cellX;
    private int cellY;
    private AppInfo appInfo;

    public int getCellX() {
        return cellX;
    }

    public void setCellX(int cellX) {
        this.cellX = cellX;
    }

    public int getCellY() {
        return cellY;
    }

    public void setCellY(int cellY) {
        this.cellY = cellY;
    }

    public AppInfo getAppInfo() {
        return appInfo;
    }

    public void setAppInfo(AppInfo appInfo) {
        this.appInfo = appInfo;
    }

    public AppIcon(Context context, int iconWidth, int iconHeight, AppInfo info) {
        super(context);
        this.appInfo = info;
        this.setGravity(Gravity.CENTER);
        View iconLayout = View.inflate(context, R.layout.icon_layout, null);
        LayoutParams lp = new LayoutParams(iconWidth, iconHeight);
        iconLayout.setLayoutParams(lp);
        ImageView image = (ImageView) iconLayout.findViewById(R.id.icon_image);
        image.setImageBitmap(info.iconBitmap);
        // set icon text.
        TextView text = (TextView) iconLayout.findViewById(R.id.icon_text);
        text.setText(info.title);
        // set info to icon tag.
        this.setTag(info);
        this.setOnLongClickListener(ZenGridViewUtil.mLauncher);

        this.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                AppInfo i = (AppInfo) v.getTag();
                ZenGridViewUtil.mLauncher.startActivitySafely(i.intent);
            }
        });
        this.addView(iconLayout);
    }

}
