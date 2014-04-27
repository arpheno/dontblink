package com.arphen.ownspritz.app;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.NumberPicker;
import android.widget.SeekBar;
import android.widget.TextView;

import com.arphen.ownspritz.app.util.SystemUiHider;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import static com.arphen.ownspritz.app.R.id.action_choose_chapter;
import static com.arphen.ownspritz.app.R.id.action_choose_file;
import static com.arphen.ownspritz.app.R.id.sample;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 *
 * @see SystemUiHider
 */
public class MainActivity extends Activity implements RunningListener, OnChapterChangedListener {

    final int ACTIVITY_CHOOSE_FILE = 1;
    final int ACTIVITY_CHOOSE_CHAPTER = 888;
    public GestureDetector gestureScanner;
    public BlinkProgressBar sb;
    private double wpmthresh;
    private int height;
    private int width;
    private TextView wpmtv;
    private BlinkView tv;
    private int interact_sb;
    private BlinkAnnouncement an;
    private BlinkNumberPicker np;
    private TextView pt;
    private TextView pb;
    private BlinkAnnouncement at;
    private MenuItem chapterfield;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        View decorView = getWindow().getDecorView();
// Hide the status bar.
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN+View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        getActionBar().setBackgroundDrawable(null);
        getActionBar().setIcon(R.drawable.app_icon);
        tv = (BlinkView) findViewById(R.id.blinkview);
        sb = (BlinkProgressBar) findViewById(R.id.seekBar);
        an = (BlinkAnnouncement) findViewById(R.id.announcement);
        np= (BlinkNumberPicker) findViewById(R.id.numberPicker);
        pt= (TextView)findViewById(R.id.previewTop);
        pb= (TextView)findViewById(R.id.previewBot);
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
        at= (BlinkAnnouncement) findViewById(R.id.authortitle);
        tv.addChapterChangedListener(sb);
        tv.addChapterChangedListener(an);
        tv.addRunningListener(sb);
        tv.addChapterChangedListener(this);
        tv.addRunningListener(this);
        tv.addRunningListener(np);
        sb.linkBlinkView(tv);
        sb.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {
                        if (!arg2)
                            return;
                        tv.setPosition(arg1);
                        pt.setText(tv.getPreview(-15,15));
                        pb.setText(tv.getPreview(1,20));
                        tv.stop();
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
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case action_choose_file:
                Intent chooseFile;
                Intent intent;
                chooseFile = new Intent(Intent.ACTION_GET_CONTENT);
                chooseFile.setType("file/*");
                intent = Intent.createChooser(chooseFile, "Choose a file");
                startActivityForResult(intent, ACTIVITY_CHOOSE_FILE);
                return true;
            case action_choose_chapter:
                Intent chooseChapter;
                chooseChapter = new Intent(getApplicationContext(), ChapterChooser.class);
                int chapters = tv.getNumberOfChapters();
                chooseChapter.putExtra("chapters", chapters);
                if (chapters != 0)
                    startActivityForResult(chooseChapter, ACTIVITY_CHOOSE_CHAPTER);
                return true;
            case sample:
                try {
                    InputStream k = getAssets().open("The Idiot.epub");
                    announce("Loading File");
                    tv.init(k);
                    stopTV();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void announce(String what) {
        an.setText(what);
        an.show();
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_activity_action, menu);
        chapterfield= menu.findItem(R.id.Chapter);
        return super.onCreateOptionsMenu(menu);
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
                       // stopTV();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            case ACTIVITY_CHOOSE_CHAPTER: {
                if (resultCode == RESULT_OK) {
                    int c = data.getIntExtra("result", 0);
                    tv.setChapter(c);
                    announce("Chapter: " + String.valueOf(c));
                    stopTV();
                }

            }
        }
    }
    public void running(Boolean running) {
        if(running) {
            View decorView = getWindow().getDecorView();
// Hide the status bar.
            int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN+View.SYSTEM_UI_FLAG_LAYOUT_STABLE+View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
            decorView.setSystemUiVisibility(uiOptions);
            //getActionBar().hide();
        }else {
            View decorView = getWindow().getDecorView();
// Hide the status bar.
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN+View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            //getActionBar().show();
        }
    }
    public void runTV() {
        pt.setText("");
        pb.setText("");
        tv.run();
    }

    public void stopTV() {
        pt.setText(tv.getPreview(-15, 15));
        pb.setText(tv.getPreview(1,20));
        tv.stop();
    }

    @Override
    public void onChapterChanged(final int c, int l) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                chapterfield.setTitle("Chapter "+String.valueOf(c));
            }
        });

    }
}
