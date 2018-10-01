package com.xpacer.movie_app.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.xpacer.movie_app.R;
import com.xpacer.movie_app.activities.DetailActivity;
import com.xpacer.movie_app.data.models.Movie;
import com.xpacer.movie_app.utils.Constants;

import java.util.List;

public class MoviesListAdapter extends RecyclerView.Adapter<MoviesListAdapter.MovieViewHolder> {

    private final Context mContext;
    private List<Movie> movieList;

    public MoviesListAdapter(Context context) {
        mContext = context;
    }

    @NonNull
    @Override
    public MovieViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(mContext);
        View moviePoster = layoutInflater.inflate(R.layout.layout_movie_poster, parent, false);

        return new MovieViewHolder(moviePoster);
    }

    @Override
    public void onBindViewHolder(@NonNull final MovieViewHolder holder, int position) {
        holder.bind(movieList.get(position));

        holder.moviePosterLayout.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        int adapterPosition = holder.getAdapterPosition();
                        Intent movieDetailIntent = new Intent(mContext, DetailActivity.class);
                        movieDetailIntent.putExtra(Constants.MOVIE_EXTRA_KEY, movieList.get(adapterPosition));
                        mContext.startActivity(movieDetailIntent);
                    }
                });
    }


    @Override
    public int getItemCount() {
        if (movieList == null)
            return 0;

        return movieList.size();
    }

    public void setMovieList(List<Movie> movieList) {
        this.movieList = movieList;
        notifyDataSetChanged();
    }

    class MovieViewHolder extends RecyclerView.ViewHolder {

        final ImageView moviePoster;
        final TextView movieTitle;
        final RelativeLayout moviePosterLayout;

        MovieViewHolder(View itemView) {
            super(itemView);
            moviePoster = itemView.findViewById(R.id.img_movie_poster);
            movieTitle = itemView.findViewById(R.id.tv_movie_title);
            moviePosterLayout = itemView.findViewById(R.id.rl_movie_poster);
        }

        void bind(Movie movie) {
            String imagePath = Constants.TMDB_GRID_POSTER_URL.concat(movie.getPosterPath());
            Picasso.with(mContext).load(imagePath).into(moviePoster);
            movieTitle.setText(movie.getTitle());
        }

    }

}
