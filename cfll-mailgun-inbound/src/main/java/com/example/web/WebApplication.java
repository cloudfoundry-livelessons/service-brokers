package com.example.web;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpHeaders;
import org.springframework.http.RequestEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@SpringBootApplication
public class WebApplication {

    @GetMapping("/hi")
    String hi (){
        return "Hello";
    }

    @RequestMapping("/inbound")
    public void inbound(RequestEntity<String> requestEntity) {
        HttpHeaders httpHeaders = requestEntity.getHeaders();
        log.info("incoming request: " + requestEntity.getBody());
        httpHeaders.forEach((k, v) -> log.info(k + '=' + v));
    }

    public static void main(String[] args) {
        SpringApplication.run(WebApplication.class, args);
    }
}
