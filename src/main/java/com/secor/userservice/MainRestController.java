package com.secor.userservice;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;

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
                                               HttpServletRequest request,
                                               HttpServletResponse response)
        {

           log.info("Received request to subscribe to plan: {}", multiUserView);
            if(authService.validateToken(token))
            {

                // Find out Relationships of these users with other Users wrt to the content that they are watching

                // update the user details with a subscription plan

                // forward the request to the sub service to activate the plan for all the associated users
                // this step can take some time to get executed

                log.info("Received request to subscribe to plan: {}", planid);
                String responseKey = subService.createSub(multiUserView, planid, token);

                // inform the front end that the request has been accepted and is being processed

                //exit

                log.info("Setting up the Cookie for the Front-end");
                Cookie cookieStage1 = new Cookie("user-service-sub-stage-1", responseKey);
                cookieStage1.setMaxAge(300);
                log.info("Cookie set up successfully");

                response.addCookie(cookieStage1);

                return ResponseEntity.ok("Subscription request accepted and is being processed");

            }
            else
            {
                log.info("Token is invalid: {}", token);
                return ResponseEntity.status(401).build();
            }



        }





}
