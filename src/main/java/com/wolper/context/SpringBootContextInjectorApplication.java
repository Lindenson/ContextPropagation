package com.wolper.context;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SpringBootContextInjectorApplication implements CommandLineRunner {


    @Autowired
    MyBusinessService myBusinessService;


    public static void main(String[] args) {
        try {
            ContextInjector.injectContext(MyBusinessService.MY_BUSINESS_SERVICE, false);
        } catch (Exception e) {
            e.printStackTrace();
        }

        SpringApplication.run(SpringBootContextInjectorApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println();
        System.out.println("TEST RUN");
        myBusinessService.processStep1();
    }
}
