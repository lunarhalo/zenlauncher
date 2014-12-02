
package com.cooeeui.brand.zenlauncher;

import android.graphics.Rect;

public interface DropTarget {

    class DragObject {

        public BubbleView dragView = null;

        public DragSource dragSource = null;

        public DragObject() {
        }
    }

    void onDrop(DragObject dragObject);

    void onDragEnter(DragObject dragObject);

    void onDragOver(DragObject dragObject);

    void onDragExit(DragObject dragObject);

    void getHitRectRelativeToDragLayer(Rect outRect);

}
