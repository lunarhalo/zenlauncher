
package com.cooeeui.brand.zenlauncher.unittest.category;

import android.os.Bundle;

public class CategorizerClassic extends Categorizer {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Init category helper.
        CategoryHelper.init(getApplicationContext());
    }
}
