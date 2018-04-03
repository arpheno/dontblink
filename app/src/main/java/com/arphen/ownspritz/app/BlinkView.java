package com.arphen.ownspritz.app;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;


public class BlinkView extends RelativeLayout implements View.OnClickListener {
    public Runnable cc;
    private Runnable setAuthorAndTitle;
    private TextView left;
    private TextView middle;
    private TextView right;
    private Thread timer;
    private double m_wpm = 500;
    private boolean m_playing;
    private String m_word;
    private String author;


    private ArrayList<String[]> book_as_list_of_arrays_of_words;

    private boolean m_init;
    private ArrayList<RunningListener> runningListeners;
    private int pos = 0;
    private int current_chapter = 0; // Needs to be initialized to -1 so we get a chapter changed event.
    private ArrayList<OnChapterChangedListener> chapterChangedListeners;
    private long lastChapterChanged;
    private String title;

    public BlinkView(Context context, AttributeSet attrs) {
        super(context, attrs);
        View view = LayoutInflater.from(context).inflate(R.layout.spritzview, this, true);
        book_as_list_of_arrays_of_words=new ArrayList<String[]>();
        left = (TextView) findViewById(R.id.textView);
        middle = (TextView) findViewById(R.id.textView2);
        right = (TextView) findViewById(R.id.textView3);
        runningListeners = new ArrayList<RunningListener>();
        chapterChangedListeners = new ArrayList<OnChapterChangedListener>();
        lastChapterChanged = System.currentTimeMillis();
        initRunnables();
    }


    public String getPreview(int offset, int amount) {
            StringBuilder result = new StringBuilder();
            int temppos = pos +offset;
            while(temppos<0){
                temppos++;
                amount--;
            }
            while(temppos+amount >= book_as_list_of_arrays_of_words.get(current_chapter).length){
                amount--;
            }


            for(int i=0;i<amount;i++){
                temppos++;
                result.append(book_as_list_of_arrays_of_words.get(current_chapter)[temppos]).append(" ");
            }
        return result.toString();
    }

    public void addRunningListener(RunningListener listener) {
        runningListeners.add(listener);
    }

    public void addChapterChangedListener(OnChapterChangedListener listener) {
        chapterChangedListeners.add(listener);
    }

    public boolean is_init() {
        return m_init;
    }

    public boolean is_playing() {
        return m_playing;
    }

    public void changeWpm(double wpm) {
        m_wpm = wpm;
    }

    public void init(ArrayList<String[]> book,String author,String title) throws IOException {
        Log.i("Blinker", "Initializing Blinker");

        this.book_as_list_of_arrays_of_words = book;
        this.author=author;
        this.title = title;
        post(setAuthorAndTitle);
        this.pos = 0;
        forceChapter(this.pos);
        this.m_init = true;
        Log.i("Blinker", "Initializing Blinker finished");
    }

    public void initRunnables() {
        setAuthorAndTitle = new Runnable() {
            @Override
            public void run() {
                Activity host = (Activity) getContext();
                if (host != null) {
                    host.getActionBar().setTitle(title);
                    host.getActionBar().setSubtitle(author);
                }
            }
        };
    }


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
                middle.setText(word.substring(1, 2));
                right.setText(word.substring(2));
                break;
            case 2:
                right.setText("");
                left.setText(word.substring(0, 1));
                middle.setText(word.substring(1, 2));
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


    private String next() {
        pos++;
        if (pos >= book_as_list_of_arrays_of_words.get(current_chapter).length) {
            Log.i("Main", "switching chapter");
            forceChapter(current_chapter + 1);
            pos = 0;
        }
        if (current_chapter == getNumberOfChapters()) {
            stop(); // Stop playing and notify listeners
            return "End of Book";
        }

        return book_as_list_of_arrays_of_words.get(current_chapter)[pos];

    }

    public void run() {
        for (RunningListener l : runningListeners) {
            l.running(true);
        }
        m_playing = true;
        timer = new Thread(new Runnable() {
            @Override
            public void run() {
                while (m_playing) {
                    m_word = next();
                    //sb.setProgress(pos);
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
    public int current_chapter_length(){
        return book_as_list_of_arrays_of_words.get(current_chapter).length;
    }
    public void setPosition(int position) {
        if (m_init) {
            forcePosition(position);
            if (pos == 0) {
                forceChapter(current_chapter - 1);
                forcePosition(0);
            }
            setText(next());
        }
    }




    public void forceChapter(final int c) {


        current_chapter = c;
        Log.i("BlinkView", "loaded text");
        final String text = book_as_list_of_arrays_of_words.get(current_chapter)[0];
        Log.i("BlinkView", "Notifying chapterchangedlisteners");
        for (OnChapterChangedListener l : chapterChangedListeners) {
            l.onChapterChanged(current_chapter, book_as_list_of_arrays_of_words.get(current_chapter).length);
        }

        cc = new Runnable() {
            @Override
            public void run() {
                setText(text);
            }
        };
        postDelayed(cc, 300);
    }

    public void forcePosition(int p) {
        pos = p;
    }

    public int getCurrent_chapter() {
        return current_chapter;
    }

    public int getNumberOfChapters() {
        if (!m_init)
            return 0;
        return book_as_list_of_arrays_of_words.size();
    }

    public void stop() {
        for (RunningListener l : runningListeners) {
            l.running(false);
        }
        m_playing = false;
    }


    @Override
    public void onClick(View view) {

    }

    public int getCurrentPosition() {
        if (m_init)
            return pos;
        return 0;
    }


}
