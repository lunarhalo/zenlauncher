
package com.cooeeui.brand.zenlauncher.weatherclock;

import android.content.Context;
import android.graphics.Color;
import android.util.TypedValue;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class DateGroup extends RelativeLayout {

    private TextView weekTextView = null;
    private TextView mouthTextView = null;
    private TextView dateTextView = null;
    private int oldWeek = -1;
    private int oldMouth = -1;
    private int oldDay = -1;
    // private ImageView dateOneView = null;
    // private ImageView dateTwoView = null;
    private String[] weeks = new String[] {
            "Sunday", "Monday", "Tuesday", "Wednesday", "Thurday", "Friday", "Saturday"
    };
    private String[] mouths = new String[] {
            "Jan.", "Feb.", "Mar.", "Apr.", "May.", "Jun.", "Jul.", "Aug.", "Sep.", "Oct.", "Nov.",
            "Dec."
    };

    // private int[] dates = new int[] {
    // R.drawable.minute_0, R.drawable.minute_1, R.drawable.minute_2,
    // R.drawable.minute_3,
    // R.drawable.minute_4, R.drawable.minute_5, R.drawable.minute_6,
    // R.drawable.minute_7,
    // R.drawable.minute_8, R.drawable.minute_9
    // };

    public DateGroup(Context context) {
        super(context);
        weekTextView = new TextView(context);
        weekTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        weekTextView.setTextColor(Color.WHITE);
        mouthTextView = new TextView(context);
        mouthTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        mouthTextView.setTextColor(Color.WHITE);
        dateTextView = new TextView(context);
        dateTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        dateTextView.setTextColor(Color.WHITE);
        // dateOneView = new ImageView(context);
        // dateTwoView = new ImageView(context);
        // this.addView(dateOneView);
        // this.addView(dateTwoView);
        this.addView(dateTextView);
        this.addView(mouthTextView);
        this.addView(weekTextView);
        setWeekTextView(1);
        setMouthTextView(10);
        setDateImage(10);
    }

    public void setDateImage(int date) {
        // TODO Auto-generated method stub
        if (oldDay != date) {
            oldDay = date;
            dateTextView.setText("" + date);
        }
        // int dateOne = date / 10;
        // int dateTwo = date % 10;
        // dateOneView.setBackgroundResource(dates[dateOne]);
        // dateTwoView.setBackgroundResource(dates[dateTwo]);

        // dateOneView.setImageResource(dates[dateOne]);
        // dateTwoView.setImageResource(dates[dateTwo]);
    }

    public void setMouthTextView(int mouth) {
        // TODO Auto-generated method stub
        if (oldMouth != mouth) {
            oldMouth = mouth;
            mouthTextView.setText(mouths[mouth]);
        }
    }

    public void setWeekTextView(int week) {
        // TODO Auto-generated method stub
        if (oldWeek != week) {
            oldWeek = week;
            weekTextView.setText(weeks[week]);
        }
    }

    public void setChildViewSize(int allWidth, int allHeight) {
        int weekX = (int) (allWidth * 0.04f);
        int weekY = (int) (allHeight * 0.42f);
        int weekWidth = allWidth;
        int weekHeight = (int) (allHeight * 0.25f);
        int mouthX = weekX;
        int mouthY = weekY + weekHeight;
        int mouthWidth = (int) (allWidth * 0.56f);
        int mouthHeight = weekHeight;
        int dateOneX = (int) (allWidth * 0.47f);
        int dateY = mouthY;
        int dateWidth = (int) (allWidth * 0.13f);
        int dateHeght = (int) (allHeight * 0.32f);
        int dateTwoX = dateOneX + dateWidth;
        weekTextView.setX(weekX);
        weekTextView.setY(weekY);
        LayoutParams weeklp = new LayoutParams(weekWidth, weekHeight);
        weekTextView.setLayoutParams(weeklp);
        mouthTextView.setX(mouthX);
        mouthTextView.setY(mouthY);
        LayoutParams mouthlp = new LayoutParams(mouthWidth, mouthHeight);
        mouthTextView.setLayoutParams(mouthlp);
        dateTextView.setX(dateOneX);
        dateTextView.setY(dateY);
        LayoutParams datelp = new LayoutParams(allWidth, dateHeght);
        dateTextView.setLayoutParams(datelp);
        // dateOneView.setX(dateOneX);
        // dateOneView.setY(dateY);
        // LayoutParams datelp = new LayoutParams(dateWidth, dateHeght);
        // dateOneView.setLayoutParams(datelp);
        // dateTwoView.setX(dateTwoX);
        // dateTwoView.setY(dateY);
        // dateTwoView.setLayoutParams(datelp);
    }
}
