package ibnk.intergrations.BetaSms;


import ibnk.intergrations.BetaSms.ResponseDto.BetaResponse;

import ibnk.intergrations.Tranzak.TranzakService;
import ibnk.service.InstitutionConfigService;
import ibnk.tools.jwtConfig.JwtService;
import ibnk.tools.nexaConfig.NexaResponse;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
public class BetaSmsService {

    @Value("${betta.sms.base.url}")
    String baserUrl;
    @Value("${betta.sms.sender.id}")
    String senderId;
    @Value("${betta.sms.public.key}")
    String publicKey;
    @Value("${betta.sms.access.key}")
    String accessKey;

    @Async
    public CompletableFuture<Boolean> sendSms(BetaResponse betaResponse){
        betaResponse.setAccessKey(accessKey);
        betaResponse.setPublicKey(publicKey);
        betaResponse.setSenderId(senderId);
        RestTemplate betaTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        JSONObject request = new JSONObject(betaResponse);
        HttpEntity<String> entity = new HttpEntity<String>(request.toString(), headers);
        String url = baserUrl + "/send/sms";
        ResponseEntity<String> response = betaTemplate.postForEntity(url, entity, String.class);
        System.out.println(response);
    return CompletableFuture.completedFuture(true);

    }



}