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

@Slf4j
@Service
public class MailgunService {

    private final String domain;
    private final String mailgunApiKey;
    private final RestTemplate restTemplate;
    private final String rootUri = "https://api.mailgun.net/v3";

    public MailgunService(@Value("${MAILGUN_API_KEY}") String apiKey,
                          @Value("${MAILGUN_DOMAIN}") String mailgunDomain) {
        this.mailgunApiKey = apiKey;
        this.domain = mailgunDomain;
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
        private int tlsPort = 465;
        private final URI smtpServer = URI.create("smtp.mailgun.org");
        private final String login, password, domain;

        public SmtpCredentials(String login, String password, String domain) {
            this.login = login;
            this.password = password;
            this.domain = domain;
        }
    }

    public SmtpCredentials createSmtpExchange(String login, String pw) {

        Assert.isTrue(pw.length() < 32, "password must be less than 32 characters!");
        Assert.isTrue(pw.length() > 5, "password must be greater than 05 characters!");

        MultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
        map.add("name", this.domain);
        map.add("login", login);
        map.add("password", pw);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        restTemplate.exchange(this.rootUri + "/domains/" + this.domain + "/credentials",
                HttpMethod.POST, new HttpEntity<>(map, headers), String.class);

        return new SmtpCredentials(login + "@" + this.domain, pw, this.domain);
    }

}
