package queryx;

import static io.agroal.api.configuration.supplier.AgroalPropertiesReader.*;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.agroal.api.AgroalDataSource;
import io.agroal.api.configuration.supplier.AgroalPropertiesReader;
import io.quarkus.logging.Log;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;

import javax.sql.DataSource;
import java.util.Map;

public class DataSourceProducer {
    @Inject
    QueryConfig cfg;

    @Inject
    ObjectMapper mapper;


    @Produces
    public DataSource newDataSource() {
        Log.info("Creating datasource");
        try {
            var credentials = getCredentials();
            var username = credentials.get("username");
            var password = credentials.get("password");
            var props = (Map<String,String>) Map.of(
                    PROVIDER_CLASS_NAME, cfg.providerClassName(),
                    JDBC_URL, cfg.jdbcURL(),
                    PRINCIPAL, username,
                    CREDENTIAL, password,
                    MIN_SIZE, "1",
                    MAX_SIZE, "5",
                    INITIAL_SIZE, "1"
            );
            var datasource = AgroalDataSource.from(new AgroalPropertiesReader()
                    .readProperties(props)
                    .get());
            return datasource;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Map<String, String> getCredentials() {
        var secretId = cfg.secretId().orElseThrow(() -> new RuntimeException("Secret ID not found"));

        try (var client = SecretsManagerClient.builder()
                .build()) {
            var valueRequest = GetSecretValueRequest.builder()
                    .secretId(secretId)
                    .build();
            var valueResponse = client.getSecretValue(valueRequest);
            var secret = valueResponse.secretString();
            Map<String, String> credentials = mapper.readValue(secret, new TypeReference<Map<String, String>>() {});
            return credentials;

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

