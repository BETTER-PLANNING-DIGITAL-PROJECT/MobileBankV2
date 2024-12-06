package ibnk.intergrations.Tranzak;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import ibnk.intergrations.Tranzak.requestDtos.AuthDto;
import ibnk.intergrations.Tranzak.requestDtos.InitiateCollection;
import ibnk.intergrations.Tranzak.requestDtos.PhoneVerification;
import ibnk.intergrations.Tranzak.responseDtos.AuthResponse;
import ibnk.intergrations.Tranzak.responseDtos.InitiateCollectionResponse;
import ibnk.intergrations.Tranzak.responseDtos.PhoneVerifyResponse;
import ibnk.models.internet.InstitutionConfig;
import ibnk.models.internet.client.Subscriptions;
import ibnk.service.InstitutionConfigService;
import ibnk.tools.error.ResourceNotFoundException;
import ibnk.tools.error.ValidationException;
import ibnk.tools.jwtConfig.JwtService;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class TranzakService {
    @Value("${spring.tranzak.baseurl}")
    String baseUrl;
    private static final Logger logger = LogManager.getLogger(TranzakService.class);
    private final RestTemplate restTemplate = new RestTemplate();
    private final JwtService jwtService;
    private final InstitutionConfigService institutionConfigService;
    @Value("${spring.tranzak.appid}")
    String APPID ;
    @Value("${TRANZAK_APP_ID}")
    String APPIDBetter ;
    @Value("${spring.tranzak.appkey}")
    String APPKEY ;
    @Value("${TRANZAK_APP_KEY}")
    String APPKEYBetter;

    public ResponseEntity<String> generateToken() {
        String apiUrl = baseUrl + "/auth/token";
        AuthDto dto = new AuthDto();

        dto.setAppId(APPID);
        dto.setAppKey(APPKEY);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<AuthDto> request = new HttpEntity<AuthDto>(dto, headers);
        logger.info("Calling API to generate token: {}", apiUrl);
        ResponseEntity res = restTemplate.postForEntity(apiUrl, request, String.class);
        logger.info("Received token response: {}", res.getBody());

        return res;
    }
    public ResponseEntity<String> generateTokenBetter() {
        String apiUrl = baseUrl + "/auth/token";
        AuthDto dto = new AuthDto();

        dto.setAppId(APPIDBetter);
        dto.setAppKey(APPKEYBetter);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<AuthDto> request = new HttpEntity<AuthDto>(dto, headers);
        logger.info("Calling API to generate token: {}", apiUrl);
        ResponseEntity res = restTemplate.postForEntity(apiUrl, request, String.class);
        logger.info("Received token response: {}", res.getBody());

        return res;
    }

    public InitiateCollectionResponse transactionResponseStatus(InitiateCollectionResponse dto) throws JsonProcessingException,  ValidationException {
//        InstitutionConfig config = institutionConfigService.listConfig();
        String apiUrl = baseUrl + "/xp021/v1/request/refresh-transaction-status";
         String authString =  generateToken().getBody();
         System.out.println(authString);
        AuthResponse auth = (AuthResponse) transformToObject(authString, new TypeReference<AuthResponse>() {});

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        assert auth != null;
        if(!auth.isSuccess()) throw new ValidationException("something went wrong try later");
        headers.setBearerAuth(auth.getData().getToken());
        HttpEntity<Object> request = new HttpEntity<>(dto.getData().getRequestId(), headers);
        logger.info("Calling API transaction status: {}", apiUrl);

        String stringResponse =  restTemplate.postForEntity(apiUrl, request, String.class).getBody();
        logger.info("Received transaction status response: {}", stringResponse);
        return (InitiateCollectionResponse) transformToObject(stringResponse, new TypeReference<InitiateCollectionResponse>() {});
    }

    public InitiateCollectionResponse generateRedirectPayment(InitiateCollection dto, Subscriptions subs) throws JsonProcessingException, ResourceNotFoundException, ValidationException {
        InstitutionConfig config = institutionConfigService.getInstConfig();
        String apiUrl = baseUrl + "/xp021/v1/request/create";
         String authString =  generateToken().getBody();
         System.out.println(authString);
        AuthResponse auth = (AuthResponse) transformToObject(authString, new TypeReference<AuthResponse>() {});

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        assert auth != null;
        if(!auth.isSuccess()) throw new ValidationException("something went wrong try later");
        headers.setBearerAuth(auth.getData().getToken());
        dto.setReturnUrl(config.getReturnUrl() + dto.getMchTransactionRef() + "&t="+jwtService.generateTokenForClient(subs).getToken());
        HttpEntity<Object> request = new HttpEntity<>(dto, headers);
        logger.info("Calling API to initiate request create: {}", apiUrl);

        String stringResponse =  restTemplate.postForEntity(apiUrl, request, String.class).getBody();
        logger.info("Received initiate request create response: {}", stringResponse);
        return (InitiateCollectionResponse) transformToObject(stringResponse, new TypeReference<InitiateCollectionResponse>() {});
    }


    public PhoneVerifyResponse verifyCustomerPhoneNumber(PhoneVerification dto) throws JsonProcessingException, ResourceNotFoundException, ValidationException {
        String apiUrl = baseUrl + "/xp021/v1/name-verification/create";
        String authString =  generateToken().getBody();
        System.out.println(authString);
        AuthResponse auth = (AuthResponse) transformToObject(authString, new TypeReference<AuthResponse>() {});

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        assert auth != null;
        if(!auth.isSuccess()) throw new ValidationException("something went wrong try later");
        headers.setBearerAuth(auth.getData().getToken());
        HttpEntity<Object> request = new HttpEntity<>(dto, headers);

        String stringResponse =  restTemplate.postForEntity(apiUrl, request, String.class).getBody();
        logger.info("Received initiate request create response: {}", stringResponse);
        return (PhoneVerifyResponse) transformToObject(stringResponse, new TypeReference<PhoneVerifyResponse>() {});
    }
    public PhoneVerifyResponse verifyCustomerPhoneNumberBetter(PhoneVerification dto) throws JsonProcessingException, ResourceNotFoundException, ValidationException {
        String apiUrl = baseUrl + "/xp021/v1/name-verification/create";
        String authString =  generateTokenBetter().getBody();
        System.out.println(authString);
        AuthResponse auth = (AuthResponse) transformToObject(authString, new TypeReference<AuthResponse>() {});

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        assert auth != null;
        if(!auth.isSuccess()) throw new ValidationException("something went wrong try later");
        headers.setBearerAuth(auth.getData().getToken());
        HttpEntity<Object> request = new HttpEntity<>(dto, headers);

        String stringResponse =  restTemplate.postForEntity(apiUrl, request, String.class).getBody();
        logger.info("Received initiate request create response: {}", stringResponse);
        return (PhoneVerifyResponse) transformToObject(stringResponse, new TypeReference<PhoneVerifyResponse>() {});
    }

    public PhoneVerifyResponse verifyCustomerPhoneNumberStatus(String ref) throws JsonProcessingException, ResourceNotFoundException, ValidationException {
        String apiUrl = baseUrl + "/xp021/v1/name-verification/details?customTransactionId=" + ref;

        String authString = generateToken().getBody();
        System.out.println(authString);

        AuthResponse auth = (AuthResponse) transformToObject(authString, new TypeReference<AuthResponse>() {});

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        assert auth != null;

        if (!auth.isSuccess()) {
            throw new ValidationException("Something went wrong, try later");
        }

        headers.setBearerAuth(auth.getData().getToken());

        HttpEntity<Object> request = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(apiUrl, HttpMethod.GET, request, String.class);

        String stringResponse = response.getBody();
        logger.info("Received initiate request create response: {}", stringResponse);

        return (PhoneVerifyResponse) transformToObject(stringResponse, new TypeReference<PhoneVerifyResponse>() {});
    }
    public PhoneVerifyResponse verifyCustomerPhoneNumberStatusBetter(String ref) throws JsonProcessingException, ResourceNotFoundException, ValidationException {
        String apiUrl = baseUrl + "/xp021/v1/name-verification/details?customTransactionId=" + ref;

        String authString = generateTokenBetter().getBody();
        System.out.println(authString);

        AuthResponse auth = (AuthResponse) transformToObject(authString, new TypeReference<AuthResponse>() {});

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        assert auth != null;

        if (!auth.isSuccess()) {
            throw new ValidationException("Something went wrong, try later");
        }

        headers.setBearerAuth(auth.getData().getToken());

        HttpEntity<Object> request = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(apiUrl, HttpMethod.GET, request, String.class);

        String stringResponse = response.getBody();
        logger.info("Received initiate request create response: {}", stringResponse);

        return (PhoneVerifyResponse) transformToObject(stringResponse, new TypeReference<PhoneVerifyResponse>() {});
    }



    private Object transformToObject(String json, TypeReference objectClass) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.configure(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, false);
        mapper.configure(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT, true);
        return mapper.readValue(json, objectClass);
    }
}
