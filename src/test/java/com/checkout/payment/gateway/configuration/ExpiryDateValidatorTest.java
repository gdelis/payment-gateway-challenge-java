package com.checkout.payment.gateway.configuration;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.checkout.payment.gateway.model.CardExpirationDate;
import java.time.YearMonth;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class ExpiryDateValidatorTest {

  private ExpiryDateValidator validator;

  @BeforeEach
  void setUp() {
    validator = new ExpiryDateValidator();
  }

  @Test
  void returnsTrueForNullValue() {
    assertTrue(validator.isValid(null, null));
  }

  @Test
  void returnsFalseWhenDateIsCurrentNotInTheFuture() {
    YearMonth now = YearMonth.now();

    CardExpirationDate ced = new CardExpirationDate(now.getMonthValue(), now.getYear());

    assertFalse(validator.isValid(ced, null));
  }

  @Test
  void returnsFalseWhenDateIsOneMonthInThePast() {
    YearMonth now = YearMonth.now();
    YearMonth past = now.minusMonths(1);

    CardExpirationDate ced = new CardExpirationDate(past.getMonthValue(), past.getYear());

    assertFalse(validator.isValid(ced, null));
  }

  @Test
  void returnsFalseForPastYear() {
    YearMonth now = YearMonth.now();
    YearMonth past = now.minusYears(1);

    CardExpirationDate ced = new CardExpirationDate(past.getMonthValue(), past.getYear());

    assertFalse(validator.isValid(ced, null));
  }

  @Test
  void returnsTrueForNextMonth() {
    YearMonth next = YearMonth.now().plusMonths(1);

    CardExpirationDate ced = new CardExpirationDate(next.getMonthValue(), next.getYear());

    assertTrue(validator.isValid(ced, null));
  }

  @Test
  void returnsTrueForNextYearSameMonth() {
    YearMonth now = YearMonth.now();
    YearMonth future = now.plusYears(1);

    CardExpirationDate ced = new CardExpirationDate(future.getMonthValue(), future.getYear());

    assertTrue(validator.isValid(ced, null));
  }

  @Test
  void handlesYearBoundaryDecToJan() {
    // If now is December, next month is January of next year; this should be valid
    YearMonth next = YearMonth.now().plusMonths(1);

    CardExpirationDate ced = new CardExpirationDate(next.getMonthValue(), next.getYear());

    assertTrue(validator.isValid(ced, null));
  }
}
