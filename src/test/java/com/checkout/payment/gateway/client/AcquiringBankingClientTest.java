package com.checkout.payment.gateway.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

class AcquiringBankingClientTest {

  private final String baseUrl = "http://acquirer";
  @Mock
  private RestTemplate restTemplate;
  private AcquiringBankingClient client;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
    client = new AcquiringBankingClient(restTemplate, baseUrl);
  }

  @Test
  void processPayment_success_whenRequiredInformationIsValid() {

    AcquiringBankPaymentRequest request = AcquiringBankPaymentRequest.builder()
        .cardNumber("4111111111111111")
        .expiryDate("12/30")
        .cvv("USD")
        .amount(1234)
        .cvv("123")
        .build();

    AcquiringBankPaymentResponse body = AcquiringBankPaymentResponse.builder()
        .authorized(true)
        .authorizationCode("AUTH-123")
        .build();

    when(restTemplate.postForEntity(eq(baseUrl + "/payments"), eq(request),
        eq(AcquiringBankPaymentResponse.class)))
        .thenReturn(new ResponseEntity<>(body, HttpStatus.OK));

    AcquiringBankPaymentResponse result = client.processPayment(request);

    assertThat(result).isNotNull();
    assertThat(result.isAuthorized()).isTrue();
    assertThat(result.getAuthorizationCode()).isEqualTo("AUTH-123");

    verify(restTemplate).postForEntity(eq(baseUrl + "/payments"), eq(request),
        eq(AcquiringBankPaymentResponse.class));
  }

  @Test
  void processPayment_success_whenRequiredInformationIsInvalid() {

    AcquiringBankPaymentRequest request = AcquiringBankPaymentRequest.builder()
        .cardNumber("12")
        .expiryDate("12/30")
        .cvv("USD")
        .amount(1234)
        .cvv("123")
        .build();

    AcquiringBankPaymentResponse body = AcquiringBankPaymentResponse.builder()
        .authorized(false)
        .authorizationCode("")
        .build();

    when(restTemplate.postForEntity(eq(baseUrl + "/payments"), eq(request),
        eq(AcquiringBankPaymentResponse.class)))
        .thenReturn(new ResponseEntity<>(body, HttpStatus.OK));

    AcquiringBankPaymentResponse result = client.processPayment(request);

    assertThat(result).isNotNull();
    assertThat(result.isAuthorized()).isFalse();
    assertThat(result.getAuthorizationCode()).isEmpty();

    verify(restTemplate).postForEntity(eq(baseUrl + "/payments"), eq(request),
        eq(AcquiringBankPaymentResponse.class));
  }

  @Test
  void processPayment_clientError() {
    AcquiringBankPaymentRequest request = AcquiringBankPaymentRequest.builder().build();

    when(restTemplate.postForEntity(eq(baseUrl + "/payments"), eq(request),
        eq(AcquiringBankPaymentResponse.class)))
        .thenReturn(new ResponseEntity<>(null, HttpStatus.BAD_REQUEST));

    assertThatThrownBy(() -> client.processPayment(request))
        .isInstanceOf(RuntimeException.class)
        .hasMessageContaining("Error processing payment");
  }

  @Test
  void processPayment_serverError() {
    AcquiringBankPaymentRequest request = AcquiringBankPaymentRequest.builder().build();

    when(restTemplate.postForEntity(eq(baseUrl + "/payments"), eq(request),
        eq(AcquiringBankPaymentResponse.class)))
        .thenReturn(new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR));

    assertThatThrownBy(() -> client.processPayment(request))
        .isInstanceOf(RuntimeException.class)
        .hasMessageContaining("Error processing payment");
  }
}
