package com.arphen.dontblink.app;

import android.content.Intent;

public class FileChooser {
    private final MainActivity mainActivity;

    public FileChooser(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    void choose_file() {
        Intent chooseFile;
        Intent intent;
        chooseFile = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        chooseFile.setType("application/epub+zip");
        intent = Intent.createChooser(chooseFile, "Choose a file");
        mainActivity.startActivityForResult(intent, mainActivity.ACTIVITY_CHOOSE_FILE);
    }
}