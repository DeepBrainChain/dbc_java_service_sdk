package com.example;

import org.apache.commons.codec.binary.Hex;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;

@SpringBootApplication
@Configuration
public class DemoApplication {

    public static void main(String[] args) {

        SpringApplication.run(DemoApplication.class, args);
    }

    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(byte[].class, String.class, Hex::encodeHexString);
    }
}
