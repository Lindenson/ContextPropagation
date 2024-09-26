package com.wolper.context;


import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class ApplicationTest {

    @Autowired
    MyBusinessService myBusinessService;

    @Test
    public void contextLoads() {
        myBusinessService.processStep1();
    }
}

