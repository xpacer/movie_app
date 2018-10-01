package com.xpacer.movie_app.adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.xpacer.movie_app.R;
import com.xpacer.movie_app.data.models.MovieReview;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MoviesReviewListAdapter extends RecyclerView.Adapter<MoviesReviewListAdapter.ReviewViewHolder> {

    private final Context mContext;
    private List<MovieReview> movieReviews;

    public MoviesReviewListAdapter(Context context) {
        mContext = context;
    }

    @NonNull
    @Override
    public ReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(mContext);
        View moviePoster = layoutInflater.inflate(R.layout.layout_item_movie_review, parent, false);

        return new MoviesReviewListAdapter.ReviewViewHolder(moviePoster);
    }

    @Override
    public void onBindViewHolder(@NonNull ReviewViewHolder holder, int position) {
        holder.bind(movieReviews.get(position));
    }

    @Override
    public int getItemCount() {
        if (movieReviews == null)
            return 0;

        return movieReviews.size();
    }

    public void setMovieReviews(List<MovieReview> movieReviews) {
        this.movieReviews = movieReviews;
        notifyDataSetChanged();
    }


    class ReviewViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.tv_review_content)
        TextView tvReviewContent;
        @BindView(R.id.tv_review_author)
        TextView tvReviewAuthor;
        @BindView(R.id.tv_read_more)
        TextView tvReadMore;

        ReviewViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        void bind(final MovieReview movieReview) {
            tvReviewAuthor.setText(movieReview.getAuthor());
            tvReviewContent.setText(movieReview.getContent());
            tvReadMore.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Uri uri = Uri.parse(movieReview.getUrl());
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    mContext.startActivity(intent);
                }
            });
        }
    }
}
