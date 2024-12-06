package ibnk.tools.Interceptors;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import reactor.core.publisher.Mono;

import java.net.URI;

public class PayGateWayInterceptor  implements ExchangeFilterFunction {

     String baseUrl;
     String Bearer;
     String publicKey;

    public PayGateWayInterceptor(String baseUrl, String token, String publicKey) {
        this.baseUrl = baseUrl;
        this.Bearer = token;
        this.publicKey = publicKey;
    }

    @Override
    public Mono<ClientResponse> filter(@NonNull ClientRequest request, @NonNull ExchangeFunction next) {
        ClientRequest newRequest = ClientRequest.from(request)
                .url(URI.create(this.baseUrl + request.url().getPath()))
                .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .header(HttpHeaders.AUTHORIZATION, this.Bearer)
                .header("publicKey", this.publicKey)
                .build();
        return next.exchange(newRequest);
    }
}
