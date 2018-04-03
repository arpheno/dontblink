package com.arphen.ownspritz.app;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
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

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import nl.siegmann.epublib.domain.Book;
import nl.siegmann.epublib.domain.Resource;
import nl.siegmann.epublib.epub.EpubReader;

import static com.arphen.ownspritz.app.R.id.action_choose_chapter;
import static com.arphen.ownspritz.app.R.id.action_choose_file;
import static com.arphen.ownspritz.app.R.id.action_choose_library;
import static com.arphen.ownspritz.app.R.id.action_choose_sample;
import static java.lang.Math.round;

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
    private SharedPreferences mPrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPrefs = this.getSharedPreferences(
                "com.arphen.dontblink", Context.MODE_PRIVATE);
        setUpUi();
        populateViews();
        linkViews();
        initFromPrefs();
    }

    private void initFromPrefs() {
       // String current_file_path = mPrefs.getString("current_file_path", "");
       // if (current_file_path != "")
       //     loadFile(current_file_path);

        final int c = mPrefs.getInt("current_chapter", -1);
        final int p = mPrefs.getInt("current_position", 0);
        if (c != -1) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    InputStream local = null;
                    try {
                        local = openFileInput("currentfile");
                        EpubExtractor epubExtractor = new EpubExtractor(local).invoke();
                        tv.init(epubExtractor.getChapters(),epubExtractor.getAuthor(),epubExtractor.getTitle());
                        stopTV();
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    Log.i("Main", "Initializing to Chapter " + String.valueOf(c));
                    tv.cc = null;
                    tv.forceChapter(c);
                    tv.forcePosition(p);
                }
            }).start();

        }

    }

    protected void onPause() {
        super.onPause();
        SharedPreferences.Editor ed = mPrefs.edit();
        ed.putInt("current_position", tv.getCurrentPosition());
        ed.putInt("current_chapter", tv.getCurrent_chapter());
        Log.i("Main", "Saving chapter to " + String.valueOf(tv.getCurrent_chapter() + " position " + String.valueOf(tv.getCurrentPosition())));
        ed.commit();
    }

    @Override
    protected void onResume() {
        super.onResume();

    }
    @Override
    protected void onStop(){
        super.onStop();

    }

    @Override
    protected void onStart(){
        super.onStart();
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
    private static final int BUFFER_SIZE = 8192;

    /**
     * Reads all bytes from an input stream and writes them to an output stream.
     */
    private long copy(InputStream source, OutputStream sink)
            throws IOException
    {
        long nread = 0L;
        byte[] buf = new byte[BUFFER_SIZE];
        int n;
        while ((n = source.read(buf)) > 0) {
            sink.write(buf, 0, n);
            nread += n;
        }
        return nread;
    }
    private void loadFile( final InputStream in) {
        Log.i("Main", "Loading ");
        announce("Loading File");
        try {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        String filename = "currentfile";
                        FileOutputStream outputStream;

                        try {
                            outputStream = openFileOutput(filename, Context.MODE_PRIVATE);
                            copy(in,outputStream);
                            outputStream.close();
                            in.close();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        InputStream local = openFileInput("currentfile");
                        EpubExtractor epubExtractor = new EpubExtractor(local).invoke();
                        tv.init(epubExtractor.getChapters(),epubExtractor.getAuthor(),epubExtractor.getTitle());
                        stopTV();

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void running(Boolean running) {
        if (running) {
            View decorView = getWindow().getDecorView();
// Hide the status bar.
            int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN + View.SYSTEM_UI_FLAG_LAYOUT_STABLE + View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
            decorView.setSystemUiVisibility(uiOptions);
            //getActionBar().hide();
        } else {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN + View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        }
    }
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

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        switch (requestCode) {
            case ACTIVITY_CHOOSE_FILE: {
                if (resultCode == RESULT_OK) {
                    Uri uri = data.getData();
                    try {
                        final InputStream inputStream = getContentResolver().openInputStream(uri);
                        loadFile(inputStream);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }

                }
                break;
            }
            case ACTIVITY_CHOOSE_CHAPTER: {
                if (resultCode == RESULT_OK) {
                    int chosen_chapter = data.getIntExtra("result", 0);
                    tv.forceChapter(chosen_chapter);
                    tv.forcePosition(0);

                    announce("Chapter: " + String.valueOf(chosen_chapter + 2));
                    stopTV();
                }
                break;
            }
            case ACTIVITY_BROWSE_LIBRARY: {
                if (resultCode == RESULT_OK) {
                    String filePath = data.getStringExtra("result");
                    //loadFile(filePath);
                    break;
                }
            }
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_activity_action, menu);
        chapterfield = menu.findItem(R.id.Chapter);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case action_choose_file:
                Intent chooseFile;
                Intent intent;
                chooseFile = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                chooseFile.setType("application/epub+zip");
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
                    InputStream in = getAssets().open("habits.epub");
                    announce("Loading File");
                    EpubExtractor epubExtractor = new EpubExtractor(in).invoke();
                    tv.init(epubExtractor.getChapters(),epubExtractor.getAuthor(),epubExtractor.getTitle());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return true;
            case action_choose_library:
                //Intent browseLibrary;
                //browseLibrary = new Intent(getApplicationContext(), LibraryBrowser.class);
                //chooseChapter.putExtra("chapters", chapters);
                //startActivityForResult(browseLibrary, ACTIVITY_BROWSE_LIBRARY);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private class EpubExtractor {
        private InputStream in;
        private String title;
        private String author;
        private ArrayList<String[]> chapters;

        public EpubExtractor(InputStream in) {
            this.in = in;
        }

        public String getTitle() {
            return title;
        }

        public String getAuthor() {
            return author;
        }

        public ArrayList<String[]> getChapters() {
            return chapters;
        }
        public String extract_from_epub(Resource resource) {
            //Decode epub content
            String decoded = null;
            try {
                decoded = new String(resource.getData(), resource.getInputEncoding());
            } catch (IOException e) {
                Log.e("Error", "Failed to decode");
                e.printStackTrace();
            }
            if (decoded.contains("<body")) {
                decoded = decoded.substring(decoded.indexOf("<body"));
            } else {
                decoded = " ";
            }
            //Split it
            decoded = Html.fromHtml(decoded).toString().replaceAll("(?s)<!--.*?-->", "");
            return decoded;
        }
        public EpubExtractor invoke() throws IOException {
            Book book = (new EpubReader()).readEpub(in);
            title = book.getMetadata().getTitles().get(0);
            author = book.getMetadata().getAuthors().get(0).toString();
            chapters = new ArrayList<String[]>(book.getSpine().getSpineReferences().size());
            for (int i = 0; i < book.getSpine().getSpineReferences().size(); i++)
                chapters.add(null);

            for(int c=0;c<chapters.size();c++) {
                String decoded = extract_from_epub(book.getSpine().getSpineReferences().get(c).getResource());
                chapters.set(c, decoded.split("\\s"));
            }
            return this;
        }
    }
}
