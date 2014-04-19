package com.arphen.ownspritz.app;

import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import nl.siegmann.epublib.domain.Book;
import nl.siegmann.epublib.domain.Resource;
import nl.siegmann.epublib.epub.EpubReader;

/**
 * Created by Arphen on 14.04.2014.
 */
public class Spritzer {
    private Book m_book;
    private ArrayList<String[]> m_chapters;
    private int m_chapterindex=0;
    public int m_wordindex=0;
    private String[] m_chapter;
    private double m_wpm=500;
    public Spritzer(InputStream epubInputStream) throws IOException {
        m_book = (new EpubReader()).readEpub(epubInputStream);
        m_chapters=new ArrayList<String[]>();
        for(Resource resource :m_book.getTableOfContents().getAllUniqueResources()) {
            String decoded = new String(resource.getData(), resource.getInputEncoding());
            decoded = android.text.Html.fromHtml(decoded).toString();
            m_chapters.add(decoded.split("(?<=[\\s.,?!-])"));
        }
        setChapter(m_chapterindex);
    }
    public String next(int dir){
        m_wordindex+=dir;
        if(m_wordindex>=m_chapter.length){
            m_chapterindex++;
            setChapter(m_chapterindex);
            m_wordindex=0;
        }else if(m_wordindex+dir<0){
            m_chapterindex--;
            setChapter(m_chapterindex);
            m_wordindex=m_chapter.length-1;
        }
       return m_chapter[m_wordindex];
    }

    public int getLengthOfChapter(){
        return m_chapter.length;
    }
    public int getBooklength(){
        return m_chapters.size();
    }
    public void m_logBook(){
        Log.i("Spritzer", "Book author: " + m_book.getMetadata().getAuthors());
        Log.i("Spritzer", "Book title: " + m_book.getMetadata().getTitles());
        Log.i("Spritzer", "Chapter: " + String.valueOf(m_chapterindex));
        Log.i("Spritzer", "Word: " + String.valueOf(m_wordindex));
    }
    public void setChapter(int c){
        Log.i("Spritzer", "Switching to Chapter"+String.valueOf(c));
        m_chapterindex=c;
        m_chapter=m_chapters.get(c);//breakUpWords(m_chapters.get(c));
        m_wordindex=0;
    }
    private String[] breakUpWords(String[] what){
        ArrayList<String> cont = new ArrayList<String>(Arrays.asList(what));
        for(int i=0;i<cont.size();i++){
            String word=cont.remove(i);
            if(word.length()>12){
                for (int start = 0; start < word.length(); start += 7) {
                    cont.addAll(i,splitEqually(word,7));
                }
            }
        }
        return cont.toArray(new String[cont.size()]);
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
}
