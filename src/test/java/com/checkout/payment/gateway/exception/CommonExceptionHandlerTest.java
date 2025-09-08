package com.checkout.payment.gateway.exception;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.checkout.payment.gateway.controller.PaymentGatewayController;
import com.checkout.payment.gateway.model.ErrorResponse;
import com.checkout.payment.gateway.service.PaymentGatewayService;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

@SpringBootTest
@AutoConfigureMockMvc
public class CommonExceptionHandlerTest {

  @Autowired
  private MockMvc mvc;

  @MockitoBean
  private PaymentGatewayService service;

  @Test
  void handleEventProcessingException_shouldReturnNotFoundWithMessage() throws Exception {
    UUID id = UUID.randomUUID();

    org.mockito.Mockito.when(service.getPaymentById(id))
        .thenThrow(new EventProcessingException("Invalid ID"));

    mvc.perform(MockMvcRequestBuilders.get("/payment/" + id))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.message").value("Page not found"));
  }

  @Test
  void directCall_handleInputValidationException_returnsBadRequest() {
    CommonExceptionHandler handler = new CommonExceptionHandler();

    // Mock exception to avoid complex construction
    org.springframework.web.bind.MethodArgumentNotValidException ex =
        org.mockito.Mockito.mock(org.springframework.web.bind.MethodArgumentNotValidException.class);
    org.mockito.Mockito.when(ex.getMessage()).thenReturn("some validation error");

    ResponseEntity<ErrorResponse> response = handler.handleInputValidationException(ex);

    assertThat(response.getStatusCode().value()).isEqualTo(400);
    assertThat(response.getBody().getMessage()).contains("Invalid input data");
  }
}
