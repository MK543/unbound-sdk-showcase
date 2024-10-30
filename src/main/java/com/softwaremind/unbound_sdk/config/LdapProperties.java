package com.softwaremind.unbound_sdk.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "sm.ldap")
@Getter
@Setter
public class LdapProperties {

    private String urls;

    private String base;

    private String username;

    private String password;
}
