package com.moviecatalogue.assessment.service;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.moviecatalogue.assessment.dto.MovieResponse;
import com.moviecatalogue.assessment.entities.Assessment;
import com.moviecatalogue.assessment.entities.Movie;
import com.moviecatalogue.assessment.exception.BussinesRuleException;
import com.moviecatalogue.assessment.repository.AssessmentRepository;
import com.moviecatalogue.assessment.repository.MovieRepository;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import reactor.netty.http.client.HttpClient;
import reactor.netty.tcp.TcpClient;

@Service
public class MovieAssessmentImpl implements MovieAssessmentService{
	
	private final WebClient.Builder webClientBuilder;
	
	@Autowired
	AssessmentRepository assessmentRepository;

	@Autowired
	MovieRepository movieRepository;
	
	public MovieAssessmentImpl(WebClient.Builder webClientBuilder) {
		this.webClientBuilder = webClientBuilder;
	}

	// define timeout
	TcpClient tcpClient = TcpClient.create().option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
			.doOnConnected(connection -> {
				connection.addHandlerLast(new ReadTimeoutHandler(5000, TimeUnit.MILLISECONDS));
				connection.addHandlerLast(new WriteTimeoutHandler(5000, TimeUnit.MILLISECONDS));
			});

	@Override
	public Assessment post(String idMovie, long rating, long idUser) throws BussinesRuleException {
		
		WebClient client = webClientBuilder.clientConnector(new ReactorClientHttpConnector(HttpClient.from(tcpClient)))
				.baseUrl("http://businessdomain-moviedataimdb/movieImdb")
				.defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
				.defaultUriVariables(Collections.singletonMap("url", "http://businessdomain-moviedataimdb/movieImdb")).build();
		JsonNode block = client
				.method(HttpMethod.GET).uri(uriBuilder -> uriBuilder.path("/getIdParameter")
						.queryParam("i", idMovie).queryParam("plot", "full").build())
				.retrieve().bodyToMono(JsonNode.class).block();
		ObjectMapper objectMapper = new ObjectMapper();
		MovieResponse movieResponse = new MovieResponse();
		movieResponse = objectMapper.convertValue(block, MovieResponse.class);
		
		if(movieResponse != null && movieResponse.getResponse()) {
			 BussinesRuleException exception = new BussinesRuleException("404", "No encontrada la pelicula", HttpStatus.NOT_FOUND);
    		 throw exception;
		}
		
		Movie saveMovie = new Movie();
		saveMovie.setNombre(movieResponse.getTitle());
		saveMovie.setDescripcion(movieResponse.getPlot());
		saveMovie.setUrl_imagen(movieResponse.getPoster());
		Assessment saveAssessment = new Assessment();
		saveAssessment.setMovie(saveMovie);
		saveAssessment.setNota(rating);
		saveAssessment.setId_usuario(idUser);
		saveAssessment = assessmentRepository.save(saveAssessment);

		return saveAssessment;
	}
	
	@Override
	public List<Assessment> list(){ 
		return assessmentRepository.findAll();
	}
	
	@Override
	public List<Movie> listMovie(){ 
		return movieRepository.findAll();
	}
	
	
	@Override
	public void deleteById(long id){ 
		assessmentRepository.deleteById(id);
	}
}
