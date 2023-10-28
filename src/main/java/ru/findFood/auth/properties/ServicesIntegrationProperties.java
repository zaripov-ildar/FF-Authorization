package ru.findFood.auth.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;


@ConfigurationProperties(prefix = "integrations")
@Data
public class ServicesIntegrationProperties {

        private Integer connectTimeout;
        private Integer readTimeout;
        private Integer writeTimeout;
}
