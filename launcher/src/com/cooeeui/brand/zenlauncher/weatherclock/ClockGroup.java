
package com.cooeeui.brand.zenlauncher.weatherclock;

import android.content.Context;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.cooeeui.brand.zenlauncher.R;

public class ClockGroup extends RelativeLayout {
    private ImageView hourOneView = null;
    private ImageView hourTwoView = null;
    private ImageView optionView = null;
    private ImageView minuteOneView = null;
    private ImageView minuteTwoView = null;
    private int oldhourone = -1;
    private int oldhourtwo = -1;
    private int oldminuteone = -1;
    private int oldminutetwo = -1;

    private int[] hourImages = new int[] {
            R.drawable.hour_0, R.drawable.hour_1, R.drawable.hour_2, R.drawable.hour_3,
            R.drawable.hour_4, R.drawable.hour_5, R.drawable.hour_6, R.drawable.hour_7,
            R.drawable.hour_8, R.drawable.hour_9
    };
    private int[] minuteImages = new int[] {
            R.drawable.minute_0, R.drawable.minute_1, R.drawable.minute_2, R.drawable.minute_3,
            R.drawable.minute_4, R.drawable.minute_5, R.drawable.minute_6, R.drawable.minute_7,
            R.drawable.minute_8, R.drawable.minute_9
    };

    public ClockGroup(Context context) {
        super(context);
        hourOneView = new ImageView(context);
        hourTwoView = new ImageView(context);
        optionView = new ImageView(context);
        minuteOneView = new ImageView(context);
        minuteTwoView = new ImageView(context);
        setImageViewByTime(16, 31);
        optionView.setBackgroundResource(R.drawable.zen_option);
        this.addView(hourOneView);
        this.addView(hourTwoView);
        this.addView(optionView);
        this.addView(minuteOneView);
        this.addView(minuteTwoView);

    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        // TODO Auto-generated method stub
        super.onLayout(changed, l, t, r, b);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // TODO Auto-generated method stub
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    public void setChildViewSize(int allWidth, int allHeight) {
        int hourWidth = (int) (allWidth * 0.57f) / 2;
        int hourHeight = (int) (allHeight * 0.95f);
        int optionWidth = (int) (allWidth * 0.04f);
        int optionHeight = (int) (allHeight * 0.37f);
        int optionX = (int) (allWidth * 0.62f);
        int optionY = (int) (allHeight * 0.57f);
        int minuteWidth = (int) (allWidth * 0.28f) / 2;
        int minuteHeight = (int) (allHeight * 0.57f);
        int minuteoneX = (int) (allWidth * 0.69f);
        int minuteY = (int) (allHeight * 0.38f);
        int minuteTwoX = minuteoneX + minuteWidth;
        hourOneView.setX(0);
        hourOneView.setY(0);
        LayoutParams hourlp = new LayoutParams(hourWidth, hourHeight);
        hourOneView.setLayoutParams(hourlp);
        hourTwoView.setX(hourWidth);
        hourTwoView.setY(0);
        hourTwoView.setLayoutParams(hourlp);
        optionView.setX(optionX);
        optionView.setY(optionY);
        LayoutParams optionlp = new LayoutParams(optionWidth, optionHeight);
        optionView.setLayoutParams(optionlp);
        minuteOneView.setX(minuteoneX);
        minuteOneView.setY(minuteY);
        LayoutParams minutelp = new LayoutParams(minuteWidth, minuteHeight);
        minuteOneView.setLayoutParams(minutelp);
        minuteTwoView.setX(minuteTwoX);
        minuteTwoView.setY(minuteY);
        minuteTwoView.setLayoutParams(minutelp);

    }

    public void setImageViewByTime(int hour, int minute) {
        // TODO Auto-generated method stub
        int hourOne = hour / 10;
        int hourTwo = hour % 10;
        int minuteOne = minute / 10;
        int minuteTwo = minute % 10;
        if (oldhourone != hourOne) {
            oldhourone = hourOne;
            hourOneView.setBackgroundResource(hourImages[hourOne]);
        }
        if (oldhourtwo != hourTwo) {
            oldhourtwo = hourTwo;
            hourTwoView.setBackgroundResource(hourImages[hourTwo]);
        }
        if (oldminuteone != minuteOne) {
            oldminuteone = minuteOne;
            minuteOneView.setBackgroundResource(minuteImages[minuteOne]);
        }
        if (oldminutetwo != minuteTwo) {
            oldminutetwo = minuteTwo;
            minuteTwoView.setBackgroundResource(minuteImages[minuteTwo]);
        }
    }
}
