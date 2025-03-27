package com.secor.userservice;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@RestController
@RequestMapping("api/v1")
public class MainRestController {

    private static final Logger log = LoggerFactory.getLogger(MainRestController.class);

    @Autowired
    AuthService authService;

    @Autowired
    UserRepository userRepository;

    @Autowired
    SubService subService;

    @Autowired
    RedisTemplate<String, Object> redisTemplate;

    @PostMapping("/update/user/details") // will eventually move to user service
    public ResponseEntity<?> updateUserDetails(@RequestBody UserView userView,
                                               @RequestHeader("Authorization") String token
                                               )
    {
        // STEP1: EXTRACTION OF PAYLOAD FROM INCOMING REQUESTS AND INJECTION OF BEANS IF ANY

        // STEP2A: INTERACTION WITH DB AND THE CACHE AND DATA PROCESSING

        // STEP2B: INTERACTION WITH OTHER SERVICES

        log.info("Received request to update user details: {}", userView);
        if(authService.validateToken(token))
        {
            log.info("Token is valid: {}", token);

                User user = new User();
                user.setUsername(userView.getUsername());
                user.setFullname(userView.getFullname());
                user.setEmail(userView.getEmail());
                user.setPhone(userView.getPhone());
                user.setRegion(userView.getRegion());
                userRepository.save(user);
                log.info("User updated: {}", user);
                return ResponseEntity.ok(user);
        }
        else
        {
            log.info("Token is invalid: {}", token);
            return ResponseEntity.status(401).build();
        }

        // STEP3: RETURN THE RESPONSE FINAL OR INTERIM
    }


        @PostMapping("/subscribe/plan/{planid}")
        public ResponseEntity<?> subscribePlan(@RequestBody MultiUserView multiUserView,
                                               @PathVariable("planid") String planid,
                                               @RequestHeader("Authorization") String token,
                                               @RequestHeader("traceparent") String traceparent,
                                               HttpServletRequest request,
                                               HttpServletResponse response)
        {

            log.info("endpoint : subscribe/plan/{} invoked", planid);

            // COOKIE VALIDATION LOGIC
            List<Cookie> cookieList = null;
            log.info("initiating cookie check");

            //Optional<String> healthStatusCookie = Optional.ofNullable(request.getHeader("health_status_cookie"));
            Cookie[] cookies = request.getCookies();
            if(cookies == null)
            {
                cookieList = new ArrayList<>();
            }
            else
            {
                // REFACTOR TO TAKE NULL VALUES INTO ACCOUNT
                cookieList = List.of(cookies);
            }
            log.info("cookie check complete");

            if( cookieList.stream().findAny().isEmpty()) // COOKIE_CHECK
            {
                log.info("no relevant cookie found.. initiating fresh request logic");
                log.info("Request forwarded to Auth-Service for Token Validation {} ", token);
                if(authService.validateToken(token))
                {

                    // Find out Relationships of these users with other Users wrt to the content that they are watching

                    // update the user details with a subscription plan

                    // forward the request to the sub service to activate the plan for all the associated users
                    // this step can take some time to get executed
                    log.info("Token is valid: {}", token);
                    log.info("Received request to subscribe to plan: {}", planid);
                    String responseKey = subService.createSub(multiUserView, planid, token);
                    log.info("Response Key generated: {}", responseKey);

                    // inform the front end that the request has been accepted and is being processed

                    //exit

                    log.info("Setting up the Cookie for the Front-end with the response key {} as the cookieValue", responseKey);
                    Cookie cookieStage1 = new Cookie("user-service-sub-stage-1", responseKey);
                    cookieStage1.setMaxAge(600);
                    log.info("Cookie set up successfully with name {} and value {}", cookieStage1.getName(), cookieStage1.getValue());

                    response.addCookie(cookieStage1);

                    log.info("Sending back the response to the Front-end with the Cookie as above");

                    return ResponseEntity.ok().header("traceparent", traceparent).body("Subscription request accepted and is being processed");
                }
                else
                {
                    log.info("Token is invalid: {}", token);
                    return ResponseEntity.status(401).build();
                }


            }
            else if( cookieList.stream().filter(cookie -> cookie.getName().equals("sub-service-stage-2")).findAny().isPresent())
            {
                // FOLLOW UP LOGIC
                log.info("found a relevant cookie..{} initiating follow up logic","sub-service-stage-2");
                log.info("initiating follow up logic for the second cookie!");
                Cookie followup_cookie =  cookieList.stream().
                        filter(cookie -> cookie.getName().equals("sub-service-stage-2")).findAny().get();

                String followup_cookie_key = followup_cookie.getValue();
                String cacheResponse = (String)redisTemplate.opsForValue().get(followup_cookie_key);
                log.info("Fetched the response from the cache which is updated by Sub-Service: {}", cacheResponse);

                String[] cacheResponseArray = cacheResponse.split(" ");


                if(cacheResponseArray[0].equals("stage2"))
                {
                    log.info("Payment Creation  still under process...");

                    return ResponseEntity.ok("Payment Creation  still under process...");
                }
                else if(cacheResponseArray[0].equals("payresponse"))
                {
                    log.info("transaction completed successfully with PaymentID: {}", cacheResponseArray[1]);
                    return ResponseEntity.ok("PaymentID: "+cacheResponseArray[1]);
                }
                else
                {
                    return ResponseEntity.ok("Error Processing the Order");
                }
            }// COOKIE_CHECK
            else if( cookieList.stream().filter(cookie -> cookie.getName().equals("user-service-sub-stage-1")).findAny().isPresent()) // COOKIE_CHECK
            {
                // FOLLOW UP LOGIC
                log.info("found a relevant cookie.. {} initiating follow up logic","user-service-sub-stage-1");

                Cookie followup_cookie =  cookieList.stream().
                        filter(cookie -> cookie.getName().equals("user-service-sub-stage-1")).findAny().get();

                String followup_cookie_key = followup_cookie.getValue();
                String cacheResponse = (String)redisTemplate.opsForValue().get(followup_cookie_key);

                String[] cacheResponseArray = cacheResponse.split(" ");

                if(cacheResponseArray[0].equals("stage1"))
                {
                    log.info("Request still under process...");

                    return ResponseEntity.ok("Request still under process...");
                }
                else if(cacheResponseArray[0].equals("subresponse"))
                {
                    log.info("Cache was updated by the ASYNC HANDLER with the response from the Sub-Service");
                    log.info("Setting up the Cookie for the Front-end stage 2");
                    Cookie cookieStage2 = new Cookie(cacheResponseArray[1], cacheResponseArray[2]);
                    cookieStage2.setMaxAge(600);
                    log.info("Cookie set up successfully {} {}", cookieStage2.getName(), cookieStage2.getValue());
                    log.info("FIRST FOLLOW_UP SUCCESS");
                    response.addCookie(cookieStage2);
                    return ResponseEntity.ok("Subscription Created and Payment Creation in Progress");
                }
                else
                {
                    return ResponseEntity.ok("Error Processing the Order");
                }


            }
            else
            {
                return ResponseEntity.ok("Error Processing the Order");
            }


        }

        @GetMapping("/get/user/{username}")
        public ResponseEntity<?> getUser(@PathVariable("username") String username)
        {
            Optional<User> user = userRepository.findById(username);

            if(user.isPresent())
            {
                return ResponseEntity.ok(user);
            }
            else
            {
                return ResponseEntity.status(404).body("User not found");
            }

        }



}
