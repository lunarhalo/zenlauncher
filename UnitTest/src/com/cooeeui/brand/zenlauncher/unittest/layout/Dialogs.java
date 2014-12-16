
package com.cooeeui.brand.zenlauncher.unittest.layout;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.cooeeui.brand.zenlauncher.unittest.R;

public class Dialogs extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.dialogs_layout);

        Button btn = (Button) findViewById(R.id.icon_dialog_button);
        btn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                new IconDialog(Dialogs.this).show();
            }
        });
    }
}
