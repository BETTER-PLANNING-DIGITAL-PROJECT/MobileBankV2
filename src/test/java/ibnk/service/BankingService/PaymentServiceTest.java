//package ibnk.service.BankingService;
//
//import ibnk.dto.BankingDto.TransferModel.PayableResponse;
//import ibnk.models.enums.ChannelCode;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.*;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.http.HttpStatus;
//import org.springframework.web.reactive.function.client.ClientResponse;
//import org.springframework.web.reactive.function.client.WebClient;
//import reactor.core.publisher.Mono;
//
//import java.net.URI;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.ArgumentMatchers.anyString;
//import static org.mockito.Mockito.*;
//@ExtendWith(MockitoExtension.class)
//class PaymentServiceTest {
//    @Mock
//    private WebClient webClient;
//
//    @Mock
//    private WebClient.RequestHeadersUriSpec requestHeadersUriSpec;
//
//    @Mock
//    private WebClient.ResponseSpec responseSpec;
//
//    @InjectMocks
//    private PaymentService paymentService;
//    @Value("${spring.http.url}")
//    String URL;
//    @Value("${spring.http.bearer}")
//    String Bearer;
//    @Value("${spring.http.publicKey}")
//    String publicKey;
//    @BeforeEach
//    void setUp() {
//        // Stubbing the behavior of webClient
//        when(webClient.get()).thenReturn(requestHeadersUriSpec);
//        when(requestHeadersUriSpec.uri((URI) any())).thenReturn(requestHeadersUriSpec);
//        when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);
//    }
//
//    @Test
//    void testSearchPayable() {
//        // Mock the WebClient and its methods
//        WebClient.RequestBodyUriSpec requestBodyUriSpec = Mockito.mock(WebClient.RequestBodyUriSpec.class);
//        when(webClient.get()).thenReturn(requestHeadersUriSpec);
//        when(requestHeadersUriSpec.uri((URI) any())).thenReturn(requestBodyUriSpec);
//        when(requestBodyUriSpec.retrieve()).thenReturn(responseSpec);
//
//        // Create a mock response
//        PayableResponse expectedResponse = new PayableResponse(/* fill with mock data */);
//        expectedResponse.setSuccess(true);
////        expectedResponse.setAmount(100.00);
////        expectedResponse.setCurrency(Currency.USD);
////        expectedResponse.se(PaymentStatus.PENDING);
//        Mono<ClientResponse> responseMono = Mono.just(ClientResponse.create(HttpStatus.OK).build());
//        when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);
//        when(responseSpec.bodyToMono(ClientResponse.class)).thenReturn(responseMono);
//        Mono<PayableResponse> responseMonoo = Mono.just(expectedResponse);
//        ClientResponse clientResponse = Mockito.mock(ClientResponse.class);
//        when(clientResponse.statusCode()).thenReturn(HttpStatus.OK);
//        when(clientResponse.bodyToMono(PayableResponse.class)).thenReturn(responseMonoo);
//        when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);
//        when(responseSpec.bodyToMono(PayableResponse.class)).thenReturn(responseMonoo);
//        when(requestHeadersUriSpec.uri((URI) null)).thenReturn(requestBodyUriSpec);
//
//        // Mock the PaymentService
////        PaymentService paymentService = new PaymentService(webClient);
//
//        // Call the method under test
//        PayableResponse actualResponse = paymentService.searchpayable(ChannelCode.CHANNEL_ENEO_BILLS_CM, "201914610");
//
//        // Verify that the response matches the expected response
//        assertEquals(expectedResponse, actualResponse);
//    }
//}