package com.example.mailgunsprint;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
public class MailgunService {

    private final String domain = "applicationconcierge.com";
    private final String rootUri = "https://api.mailgun.net/v3";

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

    private <T> ResponseEntity<T> executeApiCall(HttpMethod method, String uri, MultiValueMap<String, Object> map, HttpHeaders headers, Class<T> returnValueClazz) {
        if (null == headers) {
            headers = new HttpHeaders();
        }
        if (headers.getContentType() == null) {
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        }
        HttpMethod resolvedMethod = null == method ? HttpMethod.POST : method;
        return restTemplate.exchange(this.rootUri + uri,
                resolvedMethod, new HttpEntity<>(map, headers), returnValueClazz);
    }

    public void send(String from, String[] to, String subject, String html) {
        MultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
        map.add("subject", subject);
        map.add("html", html);
        map.add("from", from);
        for (String t : to) {
            map.add("to", t);
        }
        this.executeApiCall(null, "/" + this.domain + "/messages", map, null, String.class);
    }

    public void createRoute(int priority, String description, String filterExpression, String action) {
        LinkedMultiValueMap<String, Object> m = new LinkedMultiValueMap<>();
        m.add("priority", priority < 0 ? 0 : priority);
        m.add("description", description);
        m.add("action", action);
        m.add("expression", filterExpression);
        this.executeApiCall(null, "/routes", m, null, null);
    }
}
