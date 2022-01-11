package com.moviecatalogue.moviedataimdb.entities;

import java.util.List;

import lombok.Data;

@Data
public class SearchMovie {
	
	private List<MovieResponse> search;
	private String totalResults;
	private String Response;

}
