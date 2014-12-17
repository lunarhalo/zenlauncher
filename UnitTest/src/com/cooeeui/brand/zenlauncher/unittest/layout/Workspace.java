
package com.cooeeui.brand.zenlauncher.unittest.layout;

import com.cooeeui.brand.zenlauncher.unittest.R;

import android.app.Activity;
import android.os.Bundle;
import android.view.Window;

public class Workspace extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.workspace_layout);
    }
}
