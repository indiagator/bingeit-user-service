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

    @PostMapping("/update/user/details") // will eventually move to user service
    public ResponseEntity<?> updateUserDetails(@RequestBody UserView userView,
                                               @RequestHeader("Authorization") String token)
    {
        if(authService.validateToken(token))
        {
            if(userRepository.existsById(userView.getUsername()))
            {
                User user = userRepository.findById(userView.getUsername()).get();

                user.setFullname(userView.getFullname());
                user.setEmail(userView.getEmail());
                user.setPhone(userView.getPhone());
                user.setRegion(userView.getRegion());
                userRepository.save(user);
                return ResponseEntity.ok(user);
            }
            else {
                return ResponseEntity.notFound().build();
            }
        }
        else
        {
            return ResponseEntity.status(401).build();
        }



    }





}
