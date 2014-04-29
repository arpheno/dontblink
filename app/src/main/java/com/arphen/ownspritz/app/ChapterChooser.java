package com.arphen.ownspritz.app;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;

import com.arphen.ownspritz.app.util.SystemUiHider;


/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 *
 * @see SystemUiHider
 */
public class ChapterChooser extends Activity {
    private SystemUiHider mSystemUiHider;
    private GridView gridView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chapter_chooser);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        getActionBar().setBackgroundDrawable(null);
        getActionBar().setIcon(R.drawable.app_icon);
        getActionBar().setTitle("Choose a chapter");
        gridView = (GridView) findViewById(R.id.gridView);
        Intent i = getIntent();
        int chapters = i.getIntExtra("chapters", 0);
        String[] numbers = new String[chapters];
        for (int j = 0; j < chapters; j++)
            numbers[j] = String.valueOf(j + 1);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, numbers);

        gridView.setAdapter(adapter);

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v,
                                    int position, long id) {
                Log.i("Chapters", "Chosen chapter" + String.valueOf(position));
                Intent returnIntent = new Intent();
                returnIntent.putExtra("result", position);
                setResult(RESULT_OK, returnIntent);
                finish();
            }
        });

    }
}
