package com.chao.failfast.internal.chain;

import com.chao.failfast.Failure;
import com.chao.failfast.internal.Business;
import com.chao.failfast.internal.MultiBusiness;
import com.chao.failfast.internal.core.ResponseCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Chain OR Logic Tests (Strict Mode)")
class OrStrictTest {

    private static final ResponseCode ERROR_A = ResponseCode.of(1001, "Error A");
    private static final ResponseCode ERROR_B = ResponseCode.of(1002, "Error B");
    private static final ResponseCode ERROR_C = ResponseCode.of(1003, "Error C");

    @Test
    @DisplayName("Strict: A(Fail) OR B(Fail) -> Fail (Throws B)")
    void testStrictFailOrFail() {
        Business ex = assertThrows(Business.class, () ->
                Failure.strict()
                        .isTrue(false, ERROR_A)
                        .or()
                        .isTrue(false, ERROR_B)
                        .fail()
        );
        assertEquals(ERROR_B.getCode(), ex.getResponseCode().getCode());
    }

    @Test
    @DisplayName("Strict: A(Fail) OR B(Success) -> Success")
    void testStrictFailOrSuccess() {
        assertDoesNotThrow(() ->
                Failure.strict()
                        .isTrue(false, ERROR_A)
                        .or()
                        .isTrue(true, ERROR_B)
                        .fail()
        );
    }

    @Test
    @DisplayName("Strict: A(Success) OR B(Fail) -> Success")
    void testStrictSuccessOrFail() {
        assertDoesNotThrow(() ->
                Failure.strict()
                        .isTrue(true, ERROR_A)
                        .or()
                        .isTrue(false, ERROR_B)
                        .fail()
        );
    }

    @Test
    @DisplayName("Strict: (A fail OR B fail) AND C fail -> Fail (Errors B and C)")
    void testStrictComplexFail() {
        // Strict mode collects all errors
        // A fails -> error A
        // OR -> clears error A
        // B fails -> error B
        // C fails -> error C
        // Result: B, C

        try {
            Failure.strict()
                    .isTrue(false, ERROR_A)
                    .or()
                    .isTrue(false, ERROR_B)
                    .isTrue(false, ERROR_C)
                    .failAll();
            fail("Should throw MultiBusiness");
        } catch (Exception e) {
            // It might throw Business if size is 1, or MultiBusiness if size > 1
            // Here expected size is 2 (B and C)
            // Wait, Failure.strict() means failFast=false.
            // failAll() throws MultiBusiness if > 1 errors.

            if (e instanceof Business) {
                // If it's a single Business, we need to check if it's correct (should not happen here if logic holds)
                // But wait, failAll throws Business if size==1.
                // Let's verify expectations.
                // A fail. Valid=false. Causes=[A].
                // or(). Valid=false -> orHasSuccess=false. Causes cleared -> [].
                // B fail. Valid=false. Causes=[B].
                // C fail. Valid=false. Causes=[B, C].
                assertTrue(e instanceof MultiBusiness, "Should be MultiBusiness");
                MultiBusiness me = (MultiBusiness) e;
                assertEquals(2, me.getErrors().size());
                assertEquals(ERROR_B.getCode(), me.getErrors().get(0).getResponseCode().getCode());
                assertEquals(ERROR_C.getCode(), me.getErrors().get(1).getResponseCode().getCode());
            } else {
                fail("Unexpected exception type: " + e.getClass());
            }
        }
    }

    @Test
    @DisplayName("Strict: (A fail OR B success) AND C fail -> Fail (Error C)")
    void testStrictComplexSuccessFail() {
        // A fail -> error A
        // OR -> clears error A
        // B success -> no error B
        // C fail -> error C
        // Result: C

        Business ex = assertThrows(Business.class, () ->
                Failure.strict()
                        .isTrue(false, ERROR_A)
                        .or()
                        .isTrue(true, ERROR_B)
                        .isTrue(false, ERROR_C)
                        .failAll()
        );
        assertEquals(ERROR_C.getCode(), ex.getResponseCode().getCode());
    }
}
