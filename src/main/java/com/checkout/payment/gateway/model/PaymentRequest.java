package com.checkout.payment.gateway.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.math.BigInteger;
import java.util.Currency;
import lombok.Getter;

@Getter
public class PaymentRequest {

  @JsonProperty("card_number")
  @NotNull(message = "Card number must be provided")
  @Pattern(regexp = "\\d{14,19}", message = "Card number must contain only numeric characters and be 14-19 digits")
  private String cardNumber;

  @Valid
  @NotNull(message = "Expiry date must be provided")
  private CardExpirationDate expiryDate;

  @NotNull(message = "Currency must be provided")
  private Currency currency;

  @NotNull(message = "Amount must be provided")
  private BigInteger amount;

  @NotNull(message = "CVV number must be provided")
  @Pattern(regexp = "\\d{3,4}", message = "CVV number must contain only numeric characters and has length of 3 or 4 digits")
  private String cvv;

}
