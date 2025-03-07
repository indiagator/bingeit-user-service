package com.secor.userservice;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class AppConfig
{


    @Bean(name = "auth-service-validate")
    public WebClient webClientAuthService(WebClient.Builder webClientBuilder)
    {
        return webClientBuilder
                .baseUrl("http://localhost:8093/api/v1/validate") // hardcoding hostname and portnumber
                .filter(new LoggingWebClientFilter())
                .build();
    }

    @Bean(name = "plain-old-web-client")
    public WebClient webClientSubService(WebClient.Builder webClientBuilder)
    {
        return webClientBuilder
                .filter(new LoggingWebClientFilter())
                .build();
    }




}
