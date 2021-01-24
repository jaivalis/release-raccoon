package com.raccoon.config;

import javax.resource.spi.ConfigProperty;

import io.quarkus.arc.config.ConfigProperties;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@ConfigProperties(prefix = "mail")
public class MailConfig {

    @ConfigProperty
    String username;

    @ConfigProperty
    String password;

}
