package com.xpacer.movie_app.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.view.View;
import android.widget.Toast;

import com.google.gson.JsonSyntaxException;
import com.xpacer.movie_app.R;
import com.xpacer.movie_app.data.models.Movie;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;

public class NetworkUtils {
    private static final String MOVIE_DB_BASE_URL = "https://api.themoviedb.org";
    private static final String API_KEY_PARAM = "api_key";
    /**
     * Insert API Key Here
     */
    private static final String API_KEY = "46823330bf727748336313f01791d94d";

    public static URL buildUrl(String path) {
        Uri moviesQueryUri = Uri.parse(MOVIE_DB_BASE_URL).buildUpon()
                .path("/3/movie")
                .appendEncodedPath(path)
                .appendQueryParameter(API_KEY_PARAM, API_KEY)
                .build();

        try {
            return new URL(moviesQueryUri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String getResponseFromHttpUrl(URL url) throws IOException {
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        try {
            InputStream in = urlConnection.getInputStream();

            Scanner scanner = new Scanner(in);
            scanner.useDelimiter("\\A");

            boolean hasInput = scanner.hasNext();
            String response = null;
            if (hasInput) {
                response = scanner.next();
            }
            scanner.close();
            return response;
        } finally {
            urlConnection.disconnect();
        }
    }

    /**
     * Method to check if the device is connected to the internet
     *
     * @param context : Context
     * @return boolean
     */
    public static boolean isConnected(Context context) {

        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        // test for connection
        return cm != null && cm.getActiveNetworkInfo() != null &&
                cm.getActiveNetworkInfo().isConnected();
    }

    public interface QueryResult {
        void processResult(String result);
    }

    @SuppressLint("StaticFieldLeak")
    public static class ApiCallerTask extends AsyncTask<URL, Movie, String> {
        private final Context mContext;
        private View mProgressBar;
        private QueryResult mQueryResult;

        public ApiCallerTask(Context context, QueryResult queryResult, View progressBar) {
            mContext = context;
            mProgressBar = progressBar;
            mQueryResult = queryResult;
        }

        @Override
        protected void onPreExecute() {
            // SHOW LOADER
            if (!NetworkUtils.isConnected(mContext)) {
                Toast errorToast = Toast.makeText(mContext, mContext.getString(R.string.no_internet_message),
                        Toast.LENGTH_LONG);
                errorToast.show();
                cancel(true);
                return;
            }

            if (mProgressBar != null)
                mProgressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(URL... urls) {
            URL searchUrl = urls[0];
            String queryResults = null;

            try {
                queryResults = NetworkUtils.getResponseFromHttpUrl(searchUrl);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JsonSyntaxException je) {
                je.printStackTrace();
            }

            return queryResults;
        }

        @Override
        protected void onPostExecute(String result) {
            if (mProgressBar != null)
                mProgressBar.setVisibility(View.GONE);

            if (result == null) {
                Toast errorToast = Toast.makeText(mContext, mContext.getString(R.string.something_went_wrong), Toast.LENGTH_LONG);
                errorToast.show();

                return;
            }

            mQueryResult.processResult(result);
        }

    }
}
