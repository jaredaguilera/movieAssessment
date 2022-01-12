package com.moviecatalogue.assessment.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.moviecatalogue.assessment.exception.BussinesRuleException;
import com.moviecatalogue.assessment.service.MovieAssessmentService;


@RestController
@RequestMapping("/movieassessment")
public class MovieAssessmentController {


	@Autowired
	MovieAssessmentService assessmentService;
	
	
	@PostMapping("/enteRating")
	public ResponseEntity<?> post(@RequestParam String idMovie, @RequestParam long rating, @RequestParam long idUser) throws BussinesRuleException {
		return ResponseEntity.ok(assessmentService.post(idMovie, rating, idUser));
		
	}
	
	@GetMapping("getTotalMoviesRated")
	public ResponseEntity<?> list() {
		return ResponseEntity.ok(assessmentService.list());
	}

	@GetMapping("getTotalMovies")
	public ResponseEntity<?> listMovie() {
		return ResponseEntity.ok(assessmentService.listMovie());
	}
	
	@DeleteMapping("deleteRated")
	public ResponseEntity<?> deleteRated() {
		return ResponseEntity.ok(assessmentService.listMovie());
	}
	
}
