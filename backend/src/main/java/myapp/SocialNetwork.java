package myapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@ComponentScan(basePackages = "myapp") // Explicitly scan the myapp package
@EnableScheduling // Omogući scheduled metode
public class SocialNetwork {
    public static void main(String[] args) {
        SpringApplication.run(SocialNetwork.class, args);
    }
}
