package com.arphen.ownspritz.app.tests;

import android.test.InstrumentationTestCase;
import android.util.Log;

import com.arphen.ownspritz.app.Blinker;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;

/**
 * Created by Arphen on 21.04.2014.
 */
public class readTest extends InstrumentationTestCase {
    public void test() throws Exception {
        ArrayList<File> f = new ArrayList<File>();
        listf("/storage/sdcard1/kobo/",f);
        for(File filee: f) {
            Log.i("kk", filee.getAbsolutePath());
            if(filee.getAbsolutePath().contains(".epub")) {
                InputStream in = new FileInputStream(filee);
                 Blinker k = new Blinker(in);
            }
        }
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
