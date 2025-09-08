package com.checkout.payment.gateway.service;

import com.checkout.payment.gateway.client.AcquiringBankPaymentRequest;
import com.checkout.payment.gateway.client.AcquiringBankPaymentResponse;
import com.checkout.payment.gateway.client.AcquiringBankingClient;
import com.checkout.payment.gateway.enums.PaymentStatus;
import com.checkout.payment.gateway.exception.EventProcessingException;
import com.checkout.payment.gateway.model.PaymentRequestDTO;
import com.checkout.payment.gateway.model.PostPaymentRequest;
import com.checkout.payment.gateway.model.PostPaymentResponse;
import com.checkout.payment.gateway.repository.PaymentsRepository;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class PaymentGatewayService {

  private static final Logger LOG = LoggerFactory.getLogger(PaymentGatewayService.class);

  private final PaymentsRepository paymentsRepository;
  private final AcquiringBankingClient acquiringBankingClient;

  public PaymentGatewayService(final PaymentsRepository paymentsRepository,
      final AcquiringBankingClient acquiringBankingClient) {

    this.paymentsRepository = paymentsRepository;
    this.acquiringBankingClient = acquiringBankingClient;

  }

  public PostPaymentResponse getPaymentById(UUID id) {
    LOG.debug("Requesting access to to payment with ID {}", id);
    return paymentsRepository.get(id).orElseThrow(() -> new EventProcessingException("Invalid ID"));
  }

  // Input class is not valid cause it accepts only the 4 last digits of the card number:
  public UUID processPayment(PostPaymentRequest paymentRequest) {
    return UUID.randomUUID();
  }

  public PostPaymentResponse processPayment(final PaymentRequestDTO paymentRequestDTO) {

    // Convert PaymentRequestDTO to AcquiringBankPaymentRequest:
    AcquiringBankPaymentRequest acquiringRequest = AcquiringBankPaymentRequest.builder()
        .cardNumber(paymentRequestDTO.cardNumber())
        .expiryDate(paymentRequestDTO.expiryMonth() + "/" + paymentRequestDTO.expiryYear())
        .currency(
            paymentRequestDTO.currency() != null ? paymentRequestDTO.currency().getCurrencyCode()
                : null)
        .amount(paymentRequestDTO.amount() != null ? paymentRequestDTO.amount().intValue() : 0)
        .cvv(paymentRequestDTO.cvv())
        .build();

    // Call acquiring bank client
    AcquiringBankPaymentResponse acquiringBankPaymentResponse = acquiringBankingClient.processPayment(
        acquiringRequest);

    // Save transaction to the repository
    PostPaymentResponse postPaymentResponse = new PostPaymentResponse();

    postPaymentResponse.setStatus(
        acquiringBankPaymentResponse.isAuthorized() ? PaymentStatus.AUTHORIZED
            : PaymentStatus.DECLINED);
    postPaymentResponse.setId(UUID.fromString(acquiringBankPaymentResponse.getAuthorizationCode()));

    String cardNumber = paymentRequestDTO.cardNumber();

    // Keep the last for digits of the card number:
    postPaymentResponse.setCardNumberLastFour(
        Integer.parseInt(cardNumber.substring(cardNumber.length() - 4)));

    postPaymentResponse.setExpiryMonth(paymentRequestDTO.expiryMonth());
    postPaymentResponse.setExpiryYear(paymentRequestDTO.expiryYear());
    postPaymentResponse.setCurrency(paymentRequestDTO.currency().getCurrencyCode());
    postPaymentResponse.setAmount(paymentRequestDTO.amount().intValue());

    paymentsRepository.add(postPaymentResponse);

    return postPaymentResponse;

  }
}
