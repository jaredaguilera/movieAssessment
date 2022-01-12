package com.moviecatalogue.moviedataimdb.entities;

import java.util.List;

import lombok.Data;

@Data
public class SearchMovie {
	
	private List<MovieResponse> search;
	private String totalResults;
	private String Response;
	
	public SearchMovie() {

	}

	public SearchMovie(List<MovieResponse> search, String totalResults, String response) {
		super();
		this.search = search;
		this.totalResults = totalResults;
		Response = response;
	}
	
	public List<MovieResponse> getSearch() {
		return search;
	}
	public void setSearch(List<MovieResponse> search) {
		this.search = search;
	}
	public String getTotalResults() {
		return totalResults;
	}
	public void setTotalResults(String totalResults) {
		this.totalResults = totalResults;
	}
	public String getResponse() {
		return Response;
	}
	public void setResponse(String response) {
		Response = response;
	}

	
}
