package com.chao.failure.spi.validation;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A validation cancel token class for canceling operations
 * This class uses atomic boolean to manage cancel status, ensuring thread safety
 *
 * @author Kyrie Chao
 * @version 1.3.0
 */
public final class CancelToken {
    // Use AtomicBoolean to ensure atomic operations on cancel status, avoiding state inconsistency in multi-threaded environment
    private final AtomicBoolean cancelled = new AtomicBoolean(false);

    /**
     * Cancel operation method
     * Set cancel status to true, indicating the operation has been cancelled
     */
    public void cancel() {
        cancelled.set(true);
    }

    /**
     * Check if operation has been cancelled
     * @return true if operation has been cancelled, false otherwise
     */
    public boolean isCancelled() {
        return cancelled.get();
    }
}
