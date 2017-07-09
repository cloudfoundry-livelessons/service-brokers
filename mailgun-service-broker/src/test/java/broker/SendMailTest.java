package broker;


import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.BDDAssertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.UUID;

@Slf4j
@SpringBootTest
@RunWith(SpringRunner.class)
public class SendMailTest {

    @Configuration
    public static class MailgunServiceConfiguration {

        @Bean
        MailgunService service(@Value("${MAILGUN_API_KEY}") String apiKey,
                               @Value("${MAILGUN_DOMAIN}") String mailgunDomain) {
            return new MailgunService(apiKey, mailgunDomain);
        }
    }

    @Autowired
    private MailgunService service;

    @Test
    public void should_create_route() throws Throwable {
        this.service.createRoute(0, "a route", "match_recipient('bob@applicationconcierge.com')",
                "forward(\"starbuxman@gmail.com\")"); //store(notify="http://cfll-mailgun-inbound.cfapps.io/inbound") <- action
    }

    @Test
    public void should_send_email() throws Throwable {
        this.service.sendEmail("bob@hi.com",
                new String[]{"starbuxman@gmail.com"},
                "Re: your face", "<b> hello world</b>");
    }

    @Test
    public void should_create_smtp_credentials() throws Throwable {
        String login = UUID.randomUUID().toString();
        String pw = UUID.randomUUID().toString()
                .substring(0, 30);
        MailgunService.SmtpCredentials smtpExchange = this.service.createSmtpExchange(
                login, pw);
        BDDAssertions.then(smtpExchange.getPassword()).isEqualTo(pw);
        BDDAssertions.then(smtpExchange.getLogin()).contains(login);
        BDDAssertions.then(smtpExchange.getDomain()).contains("applicationconcierge.com");
    }
}
