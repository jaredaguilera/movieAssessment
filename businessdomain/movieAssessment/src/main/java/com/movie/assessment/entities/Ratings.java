package com.movie.assessment.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;

import lombok.Data;

@Data
public class Ratings implements Serializable {
	
	@JsonProperty ("Source")
	private String source;
	
	@JsonProperty ("Value")
	private String value;
}
