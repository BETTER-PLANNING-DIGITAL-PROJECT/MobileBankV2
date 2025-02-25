package ibnk.intergrations.nexaConfig;

import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor
@Configuration
public class NexaService {
    private String URL = "https://smsvas.com/bulk/public/index.php/api/v1/sendsms";
    static final String Username = "global.wallet@betterplanning.net";
    static final String Password = "GlobalWallet";
    private final RestTemplate RestNexa = new RestTemplate();


    @Async()
    public CompletableFuture<Boolean> SendSms(NexaResponse nexaResponse)  {
        nexaResponse.setPassword(Password);
        nexaResponse.setUser(Username);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        JSONObject map = new JSONObject(nexaResponse);
        HttpEntity<String> request = new HttpEntity<>(map.toString(), headers);
        try {
           RestNexa.exchange(URL, HttpMethod.POST, request, String.class);
            return CompletableFuture.completedFuture(true);
        } catch (Exception ex) {
            System.out.println("Network Error occurred Make sure your Connected: " + ex.getMessage());
        }
        return CompletableFuture.completedFuture(false);
    }

    @Async
    public CompletableFuture<Boolean> LessSms(NexaResponse nexaResponse) {
        nexaResponse.setPassword(Password);
        nexaResponse.setUser(Username);
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        JSONObject request = new JSONObject(nexaResponse);
        HttpEntity<String> entity = new HttpEntity<String>(request.toString(), headers);
        String url = URL;
        ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
        System.out.println(response);
        return CompletableFuture.completedFuture(true);
    }
    @Async
    public CompletableFuture<Boolean> WebSms(NexaResponse nexaResponse) {
        nexaResponse.setPassword(Password);
        nexaResponse.setUser(Username);
        JSONObject map = new JSONObject(nexaResponse);
        try {
            Object responsObject = WebClient.builder()
                    .baseUrl(URL)
                    .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .build()
                    .post()
                    .bodyValue(map)
                    .retrieve()
                    .toEntity(Object.class);
            System.out.println(responsObject);
            return CompletableFuture.completedFuture(true);
        }catch (Exception ex){
            System.out.println("Network Error Occurred Make sure your Connected: " + ex.getMessage());
            return CompletableFuture.completedFuture(false);
        }
    }
}
