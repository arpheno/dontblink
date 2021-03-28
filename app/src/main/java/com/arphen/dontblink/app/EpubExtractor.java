package com.arphen.dontblink.app;

import android.text.Html;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedList;

import nl.siegmann.epublib.domain.Book;
import nl.siegmann.epublib.domain.Metadata;
import nl.siegmann.epublib.domain.Resource;
import nl.siegmann.epublib.epub.EpubReader;

import static com.arphen.dontblink.app.TokenizerKt.tokenize;

/**
 * Created by swozny on 07.04.18.
 */
class EpubExtractor {
    private InputStream in;
    private String title;
    private Book book;
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

    public int getNumberOfWords() {
        int s = 0;
        for (String[] n : getChapters()) {
            s += n.length;
        }
        return s;
    }

    public ArrayList<String[]> getChapters() {

        int size = book.getSpine().getSpineReferences().size();
        chapters = new ArrayList<String[]>(size);

        for (int c = 0; c < size; c++) {
            String decoded = extract_from_epub(book.getSpine().getSpineReferences().get(c).getResource());
            String[] text = decoded.split("\\s");
            LinkedList<String> temp = tokenize(text);
            String[] tokens = new String[temp.size()];
            chapters.add(temp.toArray(tokens));
        }

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
        book = (new EpubReader()).readEpub(in);
        title = book.getMetadata().getTitles().get(0);
        author = book.getMetadata().getAuthors().get(0).toString();
        return this;
    }

}
