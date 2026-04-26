package com.chao.failfast.internal;

import com.chao.failfast.config.mapping.CodeMappingConfig;
import com.chao.failfast.config.properties.FailureProperties;
import com.chao.failfast.constant.Severity;
import com.chao.failfast.constant.FailureConst;
import com.chao.failfast.exception.Business;
import com.chao.failfast.internal.core.Ex;
import com.chao.failfast.internal.core.FailureContext;
import com.chao.failfast.internal.core.observability.OpenTelemetryBridge;
import com.chao.failfast.internal.core.ResponseCode;
import com.chao.failfast.internal.policy.ErrorPolicy;
import com.chao.failfast.model.TestResponseCode;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

/**
 * Business exception test class.
 *
 * @author Kyrie Chao
 * @version 1.3.0
 */
@Slf4j
@DisplayName("Business Exception Test")
@Tag("business")
@ExtendWith(MockitoExtension.class)
class BusinessTest {

    private ResponseCode testCode;

    @BeforeEach
    void setUp() {
        testCode = ResponseCode.of(40010, "BAD_REQUEST", "Bad request");
    }

    @Nested
    @DisplayName("Static Factory Methods")
    class StaticFactoryMethods {

        @Test
        @DisplayName("of(int code, String message) - should create Business exception")
        void testOfWithCodeAndMessage() {
            // Given
            int code = 40010;
            String message = "BAD_REQUEST";

            // When
            Business business = Business.of(code, message);

            // Then
            assertThat(business).isNotNull();
            assertThat(business.getResponseCode().getCode()).isEqualTo(code);
            assertThat(business.getResponseCode().getMessage()).isEqualTo(message);
        }

        @Test
        @DisplayName("of(int code, String message, String detail) - should create Business exception with detail")
        void testOfWithCodeMessageAndDetail() {
            // Given
            int code = 40010;
            String message = "BAD_REQUEST";
            String detail = "Invalid input";

            // When
            Business business = Business.of(code, message, detail);

            // Then
            assertThat(business).isNotNull();
            assertThat(business.getResponseCode().getCode()).isEqualTo(code);
            assertThat(business.getDetail()).isEqualTo(detail);
        }

        @Test
        @DisplayName("of(int code, String message, String detail, Object...) - should create Business exception with formatted detail")
        void testOfWithCodeMessageDetailAndArgs() {
            // Given
            int code = 40010;
            String message = "BAD_REQUEST";
            String detail = "Invalid input: %s";
            String arg = "test";

            // When
            Business business = Business.of(code, message, detail, arg);

            // Then
            assertThat(business).isNotNull();
            assertThat(business.getDetail()).isEqualTo("Invalid input: test");
        }

        @Test
        @DisplayName("of(ResponseCode) - should create Business exception")
        void testOfWithResponseCode() {
            // When
            Business business = Business.of(testCode);

            // Then
            assertThat(business).isNotNull();
            assertThat(business.getResponseCode()).isEqualTo(testCode);
        }

        @Test
        @DisplayName("of(ResponseCode, String) - should create Business exception with detail")
        void testOfWithResponseCodeAndDetail() {
            // Given
            String detail = "Invalid input";

            // When
            Business business = Business.of(testCode, detail);

            // Then
            assertThat(business).isNotNull();
            assertThat(business.getResponseCode()).isEqualTo(testCode);
            assertThat(business.getDetail()).isEqualTo(detail);
        }

        @Test
        @DisplayName("of(ResponseCode, String, Object...) - should create Business exception with formatted detail")
        void testOfWithResponseCodeDetailAndArgs() {
            // Given
            String detail = "Invalid input: %s";
            String arg = "test";

            // When
            Business business = Business.of(testCode, detail, arg);

            // Then
            assertThat(business).isNotNull();
            assertThat(business.getDetail()).isEqualTo("Invalid input: test");
        }

        @Test
        @DisplayName("of(ResponseCode, String, String, String) - should create Business exception with method and location")
        void testOfWithResponseCodeDetailMethodAndLocation() {
            // Given
            String detail = "Invalid input";
            String method = "testMethod";
            String location = "TestClass.java:10";

            // When
            Business business = Business.of(testCode, detail, method, location);

            // Then
            assertThat(business).isNotNull();
            assertThat(business.getResponseCode()).isEqualTo(testCode);
            assertThat(business.getDetail()).isEqualTo(detail);
            assertThat(business.getMethod()).isEqualTo(method);
            assertThat(business.getLocation()).isEqualTo(location);
        }

        @Test
        @DisplayName("compose() - should return Fabricator instance")
        void testCompose() {
            // When
            Business.Fabricator fabricator = Business.compose();

            // Then
            assertThat(fabricator).isNotNull();
        }

        @Test
        @DisplayName("simpleCode() - should create ResponseCode correctly")
        void testSimpleCode() {
            // Given
            int code = 40010;
            String message = "BAD_REQUEST";

            // When
            ResponseCode responseCode = invokeSimpleCode(code, message);

            // Then
            assertThat(responseCode).isNotNull();
            assertThat(responseCode.getCode()).isEqualTo(code);
            assertThat(responseCode.getMessage()).isEqualTo(message);
            assertThat(responseCode.getDescription()).isEqualTo(message);
        }
    }

    @Nested
    @DisplayName("Fabricator Builder")
    class FabricatorBuilder {

        @Test
        @DisplayName("materialize() - should throw IllegalArgumentException when responseCode is null")
        void testMaterializeWithNullCode() {
            // Given
            Business.Fabricator fabricator = Business.compose();

            // When & Then
            assertThatThrownBy(fabricator::materialize)
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage(FailureConst.CODE_REQUIRED);
        }

        @Test
        @DisplayName("materialize() - should use error policy default detail when detail is null")
        void testMaterializeWithNullDetailAndErrorPolicy() {
            // Given
            ErrorPolicy errorPolicy = mock(ErrorPolicy.class);
            when(errorPolicy.defaultDetail(testCode)).thenReturn("Default detail");

            FailureContext context = mock(FailureContext.class);
            when(context.getErrorPolicy()).thenReturn(errorPolicy);

            try (MockedStatic<Ex> exMock = mockStatic(Ex.class)) {
                exMock.when(Ex::getContext).thenReturn(context);

                // When
                Business business = Business.compose().responseCode(testCode).materialize();

                // Then
                assertThat(business).isNotNull();
                assertThat(business.getDetail()).isEqualTo("Default detail");
            }
        }

        @Test
        @DisplayName("materialize() - should use responseCode description when detail and error policy are null")
        void testMaterializeWithNullDetailAndNullErrorPolicy() {
            // Given
            ResponseCode codeWithDescription = ResponseCode.of(40010, "BAD_REQUEST", "Bad request description");

            FailureContext context = mock(FailureContext.class);
            when(context.getErrorPolicy()).thenReturn(null);

            try (MockedStatic<Ex> exMock = mockStatic(Ex.class)) {
                exMock.when(Ex::getContext).thenReturn(context);

                // When
                Business business = Business.compose().responseCode(codeWithDescription).materialize();

                // Then
                assertThat(business).isNotNull();
                assertThat(business.getDetail()).isEqualTo("Bad request description");
            }
        }

        @Test
        @DisplayName("materialize() - should use responseCode message when detail, error policy and description are null")
        void testMaterializeWithNullDetailErrorPolicyAndDescription() {
            // Given
            ResponseCode codeWithOnlyMessage = ResponseCode.of(40010, "BAD_REQUEST", null);

            FailureContext context = mock(FailureContext.class);
            when(context.getErrorPolicy()).thenReturn(null);

            try (MockedStatic<Ex> exMock = mockStatic(Ex.class)) {
                exMock.when(Ex::getContext).thenReturn(context);

                // When
                Business business = Business.compose().responseCode(codeWithOnlyMessage).materialize();

                // Then
                assertThat(business).isNotNull();
                assertThat(business.getDetail()).isEqualTo("BAD_REQUEST");
            }
        }

        @Test
        @DisplayName("materialize() - should use default message when all details are null")
        void testMaterializeWithAllNullDetails() {
            // Given
            ResponseCode codeWithNothing = ResponseCode.of(40010, null, null);

            FailureContext context = mock(FailureContext.class);
            when(context.getErrorPolicy()).thenReturn(null);

            try (MockedStatic<Ex> exMock = mockStatic(Ex.class)) {
                exMock.when(Ex::getContext).thenReturn(context);

                // When
                Business business = Business.compose().responseCode(codeWithNothing).materialize();

                // Then
                assertThat(business).isNotNull();
                assertThat(business.getDetail()).isEqualTo(FailureConst.MESSAGE_OR_DESCRIPTION_REQUIRED);
            }
        }

        @Test
        @DisplayName("materialize() - should set method and location when shadow trace is enabled")
        void testMaterializeWithShadowTrace() {
            // Given
            FailureContext context = mock(FailureContext.class);
            when(context.isShadowTrace()).thenReturn(true);

            try (MockedStatic<Ex> exMock = mockStatic(Ex.class)) {
                exMock.when(Ex::getContext).thenReturn(context);
                exMock.when(Ex::method).thenReturn("testMethod");
                exMock.when(Ex::location).thenReturn("TestClass.java:10");

                // When
                Business business = Business.compose().responseCode(testCode).materialize();

                // Then
                assertThat(business).isNotNull();
                assertThat(business.getMethod()).isEqualTo("testMethod");
                assertThat(business.getLocation()).isEqualTo("TestClass.java:10");
            }
        }

        @Test
        @DisplayName("materialize() - should use code mapping config for HTTP status")
        void testMaterializeWithCodeMappingConfig() {
            // Given
            CodeMappingConfig config = mock(CodeMappingConfig.class);
            when(config.resolveHttpStatus(testCode.getCode())).thenReturn(org.springframework.http.HttpStatus.BAD_REQUEST);

            FailureContext context = mock(FailureContext.class);
            when(context.getCodeMappingConfig()).thenReturn(config);

            try (MockedStatic<Ex> exMock = mockStatic(Ex.class)) {
                exMock.when(Ex::getContext).thenReturn(context);

                // When
                Business business = Business.compose().responseCode(testCode).materialize();

                // Then
                assertThat(business).isNotNull();
                assertThat(business.getHttpStatus()).isEqualTo(org.springframework.http.HttpStatus.BAD_REQUEST);
            }
        }

        @Test
        @DisplayName("materialize() - should use internal server error when code mapping config is null")
        void testMaterializeWithNullCodeMappingConfig() {
            // Given
            FailureContext context = mock(FailureContext.class);
            when(context.getCodeMappingConfig()).thenReturn(null);

            try (MockedStatic<Ex> exMock = mockStatic(Ex.class)) {
                exMock.when(Ex::getContext).thenReturn(context);

                // When
                Business business = Business.compose().responseCode(testCode).materialize();

                // Then
                assertThat(business).isNotNull();
                assertThat(business.getHttpStatus()).isEqualTo(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }

        @Test
        @DisplayName("Builder methods - should chain correctly")
        void testBuilderMethods() {
            // Given
            String detail = "Invalid input";
            String method = "testMethod";
            String location = "TestClass.java:10";
            Object invalidValue = "test";
            String path = "user.name";

            // When
            Business.Fabricator fabricator = Business.compose()
                    .responseCode(testCode)
                    .detail(detail)
                    .location(location)
                    .invalidValue(invalidValue)
                    .path(path);

            // Then
            assertThat(fabricator).isNotNull();
            // Verify through materialize
            Business business = fabricator.materialize();
            assertThat(business.getResponseCode()).isEqualTo(testCode);
            assertThat(business.getDetail()).isEqualTo(detail);
            assertThat(business.getLocation()).isEqualTo(location);
            assertThat(business.getInvalidValue()).isEqualTo(invalidValue);
            assertThat(business.getPath()).isEqualTo(path);
        }

        @Test
        @DisplayName("Fabricator.method() - should set method correctly")
        void testFabricatorMethod() {
            // Given
            String method = "testMethod";

            // When
            Business.Fabricator fabricator = Business.compose();
            // Use reflection to call package-private method
            try {
                java.lang.reflect.Method methodMethod = Business.Fabricator.class.getDeclaredMethod("method", String.class);
                methodMethod.setAccessible(true);
                Business.Fabricator result = (Business.Fabricator) methodMethod.invoke(fabricator, method);

                // Then
                assertThat(result).isSameAs(fabricator);
                // Verify through materialize
                FailureContext context = mock(FailureContext.class);
                when(context.isShadowTrace()).thenReturn(false);

                try (MockedStatic<Ex> exMock = mockStatic(Ex.class)) {
                    exMock.when(Ex::getContext).thenReturn(context);

                    Business business = fabricator.responseCode(testCode).materialize();
                    assertThat(business.getMethod()).isEqualTo(method);
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Nested
    @DisplayName("Instance Methods")
    class InstanceMethods {

        @Test
        @DisplayName("toString() - should return formatted string without method")
        void testToStringWithoutMethod() {
            // Given
            Business business = Business.of(testCode, "Invalid input");

            // When
            String result = business.toString();

            // Then
            assertThat(result).contains("code=400_10");
            assertThat(result).contains("mes=BAD_REQUEST");
            assertThat(result).contains("des=Invalid input");
        }

        @Test
        @DisplayName("toString() - should return formatted string with method")
        void testToStringWithMethod() {
            // Given
            FailureContext context = mock(FailureContext.class);
            when(context.isShadowTrace()).thenReturn(true);

            try (MockedStatic<Ex> exMock = mockStatic(Ex.class)) {
                exMock.when(Ex::getContext).thenReturn(context);
                exMock.when(Ex::method).thenReturn("testMethod");

                Business business = Business.of(testCode, "Invalid input");

                // When
                String result = business.toString();

                // Then
                assertThat(result).contains("[testMethod]");
                assertThat(result).contains("code=400_10");
            }
        }

        @Test
        @DisplayName("toString() - should return formatted string with path")
        void testToStringWithPath() {
            // Given
            Business business = Business.compose()
                    .responseCode(testCode)
                    .detail("Invalid input")
                    .path("user.name")
                    .materialize();

            // When
            String result = business.toString();

            // Then
            assertThat(result).contains("path=user.name");
        }

        @Test
        @DisplayName("toString() - should not include blank path")
        void testToStringWithBlankPath() {
            Business business = Business.compose()
                    .responseCode(testCode)
                    .detail("Invalid input")
                    .path("   ")
                    .materialize();

            String result = business.toString();
            assertThat(result).doesNotContain("path=");
        }

        @Test
        @DisplayName("toString() - should return formatted string with masked value")
        void testToStringWithMaskedValue() {
            // Given
            FailureContext context = mock(FailureContext.class);
            when(context.isDebugSnapshot()).thenReturn(true);

            try (MockedStatic<Ex> exMock = mockStatic(Ex.class)) {
                exMock.when(Ex::getContext).thenReturn(context);

                Business business = Business.compose()
                        .responseCode(testCode)
                        .detail("Invalid input")
                        .path("profile.mobile")
                        .invalidValue("13800138000") // Mobile number
                        .materialize();

                // When
                String result = business.toString();

                // Then
                assertThat(result).contains("val=138****8000");
            }
        }

        @Test
        @DisplayName("toString() - should not include val when no context")
        void testToStringWithInvalidValueWithoutContext() {
            Ex.setContext(null);
            Business business = Business.compose()
                    .responseCode(testCode)
                    .detail("Invalid input")
                    .invalidValue("13800138000")
                    .materialize();
            String result = business.toString();
            assertThat(result).doesNotContain("val=");
        }

        @Test
        @DisplayName("toString() - should not include val when debug snapshot is disabled")
        void testToStringWithInvalidValueDebugSnapshotDisabled() {
            FailureContext context = mock(FailureContext.class);
            when(context.isDebugSnapshot()).thenReturn(false);

            try (MockedStatic<Ex> exMock = mockStatic(Ex.class)) {
                exMock.when(Ex::getContext).thenReturn(context);
                Business business = Business.compose()
                        .responseCode(testCode)
                        .detail("Invalid input")
                        .invalidValue("13800138000")
                        .materialize();
                String result = business.toString();
                assertThat(result).doesNotContain("val=");
            }
        }

        @Test
        @DisplayName("toString() - should return formatted string with location")
        void testToStringWithLocation() {
            // Given
            FailureContext context = mock(FailureContext.class);
            when(context.isShadowTrace()).thenReturn(true);

            try (MockedStatic<Ex> exMock = mockStatic(Ex.class)) {
                exMock.when(Ex::getContext).thenReturn(context);
                exMock.when(Ex::location).thenReturn("TestClass.java:10");

                Business business = Business.of(testCode, "Invalid input");

                // When
                String result = business.toString();

                // Then
                assertThat(result).contains("(TestClass.java:10)");
            }
        }

        @Test
        @DisplayName("toString() - should format method with dollar sign correctly")
        void testToStringWithDollarInMethod() {
            // Given
            Business business = Business.compose()
                    .responseCode(testCode)
                    .detail("Invalid input")
                    .materialize();

            // Use reflection to set method with dollar sign
            try {
                java.lang.reflect.Field methodField = Business.class.getDeclaredField("method");
                methodField.setAccessible(true);
                methodField.set(business, "TestClass$InnerClass#testMethod");
            } catch (Exception e) {
                e.printStackTrace();
            }

            // When
            String result = business.toString();

            // Then
            assertThat(result).contains("[TestClass#testMethod]");
        }

        @Test
        @DisplayName("toString() - should keep method when no hash after dollar")
        void testToStringWithDollarWithoutHashAfterDollar() {
            Business business = Business.compose()
                    .responseCode(testCode)
                    .detail("Invalid input")
                    .materialize();

            try {
                java.lang.reflect.Field methodField = Business.class.getDeclaredField("method");
                methodField.setAccessible(true);
                methodField.set(business, "TestClass$InnerClass");
            } catch (Exception e) {
                e.printStackTrace();
            }

            String result = business.toString();
            assertThat(result).contains("[TestClass$InnerClass]");
        }
    }

    @Nested
    @DisplayName("Private Methods")
    class PrivateMethods {

        @Test
        @DisplayName("shouldFillStackTrace() - should return true when code is null")
        void testShouldFillStackTraceWithNullCode() {
            // When
            boolean result = invokeShouldFillStackTrace(null);

            // Then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("shouldFillStackTrace() - should return true when shadow trace is enabled")
        void testShouldFillStackTraceWithShadowTrace() {
            // Given
            FailureContext context = mock(FailureContext.class);
            when(context.isShadowTrace()).thenReturn(true);

            try (MockedStatic<Ex> exMock = mockStatic(Ex.class)) {
                exMock.when(Ex::getContext).thenReturn(context);

                // When
                boolean result = invokeShouldFillStackTrace(testCode);

                // Then
                assertThat(result).isTrue();
            }
        }

        @Test
        @DisplayName("shouldFillStackTrace() - should return true when code is 5xx error")
        void testShouldFillStackTraceWith5xxError() {
            // Given
            ResponseCode serverError = ResponseCode.of(50000, "SERVER_ERROR", "Server error");
            CodeMappingConfig config = mock(CodeMappingConfig.class);
            when(config.resolveHttpStatus(serverError.getCode())).thenReturn(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR);

            FailureContext context = mock(FailureContext.class);
            when(context.isShadowTrace()).thenReturn(false);
            when(context.getCodeMappingConfig()).thenReturn(config);

            try (MockedStatic<Ex> exMock = mockStatic(Ex.class)) {
                exMock.when(Ex::getContext).thenReturn(context);

                // When
                boolean result = invokeShouldFillStackTrace(serverError);

                // Then
                assertThat(result).isTrue();
            }
        }

        @Test
        @DisplayName("shouldFillStackTrace() - should return false when no conditions met")
        void testShouldFillStackTraceWithNoConditions() {
            // Given
            CodeMappingConfig config = mock(CodeMappingConfig.class);
            when(config.resolveHttpStatus(testCode.getCode())).thenReturn(org.springframework.http.HttpStatus.BAD_REQUEST);

            FailureContext context = mock(FailureContext.class);
            when(context.isShadowTrace()).thenReturn(false);
            when(context.getCodeMappingConfig()).thenReturn(config);

            try (MockedStatic<Ex> exMock = mockStatic(Ex.class)) {
                exMock.when(Ex::getContext).thenReturn(context);

                // When
                boolean result = invokeShouldFillStackTrace(testCode);

                // Then
                assertThat(result).isFalse();
            }
        }

        @Test
        @DisplayName("shouldFillStackTrace() - should return false when context is not null but codeMappingConfig is null")
        void testShouldFillStackTraceWithNullCodeMappingConfig() {
            // Given
            FailureContext context = mock(FailureContext.class);
            when(context.isShadowTrace()).thenReturn(false);
            when(context.getCodeMappingConfig()).thenReturn(null);

            try (MockedStatic<Ex> exMock = mockStatic(Ex.class)) {
                exMock.when(Ex::getContext).thenReturn(context);

                // When
                boolean result = invokeShouldFillStackTrace(testCode);

                // Then
                assertThat(result).isFalse();
            }
        }

        @Test
        @DisplayName("shouldFillStackTrace() - should return true when severity is set and fillStackTrace is true")
        void testShouldFillStackTraceWithSeverityFillStackTraceTrue() {
            // Given
            FailureContext context = mock(FailureContext.class);
            when(context.isShadowTrace()).thenReturn(false);

            try (MockedStatic<Ex> exMock = mockStatic(Ex.class)) {
                exMock.when(Ex::getContext).thenReturn(context);

                // When
                boolean result = invokeShouldFillStackTraceWithSeverity(testCode, Severity.WARNING);

                // Then
                assertThat(result).isTrue();
            }
        }

        @Test
        @DisplayName("shouldFillStackTrace() - should return false when severity is set but fillStackTrace is false")
        void testShouldFillStackTraceWithSeverityFillStackTraceFalse() {
            // Given
            FailureContext context = mock(FailureContext.class);
            when(context.isShadowTrace()).thenReturn(false);

            try (MockedStatic<Ex> exMock = mockStatic(Ex.class)) {
                exMock.when(Ex::getContext).thenReturn(context);

                // When
                boolean result = invokeShouldFillStackTraceWithSeverity(testCode, Severity.INFO);

                // Then
                assertThat(result).isFalse();
            }
        }

        @Test
        @DisplayName("firstNonBlank() - should return null when values array is empty")
        void testFirstNonBlankWithEmptyArray() {
            String result = invokeFirstNonBlank();
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("firstNonBlank() - should return null when all values are null")
        void testFirstNonBlankWithAllNullValues() {
            String result = invokeFirstNonBlank(null, null, null);
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("firstNonBlank() - should return first non-null non-blank value")
        void testFirstNonBlankWithMixedValues() {
            String result = invokeFirstNonBlank(null, "", "valid");
            assertThat(result).isEqualTo("valid");
        }

        @Test
        @DisplayName("firstNonBlank() - should skip blank strings")
        void testFirstNonBlankWithBlankStrings() {
            String result = invokeFirstNonBlank("  ", "\t", "value");
            assertThat(result).isEqualTo("value");
        }

        @Test
        @DisplayName("firstNonBlank() - should return first non-blank value among multiple")
        void testFirstNonBlankWithMultipleNonBlank() {
            String result = invokeFirstNonBlank("first", "second", "third");
            assertThat(result).isEqualTo("first");
        }

        @Test
        @DisplayName("extractFileLine() - should extract file line correctly")
        void testExtractFileLine() {
            // Given
            Business business = Business.of(testCode, "Invalid input");

            // When
            String result = invokeExtractFileLine(business, "TestClass.java:10");

            // Then
            assertThat(result).isEqualTo("TestClass.java:10");
        }

        @Test
        @DisplayName("extractFileLine() - should handle location without parentheses")
        void testExtractFileLineWithoutParentheses() {
            // Given
            Business business = Business.of(testCode, "Invalid input");

            // When
            String result = invokeExtractFileLine(business, "TestClass.java:10");

            // Then
            assertThat(result).isEqualTo("TestClass.java:10");
        }

        @Test
        @DisplayName("extractFileLine() - should handle location with dollar sign")
        void testExtractFileLineWithDollarSign() {
            // Given
            Business business = Business.of(testCode, "Invalid input");

            // When
            String result = invokeExtractFileLine(business, "(TestClass$InnerClass.java:10)");

            // Then
            assertThat(result).isEqualTo("TestClass.java:10");
        }

        @Test
        @DisplayName("extractFileLine() - should return empty string when location is null")
        void testExtractFileLineWithNullLocation() {
            Business business = Business.of(testCode, "Invalid input");
            String result = invokeExtractFileLine(business, null);
            assertThat(result).isEqualTo("");
        }

        @Test
        @DisplayName("extractFileLine() - should return original when parentheses invalid")
        void testExtractFileLineWithInvalidParentheses() {
            Business business = Business.of(testCode, "Invalid input");
            String result = invokeExtractFileLine(business, "TestClass(");
            assertThat(result).isEqualTo("TestClass(");
        }

        @Test
        @DisplayName("extractFileLine() - should extract content without dollar")
        void testExtractFileLineWithParenthesesWithoutDollar() {
            Business business = Business.of(testCode, "Invalid input");
            String result = invokeExtractFileLine(business, "at a.b.C.m(C.java:10)");
            assertThat(result).isEqualTo("C.java:10");
        }

        @Test
        @DisplayName("extractFileLine() - should keep content when no dot after dollar")
        void testExtractFileLineWithDollarWithoutDot() {
            Business business = Business.of(testCode, "Invalid input");
            String result = invokeExtractFileLine(business, "at a.b.C.m(C$Inner:10)");
            assertThat(result).isEqualTo("C$Inner:10");
        }

        @Test
        @DisplayName("maskValue() - should mask mobile number")
        void testMaskValueMobile() {
            // Given
            Business business = Business.of(testCode, "Invalid input");

            // When
            String result = invokeMaskValue(business, "13800138000", null);

            // Then
            assertThat(result).isEqualTo("138****8000");
        }

        @Test
        @DisplayName("maskValue() - should mask email")
        void testMaskValueEmail() {
            // Given
            Business business = Business.of(testCode, "Invalid input");

            // When
            String result = invokeMaskValue(business, "test@example.com", null);

            // Then
            assertThat(result).contains("****@example.com");
        }

        @Test
        @DisplayName("maskValue() - should mask card number")
        void testMaskValueCard() {
            // Given
            Business business = Business.of(testCode, "Invalid input");

            // When
            String result = invokeMaskValue(business, "1234567890123456", null);

            // Then
            assertThat(result).isEqualTo("1234****3456");
        }

        @Test
        @DisplayName("maskValue() - should truncate long string")
        void testMaskValueLongString() {
            // Given
            Business business = Business.of(testCode, "Invalid input");
            String longString = "a".repeat(60);

            // When
            String result = invokeMaskValue(business, longString, null);

            // Then
            assertThat(result).contains("...(60char)...");
        }

        @Test
        @DisplayName("maskValue() - should return empty string for empty input")
        void testMaskValueEmptyString() {
            // Given
            Business business = Business.of(testCode, "Invalid input");

            // When
            String result = invokeMaskValue(business, "", null);

            // Then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("maskValue() - should return original string for normal input")
        void testMaskValueNormalString() {
            // Given
            Business business = Business.of(testCode, "Invalid input");

            // When
            String result = invokeMaskValue(business, "normal string", null);

            // Then
            assertThat(result).isEqualTo("normal string");
        }

        @Test
        @DisplayName("maskValue() - should mask by sensitive field path")
        void testMaskValueWithSensitivePath() {
            Business business = Business.of(testCode, "Invalid input");
            String result = invokeMaskValue(business, "plain-token-value", "headers.accessToken");
            assertThat(result).isEqualTo("***[MASKED]***");
        }
    }

    @Nested
    @DisplayName("Constructors")
    class Constructors {

        @Test
        @DisplayName("Constructor with path - should initialize all fields")
        void testConstructorWithPath() {
            // Given
            String detail = "Invalid input";
            String method = "testMethod";
            String location = "TestClass.java:10";
            org.springframework.http.HttpStatus httpStatus = org.springframework.http.HttpStatus.BAD_REQUEST;
            Object invalidValue = "test";
            String path = "user.name";

            // When
            Business business = new Business(testCode, detail, method, location, httpStatus, invalidValue, path);

            // Then
            assertThat(business).isNotNull();
            assertThat(business.getResponseCode()).isEqualTo(testCode);
            assertThat(business.getDetail()).isEqualTo(detail);
            assertThat(business.getMethod()).isEqualTo(method);
            assertThat(business.getLocation()).isEqualTo(location);
            assertThat(business.getHttpStatus()).isEqualTo(httpStatus);
            assertThat(business.getInvalidValue()).isEqualTo(invalidValue);
            assertThat(business.getMaskedValue()).isEqualTo(invalidValue);
            assertThat(business.getPath()).isEqualTo(path);
        }

        @Test
        @DisplayName("Constructor without path - should initialize all fields")
        void testConstructorWithoutPath() {
            // Given
            String detail = "Invalid input";
            String method = "testMethod";
            String location = "TestClass.java:10";
            org.springframework.http.HttpStatus httpStatus = org.springframework.http.HttpStatus.BAD_REQUEST;
            Object invalidValue = "test";

            // When
            Business business = new Business(testCode, detail, method, location, httpStatus, invalidValue);

            // Then
            assertThat(business).isNotNull();
            assertThat(business.getResponseCode()).isEqualTo(testCode);
            assertThat(business.getDetail()).isEqualTo(detail);
            assertThat(business.getMethod()).isEqualTo(method);
            assertThat(business.getLocation()).isEqualTo(location);
            assertThat(business.getHttpStatus()).isEqualTo(httpStatus);
            assertThat(business.getInvalidValue()).isEqualTo(invalidValue);
            assertThat(business.getMaskedValue()).isEqualTo(invalidValue);
            assertThat(business.getPath()).isNull();
        }

        @Test
        @DisplayName("Constructor - should use internal server error when httpStatus is null")
        void testConstructorWithNullHttpStatus() {
            // Given
            String detail = "Invalid input";
            String method = "testMethod";
            String location = "TestClass.java:10";
            Object invalidValue = "test";

            // When
            Business business = new Business(testCode, detail, method, location, null, invalidValue);

            // Then
            assertThat(business.getHttpStatus()).isEqualTo(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR);
        }

        @Test
        @DisplayName("Constructor - should use unknown error when responseCode is null")
        void testConstructorWithNullResponseCode() {
            // Given
            String detail = "Invalid input";
            String method = "testMethod";
            String location = "TestClass.java:10";
            org.springframework.http.HttpStatus httpStatus = org.springframework.http.HttpStatus.BAD_REQUEST;
            Object invalidValue = "test";

            // When
            Business business = new Business(null, detail, method, location, httpStatus, invalidValue);

            // Then
            assertThat(business.getResponseCode()).isNull();
            // Check that super constructor was called with unknown error
            assertThat(business.getMessage()).isNotNull();
        }

        @Test
        @DisplayName("Serialization - should not serialize raw invalidValue")
        void testSerializationWithTransientInvalidValue() throws Exception {
            Business business = new Business(TestResponseCode.PARAM_ERROR, "Invalid input", "method", "location", org.springframework.http.HttpStatus.BAD_REQUEST, "my-secret-token", "headers.accessToken");
            Business restored = roundTrip(business);
            assertThat(restored.getInvalidValue()).isNull();
            assertThat(restored.getMaskedValue()).isEqualTo("***[MASKED]***");
        }

        @Test
        @DisplayName("Constructor - should truncate long detail")
        void testConstructorShouldTruncateLongDetail() {
            String longDetail = "a".repeat(2000);
            Business business = new Business(TestResponseCode.PARAM_ERROR, longDetail, "method", "location", org.springframework.http.HttpStatus.BAD_REQUEST, null, "p");
            assertThat(business.getDetail()).endsWith("...");
            assertThat(business.getDetail().length()).isLessThanOrEqualTo(1027);
        }

        @Test
        @DisplayName("Constructor - should sanitize dangerous detail")
        void testConstructorShouldSanitizeDangerousDetail() {
            String dangerous = "<script>alert(1)</script>";
            Business business = new Business(TestResponseCode.PARAM_ERROR, dangerous, "method", "location", org.springframework.http.HttpStatus.BAD_REQUEST, null, "p");
            assertThat(business.getDetail()).isEqualTo("Invalid detail content");
        }

        @Test
        @DisplayName("Fabricator - should support explicit severity")
        void testMaterializeWithExplicitSeverity() {
            Business business = Business.compose()
                    .responseCode(TestResponseCode.PARAM_ERROR)
                    .detail("d")
                    .severity(Severity.CRITICAL)
                    .materialize();
            assertThat(business.getSeverity()).isEqualTo(Severity.CRITICAL);
        }

        @Test
        @DisplayName("Fabricator - should carry traceId/spanId")
        void testMaterializeWithTraceAndSpan() {
            Business business = Business.compose()
                    .responseCode(TestResponseCode.PARAM_ERROR)
                    .detail("d")
                    .traceId("trace-x")
                    .spanId("span-y")
                    .materialize();
            assertThat(business.getTraceId()).isEqualTo("trace-x");
            assertThat(business.getSpanId()).isEqualTo("span-y");
        }

        @Test
        @DisplayName("Constructor with traceId/spanId - should initialize all fields correctly")
        void testConstructorWithTraceIdAndSpanId() {
            // Given
            String detail = "Invalid input";
            String method = "testMethod";
            String location = "TestClass.java:10";
            org.springframework.http.HttpStatus httpStatus = org.springframework.http.HttpStatus.BAD_REQUEST;
            Object invalidValue = "test";
            String path = "user.name";
            String traceId = "trace-123";
            String spanId = "span-456";

            // When
            Business business = new Business(TestResponseCode.PARAM_ERROR, detail, method, location, httpStatus, invalidValue, path, traceId, spanId);

            // Then
            assertThat(business).isNotNull();
            assertThat(business.getResponseCode()).isEqualTo(TestResponseCode.PARAM_ERROR);
            assertThat(business.getDetail()).isEqualTo(detail);
            assertThat(business.getMethod()).isEqualTo(method);
            assertThat(business.getLocation()).isEqualTo(location);
            assertThat(business.getHttpStatus()).isEqualTo(httpStatus);
            assertThat(business.getInvalidValue()).isEqualTo(invalidValue);
            assertThat(business.getPath()).isEqualTo(path);
            assertThat(business.getTraceId()).isEqualTo(traceId);
            assertThat(business.getSpanId()).isEqualTo(spanId);
        }

        @Test
        @DisplayName("Constructor with traceId/spanId - should prefer explicit traceId over otel")
        void testConstructorPrefersExplicitTraceId() {
            // When
            Business business = new Business(TestResponseCode.PARAM_ERROR, "detail", "method", "location",
                    HttpStatus.BAD_REQUEST, null, null, "explicit-trace", "explicit-span");

            // Then
            assertThat(business.getTraceId()).isEqualTo("explicit-trace");
            assertThat(business.getSpanId()).isEqualTo("explicit-span");
        }

        @Test
        @DisplayName("Constructor - should resolve severity by properties mapping")
        void testResolveSeverityByProperties() {
            FailureProperties properties = new FailureProperties();
            properties.getLogging().setDefaultSeverity("INFO");
            properties.getLogging().getSeverityMapping().put(String.valueOf(TestResponseCode.PARAM_ERROR.getCode()), "DEBUG");
            FailureContext context = new FailureContext(properties, new CodeMappingConfig(properties), null);
            Ex.setContext(context);
            try {
                Business business = Business.of(TestResponseCode.PARAM_ERROR, "detail");
                assertThat(business.getSeverity()).isEqualTo(Severity.DEBUG);
            } finally {
                Ex.setContext(null);
            }
        }

        @Test
        @DisplayName("FristBlank")
        void testFristBlank() {
            // 在测试中
            try (MockedStatic<OpenTelemetryBridge> mock = mockStatic(OpenTelemetryBridge.class)) {
                mock.when(OpenTelemetryBridge::currentTraceId).thenReturn(null);
                mock.when(OpenTelemetryBridge::currentSpanId).thenReturn(null);

                // 执行测试代码
                Business business = new Business(TestResponseCode.PARAM_ERROR, "detail", "method", "location", HttpStatus.BAD_REQUEST, null, null, null, null);
                log.info("business: {}", String.valueOf(business));
            }
        }
    }

    // Helper methods to invoke private methods using reflection
    private boolean invokeShouldFillStackTrace(ResponseCode code) {
        try {
            Method method = Business.class.getDeclaredMethod("shouldFillStackTrace", ResponseCode.class, Severity.class);
            method.setAccessible(true);
            return (boolean) method.invoke(null, code, null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private boolean invokeShouldFillStackTraceWithSeverity(ResponseCode code, Severity severity) {
        try {
            Method method = Business.class.getDeclaredMethod("shouldFillStackTrace", ResponseCode.class, Severity.class);
            method.setAccessible(true);
            return (boolean) method.invoke(null, code, severity);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String invokeFirstNonBlank(String... values) {
        try {
            java.lang.reflect.Method method = Business.class.getDeclaredMethod("firstNonBlank", String[].class);
            method.setAccessible(true);
            return (String) method.invoke(null, (Object) values);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String invokeExtractFileLine(Business business, String loc) {
        try {
            java.lang.reflect.Method method = Business.class.getDeclaredMethod("extractFileLine", String.class);
            method.setAccessible(true);
            return (String) method.invoke(business, loc);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String invokeMaskValue(Business business, Object value, String path) {
        try {
            java.lang.reflect.Method method = Business.class.getDeclaredMethod("maskValue", Object.class, String.class);
            method.setAccessible(true);
            return (String) method.invoke(business, value, path);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Business roundTrip(Business business) throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeObject(business);
        }
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()))) {
            return (Business) ois.readObject();
        }
    }

    private ResponseCode invokeSimpleCode(int code, String message) {
        try {
            java.lang.reflect.Method method = Business.class.getDeclaredMethod("simpleCode", int.class, String.class);
            method.setAccessible(true);
            return (ResponseCode) method.invoke(null, code, message);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
