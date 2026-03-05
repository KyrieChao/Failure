package com.chao.failfast.internal.chain;

import com.chao.failfast.Failure;
import com.chao.failfast.internal.Business;
import com.chao.failfast.internal.core.ResponseCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Chain OR Logic Tests")
class OrTest {

    private static final ResponseCode ERROR_A = new ResponseCode() {
        @Override
        public int getCode() {
            return 1001;
        }

        @Override
        public String getMessage() {
            return "Error A";
        }

        @Override
        public String getDescription() {
            return "Error A Desc";
        }
    };

    private static final ResponseCode ERROR_B = new ResponseCode() {
        @Override
        public int getCode() {
            return 1002;
        }

        @Override
        public String getMessage() {
            return "Error B";
        }

        @Override
        public String getDescription() {
            return "Error B Desc";
        }
    };

    private static final ResponseCode ERROR_C = new ResponseCode() {
        @Override
        public int getCode() {
            return 1003;
        }

        @Override
        public String getMessage() {
            return "Error C";
        }

        @Override
        public String getDescription() {
            return "Error C Desc";
        }
    };

    @Test
    @DisplayName("A(Success) OR B(Success) -> Success")
    void testSuccessOrSuccess() {
        assertDoesNotThrow(() ->
                Failure.begin()
                        .isTrue(true, ERROR_A)
                        .or()
                        .isTrue(true, ERROR_B)
                        .fail()
        );
    }

    @Test
    @DisplayName("A(Success) OR B(Fail) -> Success")
    void testSuccessOrFail() {
        assertDoesNotThrow(() ->
                Failure.begin()
                        .isTrue(true, ERROR_A)
                        .or()
                        .isTrue(false, ERROR_B)
                        .fail()
        );
    }

    @Test
    @DisplayName("A(Fail) OR B(Success) -> Success")
    void testFailOrSuccess() {
        assertDoesNotThrow(() ->
                Failure.begin()
                        .isTrue(false, ERROR_A)
                        .or()
                        .isTrue(true, ERROR_B)
                        .fail()
        );
    }

    @Test
    @DisplayName("A(Fail) OR B(Fail) -> Fail (Throws B)")
    void testFailOrFail() {
        Business ex = assertThrows(Business.class, () ->
                Failure.begin()
                        .isTrue(false, ERROR_A)
                        .or()
                        .isTrue(false, ERROR_B)
                        .fail()
        );
        assertEquals(ERROR_B.getCode(), ex.getResponseCode().getCode());
    }

    @Test
    @DisplayName("A(Fail) OR B(Fail) OR C(Success) -> Success")
    void testMultiOrSuccess() {
        assertDoesNotThrow(() ->
                Failure.begin()
                        .isTrue(false, ERROR_A)
                        .or()
                        .isTrue(false, ERROR_B)
                        .or()
                        .isTrue(true, ERROR_C)
                        .fail()
        );
    }

    @Test
    @DisplayName("A(Fail) OR B(Fail) OR C(Fail) -> Fail (Throws C)")
    void testMultiOrFail() {
        Business ex = assertThrows(Business.class, () ->
                Failure.begin()
                        .isTrue(false, ERROR_A)
                        .or()
                        .isTrue(false, ERROR_B)
                        .or()
                        .isTrue(false, ERROR_C)
                        .fail()
        );
        assertEquals(ERROR_C.getCode(), ex.getResponseCode().getCode());
    }

    @Test
    @DisplayName("Complex Chain: (A fail) OR (B success AND C success) -> Success")
    void testComplexChainSuccess() {
        assertDoesNotThrow(() ->
                Failure.begin()
                        .isTrue(false, ERROR_A) // Fail
                        .or()
                        .isTrue(true, ERROR_B)  // Success
                        .isTrue(true, ERROR_C)  // Success
                        .fail()
        );
    }

    @Test
    @DisplayName("Complex Chain: (A fail) OR (B success AND C fail) -> Fail (Throws C)")
    void testComplexChainFail() {
        Business ex = assertThrows(Business.class, () ->
                Failure.begin()
                        .isTrue(false, ERROR_A) // Fail
                        .or()
                        .isTrue(true, ERROR_B)  // Success
                        .isTrue(false, ERROR_C) // Fail
                        .fail()
        );
        assertEquals(ERROR_C.getCode(), ex.getResponseCode().getCode());
    }

    @Test
    @DisplayName("Complex Chain: (A success OR B fail) AND C success -> Success")
    void testComplexChainSuccessLeft() {
        assertDoesNotThrow(() ->
                Failure.begin()
                        .isTrue(true, ERROR_A)  // Success
                        .or()
                        .isTrue(false, ERROR_B) // Fail (Ignored because A is success)
                        .isTrue(true, ERROR_C)  // Success (Required because chain continues as AND)
                        .fail()
        );
    }
}
