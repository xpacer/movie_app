package com.xpacer.movie_app.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.xpacer.movie_app.R;
import com.xpacer.movie_app.data.models.MovieTrailer;
import com.xpacer.movie_app.utils.HelperUtils;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MoviesTrailerListAdapter extends RecyclerView.Adapter<MoviesTrailerListAdapter.TrailerViewHolder> {
    private final Context mContext;
    private List<MovieTrailer> movieTrailers;

    public MoviesTrailerListAdapter(Context context) {
        mContext = context;
    }

    @NonNull
    @Override
    public MoviesTrailerListAdapter.TrailerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(mContext);
        View moviePoster = layoutInflater.inflate(R.layout.layout_item_movie_trailer, parent, false);

        return new MoviesTrailerListAdapter.TrailerViewHolder(moviePoster);
    }

    @Override
    public void onBindViewHolder(@NonNull MoviesTrailerListAdapter.TrailerViewHolder holder, int position) {
        holder.bind(movieTrailers.get(position));
    }

    public void setMovieTrailers(List<MovieTrailer> movieTrailers) {
        this.movieTrailers = movieTrailers;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        if (movieTrailers == null)
            return 0;

        return movieTrailers.size();
    }

    class TrailerViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.tv_trailer_title)
        TextView tvTrailerTitle;
        @BindView(R.id.tv_trailer_action)
        TextView tvTrailerAction;

        TrailerViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        void bind(final MovieTrailer movieTrailer) {
            tvTrailerTitle.setText(movieTrailer.getName());
            // tvTrailerAction.setText(movieTrailer.getContent());
            tvTrailerAction.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    HelperUtils.watchYoutubeVideo(mContext, movieTrailer.getKey());
                }
            });
        }
    }
}