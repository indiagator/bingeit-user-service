package com.secor.userservice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.netflix.eureka.EurekaDiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@Configuration
public class AppConfig
{

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);


    @Autowired
    EurekaDiscoveryClient discoveryClient;

    public ServiceInstance getServiceInstance(String serviceName)
    {
        List<ServiceInstance> instances = discoveryClient.getInstances(serviceName);

        if (instances.isEmpty())
        {
            throw new RuntimeException("No instances found for "+serviceName);
        }

        return instances.get(0); // LOAD BALANCING ALGORITHM WILL GO HERE
    }

    @Bean(name = "auth-service-validate")
    @Scope("prototype")
    public WebClient webClientAuthService(WebClient.Builder webClientBuilder)
    {

        ServiceInstance instance = getServiceInstance("bingeit-auth-service");
        String hostname = instance.getHost();
        log.info("Hostname for Auth-Service: "+hostname);
        int port = instance.getPort();
        log.info("Port for Auth-Service: "+port);

        return webClientBuilder
                .baseUrl("http://"+hostname+":"+port+"/api/v1/validate")
                .filter(new LoggingWebClientFilter())
                .build();
    }

    @Bean(name = "sub-service-create-sub")
    public WebClient webClientSubService(WebClient.Builder webClientBuilder)
    {
        return webClientBuilder
                .baseUrl("http://localhost:8102/api/v1/create/subs/1") // hardcoding hostname and portnumber
                .filter(new LoggingWebClientFilter())
                .build();
    }




}
