package com.checkout.payment.gateway.model;

import com.checkout.payment.gateway.configuration.ValidExpiryDate;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Builder
@ValidExpiryDate
public class CardExpirationDate {

  @Min(value = 1, message = "Expiry month must be greater than or equal to 1")
  @Max(value = 12, message = "Expiry month must be less than or equal to 12")
  private int expiryMonth;

  @JsonProperty("expiry_year")
  private int expiryYear;

  // Constructor for Jackson to map flat JSON
  @JsonCreator
  public CardExpirationDate(
      @JsonProperty("expiry_month") int expiryMonth,
      @JsonProperty("expiry_year") int expiryYear) {
    this.expiryMonth = expiryMonth;
    this.expiryYear = expiryYear;
  }

}
