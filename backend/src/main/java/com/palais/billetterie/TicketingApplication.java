package com.palais.billetterie;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class TicketingApplication {
    private static final Logger log = LoggerFactory.getLogger(TicketingApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(TicketingApplication.class, args);
    }

    @Bean
    ApplicationRunner logDatasource(DataSourceProperties props) {
        return args -> {
            String pwdMasked = props.getPassword() == null ? "<null>" : "***";
            log.info("Effective datasource: url='{}', username='{}', password='{}'",
                    props.getUrl(), props.getUsername(), pwdMasked);
        };
    }
}

