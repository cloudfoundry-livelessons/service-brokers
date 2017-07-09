package broker;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.servicebroker.exception.ServiceInstanceBindingExistsException;
import org.springframework.cloud.servicebroker.model.*;
import org.springframework.cloud.servicebroker.service.ServiceInstanceBindingService;
import org.springframework.cloud.servicebroker.service.ServiceInstanceService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.*;

@Slf4j
@SpringBootApplication
public class MailgunBrokerApplication {

    public static void main(String[] args) {
        SpringApplication.run(MailgunBrokerApplication.class, args);
    }
}

@Configuration
class CatalogConfiguration {

    private final static String ID = "9876543210";
    private final String spacePrefix;

    CatalogConfiguration(@Value("${vcap.application.space_id:${USER:}}") String spaceId) {
        this.spacePrefix = spaceId + '-';
    }

    @Bean
    Catalog catalog(@Value("${spring.application.name}") String appName) {
        Map<String, Object> metadata = Collections.singletonMap("costs",
                Collections.singletonMap("free", true));
        Plan basic = new Plan(this.spacePrefix + ID + "-plan", "basic",
                "Mailgun route config service", metadata, true);
        ServiceDefinition definition = new ServiceDefinition(
                this.spacePrefix + ID + "-service-definition",
                appName, "Provides Mailgun access", true, Collections.singletonList(basic));
        return new Catalog(Collections.singletonList(definition));
    }
}


// service instance bindings
interface ServiceInstanceBindingRepository extends JpaRepository<ServiceInstanceBinding, String> {
}


@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
class ServiceInstanceBinding {

    @Id
    private String id;

    private String serviceInstanceId, syslogDrainUrl, appGuid;
    private int smtpPort, sslPort, tlsPort;
    private String login, password, smtpServer;

}

@Service
@Slf4j
class DefaultServiceInstanceBindingService implements ServiceInstanceBindingService {

    private final MailgunService mailgunService;
    private final ServiceInstanceBindingRepository repository;

    DefaultServiceInstanceBindingService(MailgunService mailgunService, ServiceInstanceBindingRepository repository) {
        this.mailgunService = mailgunService;
        this.repository = repository;
    }

    @Override
    public CreateServiceInstanceBindingResponse createServiceInstanceBinding(
            CreateServiceInstanceBindingRequest create) {

        if (repository.findOne(create.getBindingId()) != null) {
            throw new ServiceInstanceBindingExistsException(
                create.getServiceInstanceId(), create.getBindingId());
        }

        String login = UUID.randomUUID().toString();
        String pw = UUID.randomUUID().toString().substring(0, 30);

        MailgunService.SmtpCredentials smtpExchange = this.mailgunService
                .createSmtpExchange(login, pw);

        Map<String, Object> credentials = new HashMap<>();
        credentials.put("smtpPort", smtpExchange.getSmtpPort());
        credentials.put("sslPort", smtpExchange.getSslPort());
        credentials.put("tlsPort", smtpExchange.getTlsPort());
        credentials.put("smtpServer", smtpExchange.getSmtpServer());
        credentials.put("login", smtpExchange.getLogin());
        credentials.put("password", smtpExchange.getPassword());

        this.repository.save(
                new ServiceInstanceBinding(UUID.randomUUID().toString(),
                        create.getServiceInstanceId(),
                        create.getBoundAppGuid(),
                        null,
                        smtpExchange.getSmtpPort(),
                        smtpExchange.getSslPort(),
                        smtpExchange.getTlsPort(),
                        smtpExchange.getLogin(),
                        smtpExchange.getPassword(),
                        smtpExchange.getSmtpServer().toString()));
        return new CreateServiceInstanceAppBindingResponse()
                .withCredentials(credentials);
    }

    @Override
    public void deleteServiceInstanceBinding(
            DeleteServiceInstanceBindingRequest delete) {

        Optional.ofNullable(delete.getBindingId())
                .ifPresent(sid ->
                        Optional.ofNullable(repository.findOne(sid))
                                .ifPresent(repository::delete));
    }
}


/**
 * This is a noop because we don't need to provision the service.
 * It's already provisioned. we only act on binding.
 */
@Service
class DefaultServiceInstanceService implements ServiceInstanceService {

    @Override
    public CreateServiceInstanceResponse createServiceInstance(
            CreateServiceInstanceRequest create) {
        return new CreateServiceInstanceResponse();
    }

    @Override
    public GetLastServiceOperationResponse getLastOperation(
            GetLastServiceOperationRequest status) {
        return new GetLastServiceOperationResponse();
    }

    @Override
    public DeleteServiceInstanceResponse deleteServiceInstance(
            DeleteServiceInstanceRequest delete) {
        return new DeleteServiceInstanceResponse();
    }

    @Override
    public UpdateServiceInstanceResponse updateServiceInstance(
            UpdateServiceInstanceRequest update) {
        return new UpdateServiceInstanceResponse();
    }
}