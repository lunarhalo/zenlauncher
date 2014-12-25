
package com.cooeeui.brand.zenlauncher.scenes.ui;

import android.content.Context;
import android.graphics.Paint;
import android.graphics.Paint.FontMetrics;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.TextView;

public class ZenTextView extends TextView {

    public ZenTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // set text size by height
        int height = MeasureSpec.getSize(heightMeasureSpec);
        setTextSize(TypedValue.COMPLEX_UNIT_PX, getFontSize(height));

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    float getFontSize(float height) {
        Paint paint = new Paint();
        double fontHeight;
        FontMetrics fm;
        
        // first time
        paint.setTextSize(height);
        fm = paint.getFontMetrics();
        fontHeight = Math.ceil(fm.descent - fm.ascent);                                          
        
        // rough value
        height = height * height / (float)fontHeight - 0.5f;
        
        return height;
}
}
