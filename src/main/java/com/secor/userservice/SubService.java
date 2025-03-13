package com.secor.userservice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
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

            Mono<ClientResponse> subServiceResponse = webClient.post()
                    .header("Authorization", token)
                    .body(BodyInserters.fromValue(multiUserView)).
                    exchangeToMono(clientResponse1 -> Mono.just(clientResponse1));


            log.info("Sub Service Response: "+subServiceResponse);
            String responseKey = String.valueOf(new Random().nextInt(1000)); // this is the key that we will return from this method

            log.info("Response Key: "+responseKey);
            redisTemplate.opsForValue().set(responseKey,"stage1 complete");

            /// SETUP A HANDLER FOR THE EVENTUAL RESPONSE
            subServiceResponse.subscribe(
                    clientResponse ->
                    {
                        clientResponse.bodyToMono(String.class).subscribe(responseBody -> {
                            log.info("Response from the sub service: {}", responseBody);
                            // Extract cookies from the response
                            clientResponse.cookies().entrySet().stream()
                                    .filter(stringListEntry -> stringListEntry.getKey().equals("sub-service-stage-2"))
                                    .findFirst()
                                    .ifPresent(cookieEntry -> {
                                        // Menu creation logic to be implemented here
                                        // And put the response in Redis
                                        redisTemplate.opsForValue().set(responseKey, "subresponse " + responseBody + " " + cookieEntry.getValue().stream().findFirst());
                                    });
                        },
                                error ->
                                {
                                    log.info("error processing the response "+error.getMessage());
                                    redisTemplate.opsForValue().set(responseKey,"error "+error.getMessage());
                                });
                    });
            /// END OF HANDLER

            log.info("Returning Response Key: "+responseKey);
            return responseKey; // Interim Response
        }

}
