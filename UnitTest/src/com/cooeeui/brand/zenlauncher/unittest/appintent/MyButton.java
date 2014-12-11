
package com.cooeeui.brand.zenlauncher.unittest.appintent;

import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class MyButton extends Button {

    private Intent intent = null;
    private String viewName = null;
    private BrowserIntentUtil browserUtil = null;
    private final String browserUri = "*BROWSER*";

    public void setViewName(String viewName) {
        this.viewName = viewName;
    }

    public Intent getIntent() {
        return intent;
    }

    public void setIntent(Intent intent) {
        this.intent = intent;
    }

    public MyButton(final Context context, AttributeSet attrs) {
        super(context, attrs);

        this.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                if (intent == null && browserUri.equals(viewName)) {
                    if (browserUtil == null) {
                        browserUtil = new BrowserIntentUtil(context);
                    }
                    if (browserUtil.browserIntent == null) {
                        browserUtil.createDialog();
                    } else {
                        intent = browserUtil.browserIntent;
                    }
                }
                if (intent != null) {
                    try {
                        context.startActivity(intent);
                    } catch (Exception e) {
                        Log.e("", "startActivity error intent not exit");
                    }

                }

            }
        });
        // TODO Auto-generated constructor stub
    }

}
