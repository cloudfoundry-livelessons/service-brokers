package com.example.mailgunsprint;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.net.URI;

@Slf4j
@Service
public class MailgunService {

    private final String rootUri = "https://api.mailgun.net/v3/applicationconcierge.com";

    private final String mailgunApiKey;

    private final RestTemplate restTemplate;

    public MailgunService(@Value("${MAILGUN_API_KEY}") String apiKey) {
        this.mailgunApiKey = apiKey;
        Assert.notNull(this.mailgunApiKey, "you must specify an API key");
        this.restTemplate = new RestTemplateBuilder()
                .rootUri(this.rootUri)
                .basicAuthorization("api", this.mailgunApiKey) // the user is always 'api'
                .build();
    }

    public void send(String[] to, String subject, String html) {
        MultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
        map.add("subject", "subject");
        map.add("html", "<b>hello</b>");
        map.add("from", "josh@joshlong.com");
        map.add("to", "starbuxman@gmail.com");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        restTemplate.exchange(this.rootUri + "/messages", HttpMethod.POST,
                new HttpEntity<>(map, headers), String.class);
    }
}
