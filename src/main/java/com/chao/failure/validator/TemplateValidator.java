package com.chao.failure.validator;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.HashMap;
import java.util.Map;

/**
 * Template validator abstract class, implements FastValidator interface, provides common validation framework
 *
 * @param <T> Target type to validate
 * @author Kyrie Chao
 * @version 1.3.1
 */
public abstract class TemplateValidator<T> implements FastValidator<T> {

    /**
     * Core validation method, executes validation steps in specific order
     * @param target Target object to validate
     * @param context Validation context containing validation state information
     */
    @Override
    public final void validate(T target, ValidationContext context) {
        // First execute common validation
        validateCommon(target, context);
        // Check if validation is interrupted
        if (context != null && context.isStopped()) {
            return;
        }
        // Then execute type-specific validation
        validateSpecific(target, context);
    }

    /**
     * Empty implementation for common validation logic, subclasses can override this method
     * @param target Target object to validate
     * @param context Validation context
     */
    protected void validateCommon(T target, ValidationContext context) {
    }

    /**
     * Abstract method, implemented by subclasses for type-specific validation logic
     * @param target Target object to validate
     * @param context Validation context
     */
    protected abstract void validateSpecific(T target, ValidationContext context);

    /**
     * Get the type supported by this validator
     * @return Supported type Class object
     */
    @Override
    public Class<?> getSupportedType() {
        return resolveTypeArgument(getClass());
    }

    /**
     * Resolve generic type argument to determine actual supported type
     * @param leafClass Class to resolve
     * @return Resolved type Class object
     */
    private static Class<?> resolveTypeArgument(Class<?> leafClass) {
        // Map to store type variables to actual types
        Map<TypeVariable<?>, Type> typeVarMap = new HashMap<>();
        Class<?> cur = leafClass;
        // Traverse up the class inheritance chain
        while (cur != null && cur != Object.class) {
            // Get generic superclass of current class
            Type generic = cur.getGenericSuperclass();
            if (generic instanceof ParameterizedType pt) {
                Type raw = pt.getRawType();
                if (raw instanceof Class<?> rawClass) {
                    // Process type parameters
                    TypeVariable<?>[] vars = rawClass.getTypeParameters();
                    Type[] args = pt.getActualTypeArguments();
                    for (int i = 0; i < vars.length && i < args.length; i++) {
                        typeVarMap.put(vars[i], resolveType(args[i], typeVarMap));
                    }
                    // If TemplateValidator class is found, return its type parameter
                    if (rawClass == TemplateValidator.class) {
                        Type t = args.length > 0 ? resolveType(args[0], typeVarMap) : null;
                        return toClass(t);
                    }
                    cur = rawClass;
                    continue;
                }
            }
            if (generic instanceof Class<?> superClass) {
                cur = superClass;
                continue;
            }
            break;
        }
        return Object.class;
    }

    /**
     * Resolve type, handle type variable mapping
     * @param t Type to resolve
     * @param typeVarMap Type variable mapping table
     * @return Resolved type
     */
    private static Type resolveType(Type t, Map<TypeVariable<?>, Type> typeVarMap) {
        if (t instanceof TypeVariable<?> tv) {
            Type mapped = typeVarMap.get(tv);
            return mapped != null ? mapped : tv;
        }
        return t;
    }

    /**
     * Convert type to Class object
     * @param t Type to convert
     * @return Corresponding Class object
     */
    private static Class<?> toClass(Type t) {
        if (t instanceof Class<?> c) return c;
        if (t instanceof ParameterizedType pt && pt.getRawType() instanceof Class<?> c) return c;
        return Object.class;
    }
}
