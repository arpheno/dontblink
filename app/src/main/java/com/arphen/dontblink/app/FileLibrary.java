package com.arphen.dontblink.app;

import android.support.annotation.NonNull;
import android.util.Log;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Objects;
import java.util.Scanner;

/**
 * Created by swozny on 08.04.18.
 */

public class FileLibrary implements Iterable<LibraryBook> {
    private ArrayList<LibraryBook> books = new ArrayList<LibraryBook>();

    public FileLibrary() {
        books = new ArrayList<LibraryBook>();
    }

    public FileLibrary(FileInputStream library_file) throws IOException {
        Scanner sc = new Scanner(library_file);
        while (sc.hasNextLine()) {
            String n = sc.nextLine();
            String author = n.split("---")[0];
            String title = n.split("---")[1];
            String path = n.split("---")[2];
            int wordcount = 0;
            try {
                wordcount = Integer.parseInt(n.split("---")[3]);
            } catch (Exception e) {

            }
            String rating = "";
            try {
                rating = n.split("---")[4];
            } catch (Exception e) {
            }
            books.add(new LibraryBook(author, title, path, rating, wordcount));
        }
        library_file.close();
    }

    public void update(LibraryBook book) {
        int position = contains(book);
        books.set(position, book);
    }

    public int contains(LibraryBook book) {

        for (int i = 0; i < books.size(); i++) {
            if (Objects.equals(books.get(i).path(), book.path())) {
                return i;
            }
        }
        return -1;
    }

    public void add(LibraryBook book) {
        this.books.add(book);
    }

    public void save(FileOutputStream library_file) {
        try (Writer w = new OutputStreamWriter(library_file, "UTF-8")) {
            for (LibraryBook n : this.books)
                try {
                    String entry = String.format("%s---%s---%s---%s---%s\n", n.author(), n.title(), n.path(), n.length(), n.rating());
                    Log.i("FileLibrary", entry);
                    w.write(entry);
                } catch (Exception e) {
                }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @NonNull
    @Override
    public Iterator<LibraryBook> iterator() {
        return books.iterator();
    }

    public LibraryBook get(int position) {
        return this.books.get(position);
    }

    public void sort_by_title() {
        books.sort(Comparator.comparing(LibraryBook::title));
    }

    public void sort_by_author() {
        books.sort(Comparator.comparing(LibraryBook::author));
    }

    public void sort_by_rating() {
        books.sort(Comparator.comparing(LibraryBook::rating));
        Collections.reverse(books);
    }

}
