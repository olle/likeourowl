package com.studiomediatech.example.ai;

import java.util.Arrays;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class LikeOurOwlApplication {

    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(LikeOurOwlApplication.class);

        // `--cli` is a shorthand for the `cli` profile: an interactive Spring
        // Shell in the terminal instead of the web server (see
        // application-cli.properties). It is consumed here so that Spring Shell
        // does not see it as a command to run.
        if (Arrays.asList(args).contains("--cli")) {
            application.setAdditionalProfiles("cli");
        }

        application.run(Arrays.stream(args).filter(arg -> !arg.equals("--cli")).toArray(String[]::new));
    }

}
