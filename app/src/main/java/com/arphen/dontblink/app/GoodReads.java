package com.arphen.dontblink.app;

import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import static org.jsoup.Jsoup.parse;

/**
 * Created by swozny on 08.04.18.
 */

public class GoodReads {
    public static String getRating(String author, String title) {
        URL url = null;
        try {
            url = new URL(String.format("https://www.goodreads.com/search?q=%s+%s", author.replaceAll(" ", "+"), title.replaceAll(" ", "+")));
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            InputStream in = new BufferedInputStream(urlConnection.getInputStream());
            Document d = parse(in, "UTF-8", "https://www.goodreads.com");
            Elements elements = d.getElementsByClass("minirating");
            return elements.first().text().split(" ")[0];

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
    } catch (NullPointerException e) {
        e.printStackTrace();
    }
        return "";

    }
}
