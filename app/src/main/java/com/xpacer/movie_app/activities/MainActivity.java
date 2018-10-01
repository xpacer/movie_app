package com.xpacer.movie_app.activities;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import com.google.gson.Gson;
import com.xpacer.movie_app.R;
import com.xpacer.movie_app.adapters.MoviesListAdapter;
import com.xpacer.movie_app.data.models.Movie;
import com.xpacer.movie_app.data.enums.MovieListState;
import com.xpacer.movie_app.data.queryresults.MovieQueryResult;
import com.xpacer.movie_app.utils.Constants;
import com.xpacer.movie_app.utils.NetworkUtils;
import com.xpacer.movie_app.viewmodels.MainViewModel;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.xpacer.movie_app.data.enums.MovieListState.POPULAR;
import static com.xpacer.movie_app.data.enums.MovieListState.TOP_RATED;

public class MainActivity extends AppCompatActivity implements NetworkUtils.QueryResult {

    @BindView(R.id.rv_movies_list)
    RecyclerView rvMoviesList;

    @BindView(R.id.progress_bar)
    ProgressBar mProgressBar;

    private MoviesListAdapter moviesListAdapter;
    private List<Movie> favouriteMovies;
    private MovieListState movieListState;
    private static final String MOVIE_LIST_STATE = "movie_list_state";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        mProgressBar.setVisibility(View.GONE);
        moviesListAdapter = new MoviesListAdapter(this);

        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2);
        rvMoviesList.setLayoutManager(gridLayoutManager);
        rvMoviesList.setHasFixedSize(true);
        rvMoviesList.setAdapter(moviesListAdapter);

        /*
          Using savedInstanceState to maintain the movie list view state during rotation
          Our default list view enum state is Popular movies;
         */

        if (savedInstanceState != null) {
            int listEnumVal = savedInstanceState.getInt(MOVIE_LIST_STATE, 0);
            movieListState = MovieListState.values()[listEnumVal];
        } else {
            movieListState = MovieListState.POPULAR;
        }

        setupViewModel();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (movieListState != null)
            outState.putInt(MOVIE_LIST_STATE, movieListState.ordinal());
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_sort, menu);
        return true;
    }

    private void fetchMovieFromAPI() {
        String path = movieListState == POPULAR ? Constants.POPULAR_PATH : Constants.TOP_RATED_PATH;
        new NetworkUtils.ApiCallerTask(this, this, mProgressBar)
                .execute(NetworkUtils.buildUrl(path));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();

        switch (itemId) {
            case R.id.popularity_sort:
                movieListState = MovieListState.POPULAR;
                break;

            case R.id.top_rated_sort:
                movieListState = TOP_RATED;
                break;

            case R.id.favourite_sort:
                movieListState = MovieListState.FAVOURITES;
                break;

            default:
                return super.onOptionsItemSelected(item);
        }

        setupMovieAdapter();
        return true;
    }

    private void setupViewModel() {
        MainViewModel viewModel = ViewModelProviders.of(this).get(MainViewModel.class);
        viewModel.getMovies().observe(this, new Observer<List<Movie>>() {
            @Override
            public void onChanged(@Nullable List<Movie> movies) {
                favouriteMovies = movies;
                setupMovieAdapter();
            }
        });
    }

    void setupMovieAdapter() {
        if (movieListState == null || getSupportActionBar() == null) {
            return;
        }

        switch (movieListState) {
            case TOP_RATED:
                getSupportActionBar().setTitle(R.string.top_rated_text);
                fetchMovieFromAPI();
                break;
            case POPULAR:
                getSupportActionBar().setTitle(R.string.popular_movies_text);
                fetchMovieFromAPI();
                break;
            case FAVOURITES:
            default:
                getSupportActionBar().setTitle(R.string.my_favourites_text);
                moviesListAdapter.setMovieList(favouriteMovies);
                break;
        }
    }

    @Override
    public void processResult(String result) {
        Gson gson = new Gson();
        MovieQueryResult queryResult = gson.fromJson(result, MovieQueryResult.class);
        moviesListAdapter.setMovieList(queryResult.getResults());
    }

}
