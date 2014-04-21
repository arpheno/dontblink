package com.arphen.ownspritz.app;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.View;
import android.view.WindowManager;
import android.widget.NumberPicker;
import android.widget.SeekBar;
import android.widget.TextView;

import com.arphen.ownspritz.app.util.SystemUiHider;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 *
 * @see SystemUiHider
 */
public class MainActivity extends Activity{

    final int ACTIVITY_CHOOSE_FILE = 1;
    final int ACTIVITY_CHOOSE_CHAPTER = 888;
    public GestureDetector gestureScanner;
    public BlinkProgressBar sb;
    private double wpmthresh;
    private int height;
    private int width;
    private TextView wpmtv;
    private SpritzView tv;
    private int interact_sb;
    private BlinkButton chapterbutton;
    private BlinkButton filebutton;
    private BlinkAnnouncement an;
    private BlinkNumberPicker np;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        tv = (SpritzView) findViewById(R.id.spritzview);
        sb = (BlinkProgressBar) findViewById(R.id.seekBar);
        an = (BlinkAnnouncement) findViewById(R.id.announcement);
        np= (BlinkNumberPicker) findViewById(R.id.numberPicker);
        chapterbutton = (BlinkButton) findViewById(R.id.chapter);
        filebutton = (BlinkButton) this.findViewById(R.id.cf);
        //Metrics
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        width = dm.widthPixels;
        height = dm.heightPixels;
        wpmthresh = (0.7 * width);
        np.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker numberPicker, int i, int i2) {
                tv.changeWpm((i2-250)*10);
                np.show();
                announce(String.valueOf((i2-250)*10)+" Words per minute now");
            }
        });
        tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!tv.is_init())
                    return;
                if (tv.is_playing()) {
                    stopTV();
                } else {
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
                chooseChapter = new Intent(getApplicationContext(), ChapterChooser.class);
                int chapters = tv.getNumberOfChapters();
                chooseChapter.putExtra("chapters", chapters);
                if (chapters != 0)
                    startActivityForResult(chooseChapter, ACTIVITY_CHOOSE_CHAPTER);
            }
        });
    }

    public void announce(String what) {
        an.setText(what);
        an.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case ACTIVITY_CHOOSE_FILE: {
                if (resultCode == RESULT_OK) {
                    Uri uri = data.getData();
                    String filePath = uri.getPath();
                    Log.i("Filechooser", filePath);
                    announce("Loading File");
                    try {
                        InputStream in = new FileInputStream(filePath);
                        tv.init(in);
                        stopTV();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            case ACTIVITY_CHOOSE_CHAPTER: {
                if (resultCode == RESULT_OK) {
                    int c = data.getIntExtra("result", 0);
                    chapterFromBeginning(c);
                    announce("Chapter: " + String.valueOf(c));
                    stopTV();
                }

            }
        }
    }
public void chapterFromBeginning(int c){
    tv.setChapter(c);
    tv.setPosition(0);
    sb.setProgress(0);
    sb.show();
}
    public void runTV() {
        sb.hide();
        chapterbutton.hide();
        filebutton.hide();
        np.hide();
        tv.run();
    }

    public void stopTV() {
        tv.stop();
        sb.setMax(tv.getLengthOfChapter());
        sb.showperm();
        np.showperm();
        chapterbutton.showperm();
        filebutton.showperm();
    }
}
