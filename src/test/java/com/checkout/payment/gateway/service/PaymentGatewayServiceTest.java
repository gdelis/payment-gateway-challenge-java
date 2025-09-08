package com.checkout.payment.gateway.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.checkout.payment.gateway.client.AcquiringBankPaymentRequest;
import com.checkout.payment.gateway.client.AcquiringBankPaymentResponse;
import com.checkout.payment.gateway.client.AcquiringBankingClient;
import com.checkout.payment.gateway.enums.PaymentStatus;
import com.checkout.payment.gateway.model.PaymentRequestDTO;
import com.checkout.payment.gateway.model.PostPaymentResponse;
import com.checkout.payment.gateway.repository.PaymentsRepository;
import java.math.BigInteger;
import java.util.Currency;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

public class PaymentGatewayServiceTest {

  private static final String VALID_CARD_NUMBER = "4111111111111234";
  private static final int CARD_LAST_FOUR_DIGIT = 1234;
  private static final int VALID_EXPIRY_MONTH = 7;
  private static final int VALID_EXPIRY_YEAR = 2030;
  private static final Currency USD_CURRENCY = Currency.getInstance("USD");
  private static final BigInteger AMOUNT = BigInteger.valueOf(1599);
  private static final String CVV = "123";
  private static final UUID AUTHORIZATION_CODE = UUID.randomUUID();

  private PaymentsRepository paymentsRepository;
  private AcquiringBankingClient acquiringBankingClient;

  private PaymentGatewayService underTest;

  @BeforeEach
  void setUp() {
    paymentsRepository = mock(PaymentsRepository.class);
    acquiringBankingClient = mock(AcquiringBankingClient.class);
    underTest = new PaymentGatewayService(paymentsRepository, acquiringBankingClient);
  }

  @Test
  void processPayment_shouldMapFieldsAndPersist_whenAuthorized() {

    PaymentRequestDTO paymentRequestDTO = PaymentRequestDTO.builder()
        .cardNumber(VALID_CARD_NUMBER)
        .expiryMonth(VALID_EXPIRY_MONTH)
        .expiryYear(VALID_EXPIRY_YEAR)
        .currency(USD_CURRENCY)
        .amount(AMOUNT)
        .cvv(CVV)
        .build();

    when(acquiringBankingClient.processPayment(any(AcquiringBankPaymentRequest.class)))
        .thenReturn(AcquiringBankPaymentResponse.builder()
            .authorized(true)
            .authorizationCode(AUTHORIZATION_CODE.toString())
            .build());

    PostPaymentResponse response = underTest.processPayment(paymentRequestDTO);

    assertThat(response).isNotNull()
        .hasFieldOrPropertyWithValue("id", AUTHORIZATION_CODE)
        .hasFieldOrPropertyWithValue("status", PaymentStatus.AUTHORIZED)
        .hasFieldOrPropertyWithValue("cardNumberLastFour", CARD_LAST_FOUR_DIGIT)
        .hasFieldOrPropertyWithValue("expiryMonth", VALID_EXPIRY_MONTH)
        .hasFieldOrPropertyWithValue("expiryYear", VALID_EXPIRY_YEAR)
        .hasFieldOrPropertyWithValue("currency", USD_CURRENCY.getCurrencyCode())
        .hasFieldOrPropertyWithValue("amount", AMOUNT.intValue());

    // Verify repository add was called with the same response object
    ArgumentCaptor<PostPaymentResponse> captor = ArgumentCaptor.forClass(PostPaymentResponse.class);
    verify(paymentsRepository, times(1)).add(captor.capture());
    assertSame(response, captor.getValue());

  }

  @Test
  void processPayment_shouldGenerateRandomId_whenAuthorizationCodeEmpty() {

    PaymentRequestDTO paymentRequestDTO = PaymentRequestDTO.builder()
        .cardNumber(VALID_CARD_NUMBER)
        .expiryMonth(VALID_EXPIRY_MONTH)
        .expiryYear(VALID_EXPIRY_YEAR)
        .currency(USD_CURRENCY)
        .amount(AMOUNT)
        .cvv(CVV)
        .build();

    when(acquiringBankingClient.processPayment(any(AcquiringBankPaymentRequest.class)))
        .thenReturn(AcquiringBankPaymentResponse.builder()
            .authorized(true)
            .authorizationCode("")
            .build());

    PostPaymentResponse response = underTest.processPayment(paymentRequestDTO);

    assertThat(response).isNotNull();
    assertThat(response.getId()).isNotNull();
  }

  @Test
  void processPayment_shouldSetDeclined_whenNotAuthorized() {

    PaymentRequestDTO paymentRequestDTO = PaymentRequestDTO.builder()
        .cardNumber(VALID_CARD_NUMBER)
        .expiryMonth(VALID_EXPIRY_MONTH)
        .expiryYear(VALID_EXPIRY_YEAR)
        .currency(USD_CURRENCY)
        .amount(AMOUNT)
        .cvv(CVV)
        .build();

    UUID authCode = UUID.randomUUID();
    when(acquiringBankingClient.processPayment(any(AcquiringBankPaymentRequest.class)))
        .thenReturn(AcquiringBankPaymentResponse.builder()
            .authorized(false)
            .authorizationCode(authCode.toString())
            .build());

    PostPaymentResponse response = underTest.processPayment(paymentRequestDTO);

    assertThat(response).isNotNull()
        .extracting(PostPaymentResponse::getStatus)
        .isEqualTo(PaymentStatus.DECLINED);

    // Verify repository add was called with the same response object
    ArgumentCaptor<PostPaymentResponse> captor = ArgumentCaptor.forClass(PostPaymentResponse.class);
    verify(paymentsRepository, times(1)).add(captor.capture());
  }

  @Test
  void processPayment_overloadedMethod_withPostPaymentRequest_returnsUUID() {
    UUID result = underTest.processPayment(new com.checkout.payment.gateway.model.PostPaymentRequest());
    assertThat(result).isNotNull();
  }

  @Test
  void getPaymentById_shouldReturnFromRepository_whenPresent() {
    UUID id = UUID.randomUUID();
    PostPaymentResponse stored = new PostPaymentResponse();
    stored.setId(id);

    when(paymentsRepository.get(id)).thenReturn(java.util.Optional.of(stored));

    PostPaymentResponse result = underTest.getPaymentById(id);

    assertThat(result).isSameAs(stored);
  }
}
