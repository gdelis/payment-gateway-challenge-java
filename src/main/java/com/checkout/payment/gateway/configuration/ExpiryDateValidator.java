package com.checkout.payment.gateway.configuration;

import com.checkout.payment.gateway.model.CardExpirationDate;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.time.YearMonth;

public class ExpiryDateValidator implements
    ConstraintValidator<ValidExpiryDate, CardExpirationDate> {

  @Override
  public boolean isValid(CardExpirationDate value, ConstraintValidatorContext context) {
    // Let @NotNull on the field handle nulls; validator itself should be null-safe
    if (value == null) {
      return true;
    }

    YearMonth now = YearMonth.now();
    YearMonth expiry = YearMonth.of(value.getExpiryYear(), value.getExpiryMonth());

    return expiry.isAfter(now);
  }

}
