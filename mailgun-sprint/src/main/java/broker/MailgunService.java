package broker;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.UUID;

@Slf4j
@Service
public class MailgunService {

    private final String domain;
    private final String rootUri = "https://api.mailgun.net/v3";
    private final String mailgunApiKey;
    private final RestTemplate restTemplate;

    public MailgunService(@Value("${MAILGUN_API_KEY}") String apiKey,
                          @Value("${MAILGUN_DOMAIN}") String mailgunDomain) {

        this.mailgunApiKey = apiKey;
        Assert.notNull(this.mailgunApiKey, "you must specify an API key");
        this.domain = mailgunDomain;
        Assert.notNull(this.domain, "you must specify a Mailgun domain");

        this.restTemplate = new RestTemplateBuilder()
                .rootUri(this.rootUri)
                .basicAuthorization("api", this.mailgunApiKey) // the user is always 'api'
                .build();
    }

    @Data
    @AllArgsConstructor
    public static class SmtpCredentials {
        private int smtpPort = 25;
        private int sslPort = 587;
        private final URI mailgunSmtpServer = URI.create("smtp.mailgun.org");
        private int tlsPort = 465;

        public SmtpCredentials(String login, String password, String domain) {
            this.login = login;
            this.password = password;
            this.domain = domain;
        }

        private final String login, password, domain;
    }

    public SmtpCredentials createSmtpExchange(String login, String pw) {
        Assert.isTrue(pw.length() < 32, "password must be less than 32 characters!");
        Assert.isTrue(pw.length() > 5, "password must be greater than 05 characters!");
        MultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
        map.add("name", this.domain);
        map.add("login", login);
        map.add("password", pw);
        String uri = "/domains/" + this.domain + "/credentials";
        log.info("uri: " + uri);
        this.executeApiCall(HttpMethod.POST, uri, map, null,
                String.class);
        return new SmtpCredentials(login + "@" + this.domain, pw, this.domain);
    }

    public void sendEmail(String from, String[] to, String subject, String html) {
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
}
