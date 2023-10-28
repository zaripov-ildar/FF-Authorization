package ru.findFood.auth.integrations;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import ru.findFood.auth.dtos.EmailToNewEmail;
import ru.findFood.auth.dtos.NewRestaurantAddedToDb;
import ru.findFood.auth.dtos.NewUserAddedToDb;
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
public class PersonAreaServiceIntegration {

    private final ServicesIntegrationProperties sip;

    private WebClient webClient;


    @Value("${integrations.person-service.url}")
    private String personServiceUrl;



    public Boolean isUserNameFree(String username) {
        return Boolean.TRUE.equals(getWebClient().get()
                .uri("/api/v1/persons/check/" + username)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(Boolean.class)
                .block());
    }

    public ResponseEntity<?> createNewPerson(NewUserAddedToDb newUserAddedToDb){
        return getWebClient()
                .post()
                .uri("/api/v1/persons")
                .body(Mono.just(newUserAddedToDb), NewUserAddedToDb.class)
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


    public ResponseEntity<?> changePersonEmail(EmailToNewEmail emailToNewEmail){
        return getWebClient()
                .put()
                .uri("/api/v1/persons/change/email")
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

    public ResponseEntity<?> removePersonById(Long id){
        return getWebClient()
                .delete()
                .uri("/api/v1/persons/" + id)
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
                    .baseUrl(personServiceUrl)
                    .clientConnector(new ReactorClientHttpConnector(httpClient))
                    .build();
        }
        return webClient;
    }
}
