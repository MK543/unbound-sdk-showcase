package com.softwaremind.unbound_sdk.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.unboundid.ldap.sdk.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
@RequiredArgsConstructor
public class LdapConfig {

    private final LdapProperties ldapProperties;

    @Bean
    public ObjectMapper objectMapper() {
        log.info(ldapProperties.getUrls());
        return new ObjectMapper();
    }

    @Bean
    public LDAPConnectionPool ldapConnectionPool() throws LDAPException {
        String[] urlStrings = ldapProperties.getUrls().split(",");
        String[] addresses = new String[urlStrings.length];
        int[] ports = new int[urlStrings.length];

        for (int i = 0; i < urlStrings.length; i++) {
            String urlString = urlStrings[i].trim();
            LDAPURL ldapUrl = new LDAPURL(urlString);
            addresses[i] = ldapUrl.getHost();
            ports[i] = ldapUrl.getPort();
        }

        LDAPConnectionOptions connectionOptions = new LDAPConnectionOptions();
        connectionOptions.setConnectTimeoutMillis(1000);

        BindRequest bindRequest = new SimpleBindRequest(ldapProperties.getUsername(), ldapProperties.getPassword());

        ServerSet roundRobinServerSet = new RoundRobinServerSet(
                addresses,
                ports,
                null,
                connectionOptions,
                null,
                null,
                500
        );

        ServerSet failoverServerSet = new FailoverServerSet(roundRobinServerSet);


        LDAPConnectionPoolHealthCheck healthCheck = new LDAPConnectionPoolHealthCheck();
        LDAPConnectionPool ldapConnectionPool = new LDAPConnectionPool(
                failoverServerSet,
                bindRequest,
                4,
                8,
                4,
                null,
                false,
                healthCheck
        );
        ldapConnectionPool.setMaxWaitTimeMillis(1000);
        ldapConnectionPool.setHealthCheckIntervalMillis(30000);
        ldapConnectionPool.setMaxConnectionAgeMillis(30000);

        return ldapConnectionPool;
    }
}
