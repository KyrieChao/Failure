package com.chao.failure.config;

import com.chao.failure.annotation.FailFastBody;
import com.chao.failure.integration.mvc.OptionalBodyResolver;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.core.MethodParameter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.servlet.mvc.method.annotation.RequestResponseBodyMethodProcessor;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OptionalBodyResolverTest {

    @Test
    void testSupportsParameterWithFailFastBodyAnnotation() {
        MethodParameter parameter = Mockito.mock(MethodParameter.class);
        FailFastBody annotation = Mockito.mock(FailFastBody.class);
        Mockito.when(parameter.hasParameterAnnotation(FailFastBody.class)).thenReturn(true);

        RequestResponseBodyMethodProcessor delegate = Mockito.mock(RequestResponseBodyMethodProcessor.class);
        OptionalBodyResolver resolver = new OptionalBodyResolver(delegate);

        assertTrue(resolver.supportsParameter(parameter));
    }

    @Test
    void testSupportsParameterWithoutFailFastBodyAnnotation() {
        MethodParameter parameter = Mockito.mock(MethodParameter.class);
        Mockito.when(parameter.hasParameterAnnotation(FailFastBody.class)).thenReturn(false);

        RequestResponseBodyMethodProcessor delegate = Mockito.mock(RequestResponseBodyMethodProcessor.class);
        OptionalBodyResolver resolver = new OptionalBodyResolver(delegate);

        assertFalse(resolver.supportsParameter(parameter));
    }

    @Test
    void testResolveArgumentWithValidBody() throws Exception {
        MethodParameter parameter = Mockito.mock(MethodParameter.class);
        FailFastBody annotation = Mockito.mock(FailFastBody.class);
        Mockito.when(parameter.getParameterAnnotation(FailFastBody.class)).thenReturn(annotation);
        Mockito.when(annotation.required()).thenReturn(true);

        ModelAndViewContainer mavContainer = Mockito.mock(ModelAndViewContainer.class);
        NativeWebRequest webRequest = Mockito.mock(NativeWebRequest.class);
        WebDataBinderFactory binderFactory = Mockito.mock(WebDataBinderFactory.class);

        RequestResponseBodyMethodProcessor delegate = Mockito.mock(RequestResponseBodyMethodProcessor.class);
        Object expectedResult = new Object();
        Mockito.when(delegate.resolveArgument(any(), any(), any(), any())).thenReturn(expectedResult);

        OptionalBodyResolver resolver = new OptionalBodyResolver(delegate);
        Object result = resolver.resolveArgument(parameter, mavContainer, webRequest, binderFactory);

        assertEquals(expectedResult, result);
    }

    @Test
    void testResolveArgumentWithMissingBodyAndRequiredFalse() throws Exception {
        MethodParameter parameter = Mockito.mock(MethodParameter.class);
        FailFastBody annotation = Mockito.mock(FailFastBody.class);
        Mockito.when(parameter.getParameterAnnotation(FailFastBody.class)).thenReturn(annotation);
        Mockito.when(annotation.required()).thenReturn(false);

        ModelAndViewContainer mavContainer = Mockito.mock(ModelAndViewContainer.class);
        NativeWebRequest webRequest = Mockito.mock(NativeWebRequest.class);
        WebDataBinderFactory binderFactory = Mockito.mock(WebDataBinderFactory.class);

        RequestResponseBodyMethodProcessor delegate = Mockito.mock(RequestResponseBodyMethodProcessor.class);
        Mockito.when(delegate.resolveArgument(any(), any(), any(), any()))
                .thenThrow(new HttpMessageNotReadableException("Required request body is missing"));

        OptionalBodyResolver resolver = new OptionalBodyResolver(delegate);
        Object result = resolver.resolveArgument(parameter, mavContainer, webRequest, binderFactory);

        assertNull(result);
    }

    @Test
    void testResolveArgumentWithMissingBodyAndRequiredTrue() throws Exception {
        MethodParameter parameter = Mockito.mock(MethodParameter.class);
        FailFastBody annotation = Mockito.mock(FailFastBody.class);
        Mockito.when(parameter.getParameterAnnotation(FailFastBody.class)).thenReturn(annotation);
        Mockito.when(annotation.required()).thenReturn(true);

        ModelAndViewContainer mavContainer = Mockito.mock(ModelAndViewContainer.class);
        NativeWebRequest webRequest = Mockito.mock(NativeWebRequest.class);
        WebDataBinderFactory binderFactory = Mockito.mock(WebDataBinderFactory.class);

        RequestResponseBodyMethodProcessor delegate = Mockito.mock(RequestResponseBodyMethodProcessor.class);
        HttpMessageNotReadableException exception = new HttpMessageNotReadableException("Required request body is missing");
        Mockito.when(delegate.resolveArgument(any(), any(), any(), any())).thenThrow(exception);

        OptionalBodyResolver resolver = new OptionalBodyResolver(delegate);
        assertThrows(HttpMessageNotReadableException.class, () -> {
            resolver.resolveArgument(parameter, mavContainer, webRequest, binderFactory);
        });
    }

    @Test
    void testResolveArgumentWithOtherException() throws Exception {
        MethodParameter parameter = Mockito.mock(MethodParameter.class);
        FailFastBody annotation = Mockito.mock(FailFastBody.class);
        Mockito.when(parameter.getParameterAnnotation(FailFastBody.class)).thenReturn(annotation);
        Mockito.when(annotation.required()).thenReturn(false);

        ModelAndViewContainer mavContainer = Mockito.mock(ModelAndViewContainer.class);
        NativeWebRequest webRequest = Mockito.mock(NativeWebRequest.class);
        WebDataBinderFactory binderFactory = Mockito.mock(WebDataBinderFactory.class);

        RequestResponseBodyMethodProcessor delegate = Mockito.mock(RequestResponseBodyMethodProcessor.class);
        HttpMessageNotReadableException exception = new HttpMessageNotReadableException("Other error");
        Mockito.when(delegate.resolveArgument(any(), any(), any(), any())).thenThrow(exception);

        OptionalBodyResolver resolver = new OptionalBodyResolver(delegate);
        assertThrows(HttpMessageNotReadableException.class, () -> {
            resolver.resolveArgument(parameter, mavContainer, webRequest, binderFactory);
        });
    }

    @Test
    void testIsMissingBodyWithMissingBodyMessage() {
        HttpMessageNotReadableException exception = new HttpMessageNotReadableException("Required request body is missing");
        RequestResponseBodyMethodProcessor delegate = Mockito.mock(RequestResponseBodyMethodProcessor.class);
        OptionalBodyResolver resolver = new OptionalBodyResolver(delegate);

        // 通过反射测试isMissingBody方法
        try {
            var method = OptionalBodyResolver.class.getDeclaredMethod("isMissingBody", HttpMessageNotReadableException.class);
            method.setAccessible(true);
            boolean result = (boolean) method.invoke(resolver, exception);
            assertTrue(result);
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    void testIsMissingBodyWithOtherMessage() {
        HttpMessageNotReadableException exception = new HttpMessageNotReadableException("Other error");
        RequestResponseBodyMethodProcessor delegate = Mockito.mock(RequestResponseBodyMethodProcessor.class);
        OptionalBodyResolver resolver = new OptionalBodyResolver(delegate);

        // 通过反射测试isMissingBody方法
        try {
            var method = OptionalBodyResolver.class.getDeclaredMethod("isMissingBody", HttpMessageNotReadableException.class);
            method.setAccessible(true);
            boolean result = (boolean) method.invoke(resolver, exception);
            assertFalse(result);
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    void testIsMissingBodyWithNullMessage() {
        HttpMessageNotReadableException exception = new HttpMessageNotReadableException((String) null);
        RequestResponseBodyMethodProcessor delegate = Mockito.mock(RequestResponseBodyMethodProcessor.class);
        OptionalBodyResolver resolver = new OptionalBodyResolver(delegate);

        // 通过反射测试isMissingBody方法
        try {
            var method = OptionalBodyResolver.class.getDeclaredMethod("isMissingBody", HttpMessageNotReadableException.class);
            method.setAccessible(true);
            boolean result = (boolean) method.invoke(resolver, exception);
            assertFalse(result);
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    void testResolveArgumentWithNullAnnotation() throws Exception {
        MethodParameter parameter = Mockito.mock(MethodParameter.class);
        Mockito.when(parameter.getParameterAnnotation(FailFastBody.class)).thenReturn(null);

        ModelAndViewContainer mavContainer = Mockito.mock(ModelAndViewContainer.class);
        NativeWebRequest webRequest = Mockito.mock(NativeWebRequest.class);
        WebDataBinderFactory binderFactory = Mockito.mock(WebDataBinderFactory.class);

        RequestResponseBodyMethodProcessor delegate = Mockito.mock(RequestResponseBodyMethodProcessor.class);
        Object expectedResult = new Object();
        Mockito.when(delegate.resolveArgument(any(), any(), any(), any())).thenReturn(expectedResult);

        OptionalBodyResolver resolver = new OptionalBodyResolver(delegate);
        Object result = resolver.resolveArgument(parameter, mavContainer, webRequest, binderFactory);

        assertEquals(expectedResult, result);
    }

    @Test
    void testResolveArgumentWithFailFastBodyRequiredFalse() throws Exception {
        MethodParameter parameter = Mockito.mock(MethodParameter.class);
        FailFastBody annotation = Mockito.mock(FailFastBody.class);
        Mockito.when(parameter.getParameterAnnotation(FailFastBody.class)).thenReturn(annotation);
        Mockito.when(annotation.required()).thenReturn(false);

        ModelAndViewContainer mavContainer = Mockito.mock(ModelAndViewContainer.class);
        NativeWebRequest webRequest = Mockito.mock(NativeWebRequest.class);
        WebDataBinderFactory binderFactory = Mockito.mock(WebDataBinderFactory.class);

        RequestResponseBodyMethodProcessor delegate = Mockito.mock(RequestResponseBodyMethodProcessor.class);
        Object expectedResult = new Object();
        Mockito.when(delegate.resolveArgument(any(), any(), any(), any())).thenReturn(expectedResult);

        OptionalBodyResolver resolver = new OptionalBodyResolver(delegate);
        Object result = resolver.resolveArgument(parameter, mavContainer, webRequest, binderFactory);

        assertEquals(expectedResult, result);
    }

    @Test
    void testResolveArgumentWithNullParameterThrows() {
        RequestResponseBodyMethodProcessor delegate = Mockito.mock(RequestResponseBodyMethodProcessor.class);
        OptionalBodyResolver resolver = new OptionalBodyResolver(delegate);
        assertThrows(NullPointerException.class, () -> resolver.resolveArgument(
                null,
                Mockito.mock(ModelAndViewContainer.class),
                Mockito.mock(NativeWebRequest.class),
                Mockito.mock(WebDataBinderFactory.class)
        ));
    }

    @Test
    void testResolveArgumentWithNullWebRequestThrows() {
        MethodParameter parameter = Mockito.mock(MethodParameter.class);
        RequestResponseBodyMethodProcessor delegate = Mockito.mock(RequestResponseBodyMethodProcessor.class);
        OptionalBodyResolver resolver = new OptionalBodyResolver(delegate);
        assertThrows(NullPointerException.class, () -> resolver.resolveArgument(
                parameter,
                Mockito.mock(ModelAndViewContainer.class),
                null,
                Mockito.mock(WebDataBinderFactory.class)
        ));
    }

    @Test
    void testFailFastBodyMethodParameterOverridesRequestBodyAnnotation() throws Exception {
        class ControllerLike {
            @SuppressWarnings("unused")
            public void create(@FailFastBody(required = false) String body) {
            }
        }

        var method = ControllerLike.class.getDeclaredMethod("create", String.class);
        MethodParameter original = new MethodParameter(method, 0);
        FailFastBody ann = method.getParameters()[0].getAnnotation(FailFastBody.class);

        Class<?> wrapperType = Class.forName("com.chao.failure.integration.mvc.OptionalBodyResolver$FailFastBodyMethodParameter");
        var ctor = wrapperType.getDeclaredConstructor(MethodParameter.class, FailFastBody.class);
        ctor.setAccessible(true);
        Object wrapped = ctor.newInstance(original, ann);

        var hasAnn = wrapperType.getDeclaredMethod("hasParameterAnnotation", Class.class);
        hasAnn.setAccessible(true);
        assertTrue((boolean) hasAnn.invoke(wrapped, RequestBody.class));
        assertTrue((boolean) hasAnn.invoke(wrapped, FailFastBody.class));

        var getAnn = wrapperType.getDeclaredMethod("getParameterAnnotation", Class.class);
        getAnn.setAccessible(true);

        RequestBody requestBody = (RequestBody) getAnn.invoke(wrapped, RequestBody.class);
        assertNotNull(requestBody);
        assertFalse(requestBody.required());

        FailFastBody failFastBody = (FailFastBody) getAnn.invoke(wrapped, FailFastBody.class);
        assertNotNull(failFastBody);
    }

    @Test
    void testFailFastBodyMethodParameterWithNullFailFastBodyUsesRequiredTrue() throws Exception {
        class ControllerLike {
            @SuppressWarnings("unused")
            public void create(String body) {
            }
        }

        var method = ControllerLike.class.getDeclaredMethod("create", String.class);
        MethodParameter original = new MethodParameter(method, 0);

        Class<?> wrapperType = Class.forName("com.chao.failure.integration.mvc.OptionalBodyResolver$FailFastBodyMethodParameter");
        var ctor = wrapperType.getDeclaredConstructor(MethodParameter.class, FailFastBody.class);
        ctor.setAccessible(true);
        Object wrapped = ctor.newInstance(original, null);

        var getAnn = wrapperType.getDeclaredMethod("getParameterAnnotation", Class.class);
        getAnn.setAccessible(true);
        RequestBody requestBody = (RequestBody) getAnn.invoke(wrapped, RequestBody.class);
        assertNotNull(requestBody);
        assertTrue(requestBody.required());
    }

    @Test
    void testFailFastBodyMethodParameterWithFailFastBodyRequiredTrueUsesRequiredTrue() throws Exception {
        class ControllerLike {
            @SuppressWarnings("unused")
            public void create(@FailFastBody(required = true) String body) {
            }
        }

        var method = ControllerLike.class.getDeclaredMethod("create", String.class);
        MethodParameter original = new MethodParameter(method, 0);
        FailFastBody ann = method.getParameters()[0].getAnnotation(FailFastBody.class);

        Class<?> wrapperType = Class.forName("com.chao.failure.integration.mvc.OptionalBodyResolver$FailFastBodyMethodParameter");
        var ctor = wrapperType.getDeclaredConstructor(MethodParameter.class, FailFastBody.class);
        ctor.setAccessible(true);
        Object wrapped = ctor.newInstance(original, ann);

        var getAnn = wrapperType.getDeclaredMethod("getParameterAnnotation", Class.class);
        getAnn.setAccessible(true);
        RequestBody requestBody = (RequestBody) getAnn.invoke(wrapped, RequestBody.class);
        assertNotNull(requestBody);
        assertTrue(requestBody.required());
    }

    @Test
    void testFailFastBodyMethodParameterNullAnnotationTypeThrows() throws Exception {
        class ControllerLike {
            @SuppressWarnings("unused")
            public void create(@FailFastBody(required = false) String body) {
            }
        }

        var method = ControllerLike.class.getDeclaredMethod("create", String.class);
        MethodParameter original = new MethodParameter(method, 0);
        FailFastBody ann = method.getParameters()[0].getAnnotation(FailFastBody.class);

        Class<?> wrapperType = Class.forName("com.chao.failure.integration.mvc.OptionalBodyResolver$FailFastBodyMethodParameter");
        var ctor = wrapperType.getDeclaredConstructor(MethodParameter.class, FailFastBody.class);
        ctor.setAccessible(true);
        Object wrapped = ctor.newInstance(original, ann);

        var hasAnn = wrapperType.getDeclaredMethod("hasParameterAnnotation", Class.class);
        hasAnn.setAccessible(true);
        try {
            hasAnn.invoke(wrapped, new Object[]{null});
            fail();
        } catch (java.lang.reflect.InvocationTargetException e) {
            assertTrue(e.getCause() instanceof NullPointerException);
        }

        var getAnn = wrapperType.getDeclaredMethod("getParameterAnnotation", Class.class);
        getAnn.setAccessible(true);
        try {
            getAnn.invoke(wrapped, new Object[]{null});
            fail();
        } catch (java.lang.reflect.InvocationTargetException e) {
            assertTrue(e.getCause() instanceof NullPointerException);
        }
    }
}
