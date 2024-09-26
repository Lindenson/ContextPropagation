package com.wolper.context;

import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class MyBusinessService {

    public static final String MY_BUSINESS_SERVICE = "com.wolper.context.MyBusinessService";

    @InjectContext("myContext")
    public void processStep3() {
        System.out.println("processStep3");
        int x = 4 + 4;
        Map<Object, Object> myContext = ContextService.access();
        myContext.put("Context of processStep3", x);
        System.out.println(myContext);
    }

    @InjectContext("myContext")
    public void processStep2(int y, String message) {
        System.out.println("processStep2");
        int x = 2 + y;
        Map<Object, Object> myContext = ContextService.access();
        myContext.put("Context of processStep2_int", x);
        myContext.put("Context of processStep2_message", message);
        System.out.println(myContext);
        processStep3();
    }

    @InjectContext("myContext")
    public void processStep1() {
        System.out.println("processStep1");
        Map<Object, Object> myContext = ContextService.create();
        myContext.put("Context of processStep1", 2);
        System.out.println(myContext);
        processStep2(20, "done");
    }
}
