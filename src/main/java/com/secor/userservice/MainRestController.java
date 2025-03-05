package com.secor.userservice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Random;

@RestController
@RequestMapping("api/v1")
public class MainRestController {

    private static final Logger log = LoggerFactory.getLogger(MainRestController.class);

    @Autowired
    AuthService authService;

    @Autowired
    UserRepository userRepository;

    @PostMapping("/update/user/details") // will eventually move to user service
    public ResponseEntity<?> updateUserDetails(@RequestBody UserView userView,
                                               @RequestHeader("Authorization") String token)
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





}
