
package com.cooeeui.brand.zenlauncher.scene.drawer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.cooeeui.brand.zenlauncher.Launcher;
import com.cooeeui.brand.zenlauncher.config.GridConfig;
import com.cooeeui.brand.zenlauncher.config.IconConfig;
import com.cooeeui.brand.zenlauncher.scenes.ui.ZenGridView;

import android.graphics.Point;

public class ZenGridViewUtil {
    private static int mDefaultColumns = 4;
    private static int mColumns;
    private static int mLines;
    public static int mAllWidth = -1;
    public static int mAllHeight = -1;
    public static int mIconWidthSize;
    public static int mIconHeightSize;
    public static List<Point> mAllPositionList = null;
    public static Point[][] mAllPositions = null;// 所有的gridview都是拥有相同的坐标布局，所以使用static只统计一次
    public static Launcher mLauncher = null;
    public static HashMap<String, List<ZenGridView>> mAllZenGridViews = new HashMap<String, List<ZenGridView>>();
    private static List<String> keys = new ArrayList<String>();

    public static void initData() {
        if (mAllPositions == null) {
            mAllPositionList = new ArrayList<Point>();
            mColumns = mDefaultColumns;
            int maxWidth = IconConfig.iconSizeMax;
            int minWidth = IconConfig.iconSizeMin;
            if (mAllWidth > maxWidth * mColumns) {
                mColumns = mAllWidth / maxWidth;
            } else if (mAllWidth < minWidth * mColumns) {
                mColumns = mAllWidth / minWidth;
            }
            mIconWidthSize = mAllWidth / mColumns;
            mLines = mAllHeight / mIconWidthSize;
            mIconHeightSize = (mAllHeight - mLines * mIconWidthSize) / mLines + mIconWidthSize;
            GridConfig.setCountPerPageOfDrawer(mColumns * mLines);
            mAllPositions = new Point[mColumns][mLines];
            for (int celly = 0; celly < mLines; celly++) {
                for (int cellx = 0; cellx < mColumns; cellx++) {
                    int px = cellx * mIconWidthSize;
                    int py = celly * mIconHeightSize;
                    Point point = new Point(px, py);
                    mAllPositions[cellx][celly] = point;
                    mAllPositionList.add(point);
                }
            }
        }
    }

    public static int[] getCellXAndCellY(Point point) {
        int[] cell = new int[2];
        for (int celly = 0; celly < mLines; celly++) {
            for (int cellx = 0; cellx < mColumns; cellx++) {
                Point oldPoint = mAllPositions[cellx][celly];
                if (point.x == oldPoint.x && point.y == oldPoint.y) {
                    cell[0] = cellx;
                    cell[1] = celly;
                    return cell;
                }
            }
        }
        return cell;
    }

    public static ZenGridView getZenGridViewByKey(int tabNum) {
        ZenGridView zenGridView = null;
        int cpp = GridConfig.getCountPerPageOfDrawer();
        String keyValue = tabNum + "";
        List<ZenGridView> zenlist = mAllZenGridViews.get(keyValue);
        for (int i = 0; i < zenlist.size(); i++) {
            zenGridView = zenlist.get(i);
            if (zenGridView.getChildCount() < cpp) {
                return zenGridView;
            }
        }
        // 还有一种情况，当当前的所有gridView都满的时候，此时要增加新一页的GridView
        return null;
    }

    public static void addAllZenGridView(ZenGridView zenGridView, int tab, int position) {
        String keyValue = tab + "";
        if (keys.contains(keyValue)) {
            List<ZenGridView> zenlist = mAllZenGridViews.get(keyValue);
            zenlist.add(position, zenGridView);
        } else {
            keys.add(keyValue);
            List<ZenGridView> zenlist = new ArrayList<ZenGridView>();
            zenlist.add(position, zenGridView);
            mAllZenGridViews.put(keyValue, zenlist);
        }
    }
}
