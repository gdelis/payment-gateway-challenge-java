package com.checkout.payment.gateway.model;

import java.math.BigInteger;
import java.util.Currency;
import lombok.Builder;

@Builder
public record PaymentRequestDTO(String cardNumber,
                                int expiryMonth,
                                int expiryYear,
                                Currency currency,
                                // Amounts should be treated as BigInteger; not as an int
                                BigInteger amount,
                                String cvv) {

}
