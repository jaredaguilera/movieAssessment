package com.movie.assessment.service;

import java.util.concurrent.TimeUnit;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.movie.assessment.entities.MovieResponse;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import reactor.netty.http.client.HttpClient;
import reactor.netty.tcp.TcpClient;

@Service
public class MovieAssessmentImpl implements MovieAssessmentService{
	
	private final WebClient.Builder webClientBuilder;

	public MovieAssessmentImpl(WebClient.Builder webClientBuilder) {
		this.webClientBuilder = webClientBuilder;
	}

	// define timeout
	TcpClient tcpClient = TcpClient.create().option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
			.doOnConnected(connection -> {
				connection.addHandlerLast(new ReadTimeoutHandler(5000, TimeUnit.MILLISECONDS));
				connection.addHandlerLast(new WriteTimeoutHandler(5000, TimeUnit.MILLISECONDS));
			});

	public MovieResponse getTitle(String accountIban) {
		WebClient client = webClientBuilder.clientConnector(new ReactorClientHttpConnector(HttpClient.from(tcpClient)))
				.baseUrl("https://omdbapi.com/")
				.defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE).build();
		Object block = client.method(HttpMethod.GET)
				.uri(uriBuilder -> uriBuilder.queryParam("t", accountIban).build())
				.retrieve().bodyToFlux(Object.class).collectList().block();
		MovieResponse rs  = (MovieResponse) block;
		return rs;
	}
	
	
	
}
