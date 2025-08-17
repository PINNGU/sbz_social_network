package myapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "myapp") // Explicitly scan the myapp package
public class SocialNetwork {
    public static void main(String[] args) {
        SpringApplication.run(SocialNetwork.class, args);
    }
}
