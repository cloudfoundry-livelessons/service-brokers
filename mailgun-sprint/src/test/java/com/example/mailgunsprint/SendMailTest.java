package com.example.mailgunsprint;

import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.BDDAssertions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@SpringBootTest
@RunWith(SpringRunner.class)
public class SendMailTest {

    @Autowired
    private MailgunService service;

    @Test
    public void should_create_route() throws Throwable {
        this.service.createRoute(0, "a route", "match_recipient('bob@applicationconcierge.com')",
                "forward(\"starbuxman@gmail.com\")"); //store(notify="http://cfll-mailgun-inbound.cfapps.io/inbound") <- action
    }

    @Test
    public void should_send_email() throws Throwable {
        this.service.send("bob@hi.com",
                new String[]{"starbuxman@gmail.com"},
                "Re: your face", "<b> hello world</b>");
    }
}

