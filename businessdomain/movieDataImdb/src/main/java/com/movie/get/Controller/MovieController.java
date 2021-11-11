package com.movie.get.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.movie.get.entities.MovieResponse;
import com.movie.get.entities.SearchMovie;
import com.movie.get.service.MovieService;

@RestController
@RequestMapping("/movieImdb")
public class MovieController {

	@Autowired(required=true)
	MovieService movieService;
	
	@Value("${user.role}")
	private String role;
	
    @GetMapping("/getMovieTitle")
    public MovieResponse getMovieTitleController(@RequestParam String t,
				    		 @RequestParam(required = false) String y,
				    		 @RequestParam(required = false) String type,
				    		 @RequestParam(required = false) String plot,
				    		 @RequestParam(required = false) String r) {
    	System.out.println("rol asignado : " + role);
        return movieService.getMovieTitleService(t, y, type, plot, r);
    }  
    
    @GetMapping("/getIdParameter")
    public MovieResponse getIdMovieImdbController(@RequestParam String i,
				    		 @RequestParam(required = false) String plot,
				    		 @RequestParam(required = false) String r) {
        return movieService.getIdParameter(i, plot, r);
    } 
    
    @GetMapping("/searchParameter")
    public SearchMovie searchListMovieParameterController(
    		                 @RequestParam String s,
				    		 @RequestParam(required = false) String y,
				    		 @RequestParam(required = false) String type,
				    		 @RequestParam(required = false) String r,
				    		 @RequestParam(required = false) String page) {
        return movieService.searchParameter(s, y, type, r, page);
    }  

}
