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
import static com.arphen.ownspritz.app.R.id.action_choose_library;
import static com.arphen.ownspritz.app.R.id.action_choose_sample;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 *
 * @see SystemUiHider
 */
public class MainActivity extends Activity implements RunningListener, OnChapterChangedListener {

    private static final int ACTIVITY_BROWSE_LIBRARY = 666;
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
        setUpUi();
        populateViews();
        linkViews();
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
            case action_choose_sample:
                try {
                    InputStream k = getAssets().open("The Idiot.epub");
                    announce("Loading File");
                    tv.init(k);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return true;
            case action_choose_library:
                Intent browseLibrary;
                browseLibrary = new Intent(getApplicationContext(), LibraryBrowser .class);
                //chooseChapter.putExtra("chapters", chapters);
                startActivityForResult(browseLibrary,ACTIVITY_BROWSE_LIBRARY );
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void announce(String what) {
        an.setText(what);
        an.show();
    }
    private void linkViews(){

        np.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker numberPicker, int i, int i2) {
                tv.changeWpm((i2 - 250) * 10);
                np.show();
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
        /* Link Components */
        tv.addChapterChangedListener(sb);
        tv.addChapterChangedListener(an);
        tv.addChapterChangedListener(this);
        tv.addRunningListener(sb);
        tv.addRunningListener(np);
        tv.addRunningListener(this);
        sb.linkBlinkView(tv);
        sb.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {
                        if (!arg2)
                            return;
                        tv.setPosition(arg1);
                        stopTV();
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
    private void setUpUi(){
        setContentView(R.layout.main_activity);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN+View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        getActionBar().setBackgroundDrawable(null);
        getActionBar().setIcon(R.drawable.app_icon);
    }
    private void populateViews(){
        tv = (BlinkView) findViewById(R.id.blinkview);
        sb = (BlinkProgressBar) findViewById(R.id.seekBar);
        an = (BlinkAnnouncement) findViewById(R.id.announcement);
        np= (BlinkNumberPicker) findViewById(R.id.numberPicker);
        pt= (TextView)findViewById(R.id.previewTop);
        pb= (TextView)findViewById(R.id.previewBot);
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
                        final InputStream in = new FileInputStream(filePath);
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    tv.init(in);
                                    stopTV();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }).start();
                        // stopTV();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                break;
            }
            case ACTIVITY_CHOOSE_CHAPTER: {
                if (resultCode == RESULT_OK) {
                    int c = data.getIntExtra("result", 0);
                    tv.forceChapter(c);
                    announce("Chapter: " + String.valueOf(c + 2));
                    stopTV();
                }
                break;
            }
            case ACTIVITY_BROWSE_LIBRARY: {
                String filePath = data.getStringExtra("result");
                announce("Loading File");
                try {
                    final InputStream in = new FileInputStream(filePath);
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                tv.init(in);
                                stopTV();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }).start();
                    // stopTV();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            break;
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
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN+View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        }
    }
     public void runTV() {
        pt.setText("");
        pb.setText("");
        tv.run();
    }

    public void stopTV() {
        Thread timer = new Thread(new Runnable() {
            @Override
            public void run() {
                final String top=tv.getPreview(-15,15);
                final String bot=tv.getPreview(1,20);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        pt.setText(top);
                        pb.setText(bot);
                    }
                });
            }
        });
        timer.start();
        tv.stop();
    }

    @Override
    public void onChapterChanged(final int c, int l) {
        new Thread(new Runnable() {
            @Override
            public void run() {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                chapterfield.setTitle("Chapter "+String.valueOf(c+1));
                View decorView = getWindow().getDecorView();
                decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN+View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            }
        });
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                View decorView = getWindow().getDecorView();
                int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN+View.SYSTEM_UI_FLAG_LAYOUT_STABLE+View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
                decorView.setSystemUiVisibility(uiOptions);
            }
        });

            }
        }).start();
    }
}
