package com.xpacer.movie_app.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.xpacer.movie_app.R;
import com.xpacer.movie_app.adapters.MoviesListAdapter;
import com.xpacer.movie_app.data.Movie;
import com.xpacer.movie_app.data.MovieQueryResult;
import com.xpacer.movie_app.utils.Constants;
import com.xpacer.movie_app.utils.NetworkUtils;

import java.io.IOException;
import java.net.URL;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.rv_movies_list)
    RecyclerView rvMoviesList;

    @BindView(R.id.spinner)
    ProgressBar mSpinner;

    private MoviesListAdapter moviesListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        mSpinner.setVisibility(View.GONE);
        moviesListAdapter = new MoviesListAdapter(this);

        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2);
        rvMoviesList.setLayoutManager(gridLayoutManager);
        rvMoviesList.setHasFixedSize(true);
        rvMoviesList.setAdapter(moviesListAdapter);
    }

    @Override
    protected void onStart() {
        super.onStart();
        new MovieQueryTask(this).execute(NetworkUtils.buildUrl(Constants.POPULAR_PATH));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_sort, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();

        switch (itemId) {
            case R.id.popularity_sort:
                new MovieQueryTask(this).execute(NetworkUtils.buildUrl(Constants.POPULAR_PATH));
                return true;

            case R.id.top_rated_sort:
                new MovieQueryTask(this).execute(NetworkUtils.buildUrl(Constants.TOP_RATED_PATH));
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }

    }

    @SuppressLint("StaticFieldLeak")
    class MovieQueryTask extends AsyncTask<URL, Movie, MovieQueryResult> {
        private final Context mContext;

        MovieQueryTask(Context context) {
            mContext = context;
        }

        @Override
        protected void onPreExecute() {
            // SHOW LOADER
            if (!NetworkUtils.isConnected(mContext)) {
                Toast errorToast = Toast.makeText(mContext, getString(R.string.no_internet_message),
                        Toast.LENGTH_LONG);
                errorToast.show();
                cancel(true);
                return;
            }

            mSpinner.setVisibility(View.VISIBLE);
        }

        @Override
        protected MovieQueryResult doInBackground(URL... urls) {
            URL searchUrl = urls[0];
            MovieQueryResult movies = null;

            try {
                String queryResults = NetworkUtils.getResponseFromHttpUrl(searchUrl);
                Gson gson = new Gson();
                movies = gson.fromJson(queryResults, MovieQueryResult.class);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JsonSyntaxException je) {
                je.printStackTrace();
            }

            return movies;
        }

        @Override
        protected void onPostExecute(MovieQueryResult result) {
            mSpinner.setVisibility(View.GONE);

            if (result == null) {
                Toast errorToast = Toast.makeText(mContext, getString(R.string.something_went_wrong), Toast.LENGTH_LONG);
                errorToast.show();

                return;
            }

            moviesListAdapter.setMovieList(result.getResults());
            moviesListAdapter.notifyDataSetChanged();
        }

    }

}
