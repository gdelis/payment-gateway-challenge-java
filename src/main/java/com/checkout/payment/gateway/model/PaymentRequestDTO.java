package com.checkout.payment.gateway.model;

import java.math.BigInteger;
import java.util.Currency;

public class PaymentRequest {

  private String cardNumber;
  private String expiryMonth;
  private String expiryYear;
  private Currency currency;
  private BigInteger amount;
  private String cvv;
}
