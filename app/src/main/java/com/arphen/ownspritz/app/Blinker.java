package com.arphen.ownspritz.app;

import android.os.AsyncTask;
import android.text.Html;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import nl.siegmann.epublib.domain.Book;
import nl.siegmann.epublib.domain.Resource;
import nl.siegmann.epublib.domain.SpineReference;
import nl.siegmann.epublib.epub.EpubReader;

/**
 * Created by Arphen on 14.04.2014.
 */
public class Blinker {
    public int m_wordindex = 0;
    private Book m_book;
    private ArrayList<String[]> m_chapters;
    private int m_chapterindex = 0;
    private String[] m_chapter;
    private double m_wpm = 500;
    private boolean m_init = false;
    private ArrayList<OnChapterChangedListener> chapterChangedListeners;
    public Blinker() {
        chapterChangedListeners= new ArrayList<OnChapterChangedListener>();
    }
    public void addChapterChangedListener(OnChapterChangedListener listener){
        chapterChangedListeners.add(listener);
    }
    public String getAuthorTitle(){
        String tmp= String.valueOf(m_book.getMetadata().getAuthors());
        String tmp2=tmp+m_book.getMetadata().getTitles();
        return tmp2;
    }
public void init(InputStream epubInputStream) throws IOException {
    m_book = (new EpubReader()).readEpub(epubInputStream);
    m_chapters = new ArrayList<String[]>();
    for (SpineReference ref: m_book.getSpine().getSpineReferences()) {
        Log.i("Chapter","loading");
        new LoadChapter().execute(ref);
    }
}
    public static List<String> splitEqually(String text, int size) {
        // Give the list the right capacity to start with. You could use an array
        // instead if you wanted.
        List<String> ret = new ArrayList<String>((text.length() + size - 1) / size);

        for (int start = 0; start < text.length(); start += size) {
            ret.add(text.substring(start, Math.min(text.length(), start + size)));
        }
        return ret;
    }
    public String getPreview(int which){
        int backup = m_wordindex;
        String result="";
        int look=20;
        if(which==0){
            m_wordindex-=look+1;
            for(int i=0;i<look;i++){
                result+=next(1);
                result+=" ";
            }
            return result;
        }else{
            m_wordindex++;
            for(int i=0;i<look;i++){
                result+=next(1);
                result+=" ";
            }
            m_wordindex=backup;
            return result;
        }
    }
    public String next(int dir) {
        if(!m_init)return " ";
        m_wordindex += dir;
        if (m_wordindex >= m_chapter.length) {
            if(m_chapterindex<m_chapters.size()-1) {
                m_chapterindex++;
                setChapter(m_chapterindex);
                m_wordindex = 0;
            }else{return "Book Finished";}
        } else if (m_wordindex < 0) {
            if(m_chapterindex>0) {
                m_chapterindex--;
                setChapter(m_chapterindex);
                m_wordindex = m_chapter.length - 1;
            }else{
                return "";
            }
        }
        return m_chapter[m_wordindex];
    }

    public int getLengthOfChapter() {
        return m_chapter.length;
    }

    public int getBooklength() {
        return m_chapters.size();
    }

    public void m_logBook() {
        Log.i("Blinker", "Book author: " + m_book.getMetadata().getAuthors());
        Log.i("Blinker", "Book title: " + m_book.getMetadata().getTitles());
        Log.i("Blinker", "Chapter: " + String.valueOf(m_chapterindex));
        Log.i("Blinker", "Word: " + String.valueOf(m_wordindex));
    }

    public String setM_wordindex(int i) {
        m_wordindex = i-1;
        return next(1);
    }

    public void setChapter(int c) {
        Log.i("Blinker", "Switching to Chapter" + String.valueOf(c));
        m_chapterindex = c;
        m_chapter = m_chapters.get(c);//breakUpWords(m_chapters.get(c));
        m_wordindex = 0;
        for (OnChapterChangedListener l: chapterChangedListeners){
            l.onChapterChanged(c,m_chapter.length);
        }
    }

    private String[] breakUpWords(String[] what) {
        ArrayList<String> cont = new ArrayList<String>(Arrays.asList(what));
        for (int i = 0; i < cont.size(); i++) {
            String word = cont.remove(i);
            if (word.length() > 12) {
                for (int start = 0; start < word.length(); start += 7) {
                    cont.addAll(i, splitEqually(word, 7));
                }
            }
        }
        return cont.toArray(new String[cont.size()]);
    }
    public boolean isM_init(){return m_init;}
    class LoadChapter extends AsyncTask<SpineReference, Integer, Long> {
        protected void onProgressUpdate(Integer... progress) {

        }

        @Override
        protected Long doInBackground(SpineReference... spineReferences) {
            Resource resource = spineReferences[0].getResource();
            String decoded = null;
            try {
                decoded = new String(resource.getData(), resource.getInputEncoding());
            } catch (IOException e) {
                Log.e("Error","Failed to decode");
                e.printStackTrace();
            }
            if (decoded.contains("<body")) {
                decoded = decoded.substring(decoded.indexOf("<body"));
            } else {
                decoded = " ";
            }
            decoded = Html.fromHtml(decoded).toString().replace("\n", " ").replaceAll("(?s)<!--.*?-->", "");
            String[] temp = decoded.split("([\\s(?<=[.,?!-])])");
            if(m_chapters.isEmpty()) {
                m_chapters.add(temp);
                setChapter(0);
            }else{
                m_chapters.add(temp);
            }
            return null;
        }

        protected void onPostExecute(Long result) {
            Log.i("Async","Setting init to true");
            m_init=true;
        }



    }
}
