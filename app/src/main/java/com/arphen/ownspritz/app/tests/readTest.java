package com.arphen.ownspritz.app.tests;

import android.test.InstrumentationTestCase;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by Arphen on 21.04.2014.
 */
public class readTest extends InstrumentationTestCase {
    public void test() throws Exception {
        ArrayList<File> f = new ArrayList<File>();
        listf("/storage/sdcard1/kobo/",f);
        String t="Hello dear world. How are you today? I'm doing quite fine, I hope you too.";
        String[] temp = t.split("([\\s(?<=[.,?!-])])");

    }

    public void listf(String directoryName, ArrayList<File> files) {
        File directory = new File(directoryName);

        // get all the files from a directory
        File[] fList = directory.listFiles();
        for (File file : fList) {
            if (file.isFile()) {
                files.add(file);
            } else if (file.isDirectory()) {
                listf(file.getAbsolutePath(), files);
            }
        }
    }
}
