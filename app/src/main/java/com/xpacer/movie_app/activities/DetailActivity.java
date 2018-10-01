package com.xpacer.movie_app.activities;

import android.app.Dialog;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.gson.Gson;
import com.squareup.picasso.Picasso;
import com.xpacer.movie_app.R;
import com.xpacer.movie_app.adapters.MoviesReviewListAdapter;
import com.xpacer.movie_app.adapters.MoviesTrailerListAdapter;
import com.xpacer.movie_app.data.AppDatabase;
import com.xpacer.movie_app.data.models.Movie;
import com.xpacer.movie_app.data.models.MovieReview;
import com.xpacer.movie_app.data.models.MovieTrailer;
import com.xpacer.movie_app.data.queryresults.MovieReviewResult;
import com.xpacer.movie_app.data.queryresults.MovieTrailerResult;
import com.xpacer.movie_app.utils.AppExecutors;
import com.xpacer.movie_app.utils.Constants;
import com.xpacer.movie_app.utils.NetworkUtils;
import com.xpacer.movie_app.utils.NetworkUtils.ApiCallerTask;
import com.xpacer.movie_app.viewmodels.MainViewModel;

import java.net.URL;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class DetailActivity extends AppCompatActivity implements NetworkUtils.QueryResult {

    @BindView(R.id.tv_release_date)
    TextView mReleaseDateTextView;
    @BindView(R.id.tv_original_title)
    TextView mOriginalTitleTextView;
    @BindView(R.id.tv_ratings)
    TextView mRatingsTextView;
    @BindView(R.id.img_movie_poster)
    ImageView mPosterImageView;
    @BindView(R.id.tv_plot_summary)
    TextView mPlotSummaryTextView;
    @BindView(R.id.rv_movie_reviews)
    RecyclerView rvMovieReviews;
    @BindView(R.id.progress_bar)
    ProgressBar mSpinner;
    @BindView(R.id.tv_watch_trailer)
    TextView mWatchTrailer;
    @BindView(R.id.tv_like_movie)
    TextView mLikeMovie;
    RecyclerView rvTrailers;
    @BindView(R.id.tv_no_reviews)
    TextView mNoReviews;
    Dialog trailerDialog;

    private MoviesReviewListAdapter reviewListAdapter;
    private MoviesTrailerListAdapter trailerListAdapter;
    private ApiCallerMode mode;
    private AppDatabase mDatabase;
    private TextView tvNoData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        ButterKnife.bind(this);
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if (getIntent() == null || !getIntent().hasExtra(Constants.MOVIE_EXTRA_KEY)) {
            return;
        }

        mDatabase = AppDatabase.getInstance(this);

        final Movie movie = (Movie) getIntent().getSerializableExtra(Constants.MOVIE_EXTRA_KEY);
        setupView(movie);

        MainViewModel viewModel = ViewModelProviders.of(this).get(MainViewModel.class);
        viewModel.getMovies().observe(this, new Observer<List<Movie>>() {
            @Override
            public void onChanged(@Nullable List<Movie> movies) {
                if (movies == null)
                    return;

                isFavourite = checkListContainsId(movies, movie.getId());
                setupLikeButton(isFavourite);
            }
        });
    }

    private boolean isFavourite;

    private boolean checkListContainsId(List<Movie> movies, int id) {
        for (Movie movie : movies) {
            if (movie.getId() == id)
                return true;
        }

        return false;
    }

    private void setupView(final Movie movie) {
        if (movie == null) {
            return;
        }

        if (getSupportActionBar() != null)
            getSupportActionBar().setTitle(movie.getTitle());

        mReleaseDateTextView.setText(movie.getReleaseDate());
        mOriginalTitleTextView.setText(movie.getOriginalTitle());
        mPlotSummaryTextView.setText(movie.getOverview());
        String ratings = String.valueOf(movie.getVoteAverage()).concat("/10");
        mRatingsTextView.setText(ratings);

        String imagePath = Constants.TMDB_GRID_POSTER_URL.concat(movie.getPosterPath());
        Picasso.with(this).load(imagePath).into(mPosterImageView);

        mWatchTrailer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fetchTrailer(movie.getId());
            }
        });

        mLikeMovie.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AppExecutors.getInstance().diskIO().execute(new Runnable() {
                    @Override
                    public void run() {
                        if (isFavourite)
                            mDatabase.movieDao().deleteMovie(movie);
                        else
                            mDatabase.movieDao().insertMovie(movie);
                    }
                });
            }
        });

        setupReviewsView(movie.getId());
        setupTrailerDialog();
    }

    private void setupLikeButton(boolean movieIsFavourite) {
        if (movieIsFavourite) {
            mLikeMovie.setText(R.string.your_favourite);
            mLikeMovie.setCompoundDrawablesWithIntrinsicBounds( R.drawable.ic_star_amber_24dp, 0, 0, 0);
        } else {
            mLikeMovie.setText(R.string.favourite_text);
            mLikeMovie.setCompoundDrawablesWithIntrinsicBounds( R.drawable.ic_star_border_black_24dp, 0, 0, 0);
        }
    }

    void fetchTrailer(int movieId) {
        ApiCallerTask apiCaller = new ApiCallerTask(this, this, null);
        String reviewPath = String.format(Constants.VIDEOS_PATH, String.valueOf(movieId));
        setMode(ApiCallerMode.FETCH_TRAILERS);
        URL reviewUrl = NetworkUtils.buildUrl(reviewPath);
        apiCaller.execute(reviewUrl);
    }

    private void setupTrailerDialog() {
        trailerDialog = new Dialog(this);
        trailerDialog.setContentView(R.layout.layout_movie_trailers);
        rvTrailers = trailerDialog.findViewById(R.id.rv_trailers);
        tvNoData = trailerDialog.findViewById(R.id.tv_no_data);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        rvTrailers.setLayoutManager(linearLayoutManager);

        trailerListAdapter = new MoviesTrailerListAdapter(this);
        rvTrailers.setAdapter(trailerListAdapter);
    }

    private void setupReviewsView(int movieId) {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        rvMovieReviews.setLayoutManager(linearLayoutManager);

        reviewListAdapter = new MoviesReviewListAdapter(this);
        rvMovieReviews.setAdapter(reviewListAdapter);

        ApiCallerTask apiCaller = new ApiCallerTask(this, this, mSpinner);
        String reviewPath = String.format(Constants.REVIEWS_PATH, String.valueOf(movieId));
        setMode(ApiCallerMode.FETCH_REVIEWS);
        URL reviewUrl = NetworkUtils.buildUrl(reviewPath);
        apiCaller.execute(reviewUrl);
    }


    @Override
    public void processResult(String result) {
        Gson gson = new Gson();
        switch (getMode()) {
            case FETCH_REVIEWS:
                MovieReviewResult movieReviewResult = gson.fromJson(result, MovieReviewResult.class);
                setupReviewsList(movieReviewResult.getResults());
                break;
            case FETCH_TRAILERS:
                MovieTrailerResult movieTrailerResult = gson.fromJson(result, MovieTrailerResult.class);
                List<MovieTrailer> movieTrailers = movieTrailerResult.getResults();
                showDialog(movieTrailers);
                break;
        }
    }

    void setupReviewsList(List<MovieReview> movieReviews) {
        if (movieReviews.size() > 0) {
            reviewListAdapter.setMovieReviews(movieReviews);
            mNoReviews.setVisibility(View.GONE);
            rvMovieReviews.setVisibility(View.VISIBLE);
        } else {
            mNoReviews.setVisibility(View.VISIBLE);
            rvMovieReviews.setVisibility(View.GONE);
        }

    }

    void showDialog(List<MovieTrailer> movieTrailers) {
        trailerListAdapter.setMovieTrailers(movieTrailers);
        if (movieTrailers.size() > 0) {
            rvTrailers.setVisibility(View.VISIBLE);
            tvNoData.setVisibility(View.GONE);
        } else {
            tvNoData.setVisibility(View.VISIBLE);
            rvTrailers.setVisibility(View.GONE);
        }

        trailerDialog.show();
    }

    public void setMode(ApiCallerMode mode) {
        this.mode = mode;
    }

    public ApiCallerMode getMode() {
        return mode;
    }

    private enum ApiCallerMode {
        FETCH_REVIEWS,
        FETCH_TRAILERS
    }
}
