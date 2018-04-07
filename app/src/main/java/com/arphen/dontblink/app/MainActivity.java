package com.arphen.dontblink.app;

import android.Manifest;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.NumberPicker;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.drive.DriveScopes;
import com.arphen.dontblink.app.util.SystemUiHider;

import java.io.*;
import java.util.Arrays;
import java.util.List;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

import static com.arphen.dontblink.app.R.id.action_choose_chapter;
import static com.arphen.dontblink.app.R.id.action_choose_file;
import static com.arphen.dontblink.app.R.id.action_choose_library;
import static com.arphen.dontblink.app.R.id.action_choose_sample;
import static com.arphen.dontblink.app.R.id.action_browse_library;

import static com.arphen.dontblink.app.R.id.action_scan_library;
import static com.google.android.gms.drive.Drive.getDriveResourceClient;
import static java.lang.Math.round;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 *
 * @see SystemUiHider
 */
public class MainActivity extends Activity implements EasyPermissions.PermissionCallbacks, RunningListener, OnChapterChangedListener {

    private static final int ACTIVITY_BROWSE_LIBRARY = 666;
    private static final int REQUEST_CODE_SIGN_IN = 123;
    final int ACTIVITY_CHOOSE_FILE = 1;
    final int ACTIVITY_CHOOSE_CHAPTER = 888;
    static final int REQUEST_ACCOUNT_PICKER = 1000;
    static final int REQUEST_AUTHORIZATION = 1001;
    static final int REQUEST_GOOGLE_PLAY_SERVICES = 1002;
    static final int REQUEST_PERMISSION_GET_ACCOUNTS = 1003;
    public GestureDetector gestureScanner;
    public BlinkProgressBar sb;
    private double wpmthresh;
    private int height;
    private int width;
    private TextView wpmtv;
    private BlinkView tv;
    private int interact_sb;
    private BlinkAnnouncement an;
    private BlinkNumberPicker np;
    private TextView pt;
    private TextView pb;
    private BlinkAnnouncement at;
    private MenuItem chapterfield;
    private SharedPreferences mPrefs;
    private static final String[] SCOPES = {DriveScopes.DRIVE};
    private GoogleAccountCredential mCredential;
    private static final String PREF_ACCOUNT_NAME = "accountName";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPrefs = this.getSharedPreferences(
                "com.arphen.dontblink", Context.MODE_PRIVATE);
        setUpUi();
        populateViews();
        linkViews();
        initFromPrefs();
    }

    private void initFromPrefs() {
        // String current_file_path = mPrefs.getString("current_file_path", "");
        // if (current_file_path != "")
        //     loadFile(current_file_path);

        final int c = mPrefs.getInt("current_chapter", -1);
        final int p = mPrefs.getInt("current_position", 0);
        Log.i("Main", "Initializing to Chapter " + String.valueOf(c));

        if (c != -1) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    InputStream local = null;
                    try {
                        local = openFileInput("currentfile");
                        EpubExtractor epubExtractor = new EpubExtractor(local).invoke();
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

        np.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker numberPicker, int i, int i2) {
                tv.changeWpm((i2 - 250) * 10);
                np.show();
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
        tv.addChapterChangedListener(sb);
        tv.addChapterChangedListener(an);
        tv.addChapterChangedListener(this);
        tv.addRunningListener(sb);
        tv.addRunningListener(np);
        tv.addRunningListener(this);
        sb.linkBlinkView(tv);
        sb.setOnSeekBarChangeListener(
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
                        sb.show();
                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                        sb.show();
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
        sb = (BlinkProgressBar) findViewById(R.id.seekBar);
        an = (BlinkAnnouncement) findViewById(R.id.announcement);
        np = (BlinkNumberPicker) findViewById(R.id.numberPicker);
        pt = (TextView) findViewById(R.id.previewTop);
        pb = (TextView) findViewById(R.id.previewBot);
    }

    public void runTV() {
        pt.setText("");
        pb.setText("");
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
                        pt.setText(top);
                        pb.setText(bot);
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

    private void loadFile(final InputStream in) {
        Log.i("Main", "Loading ");
        announce("Loading File");
        try {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        String filename = "currentfile";
                        FileOutputStream outputStream;

                        try {
                            outputStream = openFileOutput(filename, Context.MODE_PRIVATE);
                            copy(in, outputStream);
                            outputStream.close();
                            in.close();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        InputStream local = openFileInput("currentfile");
                        EpubExtractor epubExtractor = new EpubExtractor(local).invoke();
                        tv.init(epubExtractor.getChapters(), epubExtractor.getAuthor(), epubExtractor.getTitle());
                        stopTV();

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void running(Boolean running) {
        if (running) {
            View decorView = getWindow().getDecorView();
// Hide the status bar.
            int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN + View.SYSTEM_UI_FLAG_LAYOUT_STABLE + View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
            decorView.setSystemUiVisibility(uiOptions);
            //getActionBar().hide();
        } else {
            View decorView = getWindow().getDecorView();
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
                        chapterfield.setTitle(String.format("Chapter %s", c + 1));
                        View decorView = getWindow().getDecorView();
                        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN + View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
                    }
                });
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        View decorView = getWindow().getDecorView();
                        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN + View.SYSTEM_UI_FLAG_LAYOUT_STABLE + View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
                        decorView.setSystemUiVisibility(uiOptions);
                    }
                });

            }
        }).start();
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        switch (requestCode) {
            case ACTIVITY_CHOOSE_FILE: {
                if (resultCode == RESULT_OK) {
                    Uri uri = data.getData();
                    try {
                        final InputStream inputStream = getContentResolver().openInputStream(uri);
                        loadFile(inputStream);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }

                }
                break;
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
            case ACTIVITY_BROWSE_LIBRARY: {
                if (resultCode == RESULT_OK) {
                    String filePath = data.getStringExtra("result");
                    announce("Loading File");
                    EpubExtractor epubExtractor = null;
                    try {
                        epubExtractor = new EpubExtractor(openFileInput(filePath)).invoke();
                        tv.init(epubExtractor.getChapters(), epubExtractor.getAuthor(), epubExtractor.getTitle());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                }

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
                Intent chooseFile;
                Intent intent;
                chooseFile = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                chooseFile.setType("application/epub+zip");
                intent = Intent.createChooser(chooseFile, "Choose a file");
                startActivityForResult(intent, ACTIVITY_CHOOSE_FILE);
                return true;
            case action_choose_chapter:
                Intent chooseChapter;
                chooseChapter = new Intent(getApplicationContext(), ChapterChooser.class);
                int chapters = tv.getNumberOfChapters();
                chooseChapter.putExtra("chapters", chapters);
                if (chapters != 0)
                    startActivityForResult(chooseChapter, ACTIVITY_CHOOSE_CHAPTER);
                return true;
            case action_browse_library:
                Intent browseLibrary = new Intent(getApplicationContext(), LibraryBrowser.class);
                startActivityForResult(browseLibrary, ACTIVITY_BROWSE_LIBRARY);
                return true;
            case action_choose_sample:
                try {
                    InputStream in = getAssets().open("habits.epub");
                    announce("Loading File");
                    EpubExtractor epubExtractor = new EpubExtractor(in).invoke();
                    tv.init(epubExtractor.getChapters(), epubExtractor.getAuthor(), epubExtractor.getTitle());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return true;
            case action_choose_library:
                mCredential = GoogleAccountCredential.usingOAuth2(
                        getApplicationContext(), Arrays.asList(SCOPES))
                        .setBackOff(new ExponentialBackOff());
                getResultsFromApi();
                return true;
            case action_scan_library:
                scan_library();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void scan_library() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                FileOutputStream library = null;
                try {
                    deleteFile("LIBRARY");
                    library = openFileOutput("LIBRARY", Context.MODE_PRIVATE);


                    try (Writer w = new OutputStreamWriter(library, "UTF-8")) {
                        for (String filename : getFilesDir().list())

                            try {
                                EpubExtractor ex = new EpubExtractor(openFileInput(filename)).invoke();

                                String entry = String.format("%s---%s---%s---%s---0\n", ex.getAuthor(), ex.getTitle(), filename, ex.getNumberOfWords());
                                Log.i("asd", entry);
                                w.write(entry);
                            } catch (Exception e) {
                            }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } catch (FileNotFoundException e) {
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
