package com.wolper.context;

import java.util.HashMap;
import java.util.Map;


public class ContextService {
    private Map<Object, Object> contextData;

    public Map<Object, Object> getContext() {
        System.out.println("Вызван GetContext");
        if (contextData == null) contextData = create();
        return contextData;
    }

    public static Map<Object, Object> access() {
        return null;
    }

    public static Map<Object, Object> create() {
        return new HashMap<Object, Object>();
    }
}
