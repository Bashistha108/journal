package com.journal;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseAutoConfiguration;

// Database connection and Liquibase enabled for Phase 2
@SpringBootApplication
public class TradingJournalApplication {

    public static void main(String[] args) {
        SpringApplication.run(TradingJournalApplication.class, args);
    }
}
