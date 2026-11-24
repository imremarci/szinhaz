package com.example.szinhaz;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.boot.SpringApplication;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mockStatic;

class SzinhazApplicationTest {

    @Test
    void constructorShouldCreateInstance() {
        SzinhazApplication application = new SzinhazApplication();

        assertThat(application).isNotNull();
    }

    @Test
    void mainShouldDelegateToSpringApplicationRun() {
        String[] args = {"--spring.main.web-application-type=none"};

        try (MockedStatic<SpringApplication> springApplication = mockStatic(SpringApplication.class)) {
            SzinhazApplication.main(args);

            springApplication.verify(() -> SpringApplication.run(SzinhazApplication.class, args));
        }
    }
}
