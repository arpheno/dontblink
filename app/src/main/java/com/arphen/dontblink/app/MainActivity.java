package com.arphen.dontblink.app;

import android.Manifest;
import android.accounts.AccountManager;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;

import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.NumberPicker;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.drive.DriveScopes;

import java.io.*;
import java.util.Arrays;
import java.util.List;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

import static com.arphen.dontblink.app.R.id.action_choose_chapter;
import static com.arphen.dontblink.app.R.id.action_choose_file;
import static com.arphen.dontblink.app.R.id.action_sync_library;
import static com.arphen.dontblink.app.R.id.action_choose_sample;
import static com.arphen.dontblink.app.R.id.action_browse_library;

import static com.arphen.dontblink.app.R.id.action_index_library;

public class MainActivity extends Activity implements EasyPermissions.PermissionCallbacks, RunningListener, OnChapterChangedListener {

    private static final int ACTIVITY_BROWSE_LIBRARY = 666;
    private static final int REQUEST_CODE_SIGN_IN = 123;
    final int ACTIVITY_CHOOSE_FILE = 1;
    final int ACTIVITY_CHOOSE_CHAPTER = 888;
    static final int REQUEST_ACCOUNT_PICKER = 1000;
    static final int REQUEST_AUTHORIZATION = 1001;
    static final int REQUEST_GOOGLE_PLAY_SERVICES = 1002;
    static final int REQUEST_PERMISSION_GET_ACCOUNTS = 1003;
    private final FileChooser fileChooser = new FileChooser(this);
    public BlinkProgressBar seekBar;
    public ProgressBar progressBar;
    private BlinkView tv;
    private BlinkAnnouncement an;
    private BlinkNumberPicker wpmChooser;
    private TextView previewTop;
    private TextView previewBottom;
    private BlinkAnnouncement at;
    private MenuItem chapterfield;
    private SharedPreferences mPrefs;
    private String current_book;
    private static final String[] SCOPES = {DriveScopes.DRIVE};
    private GoogleAccountCredential mCredential;
    private static final String PREF_ACCOUNT_NAME = "accountName";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPrefs = this.getSharedPreferences("com.arphen.dontblink", Context.MODE_PRIVATE);
        setUpUi();
        populateViews();
        linkViews();
        initFromPrefs();
    }

    private void initFromPrefs() {
        current_book = mPrefs.getString("current_book", "currene");
        final int c = mPrefs.getInt("current_chapter", -1);
        final int p = mPrefs.getInt("current_position", 0);
        Log.i("Main", "Initializing to book " + current_book);
        Log.i("Main", "Initializing to Chapter " + String.valueOf(c));
        Log.i("Main", "Initializing to Position " + String.valueOf(p));

        if (c != -1) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        EpubExtractor epubExtractor = new EpubExtractor(openFileInput(current_book));
                        tv.init(epubExtractor.getChapters(), epubExtractor.getAuthor(), epubExtractor.getTitle());
                        stopTV();
                        Log.i("Main", "Initializing to Chapter " + String.valueOf(c));
                        tv.cc = null;
                        tv.forceChapter(c);
                        tv.forcePosition(p);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }


                }
            }).start();

        }

    }

    protected void onPause() {
        super.onPause();
        SharedPreferences.Editor ed = mPrefs.edit();
        ed.putInt("current_position", tv.getCurrentPosition());
        ed.putInt("current_chapter", tv.getCurrent_chapter());
        ed.putString("current_book", current_book);
        Log.i("Main", "Saving chapter to " + String.valueOf(tv.getCurrent_chapter() + " position " + String.valueOf(tv.getCurrentPosition())));
        ed.commit();
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onStop() {
        super.onStop();

    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    public void announce(String what) {
        an.setText(what);
        an.show();
    }

    private void linkViews() {

        wpmChooser.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker numberPicker, int i, int i2) {
                tv.changeWpm((i2 - 250) * 10);
                wpmChooser.show();
            }
        });
        tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!tv.is_init())
                    return;
                if (tv.is_playing()) {
                    stopTV();
                } else {
                    runTV();
                }
            }
        });
        /* Link Components */
        tv.addChapterChangedListener(seekBar);
        tv.addChapterChangedListener(an);
        tv.addAchievementListener(an);
        tv.addChapterChangedListener(this);
        tv.addRunningListener(seekBar);
        tv.addRunningListener(wpmChooser);
        tv.addRunningListener(this);
        seekBar.linkBlinkView(tv);
        seekBar.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {
                        if (!arg2)
                            return;
                        tv.setPosition(arg1);
                        stopTV();
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {
                        MainActivity.this.seekBar.show();
                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                        MainActivity.this.seekBar.show();
                    }
                }
        );
    }

    private void setUpUi() {
        setContentView(R.layout.main_activity);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN + View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        getActionBar().setBackgroundDrawable(null);
        getActionBar().setIcon(R.drawable.app_icon);
    }

    private void populateViews() {
        tv = (BlinkView) findViewById(R.id.blinkview);
        seekBar = (BlinkProgressBar) findViewById(R.id.seekBar);
        an = (BlinkAnnouncement) findViewById(R.id.announcement);
        wpmChooser = (BlinkNumberPicker) findViewById(R.id.numberPicker);
        previewTop = (TextView) findViewById(R.id.previewTop);
        previewBottom = (TextView) findViewById(R.id.previewBot);
        progressBar = (ProgressBar) findViewById(R.id.progressBar3);
    }

    public void runTV() {
        previewTop.setText("");
        previewBottom.setText("");
        tv.run();
    }

    public void stopTV() {
        Thread timer = new Thread(new Runnable() {
            @Override
            public void run() {
                final String top = tv.getPreview(-15, 15);
                final String bot = tv.getPreview(1, 20);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        previewTop.setText(top);
                        previewBottom.setText(bot);
                    }
                });
            }
        });
        timer.start();
        tv.stop();
    }

    private static final int BUFFER_SIZE = 8192;

    /**
     * Reads all bytes from an input stream and writes them to an output stream.
     */
    private long copy(InputStream source, OutputStream sink)
            throws IOException {
        long nread = 0L;
        byte[] buf = new byte[BUFFER_SIZE];
        int n;
        while ((n = source.read(buf)) > 0) {
            sink.write(buf, 0, n);
            nread += n;
        }
        return nread;
    }


    @Override
    public void running(Boolean running) {
        View decorView = getWindow().getDecorView();
        if (running) {
            int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN + View.SYSTEM_UI_FLAG_LAYOUT_STABLE + View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
            decorView.setSystemUiVisibility(uiOptions);
        } else {
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN + View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        }
    }

    public void onChapterChanged(final int c, int l) {
        new Thread(new Runnable() {
            @Override
            public void run() {

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        stopTV();
                        chapterfield.setTitle(String.format("Chapter %s", c + 1));
                        View decorView = getWindow().getDecorView();
                        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN + View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
                        int words_read_so_far = 0;
                        for (int i = 0; i < c; i++) words_read_so_far += tv.getChapterLength(i);
                        progressBar.setMax(tv.getBookLength());
                        progressBar.setProgress(words_read_so_far);
                        progressBar.setSecondaryProgress(words_read_so_far + tv.getChapterLength(c));
                    }
                });
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        View decorView = getWindow().getDecorView();
                        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN + View.SYSTEM_UI_FLAG_LAYOUT_STABLE + View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
                        decorView.setSystemUiVisibility(uiOptions);
                        runTV();
                    }
                });

            }
        }).start();
    }

    @SuppressLint("ApplySharedPref")
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        switch (requestCode) {
            case ACTIVITY_CHOOSE_FILE: {
                if (resultCode == RESULT_OK) {
                    Uri uri = data.getData();
                    try {
                        final InputStream inputStream = getContentResolver().openInputStream(uri);
                        try {
                            String filename = "currentfile";
                            FileOutputStream outputStream;
                            try {
                                outputStream = openFileOutput(filename, Context.MODE_PRIVATE);
                                copy(inputStream, outputStream);
                                outputStream.close();
                                inputStream.close();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            loadStreamAndSavePreferences(filename);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }

                }
                break;
            }

            case ACTIVITY_BROWSE_LIBRARY: {
                if (resultCode == RESULT_OK) {
                    String temp = data.getStringExtra("result");
                    loadStreamAndSavePreferences(temp);
                    break;
                }

            }
            case ACTIVITY_CHOOSE_CHAPTER: {
                if (resultCode == RESULT_OK) {
                    int chosen_chapter = data.getIntExtra("result", 0);
                    tv.forceChapter(chosen_chapter);
                    tv.forcePosition(0);

                    announce(String.format("Chapter: %s", chosen_chapter + 1));
                    stopTV();
                }
                break;
            }
            case REQUEST_GOOGLE_PLAY_SERVICES:
                if (resultCode != RESULT_OK) {
                    Log.i("asd", String.format("This app requires Google Play Services. Please install Google Play Services on your device and relaunch this app."));
                } else {
                    getResultsFromApi();
                }
                break;
            case REQUEST_ACCOUNT_PICKER:
                if (resultCode == RESULT_OK && data != null &&
                        data.getExtras() != null) {
                    String accountName =
                            data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                    if (accountName != null) {
                        SharedPreferences settings =
                                getPreferences(Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = settings.edit();
                        editor.putString(PREF_ACCOUNT_NAME, accountName);
                        editor.apply();
                        mCredential.setSelectedAccountName(accountName);
                        getResultsFromApi();
                    }
                }
                break;
            case REQUEST_AUTHORIZATION:
                if (resultCode == RESULT_OK) {
                    getResultsFromApi();
                }
                break;
        }


    }

    @SuppressLint("ApplySharedPref")
    private void loadStreamAndSavePreferences(String temp) {
        announce("Loading File");
        current_book = temp;
        try {
            EpubExtractor epubExtractor = new EpubExtractor(openFileInput(temp));
            tv.init(epubExtractor.getChapters(), epubExtractor.getAuthor(), epubExtractor.getTitle());
            SharedPreferences settings = getPreferences(Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = settings.edit();
            editor.putString("current_book", temp);
            editor.commit();
            Log.i("main", "committed current book to " + temp);
            stopTV();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_activity_action, menu);
        chapterfield = menu.findItem(R.id.Chapter);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case action_choose_file:
                fileChooser.choose_file();
                return true;
            case action_choose_chapter:
                choose_chapter();
                return true;
            case action_browse_library:
                Intent browseLibrary = new Intent(getApplicationContext(), LibraryBrowser.class);
                startActivityForResult(browseLibrary, ACTIVITY_BROWSE_LIBRARY);
                return true;
            case action_choose_sample:
                load_sample_book();
                return true;
            case action_sync_library:
                sync_google_drive();
                return true;
            case action_index_library:
                scan_library();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void sync_google_drive() {
        mCredential = GoogleAccountCredential.usingOAuth2(
                getApplicationContext(), Arrays.asList(SCOPES))
                .setBackOff(new ExponentialBackOff());
        getResultsFromApi();
    }

    private void load_sample_book() {
        try {
            InputStream in = getAssets().open("sicp.epub");
//            InputStream in = getAssets().open("The Idiot.epub");
            announce("Loading File");
            EpubExtractor epubExtractor = new EpubExtractor(in);
            tv.init(epubExtractor.getChapters(), epubExtractor.getAuthor(), epubExtractor.getTitle());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void choose_chapter() {
        Intent chooseChapter;
        chooseChapter = new Intent(getApplicationContext(), ChapterChooser.class);
        int chapters = tv.getNumberOfChapters();
        chooseChapter.putExtra("chapters", chapters);
        if (chapters != 0)
            startActivityForResult(chooseChapter, ACTIVITY_CHOOSE_CHAPTER);
    }

    private void delete_library() {
        deleteFile("LIBRARY");
    }

    private FileLibrary getLibrary(FileInputStream filehandle) {

        FileLibrary library = null;
        try {
            filehandle = openFileInput("LIBRARY");
            library = new FileLibrary(filehandle);
            filehandle.close();
            return library;
        } catch (Exception e) {
            deleteFile("LIBRARY");
            library = new FileLibrary();
            e.printStackTrace();
            return library;
        }
    }

    private void scan_library() {
        new Thread(new Runnable() {
            @SuppressLint("DefaultLocale")
            @Override
            public void run() {
                FileInputStream filehandle = null;
                FileLibrary library = getLibrary(filehandle);
                int i = 0;
                for (String filename : getFilesDir().list()) {
                    if (library.contains(filename) == -1) {
                        i++;
                    }
                }

                announce(String.format("Indexing %d books", i));
                i = 0;
                for (String filename : getFilesDir().list()) {
                    if (library.contains(filename) != -1) continue;
                    if (i % 25 != 0) {
                        FileOutputStream filehandleo;
                        try {
                            filehandleo = openFileOutput("LIBRARY", Context.MODE_PRIVATE);
                            library.save(filehandleo);
                            filehandleo.close();
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    i++;
                    try {
                        EpubExtractor ex = new EpubExtractor(openFileInput(filename));
                        LibraryBook b = new LibraryBook(ex.getAuthor(), ex.getTitle(), filename, GoodReads.getRating(ex.getAuthor(), ex.getTitle()), ex.getNumberOfWords());
                        announce(String.format("Indexed %s - %s", ex.getAuthor(), ex.getTitle()));
                        library.add(b);

                        Log.i("Scanner", String.format("Added book %s", b.title));
                    } catch (Exception e) {
                        Log.i("Scanner", "Failed book");
                        e.printStackTrace();
                    }
                }
                Log.i("Scanner", "Scanner done");
                try {
                    library.save(openFileOutput("LIBRARY", Context.MODE_PRIVATE));
                    Log.i("Scanner", "LIbrary saved");
                } catch (FileNotFoundException e) {
                    Log.i("Scanner", "LIbrary couldn't be saved");
                    e.printStackTrace();
                }
            }
        }).start();

    }


    private void getResultsFromApi() {
        if (!isGooglePlayServicesAvailable()) {
            acquireGooglePlayServices();
        } else if (mCredential.getSelectedAccountName() == null) {
            chooseAccount();
        } else {
            new MakeRequestTask(this, mCredential).execute();
        }
    }

    @AfterPermissionGranted(REQUEST_PERMISSION_GET_ACCOUNTS)
    private void chooseAccount() {
        if (EasyPermissions.hasPermissions(
                this, Manifest.permission.GET_ACCOUNTS)) {
            String accountName = getPreferences(Context.MODE_PRIVATE)
                    .getString(PREF_ACCOUNT_NAME, null);
            if (accountName != null) {
                mCredential.setSelectedAccountName(accountName);
                getResultsFromApi();
            } else {
                // Start a dialog from which the user can choose an account
                startActivityForResult(
                        mCredential.newChooseAccountIntent(),
                        REQUEST_ACCOUNT_PICKER);
            }
        } else {
            // Request the GET_ACCOUNTS permission via a user dialog
            EasyPermissions.requestPermissions(
                    this,
                    "This app needs to access your Google account (via Contacts).",
                    REQUEST_PERMISSION_GET_ACCOUNTS,
                    Manifest.permission.GET_ACCOUNTS);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(
                requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> list) {
    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> list) {
    }

    private boolean isGooglePlayServicesAvailable() {
        GoogleApiAvailability apiAvailability =
                GoogleApiAvailability.getInstance();
        final int connectionStatusCode =
                apiAvailability.isGooglePlayServicesAvailable(this);
        return connectionStatusCode == ConnectionResult.SUCCESS;
    }

    private void acquireGooglePlayServices() {
        GoogleApiAvailability apiAvailability =
                GoogleApiAvailability.getInstance();
        final int connectionStatusCode =
                apiAvailability.isGooglePlayServicesAvailable(this);
        if (apiAvailability.isUserResolvableError(connectionStatusCode)) {
            showGooglePlayServicesAvailabilityErrorDialog(connectionStatusCode);
        }
    }

    void showGooglePlayServicesAvailabilityErrorDialog(
            final int connectionStatusCode) {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        Dialog dialog = apiAvailability.getErrorDialog(
                MainActivity.this,
                connectionStatusCode,
                REQUEST_GOOGLE_PLAY_SERVICES);
        dialog.show();
    }

}
