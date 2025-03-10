package com.secor.userservice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class AuthService
{
    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    @Autowired
    private ApplicationContext applicationContext;

    public boolean validateToken(String token)
    {
        WebClient webClient = applicationContext.getBean("auth-service-validate", WebClient.class);
        log.info("webclient bean reaccessed");

        log.info("Validating token within the AuthService: {}", token);
        log.info("Sending request to auth service to validate token: {}", token);

        String response =  webClient.get()
                                    .header("Authorization", token)
                                    .retrieve()
                                    .bodyToMono(String.class).block(); // Current Thread will pause till the final response comes back | SYNC |

        log.info("Response from auth service: {}", response);
        return response.equalsIgnoreCase("valid");
    }
}
