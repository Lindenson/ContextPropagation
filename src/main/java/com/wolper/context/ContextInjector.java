package com.wolper.context;

import javassist.*;
import javassist.expr.ExprEditor;
import javassist.expr.MethodCall;

public class ContextInjector {

    public static final String CONTEXT_SERVICE = "com.wolper.context.ContextService";
    public static final String VARIABLE = "$XXX_ContextInjector"; // Variable for context storage

    public static byte[] injectContext(String className, boolean forAgent) {
        ClassPool classPool = ClassPool.getDefault();

        try {
            // Load the class by its name
            CtClass ctClass = classPool.get(className);
            if (ctClass.isFrozen()) {
                ctClass.defrost(); // If the class is frozen, defrost it
            }

            // First pass: check methods for InjectContext annotation
            for (CtMethod ctMethod : ctClass.getDeclaredMethods()) {
                Object[] annotations = ctMethod.getAvailableAnnotations();
                for (Object annotation : annotations) {
                    if (annotation instanceof InjectContext) {
                        try {
                            updateMethod(classPool, ctClass, ctMethod.getName(), false);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

            // Second pass: find method calls to annotated methods
            for (CtMethod ctMethod : ctClass.getDeclaredMethods()) {
                Object[] annotations = ctMethod.getAvailableAnnotations();
                for (Object annotation : annotations) {
                    if (annotation instanceof InjectContext an) {
                        try {
                            var variable = an.value();
                            ctMethod.instrument(new ExprEditor() {
                                @Override
                                public void edit(MethodCall m) {
                                    String calledMethodName = m.getMethodName();
                                    try {
                                        // Get the method being called
                                        CtMethod calledMethod = ctClass.getDeclaredMethod(calledMethodName);

                                        // Check if the called method has the InjectContext annotation
                                        if (calledMethod.hasAnnotation(InjectContext.class)) {
                                            System.out.println("In annotated method: " + ctMethod.getName() + ctMethod.getSignature());
                                            System.out.println(" Found a call to another annotated method: " + calledMethodName);

                                            replaceMethodCallAddingContext(m, calledMethodName, variable);
                                        }
                                    } catch (NotFoundException e) {
                                        // Handle method not found exception
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            });
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

            // Third pass: update the methods to include the context variable
            for (CtMethod ctMethod : ctClass.getDeclaredMethods()) {
                Object[] annotations = ctMethod.getAvailableAnnotations();
                for (Object annotation : annotations) {
                    if (annotation instanceof InjectContext) {
                        try {
                            // Update method and add a local variable for the context
                            CtMethod newMethod = updateMethod(classPool, ctClass, ctMethod.getName(), true);
                            newMethod.addLocalVariable(VARIABLE, classPool.get("java.util.Map"));
                            CtClass[] paramTypes = ctMethod.getParameterTypes();
                            int length = paramTypes.length + 1;
                            newMethod.insertBefore(VARIABLE + " = $" + length + ";");

                            // Modify method calls to ensure context is handled
                            newMethod.instrument(new ExprEditor() {
                                @Override
                                public void edit(MethodCall m) {
                                    String calledMethodName = m.getMethodName();
                                    try {
                                        if (calledMethodName.equals("access") && m.getClassName().equals(CONTEXT_SERVICE)) {
                                            CtClass contextServiceClass = classPool.get(CONTEXT_SERVICE);
                                            m.replace("{" +
                                                    "$_ = " + VARIABLE + " != null ? " + VARIABLE + " : new " + contextServiceClass.getName() + "().getContext();" +
                                                    "}");
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            });
                            System.out.println("Added context access in method: " + newMethod.getName() + newMethod.getSignature());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

            // Write changes back to the class and load it
            if (!forAgent) {
                ctClass.writeFile(".");
                ctClass.toClass();
                ctClass.detach();
            }
            return ctClass.toBytecode();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // Edit body replacing method call
    private static void replaceMethodCallAddingContext(MethodCall m, String calledMethodName, String variable) throws NotFoundException, CannotCompileException {
        CtClass[] paramTypes = m.getMethod().getParameterTypes();
        int length = paramTypes.length;
        String[] parameters = new String[length];
        String parametersString = "";
        if (length > 0) {
            for (int i = 1; i <= length; i++) {
                parameters[i - 1] = "$" + i;
            }
            parametersString = String.join(", ", parameters) + ", ";
        }
        parametersString += variable;
        m.replace("{ " + calledMethodName + "(" + parametersString + "); }");
        System.out.println("Replaced with a call including the context: " + calledMethodName + "(" + parametersString + ")");
    }

    // Updates the method by creating a new one with an additional parameter (Map)
    public static CtMethod updateMethod(ClassPool pool, CtClass ctClass, String methodName, boolean update) throws Exception {
        CtMethod originalMethod = ctClass.getDeclaredMethod(methodName);
        CtClass[] originalParams = originalMethod.getParameterTypes();

        // Create a new parameter array with an additional Map parameter
        CtClass[] newParams = new CtClass[originalParams.length + 1];
        System.arraycopy(originalParams, 0, newParams, 0, originalParams.length);

        CtClass ctClassMap = pool.getCtClass("java.util.Map");
        newParams[originalParams.length] = ctClassMap;
        ctClassMap.setGenericSignature("Ljava/util/Map<Ljava/lang/Object;Ljava/lang/Object;>;");

        // Create a duplicate method with the new parameter
        CtMethod newMethod = new CtMethod(originalMethod.getReturnType(), methodName, newParams, ctClass);
        newMethod.setBody(originalMethod, null);

        if (update) {
            // Remove the original method and replace it with the new one
            CtMethod declaredMethod = ctClass.getDeclaredMethod(methodName, newParams);
            ctClass.removeMethod(declaredMethod);
            System.out.println("Created new method: " + newMethod.getName() + newMethod.getSignature());
        }

        ctClass.addMethod(newMethod);
        return newMethod;
    }
}
