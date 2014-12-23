
package com.cooeeui.brand.zenlauncher.searchbar;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.net.Uri;
import android.speech.RecognizerIntent;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cooeeui.brand.zenlauncher.Launcher;
import com.cooeeui.brand.zenlauncher.R;
import com.cooeeui.brand.zenlauncher.Workspace;

public class SearchBarGroup extends RelativeLayout {
    private EditText searchText = null;
    private Button soundsButton = null;
    private SearchOnClickListener searchOnClickListener = null;
    private final String searchTextName = "searchText";
    private final String soundsButtonName = "soundsButton";
    private String textvalue = "";
    private Intent searchIntent = null;
    private final String searchWeb = "http://www.baidu.com/";
    private String searchBegin = "http://www.baidu.com/s?from=1671a&word=";
    private String searchEnd = "";
    private SoundsUtils soundsUtils = null;
    private Activity mainActivity = null;
    private SearchUtils searchUtils = null;
    private View frontView = null;
    private int oldWidth = -100;
    private int oldHeight = -100;

    public SearchBarGroup(Context context, AttributeSet attrs) {
        super(context, attrs);
        // TODO Auto-generated constructor stub
    }

    public void setSearchUtils(SearchUtils searchUtils) {
        this.searchUtils = searchUtils;
    }

    public void initSearchBar() {
        // TODO Auto-generated method stub

        searchText = (EditText) this.findViewById(R.id.searchText);
        soundsButton = (Button) this.findViewById(R.id.soundsButton);
        searchOnClickListener = new SearchOnClickListener();

        searchText.setOnClickListener(searchOnClickListener);
        searchText.setTag(searchTextName);
        searchText.addTextChangedListener(search_TextChanged);
        searchText.setEnabled(false);

        soundsButton.setOnClickListener(searchOnClickListener);
        soundsButton.setTag(soundsButtonName);
        soundsUtils = new SoundsUtils(mainActivity);
        if (!soundsUtils.isSupportSounds()) {
            soundsButton.setEnabled(false);
        }
        frontView = new View(getContext());
        this.addView(frontView);
        frontView.setTag("frontView");
        frontView.setOnClickListener(searchOnClickListener);
        // frontView.setBackgroundColor(Color.YELLOW);

        Uri uri = Uri.parse(searchWeb);
        searchIntent = new Intent(Intent.ACTION_VIEW, uri);
        searchIntent.addCategory(Intent.CATEGORY_BROWSABLE);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        // TODO Auto-generated method stub
        super.onLayout(changed, l, t, r, b);
        int width = r - l;
        int height = b - t;
        if (width != oldWidth || height != oldHeight) {
            oldHeight = height;
            oldWidth = width;
            setfrontViewdata();
        }
    }

    /**
     * 将frontView完全覆盖到searchText上
     */

    private void setfrontViewdata() {
        // TODO Auto-generated method stub
        frontView.setX(searchText.getX());
        frontView.setY(searchText.getY());
        LayoutParams frontLp = new LayoutParams(searchText.getWidth(), searchText.getHeight());
        frontView.setLayoutParams(frontLp);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // TODO Auto-generated method stub
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    /**
     * 通过输入的text输入
     * 
     * @param textvalue
     */
    public void searchByText(String textvalue) {
        // TODO Auto-generated method stub
        String uriString = searchBegin + textvalue + searchEnd;
        Uri uri = Uri.parse(uriString);
        searchIntent.setData(uri);
        getContext().startActivity(searchIntent);
    }

    /**
     * 通过语音搜索
     */
    private void searchBySounds() {
        // TODO Auto-generated method stub
        soundsUtils.startVoiceRecognitionActivity();
    }

    public String getSearchData() {
        String data = null;
        if (searchText != null && searchText.getText() != null) {
            data = searchText.getText().toString();
        }
        return data;
    }

    private class SoundsUtils {
        private Activity mainActivity = null;

        public SoundsUtils(Activity mainActivity) {
            this.mainActivity = mainActivity;
        }

        /**
         * 判断该手机是否支持语音搜索
         * 
         * @return
         */
        public boolean isSupportSounds() {
            PackageManager pm = mainActivity.getPackageManager();
            List<ResolveInfo> activities = pm.queryIntentActivities(new Intent(
                    RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0);
            if (activities.size() > 0) {
                return true;
            }
            return false;
        }

        public void startVoiceRecognitionActivity() {
            try {
                Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                        RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "请对着麦克风说话！");
                mainActivity.startActivityForResult(intent,
                        Launcher.VOICE_RECOGNITION_REQUEST_CODE);
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(mainActivity, "找不到语音设备", Toast.LENGTH_SHORT).show();
            }
        }

    }

    private TextWatcher search_TextChanged = new TextWatcher() {

        @Override
        public void afterTextChanged(Editable s) {

            if (TextUtils.isEmpty(s)) {
                searchText.setCursorVisible(false);
            }
            Log.v("", "whj afterTextChanged s is " + s);

            // searchText.setCompoundDrawablesWithIntrinsicBounds(null, null,
            // _clear_drawable, null);

        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count,
                int after) {
            Log.v("", "whj beforeTextChanged s is " + s + " count is " + count + " start is "
                    + start);
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before,
                int count) {
            Log.v("", "whj onTextChanged s is " + s + " count is " + count + " start is " + start
                    + " before is " + before);
        }
    };

    private OnTouchListener search_OnTouch = new OnTouchListener() {

        @Override
        public boolean onTouch(View v, MotionEvent event) {

            switch (event.getAction()) {

                case MotionEvent.ACTION_UP: {

                    String data = getSearchData();
                    if (TextUtils.isEmpty(data)) {
                        return false;
                    }
                }
                default: {
                    break;
                }

            }
            return false;

        }
    };

    private class SearchOnClickListener implements OnClickListener {

        @Override
        public void onClick(View v) {
            // TODO Auto-generated method stub
            Object tag = v.getTag();
            if (tag instanceof String) {
                String tagName = (String) tag;
                if ("frontView".equals(tagName)) {
                    searchUtils.startSearchAnim();
                }
                if (searchUtils.isSearchState) {
                    if (searchTextName.equals(tagName)) {
                        textvalue = getSearchData();
                        if (textvalue != null) {
                            if (textvalue.equals("")) {
                                searchText.setCursorVisible(true);
                            } else {
                                searchByText(textvalue);
                            }
                        }
                    } else if (soundsButtonName.equals(tagName)) {
                        searchBySounds();
                    }
                }
            }
        }
    }

    public void setActivity(Activity mainActivity) {
        // TODO Auto-generated method stub
        this.mainActivity = mainActivity;
    }

    public void startSearchBar() {
        // TODO Auto-generated method stub
        searchText.setEnabled(true);
        searchText.setCursorVisible(true);
        this.removeView(frontView);
    }

    public void stopSearchBar() {
        // TODO Auto-generated method stub
        clearValue();
        searchText.setEnabled(false);
        this.addView(frontView);
    }

    public void clearValue() {
        // TODO Auto-generated method stub
        searchText.setText("");
        searchText.setCursorVisible(true);
    }

}
