
package com.cooeeui.brand.zenlauncher.searchbar;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.util.Log;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;

import com.cooeeui.brand.zenlauncher.scenes.SpeedDial;
import com.cooeeui.brand.zenlauncher.scenes.ui.FitCenterLayout;
import com.cooeeui.brand.zenlauncher.weatherclock.WeatherClockGroup;

public class SearchUtils {
    private WeatherClockGroup weatherClockGroup;
    private SpeedDial speedDial;
    private SearchBarGroup searchBarGroup;
    private Context context;
    private FrameLayout searchBarParent = null;
    private AlphaAnimation startBarAlphaAnim = null;
    private ValueAnimator startBarTranslateAnim = null;
    private AlphaAnimation stopBarAlphaAnim = null;
    private ValueAnimator stopBarTranslateAnim = null;
    private final long animDuration = 500;
    private float oldsearchBarY = -1;
    private MyAnimatorListener myAnimationListener = null;
    public static boolean isSearchState = false;// 判断是否进入搜索框模式
    private float oldspeedDialX = -1;
    private float oldweatherClockGroupX = -1;
    public InputMethodManager inputMethodManager = null;
    private boolean isAnimDone = true;// 判断动画是否执行完毕 ，没执行完上一个动画之前不能执行下一个动画

    public InputMethodManager getInputMethodManager() {
        if (inputMethodManager == null) {
            inputMethodManager = (InputMethodManager) context
                    .getSystemService(Context.INPUT_METHOD_SERVICE);
        }
        return inputMethodManager;
    }

    public SearchUtils(Context context, WeatherClockGroup weatherClockGroup, SpeedDial speedDial,
            SearchBarGroup searchBarGroup) {
        this.context = context;
        this.searchBarGroup = searchBarGroup;
        this.speedDial = speedDial;
        this.weatherClockGroup = weatherClockGroup;
        myAnimationListener = new MyAnimatorListener();
    }

    public void startSearchAnim() {
        if (!isAnimDone) {
            return;
        }
        if (!isSearchState) {
            isSearchState = true;
            // 搜索框的父类的父类的父类的坐标
            if (searchBarParent == null) {
                if (searchBarGroup.getParent() instanceof FitCenterLayout) {
                    if (searchBarGroup.getParent().getParent() instanceof FrameLayout) {
                        if (searchBarGroup.getParent().getParent().getParent() instanceof FrameLayout) {
                            searchBarParent = (FrameLayout) searchBarGroup.getParent().getParent()
                                    .getParent();
                            oldsearchBarY = searchBarParent.getY();
                        }
                    }
                }
            }
            if (searchBarParent != null) {
                if (startBarTranslateAnim == null) {
                    startBarTranslateAnim = ValueAnimator.ofFloat(0, -oldsearchBarY);
                    startBarTranslateAnim.setDuration(animDuration);
                    startBarTranslateAnim.addListener(myAnimationListener);
                }
                startBarTranslateAnim.addUpdateListener(new AnimatorUpdateListener() {

                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        float targetY = (Float) animation.getAnimatedValue();
                        searchBarParent.setTranslationY(targetY);
                    }
                });
                startBarTranslateAnim.start();
                isAnimDone = false;
            }

            if (startBarAlphaAnim == null) {
                startBarAlphaAnim = new AlphaAnimation(1.0f, 0.0f);
                startBarAlphaAnim.setDuration(animDuration);
                startBarAlphaAnim.setFillAfter(true);
                oldspeedDialX = speedDial.getX();
                oldweatherClockGroupX = weatherClockGroup.getX();
            }
            speedDial.startAnimation(startBarAlphaAnim);
            weatherClockGroup.startAnimation(startBarAlphaAnim);
        }

    }

    private class MyAnimatorListener implements AnimatorListener {

        @Override
        public void onAnimationStart(Animator animation) {
            // do nothing.
        }

        @Override
        public void onAnimationEnd(Animator animation) {
            if (animation == startBarTranslateAnim) {
                Log.v("", "startBarTranslateAnim onAnimationEnd is end");
                // speedDial.setTranslationX(speedDial.getWidth() * (-2));
                // weatherClockGroup.setTranslationX(weatherClockGroup.getWidth()
                // * (-2));
                openSoftKeyboard();
                searchBarGroup.startSearchBar();
            }
            if (animation == stopBarTranslateAnim) {
                searchBarGroup.stopSearchBar();
            }
            isAnimDone = true;
        }

        @Override
        public void onAnimationCancel(Animator animation) {
            // do nothing.
        }

        @Override
        public void onAnimationRepeat(Animator animation) {
            // do nothing.
        }

    }

    /**
     * 强制弹出软键盘
     */
    public void openSoftKeyboard() {
        getInputMethodManager();
        inputMethodManager.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);// 强制弹出软键盘
    }

    /**
     * 停止搜索模式
     */
    public void stopSearchBar() {
        if (!isAnimDone) {
            return;
        }
        if (searchBarParent != null) {
            if (stopBarTranslateAnim == null) {
                stopBarTranslateAnim = ValueAnimator.ofFloat(-oldsearchBarY, 0);
                stopBarTranslateAnim.setDuration(animDuration);
                stopBarTranslateAnim.addListener(myAnimationListener);
            }
            stopBarTranslateAnim.addUpdateListener(new AnimatorUpdateListener() {

                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    float targetY = (Float) animation.getAnimatedValue();
                    searchBarParent.setTranslationY(targetY);
                }
            });
            stopBarTranslateAnim.start();
            isAnimDone = false;
        }

        if (isSearchState) {
            isSearchState = false;
            // speedDial.setTranslationX(speedDial.getWidth() * 2);
            // weatherClockGroup.setTranslationX(weatherClockGroup.getWidth() *
            // 2);

            if (stopBarAlphaAnim == null) {
                stopBarAlphaAnim = new AlphaAnimation(0.0f, 1.0f);
                stopBarAlphaAnim.setDuration(animDuration);
                stopBarAlphaAnim.setFillAfter(true);
            }
            speedDial.startAnimation(stopBarAlphaAnim);
            weatherClockGroup.startAnimation(stopBarAlphaAnim);
        }
    }

    public void clearValue() {
        searchBarGroup.clearValue();
    }
}
