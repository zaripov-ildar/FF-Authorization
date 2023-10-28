package ru.findFood.auth.integrations;


import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import ru.findFood.auth.dtos.EmailToNewEmail;
import ru.findFood.auth.dtos.NewRestaurantAddedToDb;
import ru.findFood.auth.exceptions.ClientError;
import ru.findFood.auth.exceptions.ServerError;
import ru.findFood.auth.exceptions.WebClientRequestException;
import ru.findFood.auth.properties.ServicesIntegrationProperties;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Component
@EnableConfigurationProperties(
        {ServicesIntegrationProperties.class}
)
@RequiredArgsConstructor
@Slf4j
public class RestaurantsServiceIntegration {

    private final ServicesIntegrationProperties sip;
    private WebClient webClient;

    @Value("${integrations.restaurants-service.url}")
    private String restaurantServiceUrl;

    public Boolean isRestTitleFree(String title) {
        return Boolean.TRUE.equals(getWebClient().get()
                .uri("/api/v1/restaurants/check/" + title)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(Boolean.class)
                .block());
    }

    public ResponseEntity<?> createNewRestaurant(NewRestaurantAddedToDb newRestaurantAddedToDb) {
        return (ResponseEntity<?>) getWebClient()
                .post()
                .uri("/api/v1/restaurants")
                .body(Mono.just(newRestaurantAddedToDb), NewRestaurantAddedToDb.class)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .onStatus(
                        HttpStatusCode::is4xxClientError,
                        clientResponse -> clientResponse.toEntity(new ParameterizedTypeReference<ClientError>() {})
                                .map(clientError -> {
                                    throw new WebClientRequestException(Objects.requireNonNull(clientError.getBody()).getMessage(), clientError.getBody().getStatus());
                                }
                        ))
                .onStatus(
                        HttpStatusCode::is5xxServerError,
                        clientResponse -> clientResponse.toEntity(new ParameterizedTypeReference<ServerError>() {})
                                .map(serverError -> {
                                            throw new WebClientRequestException(Objects.requireNonNull(serverError.getBody()).getError(), serverError.getBody().getStatus());
                                        }
                                ))
                .toEntity(HttpStatus.class)
                .block();
    }


    public ResponseEntity<?> changeRestaurantEmail(EmailToNewEmail emailToNewEmail) {
        return (ResponseEntity<?>) getWebClient()
                .put()
                .uri("/api/v1/restaurants/info/change/email")
                .body(Mono.just(emailToNewEmail), EmailToNewEmail.class)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .onStatus(
                        HttpStatusCode::is4xxClientError,
                        clientResponse -> clientResponse.toEntity(new ParameterizedTypeReference<ClientError>() {})
                                .map(clientError -> {
                                            throw new WebClientRequestException(Objects.requireNonNull(clientError.getBody()).getMessage(), clientError.getBody().getStatus());
                                        }
                                ))
                .onStatus(
                        HttpStatusCode::is5xxServerError,
                        clientResponse -> clientResponse.toEntity(new ParameterizedTypeReference<ServerError>() {})
                                .map(serverError -> {
                                            throw new WebClientRequestException(Objects.requireNonNull(serverError.getBody()).getError(), serverError.getBody().getStatus());
                                        }
                                ))
                .toEntity(HttpStatus.class)
                .block();
    }

    public ResponseEntity<?> deleteRestaurantById(Long id){
        return getWebClient()
                .delete()
                .uri("/api/v1/restaurants/" + id)
                .retrieve()
                .onStatus(
                        HttpStatusCode::is4xxClientError,
                        clientResponse -> clientResponse.toEntity(new ParameterizedTypeReference<ClientError>() {})
                                .map(clientError -> {
                                            throw new WebClientRequestException(Objects.requireNonNull(clientError.getBody()).getMessage(), clientError.getBody().getStatus());
                                        }
                                ))
                .onStatus(
                        HttpStatusCode::is5xxServerError,
                        clientResponse -> clientResponse.toEntity(new ParameterizedTypeReference<ServerError>() {})
                                .map(serverError -> {
                                            throw new WebClientRequestException(Objects.requireNonNull(serverError.getBody()).getError(), serverError.getBody().getStatus());
                                        }
                                ))
                .toEntity(HttpStatus.class)
                .block();
    }


    private WebClient getWebClient() {
        if (webClient == null) {
            HttpClient httpClient = HttpClient.create()
                    .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, sip.getConnectTimeout())
                    .doOnConnected(connection -> connection.addHandlerLast(new ReadTimeoutHandler(sip.getReadTimeout(), TimeUnit.MILLISECONDS))
                            .addHandlerLast(new WriteTimeoutHandler(sip.getWriteTimeout(), TimeUnit.MILLISECONDS)));

            webClient = WebClient.builder()
                    .baseUrl(restaurantServiceUrl)
                    .clientConnector(new ReactorClientHttpConnector(httpClient))
                    .build();
        }
        return webClient;
    }
}
