package com.checkout.payment.gateway.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.checkout.payment.gateway.enums.PaymentStatus;
import com.checkout.payment.gateway.model.PostPaymentResponse;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class PaymentsRepositoryTest {

  private PaymentsRepository repository;

  @BeforeEach
  void setUp() {
    repository = new PaymentsRepository();
  }

  @Test
  void addAndGet_shouldReturnSameObject_whenPaymentExists() {
    PostPaymentResponse payment = new PostPaymentResponse();
    UUID id = UUID.randomUUID();
    payment.setId(id);
    payment.setAmount(100);
    payment.setCurrency("USD");
    payment.setStatus(PaymentStatus.AUTHORIZED);
    payment.setExpiryMonth(12);
    payment.setExpiryYear(2030);
    payment.setCardNumberLastFour(1234);

    repository.add(payment);

    Optional<PostPaymentResponse> found = repository.get(id);
    assertThat(found).isPresent();
    assertThat(found.get()).isSameAs(payment);
  }

  @Test
  void get_shouldReturnEmpty_whenPaymentDoesNotExist() {
    Optional<PostPaymentResponse> found = repository.get(UUID.randomUUID());
    assertThat(found).isEmpty();
  }

  @Test
  void add_shouldOverwriteExisting_whenSameIdAddedAgain() {
    UUID id = UUID.randomUUID();
    PostPaymentResponse first = new PostPaymentResponse();
    first.setId(id);
    first.setAmount(1);

    PostPaymentResponse second = new PostPaymentResponse();
    second.setId(id);
    second.setAmount(2);

    repository.add(first);
    repository.add(second);

    Optional<PostPaymentResponse> found = repository.get(id);
    assertThat(found).isPresent();
    assertThat(found.get()).isSameAs(second);
    assertThat(found.get().getAmount()).isEqualTo(2);
  }
}
