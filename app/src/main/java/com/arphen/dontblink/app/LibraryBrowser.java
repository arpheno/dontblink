package com.arphen.dontblink.app;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ListAdapter;
import android.widget.SimpleAdapter;

import com.arphen.dontblink.app.util.SystemUiHider;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;


/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 *
 * @see SystemUiHider
 */
public class LibraryBrowser extends Activity {
    private SystemUiHider mSystemUiHider;
    private GridView gridView;
    private ArrayList<String> lines;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.library_browser);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        getActionBar().setBackgroundDrawable(null);
        getActionBar().setIcon(R.drawable.app_icon);
        getActionBar().setTitle("Browse your library");
        gridView = (GridView) findViewById(R.id.gridView);
        Intent i = getIntent();
        Log.i("Files", "Chosen files nigger");

        lines = new ArrayList<String>();
        String[] arr;
        try {
            FileInputStream library = openFileInput("LIBRARY");
            Scanner sc = new Scanner(library);
            while (sc.hasNextLine()) {
                lines.add(sc.nextLine());
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        List<Map<String, String>> data = new ArrayList<Map<String, String>>();
        for (String n : lines) {
            Log.i("Files", String.format("Chosen files%s", n));
            Map<String, String> datum = new HashMap<String, String>(2);
            datum.put("title", n.split("---")[1]);

            int wordcount = 0;
            try {
                wordcount = Integer.parseInt(n.split("---")[3]);
            } catch (Exception e) {
            }
            String subtitle = String.format("%s %s words %s hours %s minutes", n.split("---")[0], wordcount, wordcount / 30000, (wordcount % 30000) / 500);
            datum.put("author", subtitle);
            data.add(datum);
        }
        SimpleAdapter adapter = new SimpleAdapter(this, data,
                R.layout.book_item,
                new String[]{"title", "author"},
                new int[]{android.R.id.text1,
                        android.R.id.text2});

        gridView.setAdapter(adapter);

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v,
                                    int position, long id) {
                String res = lines.get(position).split("---")[2];
                Log.i("Files", "Chosen files" + res);
                Intent returnIntent = new Intent();
                returnIntent.putExtra("result", res);
                setResult(RESULT_OK, returnIntent);
                finish();
            }
        });

    }
}
