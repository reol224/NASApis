package com.jul.NASapis.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Configuration
@ConfigurationProperties(prefix = "nasa.api")
@Getter
@Setter
@Component
public class ApplicationPropertiesConfig {
    private String key;
}
