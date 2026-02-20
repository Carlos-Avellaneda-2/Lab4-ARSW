package edu.eci.arsw.blueprints;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories(basePackages = "edu.eci.arsw.blueprints.persistence")
public class BlueprintsApplication {
    public static void main(String[] args) {
        SpringApplication.run(BlueprintsApplication.class, args);
    }
}
