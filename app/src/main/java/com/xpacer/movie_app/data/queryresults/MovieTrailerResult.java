package com.xpacer.movie_app.data.queryresults;

import com.google.gson.annotations.SerializedName;
import com.xpacer.movie_app.data.models.MovieTrailer;

import java.util.List;

public class MovieTrailerResult {

    private int id;

    @SerializedName("results")
    private List<MovieTrailer> results;

    public List<MovieTrailer> getResults() {
        return results;
    }

    public void setResults(List<MovieTrailer> results) {
        this.results = results;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
