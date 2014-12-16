
package com.cooeeui.brand.zenlauncher.unittest.layout;

import com.cooeeui.brand.zenlauncher.unittest.R;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;

public class IconDialog extends Dialog {

    public IconDialog(Context context) {
        super(context, R.style.PopupStyle);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.icon_dialog_layout);
    }
}
