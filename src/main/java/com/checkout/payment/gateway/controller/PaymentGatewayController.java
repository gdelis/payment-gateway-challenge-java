package com.checkout.payment.gateway.controller;

import com.checkout.payment.gateway.model.PaymentRequest;
import com.checkout.payment.gateway.model.PaymentRequestDTO;
import com.checkout.payment.gateway.model.PostPaymentResponse;
import com.checkout.payment.gateway.service.PaymentGatewayService;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController("api")
public class PaymentGatewayController {

  private final PaymentGatewayService paymentGatewayService;

  public PaymentGatewayController(PaymentGatewayService paymentGatewayService) {
    this.paymentGatewayService = paymentGatewayService;
  }

  @GetMapping("/payment/{id}")
  public ResponseEntity<PostPaymentResponse> getPostPaymentEventById(@PathVariable UUID id) {
    return new ResponseEntity<>(paymentGatewayService.getPaymentById(id), HttpStatus.OK);
  }

  @PostMapping("/payment")
  public ResponseEntity<PostPaymentResponse> processPayment(
      @Valid @RequestBody final PaymentRequest paymentRequest) {

    // Convert PaymentRequest to PaymentRequestDTO (internal DTO object):
    PaymentRequestDTO paymentRequestDTO = PaymentRequestDTO.builder()
        .cardNumber(paymentRequest.getCardNumber())
        .expiryMonth(paymentRequest.getExpiryDate().getExpiryMonth())
        .expiryYear(paymentRequest.getExpiryDate().getExpiryYear())
        .currency(paymentRequest.getCurrency())
        .amount(paymentRequest.getAmount())
        .cvv(paymentRequest.getCvv())
        .build();

    PostPaymentResponse response = paymentGatewayService.processPayment(paymentRequestDTO);

    return new ResponseEntity<>(response, HttpStatus.CREATED);
  }
}
