
package com.cooeeui.brand.zenlauncher.searchbar;

import java.util.List;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.cooeeui.brand.zenlauncher.R;

public class SearchBarGroup extends RelativeLayout {
    private Button soundsButton = null;
    private ImageView imSearchBack = null;
    private SearchOnClickListener searchOnClickListener = null;
    private final String soundsButtonName = "soundsButton";
    private final String searchBackName = "searchBack";
    private Intent searchIntent = null;
    private final String searchWeb = "http://www.google.cn/";
    private int oldWidth = -100;
    private int oldHeight = -100;
    private Intent googleSearchIntent = null;
    private final String googleSearchPkg = "com.google.android.googlequicksearchbox";
    private final String googleSearchCls = "com.google.android.googlequicksearchbox.SearchActivity";

    private Intent googleSoundsIntent = null;
    private final String googleSoundsPkg = "com.google.android.googlequicksearchbox";
    private final String googleSoundsCls = "com.google.android.googlequicksearchbox.VoiceSearchActivity";

    public SearchBarGroup(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void initSearchBar() {
        imSearchBack = (ImageView) this.findViewById(R.id.im_search_back);
        soundsButton = (Button) this.findViewById(R.id.soundsButton);
        searchOnClickListener = new SearchOnClickListener();
        imSearchBack.setOnClickListener(searchOnClickListener);
        imSearchBack.setTag(searchBackName);
        soundsButton.setOnClickListener(searchOnClickListener);
        soundsButton.setTag(soundsButtonName);

        Uri uri = Uri.parse(searchWeb);
        searchIntent = new Intent(Intent.ACTION_VIEW, uri);
        searchIntent.addCategory(Intent.CATEGORY_BROWSABLE);

        googleSearchIntent = getIntentByPkgAndCls(googleSearchPkg, googleSearchCls);
        googleSoundsIntent = getIntentByPkgAndCls(googleSoundsPkg, googleSoundsCls);

    }

    private Intent getIntentByPkgAndCls(String pkgName, String clsName) {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        ComponentName cp = new ComponentName(pkgName, clsName);
        intent.setComponent(cp);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        return intent;
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        int width = r - l;
        int height = b - t;
        if (width != oldWidth || height != oldHeight) {
            oldHeight = height;
            oldWidth = width;
        }
    }

    private class SearchOnClickListener implements OnClickListener {

        @Override
        public void onClick(View v) {
            Object tag = v.getTag();
            if (tag instanceof String) {
                String tagName = (String) tag;
                if (tagName.equals(searchBackName)) {
                    startSearchBackClick();
                } else if (tagName.equals(soundsButtonName)) {
                    startSearchSoundsClick();
                }
            }
        }
    }

    /**
     * 处理语音点击事件
     */
    private void startSearchSoundsClick() {
        if (isIntentAvailable(getContext(), googleSoundsIntent)) {
            getContext().startActivity(googleSoundsIntent);
        } else {
            Toast.makeText(getContext(), R.string.sounds_not_install, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 通过该intent查找该应用是否存在于该手机中
     */
    private boolean isIntentAvailable(Context context, Intent intent) {
        if (context == null || intent == null) {
            return false;
        }
        final PackageManager packageManager = context.getPackageManager();
        List<ResolveInfo> list = packageManager.queryIntentActivities(intent,
                PackageManager.GET_ACTIVITIES);
        return list.size() > 0;
    }

    /**
     * 处理点击搜索背景的事件
     */
    private void startSearchBackClick() {
        if (isIntentAvailable(getContext(), googleSearchIntent)) {
            getContext().startActivity(googleSearchIntent);
        } else {
            getContext().startActivity(searchIntent);
        }
    }

}
