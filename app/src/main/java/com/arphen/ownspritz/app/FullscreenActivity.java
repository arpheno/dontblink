package com.arphen.ownspritz.app;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.SeekBar;
import android.widget.TextView;

import com.arphen.ownspritz.app.util.SystemUiHider;

import java.io.IOException;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 *
 * @see SystemUiHider
 */
public class FullscreenActivity extends Activity implements GestureDetector.OnGestureListener{

    public GestureDetector gestureScanner;
    private double wpmthresh;
    private int height;
    private int width;
    private TextView wpmtv;
    private SpritzView tv;
    public BlinkProgressBar sb;
    private int interact_sb;
    final int ACTIVITY_CHOOSE_FILE = 1;
    final int ACTIVITY_CHOOSE_CHAPTER=888;
    private BlinkButton chapterbutton;
    private BlinkButton filebutton;
    private BlinkAnnouncement an;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fullscreen);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        wpmtv=(TextView)findViewById(R.id.wpmtv);
        tv= (SpritzView) findViewById(R.id.spritzview);
        sb = (BlinkProgressBar) findViewById(R.id.seekBar);
        an = (BlinkAnnouncement)findViewById(R.id.announcement);
        chapterbutton = (BlinkButton) findViewById(R.id.chapter);
        filebutton = (BlinkButton) this.findViewById(R.id.cf);
        gestureScanner = new GestureDetector(this);
        //Metrics
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        width = dm.widthPixels;
        height = dm.heightPixels;
        wpmthresh = (0.7 * width);
        tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!tv.is_init())
                    return;
                if (tv.is_playing()) {
                    stopTV();
                }else {
                    runTV();
                }
            }
        });
        sb.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {
                        if (!arg2)
                            return;
                        tv.setPosition(arg1);
                        tv.stop();
                        sb.show();
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {
                        sb.show();
                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                        sb.show();
                        runTV();
                    }
                }
        );

        filebutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent chooseFile;
                Intent intent;
                chooseFile = new Intent(Intent.ACTION_GET_CONTENT);
                chooseFile.setType("file/*");
                intent = Intent.createChooser(chooseFile, "Choose a file");
                startActivityForResult(intent, ACTIVITY_CHOOSE_FILE);
            }
        });
        chapterbutton.setOnClickListener(new View.OnClickListener() {


            @Override
            public void onClick(View v) {
                Intent chooseChapter;
                Intent intent;
                chooseChapter = new Intent(getApplicationContext(), FullscreenActivity2.class);
                int chapters = tv.getNumberOfChapters();
                chooseChapter.putExtra("chapters", chapters);
                if (chapters != 0)
                    startActivityForResult(chooseChapter, ACTIVITY_CHOOSE_CHAPTER);
            }
        });
    }
public void announce(String what){
    an.setText(what);
    an.show();
}

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch(requestCode) {
            case ACTIVITY_CHOOSE_FILE: {
                if (resultCode == RESULT_OK){
                    Uri uri = data.getData();
                    String filePath = uri.getPath();
                    Log.i("Filechooser",filePath);
                    announce("Loading File");
                    try {
                        tv.init(filePath);
                        stopTV();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            case ACTIVITY_CHOOSE_CHAPTER: {
                if (resultCode == RESULT_OK){
                    int c = data.getIntExtra("result",0);
                    tv.setChapter(c);
                    tv.setPosition(0);
                    sb.setProgress(0);
                    announce("Chapter: "+String.valueOf(c));
                    stopTV();
                }

            }
        }
    }
    public void runTV(){
        sb.hide();
        chapterbutton.hide();
        filebutton.hide();
        tv.run();
    }
    public void stopTV(){
        tv.stop();
        sb.setMax(tv.getLengthOfChapter());
        sb.showperm();
        chapterbutton.showperm();
        filebutton.showperm();
    }
    @Override
    public boolean onDown(MotionEvent motionEvent) {
        return false;    }

    @Override
    public void onShowPress(MotionEvent motionEvent) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent motionEvent) {
return true;
    }

    @Override
    public boolean onScroll(MotionEvent motionEvent, MotionEvent motionEvent2, float v, float v2) {
        Log.i("Gesturesscroll",String.valueOf(motionEvent)+String.valueOf(motionEvent2));
        if( (motionEvent.getY() > height-150) ){
            Log.i("Gesturesscroll","Bottom Bezel");
            sb.show();
            chapterbutton.show();
            filebutton.show();
            sb.setMax(tv.getLengthOfChapter());
            sb.setProgress(tv.getCurrentPosition());
            Log.i("Gesturesscroll", String.valueOf(tv.getCurrentPosition()));
        }
        return false;
    }
    @Override
    public boolean onTouchEvent(MotionEvent event) {sb.show();
        sb.setMax(tv.getLengthOfChapter());
        sb.setProgress(tv.getCurrentPosition());
        sb.show();
        chapterbutton.show();
        filebutton.show();
        return gestureScanner.onTouchEvent(event);
    }
    @Override
    public void onLongPress(MotionEvent motionEvent) {

    }

    @Override
    public boolean onFling(MotionEvent motionEvent, MotionEvent motionEvent2, float v, float v2) {
        if(motionEvent.getX()>wpmthresh && motionEvent2.getX()>wpmthresh) {
            tv.changeWpm(-v2 / 5000 * 200);
            wpmtv.setText(String.valueOf(tv.getWpm()));
        }
        return false;
    }
}
