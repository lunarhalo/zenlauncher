
package com.cooeeui.brand.zenlauncher.unittest.appintent;

import com.cooeeui.brand.zenlauncher.unittest.R;

import android.app.Activity;
import android.os.Bundle;
import android.view.Window;

public class AppIntentActivity extends Activity {

    private final String tellUri = "intent:#Intent;action=android.intent.action.DIAL;end";
    private final String msmUri = "intent:#Intent;action=android.intent.action.MAIN;type=vnd.android-dir/mms-sms;end";
    private final String cameraUri = "intent:#Intent;action=android.intent.action.MAIN;component=com.android.camera/com.android.camera.Camera;end";
    private final String contactUri = "intent:content://com.android.contacts/contacts#Intent;action=android.intent.action.VIEW;end";
    private final String defaultAppUri = "intent:#Intent;action=android.settings.SETTINGS;end";
    private final String cameraName = "camera";
    private final String browserUri = "*BROWSER*";
    private final String defaultAppName = "defaultApp";
    private AppIntentUtil intentUtil = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.appintent_layout);
        intentUtil = new AppIntentUtil(this);
        MyButton tellButton = (MyButton) this.findViewById(R.id.dial);
        MyButton msmButton = (MyButton) this.findViewById(R.id.msm);
        MyButton browserButton = (MyButton) this.findViewById(R.id.browser);
        MyButton cameraButton = (MyButton) this.findViewById(R.id.camera);
        MyButton contactButton = (MyButton) this.findViewById(R.id.contact);
        MyButton defaultButton = (MyButton) this.findViewById(R.id.defaultApp);
        tellButton.setIntent(intentUtil.getIntentByUri(tellUri, "tell"));
        msmButton.setIntent(intentUtil.getIntentByUri(msmUri, "msm"));
        browserButton.setIntent(intentUtil.getIntentByUri(browserUri, "browser"));
        browserButton.setViewName(browserUri);
        cameraButton.setIntent(intentUtil.getIntentByUri(cameraUri, cameraName));
        contactButton.setIntent(intentUtil.getIntentByUri(contactUri, "contact"));
        defaultButton.setIntent(intentUtil.getIntentByUri(defaultAppUri, defaultAppName));
    }
}
