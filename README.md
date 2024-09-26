# Context Propagation Framework

This project introduces a mechanism for implicitly passing a context parameter between method calls across different classes or services. By leveraging a custom @InjectContext annotation, methods can seamlessly manage and transmit a hidden context map without requiring explicit parameter passing. This approach simplifies code, reduces boilerplate, and enhances maintainability by automating context management between method invocations.

## Overview

In a typical application, methods may need to share some context data while performing business logic. Instead of explicitly passing a context object through every method, this framework provides an annotation-driven solution where the context is automatically managed and passed behind the scenes.

This is particularly useful when one method invokes another, and both need access to the same shared context without the need for explicit context-passing in the method signatures.
Key Features

    Custom Annotation @InjectContext: Automatically injects a shared context parameter into annotated methods.
    Context Propagation: When one annotated method calls another, the context is implicitly passed, allowing the callee method to modify or access the shared context.
    Context Management Service: The ContextService handles the creation and access of the context object.

## Example: MyBusinessService

The MyBusinessService class demonstrates how methods can be annotated with @InjectContext to receive a hidden context parameter.

```java
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
```



## How it Works

    processStep1: The method creates a new context using ContextService.create() and stores some data in the context. It then calls processStep2 with parameters.

    processStep2: This method receives the context implicitly, even though it’s not part of its signature. It adds more data to the context, then calls processStep3.

    processStep3: This method also accesses the shared context implicitly and adds more data to it.

At runtime, the ContextService ensures that each annotated method shares the same context during the execution chain, allowing them to read and write from it.

### Output Example

For a method invocation like myBusinessService.processStep1();, the following output could be expected:
```
processStep1
{Context of processStep1=2}
processStep2
{Context of processStep1=2, Context of processStep2_int=22, Context of processStep2_message=done}
processStep3
{Context of processStep1=2, Context of processStep2_int=22, Context of processStep2_message=done, Context of processStep3=8}
```

As shown, each method can seamlessly interact with the shared context without needing to explicitly pass it as an argument.
## How to Use

    Annotate Methods: Apply the @InjectContext annotation to the methods where you want to implicitly manage a context. You must provide a string value (context identifier) to the annotation.
```java
    @InjectContext("myContext")
    public void someMethod() {
        // method logic
    }

```
    Access Context: Use ContextService.access() to access the existing context or ContextService.create() to create a new one at the entry point.

    Modify Context: The context is a simple Map<Object, Object> where key-value pairs can be stored.

## Future Enhancements

    Error Handling: Implement stricter validation to handle cases where the context is not available.
    Thread Safety: Ensure the context is thread-safe for multi-threaded environments.
    Custom Context Types: Allow more flexible context types (not just Map<Object, Object>).

# Compilation

1. First the agent</br>
   ```mvn clean install -P agent```
2. Then app</br>
   ```mvn clean install -P lib```


## Links
[Javassist]https://github.com/jboss-javassist/javassist

