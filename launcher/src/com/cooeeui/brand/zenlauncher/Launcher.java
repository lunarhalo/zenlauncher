
package com.cooeeui.brand.zenlauncher;

import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;

import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.AlphaAnimation;
import android.view.WindowManager;
import android.widget.Toast;

import com.cooeeui.brand.zenlauncher.appIntentUtils.AppIntentUtil;
import com.cooeeui.brand.zenlauncher.apps.AppInfo;
import com.cooeeui.brand.zenlauncher.apps.IconCache;
import com.cooeeui.brand.zenlauncher.apps.ItemInfo;
import com.cooeeui.brand.zenlauncher.apps.ShortcutInfo;
import com.cooeeui.brand.zenlauncher.category.CategoryData;
import com.cooeeui.brand.zenlauncher.config.IconConfig;
import com.cooeeui.brand.zenlauncher.debug.Logger;
import com.cooeeui.brand.zenlauncher.scene.drawer.AppListUtil;
import com.cooeeui.brand.zenlauncher.scenes.Drawer;
import com.cooeeui.brand.zenlauncher.scenes.LoadingView;
import com.cooeeui.brand.zenlauncher.scenes.SpeedDial;
import com.cooeeui.brand.zenlauncher.scenes.Workspace;
import com.cooeeui.brand.zenlauncher.scenes.ZenSetting;
import com.cooeeui.brand.zenlauncher.scenes.ui.BubbleView;
import com.cooeeui.brand.zenlauncher.scenes.ui.ChangeIcon;
import com.cooeeui.brand.zenlauncher.scenes.ui.PopupDialog;
import com.cooeeui.brand.zenlauncher.scenes.utils.DragController;
import com.cooeeui.brand.zenlauncher.scenes.utils.DragLayer;
import com.cooeeui.brand.zenlauncher.searchbar.SearchBarGroup;
import com.cooeeui.brand.zenlauncher.searchbar.SearchUtils;
import com.cooeeui.brand.zenlauncher.weatherclock.WeatherClockGroup;

public class Launcher extends FragmentActivity implements View.OnClickListener,
        OnLongClickListener,
        LauncherModel.Callbacks {

    public static final String TAG = "Launcher";
    public static final String EXTRA_SHORTCUT_DUPLICATE = "duplicate";

    SharedPreferences mSharedPrefs;
    LauncherModel mModel;
    IconCache mIconCache;

    private SpeedDial mSpeedDial;
    private DragLayer mDragLayer;
    private Workspace mWorkspace;
    private View mBackground;
    private Drawer mDrawer;
    private DragController mDragController;
    private ArrayList<AppInfo> mApps;
    private WeatherClockGroup mWeather;

    private GestureDetector mGestureDetector;

    private Dialog mLoading;
    private boolean mOnResumeNeedsLoad;
    private boolean mPaused = true;
    private ArrayList<Runnable> mBindOnResumeCallbacks = new ArrayList<Runnable>();
    private SearchBarGroup mSearchBarGroup = null;
    private SearchUtils mSearchUtils = null;
    public static final int VOICE_RECOGNITION_REQUEST_CODE = 1234;

    private ValueAnimator mAnimator;
    private float mAnimatorValue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        IconConfig.init(this);

        LauncherAppState.setApplicationContext(getApplicationContext());
        LauncherAppState app = LauncherAppState.getInstance();
        LauncherAppState.setAppIntentUtil(new AppIntentUtil(this));// 将appIntent的帮助类传递到AppState
        // the LauncherApplication should call this, but in case of
        // instrumentation it might not be present yet
        mSharedPrefs = getSharedPreferences(
                LauncherAppState.getSharedPreferencesKey(),
                Context.MODE_PRIVATE);
        mModel = app.setLauncher(this);
        mIconCache = app.getIconCache();
        mDragController = new DragController(this);
        mPaused = false;

        setContentView(R.layout.launcher);

        mDragLayer = (DragLayer) findViewById(R.id.drag_layer);
        mDragLayer.setup(this, mDragController);

        mSpeedDial = (SpeedDial) mDragLayer.findViewById(R.id.speed_dial);
        mSpeedDial.setup(this, mDragController);
        mSpeedDial.setOnClickListener(this);

        mWorkspace = (Workspace) findViewById(R.id.workspace);
        mWorkspace.setOnClickListener(this);

        mWeather = (WeatherClockGroup) findViewById(R.id.weatherclock);
        mWeather.setup(this);

        mDrawer = (Drawer) findViewById(R.id.appHostGroup);
        mDrawer.setup(this);
        AppListUtil util = new AppListUtil(this);
        mDrawer.setUtil(util);
        mDrawer.initViewData();

        mBackground = findViewById(R.id.background);

        mGestureDetector = new GestureDetector(this, new LauncherGestureLisenter());

        mAnimator = ValueAnimator.ofFloat(0, 1);
        mAnimator.setDuration(getResources().getInteger(
                R.integer.swipe_animation_duration));
        mAnimator.addUpdateListener(new AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                Float value = (Float) animation.getAnimatedValue();
                mWorkspace.setAlpha(1.0f - value);
                mWorkspace.setPivotX(mWorkspace.getWidth() * 0.5f);
                mWorkspace.setPivotY(mWorkspace.getHeight() * 0.5f);
                mWorkspace.setScaleX(1.0f - 0.8f * value);
                mWorkspace.setScaleY(1.0f - 0.8f * value);

                mDrawer.setTranslationY(mDrawer.getHeight() * (1 - value));
                mDrawer.setVisibility(View.VISIBLE);
                mAnimatorValue = value;

                float alpha = 0.75f * value * 255;
                mBackground.setBackgroundColor(Color.argb((int) alpha, 0, 0, 0));
            }
        });
        mAnimatorValue = 0.0f;

        mSearchBarGroup = (SearchBarGroup) this.findViewById(R.id.search_bar);
        mSearchUtils = new SearchUtils(this, mWeather, mSpeedDial, mSearchBarGroup);
        mSearchBarGroup.setActivity(this);
        mSearchBarGroup.setSearchUtils(mSearchUtils);
        mSearchBarGroup.initSearchBar();

        showLoadingView();

        mModel.startLoader(true);

        showOptionMenu();
    }

    public void showOptionMenu() {
        try {
            getWindow().addFlags(
                    WindowManager.LayoutParams.class.getField("FLAG_NEEDS_MENU_KEY").getInt(null));
        } catch (Exception e) {
            // Ignore
        }
    }

    public void hideOptionMenu() {
        try {
            getWindow().clearFlags(
                    WindowManager.LayoutParams.class.getField("FLAG_NEEDS_MENU_KEY").getInt(null));
        } catch (Exception e) {
            // Ignore
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == VOICE_RECOGNITION_REQUEST_CODE) {
            ArrayList<String> matchResults = data
                    .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            String voice_str = matchResults.get(0).toString();// 只要最相似的就行，去第一个
            Log.v("", "voice_str is " + voice_str);
            if (voice_str != null && !voice_str.equals("")) {
                mSearchBarGroup.searchByText(voice_str);
            }
        }
    }

    void showLoadingView() {
        LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        mLoading = new Dialog(this, R.style.LoadingViewStyle);
        mLoading.setContentView(new LoadingView(this), params);
        mLoading.setCancelable(false);
        mLoading.setCanceledOnTouchOutside(false);
        mLoading.show();
    }

    void closeLoadingView() {
        if (mLoading != null && mLoading.isShowing()) {
            mLoading.dismiss();
            mLoading = null;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        mWeather.register();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mWeather.unRegister();
    }

    @Override
    protected void onPause() {
        super.onPause();

        mPaused = true;
        mDragController.cancelDrag();
        if (mSearchUtils != null && SearchUtils.isSearchState) {
            mSearchUtils.clearValue();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mDragLayer.getVisibility() == View.INVISIBLE) {
            mDragLayer.setAlpha(0);
            mDragLayer.setVisibility(View.VISIBLE);
            startDragLayerAnim();
        }
        mPaused = false;

        if (mOnResumeNeedsLoad) {
            mModel.startLoader(true);
            mOnResumeNeedsLoad = false;
        }

        if (mBindOnResumeCallbacks.size() > 0) {
            for (int i = 0; i < mBindOnResumeCallbacks.size(); i++) {
                mBindOnResumeCallbacks.get(i).run();
            }
            mBindOnResumeCallbacks.clear();
        }
        if (mWeather != null) {
            mWeather.changeTimeAndDate();
        }
    }

    /**
     * 将mDragLayer有
     */
    private void startDragLayerAnim() {
        ValueAnimator valueAnimator = ValueAnimator.ofFloat(0, 1f);
        valueAnimator.setDuration(500);
        valueAnimator.addUpdateListener(new AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                // TODO Auto-generated method stub
                float alpha = (Float) animation.getAnimatedValue();
                mDragLayer.setAlpha(alpha);
            }
        });
        valueAnimator.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mModel != null) {
            mModel.unbindItemInfosAndClearQueuedBindRunnables();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.launcher_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.add:
                new PopupDialog(this, PopupDialog.ADD_VIEW).show();
                return true;

            case R.id.wallpaper:
                startWallpaper();
                return true;

            case R.id.settings:
                startSetting();
                return true;

            case R.id.zen:
                entryZenSetting();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        if (SearchUtils.isSearchState) {
            return false;
        }

        if (mSpeedDial.isFull()) {
            menu.findItem(R.id.add).setVisible(false);
        } else {
            menu.findItem(R.id.add).setVisible(true);
        }
        return true;
    }

    private void startWallpaper() {
        final Intent pickWallpaper = new Intent(Intent.ACTION_SET_WALLPAPER);
        pickWallpaper.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        startActivitySafely(pickWallpaper);
    }

    private void startSetting() {
        Intent settings = new Intent(android.provider.Settings.ACTION_SETTINGS);
        settings.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        startActivitySafely(settings);
    }

    @Override
    public boolean onLongClick(View v) {
        if (v instanceof BubbleView) {
            BubbleView view = (BubbleView) v;
            mSpeedDial.startDrag(view);
        }
        return true;
    }

    boolean startActivitySafely(Intent intent) {
        try {
            startActivity(intent);
            return true;
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, R.string.activity_not_found,
                    Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    @Override
    public void onClick(View v) {
        Object tag = v.getTag();

        if (SearchUtils.isSearchState) {
            return;
        }

        if (tag instanceof Integer) {
            Integer num = (Integer) v.getTag();
            switch (num.intValue()) {
                case SpeedDial.EDIT_VIEW_ICON:
                    new ChangeIcon(this).show();
                    break;
                case SpeedDial.EDIT_VIEW_CHANGE:
                    new PopupDialog(this, PopupDialog.CHANGE_VIEW).show();
                    break;
                case SpeedDial.EDIT_VIEW_DELETE:
                    mSpeedDial.removeBubbleView();
                    break;
            }
            return;
        }

        if (tag instanceof ShortcutInfo) {
            final ShortcutInfo shortcut = (ShortcutInfo) tag;
            final Intent intent = shortcut.intent;
            if (intent != null) {
                startActivitySafely(intent);
                return;
            } else if (intent == null && "*BROWSER*".equals(shortcut.title)) {
                LauncherAppState.getAppIntentUtil().startBrowserIntent();
            }
        }
        // stop speed dial drag at last.
        mSpeedDial.stopDrag();
    }

    public DragLayer getDragLayer() {
        return mDragLayer;
    }

    public SpeedDial getSpeedDial() {
        return mSpeedDial;
    }

    public Drawer getDrawer() {
        return mDrawer;
    }

    public ArrayList<AppInfo> getApps() {
        return mApps;
    }

    @Override
    public void onBackPressed() {
        mSpeedDial.stopDrag();
    }

    @Override
    public void dump(String prefix, FileDescriptor fd, PrintWriter writer, String[] args) {
        super.dump(prefix, fd, writer, args);
        Logger.dump(writer);
    }

    private boolean waitUntilResume(Runnable run, boolean deletePreviousRunnables) {
        if (mPaused) {
            Log.i(TAG, "Deferring update until onResume");
            if (deletePreviousRunnables) {
                while (mBindOnResumeCallbacks.remove(run)) {
                }
            }
            mBindOnResumeCallbacks.add(run);
            return true;
        } else {
            return false;
        }
    }

    private boolean waitUntilResume(Runnable run) {
        return waitUntilResume(run, false);
    }

    @Override
    public boolean setLoadOnResume() {
        if (mPaused) {
            mOnResumeNeedsLoad = true;
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void startBinding() {
        mBindOnResumeCallbacks.clear();

        mSpeedDial.startBind();
    }

    @Override
    public void bindItems(final ArrayList<ItemInfo> shortcuts, final int start, final int end,
            final boolean forceAnimateIcons) {
        Runnable r = new Runnable() {
            public void run() {
                bindItems(shortcuts, start, end, forceAnimateIcons);
            }
        };
        if (waitUntilResume(r)) {
            return;
        }

        for (int i = start; i < end; i++) {
            final ItemInfo item = shortcuts.get(i);
            ShortcutInfo info = (ShortcutInfo) item;
            mSpeedDial.addBubbleViewFromBind(info);
        }
    }

    protected void onFinishBindingItems() {
        mSpeedDial.finishBind();
        closeLoadingView();
    }

    @Override
    public void finishBindingItems() {
        Runnable r = new Runnable() {
            public void run() {
                finishBindingItems();
            }
        };
        if (waitUntilResume(r)) {
            return;
        }

        onFinishBindingItems();
    }

    @Override
    public void bindAllApplications(ArrayList<AppInfo> apps) {
        mApps = apps;

        CategoryData.init(this, mApps);

        mDrawer.notifyDataSetChanged();
    }

    @Override
    public void bindAppsAdded(ArrayList<AppInfo> addedApps) {
        // TODO Auto-generated method stub

    }

    @Override
    public void bindAppsUpdated(ArrayList<AppInfo> apps) {
        // TODO Auto-generated method stub

    }

    @Override
    public void bindComponentsRemoved(ArrayList<String> packageNames, ArrayList<AppInfo> appInfos,
            boolean matchPackageNamesOnly) {
        // TODO Auto-generated method stub

    }

    @Override
    public void bindSearchablesChanged() {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean isAllAppsButtonRank(int rank) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void onPageBoundSynchronously(int page) {
        // TODO Auto-generated method stub
    }

    void entryZenSetting() {
        Intent intent = new Intent(this, ZenSetting.class);
        startActivity(intent);
    }

    boolean canSwipe() {
        boolean ret = true;
        if (mSearchUtils != null && SearchUtils.isSearchState)
            ret = false;
        if (mDragController.isDragging())
            ret = false;
        return ret;
    }

    public void swipeUp() {
        if (!canSwipe())
            return;
        mAnimator.setFloatValues(mAnimatorValue, 1);
        mAnimator.start();
    }

    public void swipeDown() {
        if (!canSwipe())
            return;
        mAnimator.setFloatValues(mAnimatorValue, 0);
        mAnimator.start();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        mGestureDetector.onTouchEvent(ev);
        return super.dispatchTouchEvent(ev);
    }

    class LauncherGestureLisenter extends SimpleOnGestureListener {
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            final float SENSITIVITY = 1000;
            final float FORCE_Y = 2.5f;
            Log.v("suyu", "velocityX = " + velocityX + ", velocityY = " + velocityY);

            if (Math.abs(velocityY / velocityX) >= FORCE_Y) {
                if (velocityY > SENSITIVITY) {
                    swipeDown();
                    return true;
                } else if (velocityY < -SENSITIVITY) {
                    swipeUp();
                    return true;
                }
            }

            return false;
        }
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (mSearchUtils != null && SearchUtils.isSearchState) {
                mSearchUtils.stopSearchBar();
            } else {
                swipeDown();
            }
        }
        return super.onKeyUp(keyCode, event);
    }
}
