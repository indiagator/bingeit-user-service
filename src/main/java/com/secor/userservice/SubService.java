package com.secor.userservice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
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
            log.info("Creating subscription for plan: {}", planid);

            Mono<String> subServiceResponse = webClient.post()
                    .header("Authorization", token)
                    .body(BodyInserters.fromValue(multiUserView))
                    .retrieve()
                    .bodyToMono(String.class); // This is an Async Request

            log.info("Sub Service Response: "+subServiceResponse);
            String responseKey = String.valueOf(new Random().nextInt(1000)); // this is the key that we will return from this method

            log.info("Response Key: "+responseKey);
            redisTemplate.opsForValue().set(responseKey,"stage1 complete");

            /// SETUP A HANDLER FOR THE EVENTUAL RESPONSE
            subServiceResponse.subscribe(
                    (response) ->
                    {
                        log.info(response+" from the sub service");
                        // MENU CREATION LOGIC TO BE IMPLEMENTED HERE
                        // AND PUT THE RESPONSE IN REDIS
                        redisTemplate.opsForValue().set(responseKey,"subresponse "+response);
                    },
                    error ->
                    {
                        log.info("error processing the response "+error.getMessage());
                        redisTemplate.opsForValue().set(responseKey,"error "+error.getMessage());
                    });
            /// END OF HANDLER

            log.info("Returning Response Key: "+responseKey);
            return responseKey; // Interim Response
        }

}
