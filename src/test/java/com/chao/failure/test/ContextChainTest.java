package com.chao.failure.test;

import com.chao.failure.Failure;
import com.chao.failure.validator.FastValidator.ValidationContext;
import com.chao.failure.internal.core.ResponseCode;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

public class ContextChainTest {

    @Test
    public void testStrictWithContext() {
        // Create a context (fast=false -> strict mode)
        ValidationContext context = new ValidationContext(false);

        // Run validation chain
        Failure.with(context)
                .isTrue(false, ResponseCode.of(1001, "Error 1"))
                .isTrue(true, ResponseCode.of(1002, "Success"))
                .isTrue(false, ResponseCode.of(1003, "Error 2"))
                .verify();

        // Verify errors are reported to context
        assertFalse(context.isValid());
        assertEquals(2, context.hasCauses().size());
        assertEquals(1001, context.hasCauses().get(0).getResponseCode().getCode());
        assertEquals(1003, context.hasCauses().get(1).getResponseCode().getCode());
    }

    @Test
    public void testStrictWithFastContext() {
        // Create a context (fast=true -> fail fast mode)
        ValidationContext context = new ValidationContext(true);

        // Run validation chain
        Failure.with(context)
                .isTrue(false, ResponseCode.of(2001, "Error 1"))
                .isTrue(false, ResponseCode.of(2002, "Error 2")) // Should be skipped if chain respects context stop
                .verify();

        // Verify context stopped
        assertTrue(context.isStopped());
        assertEquals(1, context.hasCauses().size());
        assertEquals(2001, context.hasCauses().get(0).getResponseCode().getCode());
    }

    @Test
    public void testDateChecksWithContext() {
        // Create a context (fast=false -> strict mode)
        ValidationContext context = new ValidationContext(false);
        Date now = new Date();
        Date past = new Date(now.getTime() - 10000);
        Date future = new Date(now.getTime() + 10000);

        // Run validation chain
        Failure.with(context)
                .isFuture(past, ResponseCode.of(3001, "Should be future")) // Fail
                .isPast(future, ResponseCode.of(3002, "Should be past"))   // Fail
                .isFuture(future, ResponseCode.of(3003, "Success future")) // Pass
                .isPast(past, ResponseCode.of(3004, "Success past"))       // Pass
                .verify();

        // Verify context
        assertFalse(context.isValid());
        assertEquals(2, context.hasCauses().size());
        assertEquals(3001, context.hasCauses().get(0).getResponseCode().getCode());
        assertEquals(3002, context.hasCauses().get(1).getResponseCode().getCode());
    }

    @Test
    public void testJava8DateChecksWithContext() {
        ValidationContext context = new ValidationContext(false);
        java.time.LocalDate now = java.time.LocalDate.now();
        java.time.LocalDate past = now.minusDays(1);
        java.time.LocalDate future = now.plusDays(1);

        Failure.with(context)
                .isFuture(past, ResponseCode.of(4001, "LocalDate future fail"))
                .isPast(future, ResponseCode.of(4002, "LocalDate past fail"))
                .isFuture(future, ResponseCode.of(4003, "LocalDate future pass"))
                .isPast(past, ResponseCode.of(4004, "LocalDate past pass"))
                .verify();

        assertEquals(2, context.hasCauses().size());
        assertEquals(4001, context.hasCauses().get(0).getResponseCode().getCode());
        assertEquals(4002, context.hasCauses().get(1).getResponseCode().getCode());
    }

    @Test
    public void testShouldStop() {
        ValidationContext context = new ValidationContext(false);
        assertFalse(context.isFailed());

        context.reportError(ResponseCode.of(5001, "Error"));
        assertTrue(context.isFailed()); // isValid() == false

        ValidationContext context2 = new ValidationContext(false);
        context2.stop();
        assertTrue(context2.isFailed()); // stopped == true

        ValidationContext context3 = new ValidationContext(true); // fast=true
        context3.reportError(ResponseCode.of(5002, "Error"));
        assertTrue(context3.isFailed()); // isValid() == false AND stopped == true
    }
}
