package com.arphen.dontblink.app;

/**
 * Created by swozny on 08.04.18.
 */

public class LibraryBook {
    public String author;
    public String title;
    public String path;
    public String rating;
    public int length;

    public LibraryBook(String author, String title, String path, String rating, int length) {
        this.author = author;
        this.title = title;
        this.path = path;
        this.rating = rating;
        this.length = length;
    }

    public int hours() {
        return this.length / 30000;
    }

    public int minutes() {
        return (this.length % 30000) / 500;
    }

    public String author() {
        return this.author;
    }

    public String title() {
        return this.title;
    }

    public String rating() {
        return this.rating;
    }

    public int length() {
        return this.length;
    }

    public String path() {
        return this.path;
    }
}
