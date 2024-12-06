package ibnk.service.BankingService;

import ibnk.dto.BankingDto.PaymentDto;
import ibnk.dto.BankingDto.TransferModel.ExecutePayment;
import ibnk.dto.BankingDto.TransferModel.InitPayment;
import ibnk.dto.BankingDto.TransferModel.PayableResponse;
import ibnk.intergrations.Tranzak.TranzakService;
import ibnk.models.internet.enums.ChannelCode;
import ibnk.service.BankingService.Bnkinterface.PaymentInterface;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;


@Service
@RequiredArgsConstructor
public class PaymentService implements PaymentInterface {

    @Value("${spring.http.url}")
    String URL;
    @Value("${spring.http.bearer}")
    String Bearer;
    @Value("${spring.http.publicKey}")
    String publicKey;

    private WebClient webClient;
    private static final Logger logger = LogManager.getLogger(PaymentService.class);

    public PaymentService(@Value("${spring.http.url}") String URL, @Value("${spring.http.bearer}") String Bearer, @Value("${spring.http.publicKey}") String publicKey) {
        this.URL = URL;
        this.Bearer = Bearer;
        this.publicKey = publicKey;

        this.webClient = WebClient.builder().baseUrl(this.URL).defaultHeaders(httpHeaders -> {
            httpHeaders.add(HttpHeaders.AUTHORIZATION, this.Bearer);
        }).build();
    }


    @PostConstruct
    public void init() {
        webClient = WebClient.builder().baseUrl(this.URL).defaultHeaders(httpHeaders -> {
            httpHeaders.add(HttpHeaders.AUTHORIZATION, this.Bearer);
        }).build();
        logger.info("Calling API to Authenticate : {}", this.URL);

//        logger.info("Received token response: {}", webClient.getBody());
    }

//    @Override
//    public PaymentDto initiatePayment(InitPayment body) {
//        return webClient.post()
//                .uri(uriBuilder -> uriBuilder.path("/payment").queryParam("publicKey", this.publicKey).build())
//                .body(BodyInserters.fromValue(body))
//                .retrieve()
//                .onStatus(HttpStatus::is4xxClientError, clientResponse -> {
//            // Capture the body of the response in case of a 4xx error
//            return clientResponse.bodyToMono(String.class).doOnNext(responseBody -> {
//                // Log the full error response body and status code
//                logger.error("Received 4xx error response: Status = {}, Body = {}", clientResponse.statusCode(), responseBody);
//            }).then(Mono.error(new RuntimeException("Unauthorized")));  // Return Mono.empty() to avoid throwing an exception
//        }).onStatus(HttpStatus::is5xxServerError, clientResponse -> {
//            // Capture the body of the response in case of a 5xx error
//            return clientResponse.bodyToMono(String.class).doOnNext(responseBody -> {
//                // Log the full error response body and status code
//                logger.error("Received 5xx error response: Status = {}, Body = {}", clientResponse.statusCode(), responseBody);
//            }).then(Mono.empty());  // Return Mono.empty() to avoid throwing an exception
//        })
//                .bodyToMono(PaymentDto.class)
//                .block(); // Blocking call, adjust accordingly for reactive flows
//    }
    @Override
    public PaymentDto initiatePayment(InitPayment body) {
        return webClient.post()
                .uri("/payment", uri -> uri.queryParam("publicKey", this.publicKey).build())
                .body(BodyInserters.fromValue(body))
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, clientResponse -> {
                    logger.info("Calling API to initiatePayment : {}", this.URL+"/payment");

//                    logger.info("Received token response: {}", webClient.getBody());
                    if (clientResponse.statusCode().equals(HttpStatus.UNAUTHORIZED)) {
                        return clientResponse.bodyToMono(String.class)
                                .doOnNext(Response->{
                                    logger.error("Received 4xx error response: Status = {}, Body = {}", clientResponse.statusCode(), Response);
                                } )
                                .then(Mono.error(new RuntimeException("Unauthorized")));
                    } else {
                        return clientResponse.bodyToMono(String.class)
                                .doOnNext((Response->{
                                    logger.error("Received 4xx error response: Status = {}, Body = {}", clientResponse.statusCode(), Response);
                                } ))
                                .then(Mono.error(new RuntimeException("Some other 4xx error")));
                    }
                })
                .bodyToMono(PaymentDto.class).doOnTerminate(() -> logger.info("Payment initiation completed"))
                .block();
    }

    @Override
    public PaymentDto executePayment(ExecutePayment body, String uuid) {
        String url = "/payment/" + uuid;
        return webClient.put().uri(url, uri -> uri.queryParam("publicKey", this.publicKey).build()).body(BodyInserters.fromValue(body)).retrieve().onStatus(HttpStatusCode::is4xxClientError, clientResponse -> {
            if (clientResponse.statusCode().equals(HttpStatus.UNAUTHORIZED)) {
                return clientResponse.bodyToMono(String.class).doOnNext(System.out::println).then(Mono.error(new RuntimeException("Unauthorized")));
            } else {
                return clientResponse.bodyToMono(String.class).doOnNext(System.out::println).then(Mono.error(new RuntimeException("Some other 4xx error")));
            }
        }).bodyToMono(PaymentDto.class).block();
    }

    @Override
    public PaymentDto getPayment(String uuid) {
        String url = "/payment/" + uuid;
        return webClient.get().uri(url, uri -> uri.queryParam("publicKey", this.publicKey).build()).retrieve().onStatus(HttpStatusCode::is4xxClientError, clientResponse -> {
            if (clientResponse.statusCode().equals(HttpStatus.UNAUTHORIZED)) {
                return clientResponse.bodyToMono(String.class).doOnNext(System.out::println).then(Mono.error(new RuntimeException("Unauthorized")));
            } else {
                return clientResponse.bodyToMono(String.class).doOnNext(System.out::println).then(Mono.error(new RuntimeException("Some other 4xx error")));
            }
        }).bodyToMono(PaymentDto.class).block();
    }

    @Override
    public PaymentDto transactionStatus(String uuid) {
        String url = "/transaction/" + uuid;
        return webClient.get().uri(uriBuilder -> uriBuilder.path(url).queryParam("publicKey", this.publicKey).build()).retrieve().onStatus(HttpStatusCode::is4xxClientError, clientResponse -> {
                    if (clientResponse.statusCode().equals(HttpStatus.UNAUTHORIZED)) {
                        return clientResponse.bodyToMono(String.class).doOnNext(System.out::println).then(Mono.error(new RuntimeException("Unauthorized")));
                    } else if (clientResponse.statusCode().equals(HttpStatus.NOT_FOUND)) {
                        // Return an empty Mono for 404 Not Found
                        return Mono.empty();
                    } else {
                        return clientResponse.bodyToMono(String.class).doOnNext(System.out::println).then(Mono.error(new RuntimeException("Some other 4xx error")));
                    }
                }).bodyToMono(PaymentDto.class).onErrorResume(e -> Mono.empty()) // Handle any other errors by returning empty
                .blockOptional()  // Use blockOptional to handle an empty result
                .orElse(null);  // Return null if no result is found
    }

    @Override
    public PayableResponse searchpayable(ChannelCode channel, String billId) {
        String url = "/payable";
        return webClient.get().uri(url, uri -> uri.queryParam("publicKey", this.publicKey).queryParam("channel", channel).queryParam("query", billId).build()).retrieve().onStatus(HttpStatusCode::is4xxClientError, clientResponse -> {
            if (clientResponse.statusCode().equals(HttpStatus.UNAUTHORIZED)) {
                return clientResponse.bodyToMono(String.class).doOnNext(System.out::println).then(Mono.error(new RuntimeException("Unauthorized")));
            } else {
                return clientResponse.bodyToMono(String.class).doOnNext(System.out::println).then(Mono.error(new RuntimeException("Some other 4xx error")));
            }
        }).bodyToMono(PayableResponse.class).block();
    }


}
