package com.checkout.payment.gateway.client;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Builder
public class AcquiringBankPaymentResponse implements Serializable {

  private Boolean authorized;

  @JsonProperty("authorization_code")
  private String authorizationCode;

}
