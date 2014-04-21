package com.arphen.ownspritz.app;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.widget.NumberPicker;

public class BlinkNumberPicker extends NumberPicker {
    public BlinkNumberPicker(Context context) {
        super(context);
        init(null, 0);
    }

    public BlinkNumberPicker(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public BlinkNumberPicker(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    private Runnable hide;
    private Runnable show;
    private boolean onTop = false;

    public void hide() {
        removeCallbacks(hide);
        postDelayed(hide, 3000);
        onTop = false;
    }

    public void showperm() {
        onTop = true;
        removeCallbacks(hide);
        postDelayed(show, 0);
    }

    public void show() {
        if (!onTop) {
            removeCallbacks(hide);
            postDelayed(show, 0);
            hide();
        }
    }

    private void init(AttributeSet attrs, int defStyle) {
        setMaxValue(500);
        setMinValue(0);
        String[] nums = new String[501];
        for(int q=500;q>=0;q--){
            nums[q]=String.valueOf((q-250)*10);
        }
        setDisplayedValues(nums);
        setValue(300);
        hide = new Runnable() {
            @Override
            public void run() {
                animate().alpha(0f).setDuration(1000).setListener(null);
            }
        };
        show = new Runnable() {
            @Override
            public void run() {
                animate().alpha(1f).setDuration(1000).setListener(null);
            }
        };
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
    }
}