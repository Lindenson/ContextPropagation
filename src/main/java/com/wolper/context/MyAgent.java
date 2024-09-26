package com.wolper.context;


import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;

import static com.wolper.context.MyBusinessService.MY_BUSINESS_SERVICE;


public class MyAgent {

    public static boolean done = false;

    public static void premain(String agentArgs, Instrumentation inst) {
        System.out.println("Java Agent Started");

        inst.addTransformer(new ClassFileTransformer() {
            @Override
            public byte[] transform(ClassLoader loader, String className,
                                    Class<?> classBeingRedefined, ProtectionDomain protectionDomain,
                                    byte[] classfileBuffer) {

                if (done) return classfileBuffer;

                if (classBeingRedefined != null) {
                    System.out.println("Class " + className + " is already defined, skipping...");
                    return classfileBuffer;
                }

                try {
                    if (className.contains("MyBusinessService")) {
                        System.out.println("Start modifying!");
                        byte[] bytes = ContextInjector.injectContext(MY_BUSINESS_SERVICE, true);
                        System.out.println("Class modified!");
                        done = true;
                        return bytes;
                    }
                } catch (Exception e) {
                    e.printStackTrace();

                }
                return classfileBuffer;
            }
        });
    }
}
