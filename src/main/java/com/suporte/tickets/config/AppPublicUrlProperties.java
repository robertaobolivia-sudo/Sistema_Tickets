package com.suporte.tickets.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "app")
public class AppPublicUrlProperties {

    /**
     * Base pública para links (ex.: http://localhost:8080). Sem barra final.
     */
    private String publicBaseUrl = "http://localhost:8080";
}
