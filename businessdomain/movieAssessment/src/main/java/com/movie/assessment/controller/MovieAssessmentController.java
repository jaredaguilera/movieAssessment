package com.movie.assessment.controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.movie.assessment.entities.Assessment;
import com.movie.assessment.entities.MovieAssessment;
import com.movie.assessment.entities.MovieResponse;
import com.movie.assessment.repository.AssessmentRepository;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import reactor.netty.http.client.HttpClient;
import reactor.netty.tcp.TcpClient;

@RestController
@RequestMapping("/movieassessment")
public class MovieAssessmentController {

	@Autowired(required = true)
	AssessmentRepository assessmentRepository;

	private final WebClient.Builder webClientBuilder;

	public MovieAssessmentController(WebClient.Builder webClientBuilder) {
		this.webClientBuilder = webClientBuilder;
	}

	// define timeout
	TcpClient tcpClient = TcpClient.create().option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
			.doOnConnected(connection -> {
				connection.addHandlerLast(new ReadTimeoutHandler(5000, TimeUnit.MILLISECONDS));
				connection.addHandlerLast(new WriteTimeoutHandler(5000, TimeUnit.MILLISECONDS));
			});

	@GetMapping("movieTitle/{titleMovie}")
	public MovieAssessment get(@PathVariable String titleMovie) {
		MovieAssessment movieAssessment = new MovieAssessment();
		Assessment assessment = assessmentRepository.findByTitleMovie(titleMovie);
		WebClient client = webClientBuilder.clientConnector(new ReactorClientHttpConnector(HttpClient.from(tcpClient)))
				.baseUrl("http://businessdomain-moviedataimdb/movieImdb")
				.defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
				.defaultUriVariables(Collections.singletonMap("url", "http://businessdomain-moviedataimdb/movieImdb")).build();
		JsonNode block = client
				.method(HttpMethod.GET).uri(uriBuilder -> uriBuilder.path("/getMovieTitle")
						.queryParam("t", assessment.getTitleMovie()).queryParam("plot", "full").build())
				.retrieve().bodyToMono(JsonNode.class).block();
		ObjectMapper objectMapper = new ObjectMapper();
		MovieResponse movieResponse = new MovieResponse();
		movieResponse = objectMapper.convertValue(block, MovieResponse.class);
		movieAssessment.setAssessment(assessment);
		movieAssessment.setMovieResponse(movieResponse);
		return movieAssessment;
	}

	@PostMapping("/enteRating")
	public ResponseEntity<?> post(@RequestBody Assessment input) {
		Assessment save = assessmentRepository.save(input);
		return ResponseEntity.ok(save);
	}

	@GetMapping("getTotalMoviesRated")
	public List<MovieAssessment> list() {
		List<MovieAssessment> movieAssessmentsList = new ArrayList<MovieAssessment>();
		List<Assessment> listAssessment = assessmentRepository.findAll();
		for (Assessment assessment : listAssessment) {
			WebClient client = webClientBuilder
					.clientConnector(new ReactorClientHttpConnector(HttpClient.from(tcpClient)))
					.baseUrl("http://businessdomain-moviedataimdb/movieImdb")
					.defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
					.defaultUriVariables(Collections.singletonMap("url", "http://businessdomain-moviedataimdb/movieImdb")).build();
			JsonNode block = client
					.method(HttpMethod.GET).uri(uriBuilder -> uriBuilder.path("/getIdParameter")
							.queryParam("i", assessment.getImdbID()).queryParam("plot", "full").build())
					.retrieve().bodyToMono(JsonNode.class).block();
			ObjectMapper objectMapper = new ObjectMapper();
			MovieResponse movieResponse = new MovieResponse();
			MovieAssessment movieAssessment = new MovieAssessment();

			movieResponse = objectMapper.convertValue(block, MovieResponse.class);
			movieAssessment.setAssessment(assessment);
			movieAssessment.setMovieResponse(movieResponse);
			movieAssessmentsList.add(movieAssessment);
		}
		return movieAssessmentsList;
	}
}
