package com.bandwidth;

import java.util.Collections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
public class Main {

    static Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        String port = System.getenv("LOCAL_PORT");
        if (System.getenv("BW_ACCOUNT_ID") == null || System.getenv("BW_CLIENT_ID") == null
                || System.getenv("BW_CLIENT_SECRET") == null || System.getenv("BW_MESSAGING_APPLICATION_ID") == null
                || System.getenv("BW_NUMBER") == null || port == null || !port.matches("\\d+")) {
            System.err.println("Please set the environmental variables defined in the README");
            System.exit(1);
        }

        SpringApplication application = new SpringApplication(Main.class);
        application.setDefaultProperties(Collections.singletonMap("server.port", port));
        application.run(args);
        logger.info("Server has started...");
    }

}
