package com.movie.assessment.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.movie.assessment.entities.Assessment;

public interface AssessmentRepository extends JpaRepository<Assessment, Long> {
	
    //@Query("SELECT t FROM Assessment t WHERE t.title = ?1")
    public Assessment findByTitleMovie(String titleMovie);
 
}
