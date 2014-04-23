package com.arphen.ownspritz.app;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;

public class BlinkView extends RelativeLayout implements View.OnClickListener {
    public Blinker gen;
    private TextView left;
    private TextView middle;
    private BlinkProgressBar sb;
    private TextView right;
    private Thread timer;
    private double m_wpm = 500;
    private boolean m_playing;
    private String m_word;
    private boolean m_init;
    private Paint paint;

    public BlinkView(Context context, AttributeSet attrs) {
        super(context, attrs);
        View view = LayoutInflater.from(context).inflate(R.layout.spritzview, this, true);
        gen = new Blinker();
        left = (TextView) findViewById(R.id.textView);
        middle = (TextView) findViewById(R.id.textView2);
        right = (TextView) findViewById(R.id.textView3);
    }
    public void setProgressBar(BlinkProgressBar k){
        sb=k;
    }
    public String getPreview(int which) {
        if (which == 0) {
            return gen.getPreview(0);
        } else {
            return gen.getPreview(1);
        }
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
        Log.i("Blinker", "Initializing Blinker");
        gen.init(in);
    }

    public void setText(String word) {
        int piv = m_getpivot(word);
        try{
            left.setText(word.substring(0, piv));
        }catch (StringIndexOutOfBoundsException a){
            left.setText("");
        }
        try{
            middle.setText(word.substring(piv, piv + 1));
        }catch (StringIndexOutOfBoundsException a){
            middle .setText("");
        }

        try {
            right.setText(word.substring(piv + 1));
        }catch (StringIndexOutOfBoundsException a){
        right.setText("");

        }
        }

    private int m_getpivot(String s) {
        switch (s.length()) {
            case 0:
            case 1:
                return 0;
            case 2:
            case 3:
            case 4:
            case 5:
                return 1;
            case 6:
            case 7:
            case 8:
            case 9:
                return 2;
            case 10:
            case 11:
            case 12:
            case 13:
                return 3;
            default:
                return 4;
        }
    }

    public void setChapter(int c) {
        if (!gen.isM_init()) return;
        gen.setChapter(c);
    }

    public void run() {
        if (!gen.isM_init()) return;
        m_playing = true;
        timer = new Thread(new Runnable() {
            @Override
            public void run() {
                while (m_playing) {
                    m_word = gen.next((int) Math.signum(m_wpm));
                    sb.setProgress(gen.m_wordindex);
                    post(new Runnable() {
                        @Override
                        public void run() {
                            setText(m_word);
                        }
                    });
                    try {
                        double delaymult = 1;
                        if (m_word.contains("."))
                            delaymult *= 2;
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
                setText(gen.setM_wordindex(position));
        }
    }

    public int getNumberOfChapters() {
        if (!gen.isM_init())
            return 0;
        return gen.getBooklength();
    }

    public int getWpm() {
        return (int) m_wpm;
    }

    public void stop() {
        m_playing = false;
    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
    }

    @Override
    public void onClick(View view) {

    }

    public int getCurrentPosition() {
        if (gen.isM_init())
            return gen.m_wordindex;
        return 0;
    }

    public int getLengthOfChapter() {
        if (gen.isM_init())
            return gen.getLengthOfChapter();
        return 5;
    }
}
