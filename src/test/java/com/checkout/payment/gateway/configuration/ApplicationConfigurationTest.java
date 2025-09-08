package com.checkout.payment.gateway.configuration;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.client.RestTemplate;

@SpringBootTest
public class ApplicationConfigurationTest {

  @Autowired
  private RestTemplate restTemplate;

  @Test
  void contextLoads_andRestTemplateBeanExists() {
    assertThat(restTemplate).isNotNull();
  }
}
