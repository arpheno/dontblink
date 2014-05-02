package com.arphen.ownspritz.app;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class BlinkView extends RelativeLayout implements View.OnClickListener {
    public Blinker gen;
    public Runnable cc;
    private Runnable setAuthorAndTitle;
    private TextView left;
    private TextView middle;
    private TextView right;
    private Thread timer;
    private double m_wpm = 500;
    private boolean m_playing;
    private String m_word;
    private boolean m_init;
    private ArrayList<RunningListener> runningListeners;
    private int m_pos=0;
    private int m_chapter=0; // Needs to be initialized to -1 so we get a chapter changed event.
    private ArrayList<OnChapterChangedListener> chapterChangedListeners;
    private long lastChapterChanged;

    public BlinkView(Context context, AttributeSet attrs) {
        super(context, attrs);
        View view = LayoutInflater.from(context).inflate(R.layout.spritzview, this, true);
        gen = new Blinker();
        left = (TextView) findViewById(R.id.textView);
        middle = (TextView) findViewById(R.id.textView2);
        right = (TextView) findViewById(R.id.textView3);
        runningListeners=new ArrayList<RunningListener>();
        chapterChangedListeners= new ArrayList<OnChapterChangedListener>();
        lastChapterChanged=System.currentTimeMillis();
        initRunnables();
    }

    /**
     * Gets amount words from the book starting at current position + offset
     * @param offset how far to offset(useful for previewing already read items)
     * @param amount how many words should be returned
     * @return A String with amount of words.
     */
    public String getPreview(int offset, int amount) {
            String result = "";
            int temppos = m_pos+offset;
            int tempchap =m_chapter;
            for(int i=0;i<amount;i++,temppos++){
                result+=gen.getWord(tempchap, temppos)+" ";
            }
        return result;
    }
    public void addRunningListener(RunningListener listener){
        runningListeners.add(listener);
    }
    public void addChapterChangedListener(OnChapterChangedListener listener){
        chapterChangedListeners.add(listener);
    }

    public boolean is_init() {
        return gen.isM_init();
    }

    public boolean is_playing() {
        return m_playing;
    }

    public void changeWpm(double wpm) {
        m_wpm = wpm;
    }
    public void init(InputStream in) throws IOException {
        init(in, 0);
    }

    public void init(InputStream in, int c) throws IOException {
        Log.i("Blinker", "Initializing Blinker");
        gen.init(in);
        post(setAuthorAndTitle);
        forceChapter(c);
    }
    public void initRunnables() {
        setAuthorAndTitle = new Runnable() {
            @Override
            public void run() {
                Activity host = (Activity) getContext();
                if (host != null) {
                    host.getActionBar().setTitle(gen.getTitle());
                    host.getActionBar().setSubtitle(gen.getAuthor());
                }
            }
        };
    }
    /**
     * Set's the current display content to word.
     * In accordance with the length of the word one character
     * will be chosen as pivot and marked red.
     *
     * @param word
     */
    public void setText(String word) {
        switch (word.length()) {
            case 13:
            case 12:
            case 11:
            case 10:
                left.setText(word.substring(0, 3));
                middle.setText(word.substring(3, 4));
                right.setText(word.substring(4));
                break;
            case 9:
            case 8:
            case 7:
            case 6:
                left.setText(word.substring(0, 2));
                middle.setText(word.substring(2, 3));
                right.setText(word.substring(3));
                break;
            case 5:
            case 4:
            case 3:
                left.setText(word.substring(0, 1));
                middle.setText(word.substring(1,2));
                right.setText(word.substring(2));
                break;
            case 2:
                right.setText("");
                left.setText(word.substring(0, 1));
                middle.setText(word.substring(1,2));
                break;
            case 1:
                right.setText("");
                left.setText("");
                middle.setText(word);
                break;
            case 0:
                break;
            default:
                left.setText(word.substring(0, 4));
                middle.setText(word.substring(4, 5));
                right.setText(word.substring(5));
                break;

        }
    }

    public void setChapter(int c) {

    }

    private String next(){
        m_pos++;
        String temp=gen.getWord(m_chapter,m_pos); // Try to get next word
        if(temp==""){
            forceChapter(m_chapter + 1);
            if(m_chapter==getNumberOfChapters()){
                stop(); // Stop playing and notify listeners
                return "End of Book";
            }
        }
        return temp;
    }
    public void run() {
        for (RunningListener l: runningListeners){
            l.running(true);
        }
        m_playing = true;
        timer = new Thread(new Runnable() {
            @Override
            public void run() {
                while (m_playing) {
                    m_word = next();
                    //sb.setProgress(m_pos);
                    post(new Runnable() {
                        @Override
                        public void run() {
                            setText(m_word);
                        }
                    });
                    try {
                        double delaymult = 1;
                        if (m_word.contains("."))
                            delaymult *= 1.7;
                        if (m_word.contains(","))
                            delaymult *= 1.3;
                        if (m_word.length() > 6)
                            delaymult *= 1.3;
                        if (m_word.length() > 10)
                            delaymult *= 1.3;
                        Thread.sleep((long) (delaymult * 60000 / m_wpm));

                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        timer.start();
    }

    public void setPosition(int position) {
        if (gen.isM_init()) {
            m_pos=position;
            if(m_pos==0)
                forceChapter(m_chapter-1);
            setText(next());
        }
    }
    public void forceChapter(final int c){
        forceChapter(c, 0);
    }

    public void forceChapter(final int c, final int p) {
        if(cc!=null)
            return;
        Log.i("TV", "Switching to chapter " + String.valueOf(c));
        gen.lazy_load_chapter(c,0);
        m_chapter=c;
        m_pos = p;
        cc=new Runnable() {
            @Override
            public void run() {
                if (!gen.isM_init()) return;
                final String text = gen.getWord(m_chapter, p);
                setText(text);
                Log.i("BlinkView", "Notifying chapterchangedlisteners");
                for (OnChapterChangedListener l: chapterChangedListeners){
                    l.onChapterChanged(m_chapter,gen.getLengthOfChapter(m_chapter));
                }
                cc = null;
            }
        };
        postDelayed(cc,300);
    }

    public int getM_chapter() {
        return m_chapter;
    }
    public int getNumberOfChapters() {
        if (!gen.isM_init())
            return 0;
        return gen.getBooklength();
    }
    public void stop() {
        for (RunningListener l: runningListeners) {
            l.running(false);
        }
        m_playing = false;
    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
    }

    @Override
    public void onClick(View view) {

    }

    /**
     * Called by the progressbar to determine current progress
     * @return
     */
    public int getCurrentPosition() {
        if (gen.isM_init())
            return m_pos;
        return 0;
    }


}
