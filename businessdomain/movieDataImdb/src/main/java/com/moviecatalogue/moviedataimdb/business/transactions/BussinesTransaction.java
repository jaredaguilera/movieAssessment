package com.moviecatalogue.moviedataimdb.business.transactions;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.moviecatalogue.moviedataimdb.entities.MovieResponse;
import com.moviecatalogue.moviedataimdb.entities.SearchMovie;
import com.moviecatalogue.moviedataimdb.exception.BussinesRuleException;
import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import reactor.netty.http.client.HttpClient;
import reactor.netty.tcp.TcpClient;


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
     
    @Autowired
    RestTemplate restTemplate;
    
    public MovieResponse getMovieTitleService(String tittle, String year, String c, String plot, String r) throws BussinesRuleException {
    	MovieResponse movies =  new MovieResponse();

    		
    	HttpHeaders headers = new HttpHeaders();
    	headers.add("Content-Type", "application/json; charset=utf-8");
    	HttpEntity<?> entity = new HttpEntity<>(headers);
	
    	String urlTemplate = UriComponentsBuilder.fromHttpUrl("https://www.omdbapi.com/")
    	        .queryParam("t", funcionReemplazarEspacios(tittle))
    	        .queryParam("y", year)
    	        .queryParam("c", c)
    	        .queryParam("plot", plot)
    	        .queryParam("r", r)
    	        .queryParam("apikey", apiKey)
    	        .encode()
    	        .toUriString();
    	HttpEntity<MovieResponse> response =
	            restTemplate.exchange(convertStringToHex(urlTemplate), HttpMethod.GET, entity, MovieResponse.class);
    	if(response.getBody().getResponse()) {
    		movies = response.getBody();
    	}else {
    		 BussinesRuleException exception = new BussinesRuleException("404", "No encontrada la pelicula", HttpStatus.NOT_FOUND);
    		 throw exception;
    	}

	  return  movies;
    }
    
    public String funcionReemplazarEspacios(String cadena) {
        return cadena.replace(" ", "+");
    }
    
    private String convertStringToHex(String str) {
        return str.replace("%C3%B1", "ñ");
    }
    
	public MovieResponse getIdParameter(String i, String plot, String r) throws BussinesRuleException {
		MovieResponse movies =  new MovieResponse();
    	HttpHeaders headers = new HttpHeaders();
    	headers.add("Content-Type", "application/xml; charset=utf-8");
    	HttpEntity<?> entity = new HttpEntity<>(headers);
	
    	String urlTemplate = UriComponentsBuilder.fromHttpUrl("https://www.omdbapi.com/")
    			.queryParam("i", i)
				.queryParam("plot", plot)
				.queryParam("r", r)
				.queryParam("apikey", apiKey)
    	        .encode()
    	        .toUriString();
	    	
    	HttpEntity<MovieResponse> response =
	            restTemplate.exchange(urlTemplate, HttpMethod.GET, entity, MovieResponse.class);
    	if(response.getBody().getResponse()) {
    		movies = response.getBody();
    	}else {
    		 BussinesRuleException exception = new BussinesRuleException("404", "No encontrada la pelicula", HttpStatus.NOT_FOUND);
    		 throw exception;
    	}
		return movies;
	}
    
  
    
	/*public MovieResponse getMovieTitleService(String t, String y, String type, String plot, String r) {
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
	}*/
	

	
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
