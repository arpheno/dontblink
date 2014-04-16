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
import android.widget.Button;
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
    public SeekBar sb;
    private int interact_sb;
    final int ACTIVITY_CHOOSE_FILE = 1;
    final int ACTIVITY_CHOOSE_CHAPTER=888;
    private Button chpter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fullscreen);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        wpmtv=(TextView)findViewById(R.id.wpmtv);
        tv= (SpritzView) findViewById(R.id.spritzview);
        sb = (SeekBar) findViewById(R.id.seekBar);
        chpter = (Button) findViewById(R.id.chapter);
        gestureScanner = new GestureDetector(this);
        //Metrics
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        width = dm.widthPixels;
        height = dm.heightPixels;
        wpmthresh = (0.7 * width);
        sb.postDelayed(new Runnable() {
            @Override
            public void run() {
                sb.animate()
                        .alpha(0f)
                        .setDuration(1000)
                        .setListener(null);
            }
        },5000);
        sb.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {
                        if (!arg2)
                            return;
                        tv.setPosition(arg1);
                        interact_sb = (int) (System.currentTimeMillis());
                        sb.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                if((int) (System.currentTimeMillis()) - interact_sb>4800)
                                sb.animate()
                                        .alpha(0f)
                                        .setDuration(1000)
                                        .setListener(null);
                            }
                        },5000);
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {
                        interact_sb = (int) (System.currentTimeMillis());
                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                        interact_sb = (int) (System.currentTimeMillis());
                    }
                });

        Button btn = (Button) this.findViewById(R.id.cf);
        btn.setOnClickListener(new View.OnClickListener() {
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
        chpter.setOnClickListener(new View.OnClickListener() {


            @Override
            public void onClick(View v) {
                Intent chooseChapter;
                Intent intent;
                chooseChapter = new Intent(getApplicationContext(), FullscreenActivity2.class);
                int chapters = tv.getNumberOfChapters();
                chooseChapter.putExtra("chapters", chapters);
                if(chapters!=0)
                    startActivityForResult(chooseChapter, ACTIVITY_CHOOSE_CHAPTER);
            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch(requestCode) {
            case ACTIVITY_CHOOSE_FILE: {
                if (resultCode == RESULT_OK){
                    Uri uri = data.getData();
                    String filePath = uri.getPath();
                    Log.i("Filechooser",filePath);
                    try {
                        tv.init(filePath);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            case ACTIVITY_CHOOSE_CHAPTER: {
                if (resultCode == RESULT_OK){
                    int c = data.getIntExtra("result",0);
                    tv.setChapter(c);
                }

            }
        }
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
            Log.i("Gesturesscroll","bottom bezel");
            interact_sb = (int) (System.currentTimeMillis());
            sb.post(new Runnable() {
                @Override
                public void run() {
                     sb.animate()
                            .alpha(1f)
                            .setDuration(1000)
                            .setListener(null);
                }
            });
        }
        return false;
    }
    @Override
    public boolean onTouchEvent(MotionEvent event) {
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
        if( (motionEvent.getY() > height-150) ){
            interact_sb = (int) (System.currentTimeMillis());

            sb.post(new Runnable() {
                @Override
                public void run() {
                    sb.animate()
                            .alpha(1f)
                            .setDuration(1000)
                            .setListener(null);
                }
            });
        }
        return false;
    }
}
