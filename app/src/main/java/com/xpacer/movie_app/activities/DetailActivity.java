package com.xpacer.movie_app.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.xpacer.movie_app.R;
import com.xpacer.movie_app.data.Movie;
import com.xpacer.movie_app.utils.Constants;

import butterknife.BindView;
import butterknife.ButterKnife;

public class DetailActivity extends AppCompatActivity {

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        ButterKnife.bind(this);
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if (getIntent().hasExtra(Constants.MOVIE_EXTRA_KEY)) {
            Movie movie = (Movie) getIntent().getSerializableExtra(Constants.MOVIE_EXTRA_KEY);
            setupView(movie);
        }
    }


    private void setupView(Movie movie) {
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
    }
}
