package com.movie.assessment.entities;

import java.io.Serializable;
import lombok.Data;

@Data
public class MovieAssessment implements Serializable {
	
	private Assessment assessment;
	private MovieResponse movieResponse;
	
}
