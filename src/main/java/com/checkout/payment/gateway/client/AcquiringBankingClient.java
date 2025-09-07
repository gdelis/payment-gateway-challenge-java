package com.checkout.payment.gateway.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class AcquiringBankingClient {

  // TODO - RestClient should be used based on the latest Spring Boot

  private final RestTemplate restTemplate;
  private final String baseUrl;

  public AcquiringBankingClient(final RestTemplate restTemplate,
      @Value("${services.acquiring-banking.base-url}") final String baseUrl) {

    this.restTemplate = restTemplate;
    this.baseUrl = baseUrl;
  }

  public AcquiringBankPaymentResponse processPayment(final AcquiringBankPaymentRequest request) {

    ResponseEntity<AcquiringBankPaymentResponse> response = restTemplate.postForEntity(
        baseUrl + "/payments", request, AcquiringBankPaymentResponse.class);

    if (response.getStatusCode().is5xxServerError() || response.getStatusCode()
        .is4xxClientError()) {
      throw new RuntimeException("Error processing payment");
    }

    return response.getBody();
  }
}
