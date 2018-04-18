package com.arphen.dontblink.app;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.SimpleAdapter;

import com.arphen.dontblink.app.util.SystemUiHider;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class LibraryBrowser extends Activity {
    private SystemUiHider mSystemUiHider;
    private GridView gridView;
    FileLibrary library = null;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.library_action, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.library_browser);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        getActionBar().setBackgroundDrawable(null);
        getActionBar().setIcon(R.drawable.app_icon);
        getActionBar().setTitle("Browse your library");
        initialize_grid();
        try {
            library = new FileLibrary(openFileInput("LIBRARY"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        build_grid_from_library();


    }


    private void build_grid_from_library() {
        List<Map<String, String>> data = new ArrayList<Map<String, String>>();
        for (LibraryBook n : library) {
            String template = "%s %s hours %s minutes Goodreads: %s ";
            String subtitle = String.format(template, n.author, n.hours(), n.minutes(), n.rating);

            Map<String, String> datum = new HashMap<String, String>(2);
            datum.put("title", n.title);
            datum.put("author", subtitle);
            data.add(datum);
        }

        SimpleAdapter adapter = new SimpleAdapter(this, data,
                R.layout.book_item,
                new String[]{"title", "author"},
                new int[]{android.R.id.text1, android.R.id.text2});

        gridView.setAdapter(adapter);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.action_sort_by_author:
                library.sort_by_author();
                build_grid_from_library();
                return true;
            case R.id.action_sort_by_rating:
                library.sort_by_rating();
                build_grid_from_library();
                return true;
            case R.id.action_sort_by_title:
                library.sort_by_title();
                build_grid_from_library();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void initialize_grid() {
        gridView = (GridView) findViewById(R.id.gridView);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v,
                                    int position, long id) {
                String res = library.get(position).path;
                Log.i("Files", "Chosen files" + res);
                Intent returnIntent = new Intent();
                returnIntent.putExtra("result", res);
                setResult(RESULT_OK, returnIntent);
                finish();
            }
        });
    }
}
