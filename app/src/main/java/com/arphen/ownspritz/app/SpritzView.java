package com.arphen.ownspritz.app;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class SpritzView extends RelativeLayout implements View.OnClickListener {
    private TextView left;
    private TextView middle;
    private TextView right;
    public Spritzer gen;
    private Thread timer;
    private double m_wpm = 500;
    private boolean m_playing;
    private String m_word;
    private boolean m_init;

    public SpritzView(Context context, AttributeSet attrs) {
        super(context, attrs);
        View view = LayoutInflater.from(context).inflate(R.layout.spritzview, this, true);
        left = (TextView) findViewById(R.id.textView);
        view.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!m_init)
                    return;
                if (m_playing)
                    stop();
                else
                    run();
            }
        });
        middle = (TextView) findViewById(R.id.textView2);
        right = (TextView) findViewById(R.id.textView3);
    }

    public void changeWpm(double wpm){
        m_wpm+=wpm;
}
    public void init(String path) throws IOException {
        Log.i("Spritzer", "Initializing Spritzer");
        InputStream in = new FileInputStream(path);
        gen = new Spritzer(in);
        m_init=true;
    }

    public void setText(String word) {
        int piv = m_getpivot(word);
        left.setText(word.substring(0, piv));
        middle.setText(word.substring(piv, piv + 1));
        right.setText(word.substring(piv + 1));
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
public void setChapter(int c){
    if(!m_init)return;
    gen.setChapter(c);
}
    public void run() {
        if(!m_init)return;
        m_playing=true;
         timer = new Thread(new Runnable() {
            @Override
            public void run() {
                while (m_playing) {
                    m_word=gen.next((int) Math.signum(m_wpm));
                    post(new Runnable() {
                        @Override
                        public void run() {
                            setText(m_word);
                        }
                    });
                    try {
                        double delaymult = 1;
                        if(m_word.contains("."))
                            delaymult*=2;
                        if(m_word.contains(","))
                            delaymult*=1.3;
                        if(m_word.length()>6)
                            delaymult*=1.6;
                        Thread.sleep((long) (delaymult*60000/m_wpm));

                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        timer.start();
    }
    public void setPosition(int position){
        if(m_init)
        gen.m_wordindex=position;
    }
    public int getNumberOfChapters(){
        if(!m_init)
            return 0;
        return gen.getBooklength();
    }
    public int getWpm(){
        return (int) m_wpm;
    }

    public void stop(){
        m_playing=false;
    }
    protected void onDraw (Canvas canvas) {
        super.onDraw(canvas);
    }

    @Override
    public void onClick(View view) {

    }
}
