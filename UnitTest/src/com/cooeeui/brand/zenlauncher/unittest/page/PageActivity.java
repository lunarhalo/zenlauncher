
package com.cooeeui.brand.zenlauncher.unittest.page;

import android.app.Activity;
import android.os.Bundle;
import android.view.Window;

import com.cooeeui.brand.zenlauncher.unittest.R;

public class PageActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.page);
    }

}
