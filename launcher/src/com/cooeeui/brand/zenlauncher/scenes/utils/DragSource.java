
package com.cooeeui.brand.zenlauncher.scenes.utils;

import com.cooeeui.brand.zenlauncher.scenes.ui.BubbleView;
import com.cooeeui.brand.zenlauncher.scenes.utils.DropTarget.DragObject;

public interface DragSource {

    void onDropCompleted(BubbleView target, DragObject d);
}
