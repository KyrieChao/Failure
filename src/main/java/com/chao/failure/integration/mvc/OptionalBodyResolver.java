package com.chao.failure.integration.mvc;

import com.chao.failure.annotation.FailFastBody;
import lombok.NonNull;
import org.springframework.core.MethodParameter;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.servlet.mvc.method.annotation.RequestResponseBodyMethodProcessor;

import java.lang.annotation.Annotation;
import java.util.Map;

/**
 * FailFast body argument resolver - handles @FailFastBody annotation.
 *
 * @author Kyrie Chao
 * @version 1.3.1
 */
public class OptionalBodyResolver implements HandlerMethodArgumentResolver {

    private final RequestResponseBodyMethodProcessor delegate;

    /**
     * Constructor.
     *
     * @param delegate RequestResponseBodyMethodProcessor delegate
     */
    public OptionalBodyResolver(RequestResponseBodyMethodProcessor delegate) {
        this.delegate = delegate;
    }

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(FailFastBody.class);
    }

    @Override
    public Object resolveArgument(@NonNull MethodParameter parameter, ModelAndViewContainer mavContainer,
                                  @NonNull NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {

        FailFastBody ann = parameter.getParameterAnnotation(FailFastBody.class);
        boolean required = ann == null || ann.required();

        // Create MethodParameter wrapper
        MethodParameter wrappedParameter = new FailFastBodyMethodParameter(parameter, ann);

        try {
            return delegate.resolveArgument(wrappedParameter, mavContainer, webRequest, binderFactory);
        } catch (HttpMessageNotReadableException e) {
            if (!required && isMissingBody(e)) {
                return null;
            }
            throw e;
        }
    }

    private boolean isMissingBody(HttpMessageNotReadableException e) {
        String msg = e.getMessage();
        if (msg == null) return false;
        return msg.contains("Required request body is missing");
    }

    /**
     * MethodParameter wrapper that overrides RequestBody annotation behavior.
     */
    private static class FailFastBodyMethodParameter extends MethodParameter {
        private final FailFastBody failFastBody;

        public FailFastBodyMethodParameter(MethodParameter original, FailFastBody failFastBody) {
            super(original);
            this.failFastBody = failFastBody;
        }

        @Override
        public <A extends Annotation> boolean hasParameterAnnotation(@NonNull Class<A> annotationType) {
            if (annotationType == RequestBody.class) {
                return true;
            }
            return super.hasParameterAnnotation(annotationType);
        }

        @Override
        public <A extends Annotation> A getParameterAnnotation(@NonNull Class<A> annotationType) {
            if (annotationType == RequestBody.class) {
                boolean required = failFastBody == null || failFastBody.required();
                return annotationType.cast(AnnotationUtils.synthesizeAnnotation(
                        Map.of("required", required),
                        RequestBody.class,
                        null
                ));
            }
            return super.getParameterAnnotation(annotationType);
        }
    }
}

