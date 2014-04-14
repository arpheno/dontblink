package com.arphen.ownspritz.app;

import android.app.Activity;
import android.content.Intent;
import android.content.res.AssetManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import com.arphen.ownspritz.app.util.SystemUiHider;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import nl.siegmann.epublib.domain.Book;
import nl.siegmann.epublib.domain.Resource;
import nl.siegmann.epublib.epub.EpubReader;

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
    private float wpm;
    private TextView wpmtv;
    private SpritzView tv;
    private SeekBar sb;
    private boolean m_running;
    private String[] m_content;
    private int m_direction=1;
    private int m_pos=0;
    final int ACTIVITY_CHOOSE_FILE = 1;
    final int ACTIVITY_CHOOSE_CHAPTER=888;
    final Handler myHandler = new Handler();
    private Timer myTimer;
    private boolean m_timer_up=false;
    private Button chpter;
    private ArrayList<String[]> m_chapters;
    private int m_chapter=0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fullscreen);
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

        // Ebook init
        m_running=false;
        sb.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {
                        if (!arg2)
                            return;
                        m_pos = arg1;
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {
                        m_stop();
                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                        m_run();
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
                chooseChapter.putExtra("chapters", m_chapters.size());
                startActivityForResult(chooseChapter, ACTIVITY_CHOOSE_CHAPTER);
            }
        });
        AssetManager assetManager = getAssets();
        try {
            InputStream epubInputStream = assetManager
                    .open("book.epub");
            m_readchapters(epubInputStream);
            m_setcontent(m_chapters.get(m_chapter));
        } catch (IOException e) {
            Log.e("epublib", e.getMessage());
        }
    }
    private void m_setcontent(String[] text){
        sb.setMax(text.length);
        sb.setProgress(0);
        m_content=text;
    }
    private void m_readchapters(InputStream epubInputStream) throws IOException {
        Book book = (new EpubReader()).readEpub(epubInputStream);
        Log.i("epublib", "author(s): " + book.getMetadata().getAuthors());
        Log.i("epublib", "titles: " + book.getMetadata().getTitles());
        m_chapters=new ArrayList<String[]>();
        m_pos=0;
        wpm=500;
        for(Resource resource :book.getTableOfContents().getAllUniqueResources()) {
            String decoded = new String(resource.getData(), "UTF-8");
            decoded = android.text.Html.fromHtml(decoded).toString();
            m_chapters.add(decoded.split("(?<=[\\s.,?!-])"));
        }
        // Log TOC lengths
        for(Resource resource :book.getTableOfContents().getAllUniqueResources()) {
            String decoded = new String(resource.getData(), "UTF-8");
            Log.i("epublib", String.valueOf(decoded.length()));
        }

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
                        InputStream in = new FileInputStream(filePath);
                        m_readchapters(in);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            case ACTIVITY_CHOOSE_CHAPTER: {
                if (resultCode == RESULT_OK){
                    int c = data.getIntExtra("result",0);
                    m_chapter=c;
                    m_setcontent(m_chapters.get(c));
                    m_pos=0;
                }

            }
        }
    }
    private void UpdateGUI() {
        myHandler.post(myRunnable);
    }


    final Runnable myRunnable = new Runnable() {
        public void run() {
            if(m_pos+m_direction>m_content.length){
                m_chapter++;
                m_setcontent(m_chapters.get(m_chapter));
                m_pos=0;
            }else if(m_pos+m_direction<0){
                m_chapter--;
                m_setcontent(m_chapters.get(m_chapter));
                m_pos=m_chapters.get(m_chapter).length;
            }
            m_pos+=m_direction;
            sb.setProgress(m_pos);
            tv.setText(m_content[m_pos]);
        }
    };


    @Override
    public boolean onDown(MotionEvent motionEvent) {
return false;    }

    private void m_run() {
        Log.i("Timer", "Starting Timer");
        m_running=true;
        m_settimer();
    }

    private void m_stop() {
        m_running=false;
        Log.i("Timer", "Stopping Timer");
        myTimer.cancel();
    }

    @Override
    public void onShowPress(MotionEvent motionEvent) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent motionEvent) {
        Log.i("Progress",String.valueOf(sb.getProgress()));
        if(m_running)
            m_stop();
        else
            m_run();
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent motionEvent, MotionEvent motionEvent2, float v, float v2) {
        return false;
    }
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        Log.d("lol", "SCROLL velocity: ");
        return gestureScanner.onTouchEvent(event);
    }
    @Override
    public void onLongPress(MotionEvent motionEvent) {

    }
public void m_settimer(){
    myTimer = new Timer();
    m_timer_up=true;
    int rate= (int) (60000/wpm);
    if(rate<0) {
        rate *= -1;
        m_direction = -1;
    }else{
        m_direction=1;
    }
    myTimer.schedule(new TimerTask() {
        @Override
        public void run() {UpdateGUI();}
    }, rate, rate);
}
    @Override
    public boolean onFling(MotionEvent motionEvent, MotionEvent motionEvent2, float v, float v2) {
            wpm += -v2/5000*200;
            wpmtv.setText(String.valueOf(wpm));
        if(m_running==true){
            myTimer.cancel();
            Log.i("Timer", "Restarting Timer");
            m_settimer();
        }
        return false;
    }
}
