package com.example.apitest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.client.RestTemplate;

import com.example.userauth.repository.GoodsRepository;
import com.example.userauth.repository.CategoryRepository;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Goods Management API Test Class
 */
//@SpringBootApplication
@ComponentScan("com.example.userauth")
public class GoodsApiTest {

    public static void main(String[] args) {
        SpringApplication.run(GoodsApiTest.class, args);
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public CommandLineRunner testGoodsApi(@Autowired GoodsRepository goodsRepository, 
                                         @Autowired CategoryRepository categoryRepository) {
        return args -> {
            // Write output to file
            try (PrintWriter writer = new PrintWriter(new FileWriter("goods_api_test_output.txt"))) {
                writer.println("Starting goods API test...");

                try {
                    // Test goods query
                    writer.println("Testing goods query...");
                    long goodsCount = goodsRepository.count();
                    writer.println("Total goods count: " + goodsCount);

                    // Test category query
                    writer.println("Testing category query...");
                    long categoryCount = categoryRepository.count();
                    writer.println("Total category count: " + categoryCount);

                    writer.println("Goods API test completed successfully!");
                    System.out.println("Test completed. Check goods_api_test_output.txt for results.");
                    System.exit(0); // Normal exit
                } catch (Exception e) {
                    writer.println("Goods API test failed: " + e.getMessage());
                    e.printStackTrace(writer);
                    System.err.println("Test failed. Check goods_api_test_output.txt for details.");
                    System.exit(1); // Exception exit
                }
            } catch (IOException e) {
                System.err.println("Failed to create output file: " + e.getMessage());
                System.exit(1);
            }
        };
    }
}