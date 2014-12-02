
package com.cooeeui.brand.zenlauncher;

import com.cooeeui.brand.zenlauncher.DropTarget.DragObject;

public interface DragSource {

    void onDropCompleted(BubbleView target, DragObject d);
}
