package com.arphen.ownspritz.app;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class SpritzView extends RelativeLayout{
    private TextView left;
    private TextView middle;
    private TextView right;

    public SpritzView(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater.from(context).inflate(R.layout.spritzview, this, true);
        left= (TextView) findViewById(R.id.textView);
        middle= (TextView) findViewById(R.id.textView2);
        right= (TextView) findViewById(R.id.textView3);
    }
    public void setText(String word){
        int piv=m_getpivot(word);
        left.setText(word.substring(0,piv));
        middle.setText(word.substring(piv,piv+1));
        right.setText(word.substring(piv+1));
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

    protected void onDraw (Canvas canvas) {
        super.onDraw(canvas);
    }

}
