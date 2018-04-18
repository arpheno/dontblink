package com.arphen.dontblink.app;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * An asynchronous task that handles the Drive API call.
 * Placing the API calls in their own task ensures the UI stays responsive.
 */
public class MakeRequestTask extends AsyncTask<Void, Void, List<String>> {
    private MainActivity mainActivity;
    private com.google.api.services.drive.Drive mService = null;
    private Exception mLastError = null;

    MakeRequestTask(MainActivity mainActivity, GoogleAccountCredential credential) {
        this.mainActivity = mainActivity;
        HttpTransport transport = AndroidHttp.newCompatibleTransport();
        JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
        mService = new com.google.api.services.drive.Drive.Builder(
                transport, jsonFactory, credential)
                .setApplicationName("Drive API Android Quickstart")
                .build();
    }

    /**
     * Background task to call Drive API.
     *
     * @param params no parameters needed for this task.
     */
    @Override
    protected List<String> doInBackground(Void... params) {
        try {
            return getDataFromApi();
        } catch (Exception e) {
            mLastError = e;
            cancel(true);
            return null;
        }
    }

    private List<String> getDataFromApi() throws IOException {
        // Get a list of up to 10 files.
        String pageToken = null;
        List<String> fileInfo = new ArrayList<String>();
        do {
            FileList result = mService.files().list()
                    .setQ("mimeType='application/epub+zip'")
                    .setPageSize(100)
                    .setFields("nextPageToken, files(id, name)")
                    .setPageToken(pageToken)
                    .execute();
            List<File> files = result.getFiles();
            for (File file : files) {
                try {
                    mainActivity.openFileInput(file.getName());
                    Log.i("asd", String.format("Already present %s (%s)\n", file.getName(), file.getId()));

                } catch (FileNotFoundException e) {
                    OutputStream outputStream = mainActivity.openFileOutput(file.getName(), Context.MODE_PRIVATE);
                    Log.i("asd", String.format("Downloading %s (%s)\n", file.getName(), file.getId()));
                    mService.files().get(file.getId()).executeMediaAndDownloadTo(outputStream);
                    outputStream.close();
                }
            }

            pageToken = result.getNextPageToken();
        } while (pageToken != null);

        return fileInfo;
    }


    @Override
    protected void onPreExecute() {
        Log.i("asd", String.format("onPreExecute"));
    }

    @Override
    protected void onPostExecute(List<String> output) {
        Log.i("asd", String.format("onPostExecute"));
        for (String n : output) {
            Log.i("asd", String.format("%s", n));


        }

    }

    @Override
    protected void onCancelled() {

        if (mLastError != null) {
            if (mLastError instanceof GooglePlayServicesAvailabilityIOException) {
                mainActivity.showGooglePlayServicesAvailabilityErrorDialog(
                        ((GooglePlayServicesAvailabilityIOException) mLastError)
                                .getConnectionStatusCode());
            } else if (mLastError instanceof UserRecoverableAuthIOException) {
                mainActivity.startActivityForResult(
                        ((UserRecoverableAuthIOException) mLastError).getIntent(),
                        MainActivity.REQUEST_AUTHORIZATION);
            } else {
                Log.i("asd", String.format("The following error occurred:\n" + mLastError.getMessage()));
            }
        } else {
            Log.i("asd", String.format("request cancelled."));
        }
    }
}
