package com.arphen.dontblink.app;

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
import java.util.HashSet;
import java.util.Set;


public class BlinkView extends RelativeLayout implements View.OnClickListener {
    public Runnable cc;
    public ArrayList<String[]> book_as_list_of_arrays_of_words = new ArrayList<String[]>();
    private Runnable setAuthorAndTitle;
    private TextView left;
    private TextView middle;
    private TextView right;
    private Thread timer;
    private double m_wpm = 500;
    private boolean m_playing;
    private String m_word;
    private String author;
    private Set<Integer> run_limits = new HashSet<Integer>();
    private boolean m_init = false;
    private int current_position = 0;
    private int current_chapter = 0; // Needs to be initialized to -1 so we get a chapter changed event.
    private String title;
    private ArrayList<OnChapterChangedListener> chapterChangedListeners;
    private ArrayList<RunningListener> runningListeners;
    private ArrayList<OnAchievementListener> achievementListeners = new ArrayList<OnAchievementListener>();
    private int current_word_run = 0;

    public BlinkView(Context context, AttributeSet attrs) {
        super(context, attrs);
        run_limits.add(1000);
        run_limits.add(2500);
        run_limits.add(5000);
        run_limits.add(10000);
        run_limits.add(20000);
        View view = LayoutInflater.from(context).inflate(R.layout.spritzview, this, true);
        book_as_list_of_arrays_of_words = new ArrayList<String[]>();
        left = (TextView) findViewById(R.id.textView);
        middle = (TextView) findViewById(R.id.textView2);
        right = (TextView) findViewById(R.id.textView3);
        runningListeners = new ArrayList<RunningListener>();
        chapterChangedListeners = new ArrayList<OnChapterChangedListener>();
        initRunnables();
    }

    public int getChapterLength(int chapter) {
        return book_as_list_of_arrays_of_words.get(chapter).length;
    }

    public int getBookLength() {
        return book_as_list_of_arrays_of_words.stream().map((a) -> a.length).reduce((a, b) -> a + b).get();
    }

    public String getPreview(int offset, int amount) {
        StringBuilder result = new StringBuilder();
        int temppos = current_position + offset;
        while (temppos < 0) {
            temppos++;
            amount--;
        }
        while (temppos + amount >= book_as_list_of_arrays_of_words.get(current_chapter).length) {
            amount--;
        }


        for (int i = 0; i < amount; i++) {
            temppos++;
            result.append(book_as_list_of_arrays_of_words.get(current_chapter)[temppos]).append(" ");
        }
        return result.toString();
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

    public void init(ArrayList<String[]> book, String author, String title) throws IOException {
        Log.i("Blinker", "Initializing Blinker");

        this.book_as_list_of_arrays_of_words = book;
        this.author = author;
        this.title = title;
        post(setAuthorAndTitle);
        this.current_position = 0;
        forceChapter(this.current_position);
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
        current_word_run++;
        current_word_run++;
        if (run_limits.contains(current_word_run)) {
            for (OnAchievementListener l : achievementListeners) {
                changeWpm(m_wpm * 1.03);
                l.onAchievement(String.format("%d words streak!", current_word_run));
            }

        }

        current_position++;
        if (current_position >= book_as_list_of_arrays_of_words.get(current_chapter).length) {
            Log.i("Main", "switching chapter");
            forceChapter(current_chapter + 1);
            current_position = 0;
        }
        if (current_chapter == getNumberOfChapters()) {
            stop(); // Stop playing and notify listeners
            return "End of Book";
        }
        return book_as_list_of_arrays_of_words.get(current_chapter)[current_position];


    }

    public void run() {
        for (RunningListener l : runningListeners) {
            l.running(true);
        }
        current_word_run = 0;
        m_playing = true;
        timer = new Thread(new Runnable() {
            @Override
            public void run() {
                while (m_playing) {
                    m_word = next();
                    //seekBar.setProgress(current_position);
                    post(new Runnable() {
                        @Override
                        public void run() {
                            setText(m_word);
                        }
                    });
                    try {
                        Thread.sleep((long) (60000 / m_wpm));

                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        timer.start();
    }

    public void setPosition(int position) {
        if (m_init) {
            forcePosition(position);
            if (current_position == 0) {
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
        current_position = p;
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
        return current_position;

    }


    public void addAchievementListener(OnAchievementListener listener) {
        achievementListeners.add(listener);
    }

    public void addRunningListener(RunningListener listener) {
        runningListeners.add(listener);
    }

    public void addChapterChangedListener(OnChapterChangedListener listener) {
        chapterChangedListeners.add(listener);
    }
}
