package com.arphen.dontblink.app;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.SeekBar;

import java.util.Timer;
import java.util.TimerTask;


/**
 * TODO: document your custom view class.
 */
public class BlinkProgressBar extends SeekBar implements OnChapterChangedListener, RunningListener {

    private Runnable hide;
    private Runnable show;
    private boolean onTop = true;
private BlinkView bv;
    private Timer timer;
    private Runnable stopupdate;

    public BlinkProgressBar(Context context) {
        super(context);
        init(null, 0);
    }
    public BlinkProgressBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public BlinkProgressBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    public void linkBlinkView(BlinkView display) {
        bv = display;
    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
    }

    public void hide() {
        removeCallbacks(hide);
        start_updating();
        postDelayed(hide, 3000);
        postDelayed(stopupdate,3000);
        onTop = false;
    }

    public void showperm() {
        onTop = true;
        removeCallbacks(hide);
        postDelayed(show, 0);
    }
    public void start_updating() {
        timer = new Timer();
        Log.i("Progressbar","Starting updates!");
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                Activity host = (Activity) getContext();
                post(new Runnable() {
                    @Override
                    public void run() {
                        setProgress(bv.getCurrentPosition());
                    }
                });
            }
        },0,500);
    }
    public void show() {
        if (!onTop) {
            removeCallbacks(hide);
            removeCallbacks(stopupdate);
            postDelayed(show, 0);
            start_updating();
            hide();
        }
    }

    private void init(AttributeSet attrs, int defStyle) {
        timer=new Timer();
        hide = new Runnable() {
            @Override
            public void run() {
                animate().alpha(0f).setDuration(1000).setListener(null);
            }
        };
        stopupdate=new Runnable() {
            @Override
            public void run() {
                Log.i("Progressbar","Stopping updates!");
                timer.cancel();
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
    public void onChapterChanged(int c,int l) {
        Log.i("Progress","Chapter changed");
        setProgress(0);
        setMax(l);
        show();
    }

    @Override
    public void running(Boolean running) {
        if(running)
            hide();
        else
            showperm();

    }
}
