package io.github.sipratama.penatika;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration;

@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)
public class PenatikaApplication {

    public static void main(String[] args) {
        SpringApplication.run(PenatikaApplication.class, args);
    }
}
