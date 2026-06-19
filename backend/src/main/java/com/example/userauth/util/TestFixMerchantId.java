package com.example.userauth.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class TestFixMerchantId implements CommandLineRunner {

    @Autowired
    private FixMerchantId fixMerchantId;

    @Override
    public void run(String... args) throws Exception {
        // 运行检查
        fixMerchantId.run();
    }
}