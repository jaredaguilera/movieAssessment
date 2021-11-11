package com.movie.get.service;

import com.movie.get.entities.MovieResponse;
import com.movie.get.entities.SearchMovie;

public interface MovieService {

	public MovieResponse getMovieTitleService(String t, String y, String type, String plot, String r);

	public MovieResponse getIdParameter(String i, String plot, String r);

	public SearchMovie searchParameter(String s, String y, String type, String r, String page);

}
