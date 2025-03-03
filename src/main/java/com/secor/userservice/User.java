package com.secor.userservice;


import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "users")
@Getter @Setter
public class User {

    @Id
    private String username;
    private String password;

    private String fullname;
    private String email;
    private String phone;
    private String region;

}
