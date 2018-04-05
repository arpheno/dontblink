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

import com.arphen.dontblink.app.util.SystemUiHider;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
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
        List<String> better = new ArrayList<String>();
        for(String n:lines){
            better.add(String.format("%s - %s",n.split("---")[0],n.split("---")[1]));
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, better);

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
