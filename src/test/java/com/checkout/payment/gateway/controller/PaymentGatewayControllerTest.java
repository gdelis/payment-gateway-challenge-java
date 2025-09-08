package com.checkout.payment.gateway.controller;


import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.checkout.payment.gateway.enums.PaymentStatus;
import com.checkout.payment.gateway.model.PostPaymentResponse;
import com.checkout.payment.gateway.service.PaymentGatewayService;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

@SpringBootTest
@AutoConfigureMockMvc
public class PaymentGatewayControllerTest {

  @Autowired
  private MockMvc mvc;
  @MockitoBean
  private PaymentGatewayService paymentGatewayService;

  @Test
  void whenPaymentWithIdExistThenCorrectPaymentIsReturned() throws Exception {
    PostPaymentResponse payment = new PostPaymentResponse();
    payment.setId(UUID.randomUUID());
    payment.setAmount(10);
    payment.setCurrency("USD");
    payment.setStatus(PaymentStatus.AUTHORIZED);
    payment.setExpiryMonth(12);
    payment.setExpiryYear(2024);
    payment.setCardNumberLastFour(4321);

    org.mockito.Mockito.when(paymentGatewayService.getPaymentById(payment.getId()))
        .thenReturn(payment);

    mvc.perform(MockMvcRequestBuilders.get("/payment/" + payment.getId()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value(payment.getStatus().getName()))
        .andExpect(jsonPath("$.cardNumberLastFour").value(payment.getCardNumberLastFour()))
        .andExpect(jsonPath("$.expiryMonth").value(payment.getExpiryMonth()))
        .andExpect(jsonPath("$.expiryYear").value(payment.getExpiryYear()))
        .andExpect(jsonPath("$.currency").value(payment.getCurrency()))
        .andExpect(jsonPath("$.amount").value(payment.getAmount()));
  }

  @Test
  void whenPaymentWithIdDoesNotExistThen404IsReturned() throws Exception {
    UUID id = UUID.randomUUID();
    org.mockito.Mockito.when(paymentGatewayService.getPaymentById(id))
        .thenThrow(
            new com.checkout.payment.gateway.exception.EventProcessingException("Invalid ID"));

    mvc.perform(MockMvcRequestBuilders.get("/payment/" + id))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.message").value("Page not found"));
  }

  @Test
  void whenValidPostPayment_thenCreatedAndBodyReturned() throws Exception {
    String body = """
        {
          "card_number": "4111111111111234",
          "expiry_month": 7,
          "expiry_year": 2030,
          "currency": "USD",
          "amount": 1599,
          "cvv": "123"
        }
        """;

    UUID authCode = UUID.randomUUID();
    PostPaymentResponse response = new PostPaymentResponse();
    response.setId(authCode);
    response.setStatus(PaymentStatus.AUTHORIZED);
    response.setCardNumberLastFour(1234);
    response.setExpiryMonth(7);
    response.setExpiryYear(2030);
    response.setCurrency("USD");
    response.setAmount(1599);

    when(paymentGatewayService.processPayment(any(
        com.checkout.payment.gateway.model.PaymentRequestDTO.class)))
        .thenReturn(response);

    mvc.perform(MockMvcRequestBuilders.post("/payment")
            .contentType(MediaType.APPLICATION_JSON)
            .content(body))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").value(authCode.toString()))
        .andExpect(jsonPath("$.status").value("Authorized"))
        .andExpect(jsonPath("$.cardNumberLastFour").value(1234))
        .andExpect(jsonPath("$.expiryMonth").value(7))
        .andExpect(jsonPath("$.expiryYear").value(2030))
        .andExpect(jsonPath("$.currency").value("USD"))
        .andExpect(jsonPath("$.amount").value(1599));
  }

  @Test
  void whenInvalidPostPayment_CVVInvalid_thenBadRequestWithMessage() throws Exception {

    String body = """
        {
          "card_number": "4111111111111234",
          "expiry_month": 7,
          "expiry_year": 2030,
          "currency": "USD",
          "amount": 1599,
          "cvv": "12a"
        }
        """;

    mvc.perform(MockMvcRequestBuilders.post("/payment")
            .contentType(MediaType.APPLICATION_JSON)
            .content(body))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message", containsString("Invalid input data")));
  }

  @Test
  void whenInvalidPostPayment_expirationDateInThePast_thenBadRequestWithMessage() throws Exception {

    String body = """
        {
          "card_number": "4111111111111234",
          "expiry_month": 7,
          "expiry_year": 2000,
          "currency": "USD",
          "amount": 1599,
          "cvv": "123"
        }
        """;

    mvc.perform(MockMvcRequestBuilders.post("/payment")
            .contentType(MediaType.APPLICATION_JSON)
            .content(body))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message", containsString("Invalid input data")));
  }


  @Test
  void whenGetWithInvalidUUID_thenBadRequest() throws Exception {
    mvc.perform(MockMvcRequestBuilders.get("/payment/not-a-uuid"))
        .andExpect(status().isBadRequest());
  }
}
