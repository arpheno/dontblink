package com.arphen.dontblink.app;

import android.text.Html;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import nl.siegmann.epublib.domain.Book;
import nl.siegmann.epublib.domain.Resource;
import nl.siegmann.epublib.epub.EpubReader;

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

        chapters = new ArrayList<String[]>(book.getSpine().getSpineReferences().size());
        for (int i = 0; i < book.getSpine().getSpineReferences().size(); i++)
            chapters.add(null);

        for (int c = 0; c < chapters.size(); c++) {
            String decoded = extract_from_epub(book.getSpine().getSpineReferences().get(c).getResource());
            chapters.set(c, decoded.split("\\s"));
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
