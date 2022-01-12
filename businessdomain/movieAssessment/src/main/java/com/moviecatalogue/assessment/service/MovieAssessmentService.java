package com.moviecatalogue.assessment.service;

import java.util.List;

import com.moviecatalogue.assessment.entities.Assessment;
import com.moviecatalogue.assessment.entities.Movie;
import com.moviecatalogue.assessment.exception.BussinesRuleException;

public interface MovieAssessmentService{

	Assessment post(String idMovie, long rating, long idUser) throws BussinesRuleException;

	List<Assessment> list();

	List<Movie> listMovie();

	void deleteById(long id);

}
