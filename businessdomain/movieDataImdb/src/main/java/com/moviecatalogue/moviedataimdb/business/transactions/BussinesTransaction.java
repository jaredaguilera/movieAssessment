package com.moviecatalogue.moviedataimdb.business.transactions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.moviecatalogue.moviedataimdb.entities.MovieResponse;
import com.moviecatalogue.moviedataimdb.entities.SearchMovie;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import reactor.netty.http.client.HttpClient;
import reactor.netty.tcp.TcpClient;
import java.net.UnknownHostException;

@Service
public class BussinesTransaction {

    static final String SEARCH = "Search";
    static final String TOTALRESULTS = "totalResults";
    static final String RESPONSE = "Response";
    private final WebClient.Builder webClientBuilder;

    public BussinesTransaction(WebClient.Builder webClientBuilder) {
        this.webClientBuilder = webClientBuilder;
    }
    
    @Value("${apikeyimdbapi}")
    protected String apiKey;
    
    //define timeout
    TcpClient tcpClient = TcpClient
            .create()
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
            .doOnConnected(connection -> {
                connection.addHandlerLast(new ReadTimeoutHandler(50000, TimeUnit.MILLISECONDS));
                connection.addHandlerLast(new WriteTimeoutHandler(50000, TimeUnit.MILLISECONDS));
            });
     
	public MovieResponse getMovieTitleService(String t, String y, String type, String plot, String r) {
		MovieResponse newJsonNode = new MovieResponse();
		try {
	
			WebClient client = webClientBuilder.clientConnector(new ReactorClientHttpConnector(HttpClient.from(tcpClient)))
					.baseUrl("https://www.omdbapi.com/")
					.defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE).build();
			
			JsonNode block = client
					.method(HttpMethod.GET).uri(uriBuilder -> uriBuilder
							 .queryParam("t", t)
							 .queryParam("y", y)
							 .queryParam("type", type)
							 .queryParam("plot", plot)
							 .queryParam("r", r)
							 .queryParam("apikey", apiKey).build())
					.retrieve().bodyToMono(JsonNode.class)
					.doOnNext(s -> System.out.println("Value: " + s)).block();
			ObjectMapper objectMapper = new ObjectMapper();
			newJsonNode = objectMapper.convertValue(block, MovieResponse.class);
		
		} catch (WebClientResponseException e) {
			System.out.println(e.getMessage());
		}
		return newJsonNode;
	}
	
	public MovieResponse getIdParameter(String i, String plot, String r) {
		WebClient client = webClientBuilder.clientConnector(new ReactorClientHttpConnector(HttpClient.from(tcpClient)))
				.baseUrl("http://www.omdbapi.com/")
				.defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE).build();
		JsonNode block = client
				.method(HttpMethod.GET).uri(uriBuilder -> uriBuilder
						.queryParam("i", i)
						.queryParam("plot", plot)
						.queryParam("r", r)
						.queryParam("apikey", apiKey).build())
				.retrieve().bodyToMono(JsonNode.class).block();
		ObjectMapper objectMapper = new ObjectMapper();
		MovieResponse newJsonNode = new MovieResponse();

		newJsonNode = objectMapper.convertValue(block, MovieResponse.class);

		return newJsonNode;
	}
	
	public SearchMovie searchParameter(String s, String y, String type, String r, String page) {
		WebClient client = webClientBuilder.clientConnector(new ReactorClientHttpConnector(HttpClient.from(tcpClient)))
				.baseUrl("http://www.omdbapi.com/")
				.defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE).build();
		Object block = client
				.method(HttpMethod.GET).uri(uriBuilder -> uriBuilder
						.queryParam("s", s)
						.queryParam("y", y)
						.queryParam("type", type)
						.queryParam("r", r)
						.queryParam("page", page)
						.queryParam("apikey", apiKey).build())
				.retrieve().bodyToMono(Object.class).block();
	
		SearchMovie searchMovie = new SearchMovie();
		List<MovieResponse> movieResponseList = new ArrayList<MovieResponse>();
		
		ObjectMapper objectMapper = new ObjectMapper();
		LinkedHashMap object = (LinkedHashMap) objectMapper.convertValue(block, Object.class);
		
		for (LinkedHashMap movieResume : ((ArrayList<LinkedHashMap>) object.get(SEARCH))) {
			MovieResponse newJsonNode = new MovieResponse();
			newJsonNode = objectMapper.convertValue(movieResume, MovieResponse.class);
			movieResponseList.add(newJsonNode);
		}
		
		searchMovie.setSearch(movieResponseList);
		searchMovie.setTotalResults((String) object.get(TOTALRESULTS));
		searchMovie.setResponse((String) object.get(RESPONSE));
		
		return searchMovie;
	}

}
