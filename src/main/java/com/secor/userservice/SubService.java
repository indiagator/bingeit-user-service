package com.secor.userservice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import java.util.Random;

@Service
public class SubService
{
    private static final Logger log = LoggerFactory.getLogger(SubService.class);

    @Autowired
    RedisTemplate<String, Object> redisTemplate;

    @Autowired
    @Qualifier("sub-service-create-sub")
    WebClient webClient;

    public String createSub(MultiUserView multiUserView,
                            String planid,
                            String token)
        {
            log.info("SubService inside User-Service invoked | Creating subscription for plan: {}", planid);
            log.info("Request forwarded to the SubService with Payload:  "+multiUserView);

            Mono<String> subServiceResponse = webClient.post()
                    .header("Authorization", token)
                    .body(BodyInserters.fromValue(multiUserView)).
                   retrieve().bodyToMono(String.class);

            String responseKey = String.valueOf(new Random().nextInt(1000)); // this is the key that we will return from this method

            log.info("Response Key generated within the SubService of User-Service: "+responseKey);
            redisTemplate.opsForValue().set(responseKey,"stage1 complete");
            log.info("Response Key {} saved in Redis with the value stage1 complete",responseKey);

           subServiceResponse.subscribe(response ->
                   {
                        log.info("ASYNC HANDLER in User-Service INVOKED with response {} from Sub-Service",response);
                        redisTemplate.opsForValue().set(responseKey, response);
                        log.info("Response Key {} UPDATE IN REDIS with the final response {}", responseKey,response);
                    },
                   error ->{
                    log.info("Error from the SubService: "+error.getMessage());
                });

            log.info("Returning Response Key from the SubService within User-Service after setting up the AYNC HANDLER: "+responseKey);
            return responseKey; // Interim Response
        }

}
