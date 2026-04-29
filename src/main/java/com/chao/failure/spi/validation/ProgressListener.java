package com.chao.failure.spi.validation;

/**
 * Progress listener interface for validation operations.
 *
 * @author Kyrie Chao
 * @version 1.3.1
 */

import com.chao.failure.exception.Business;

import java.util.List;

public interface ProgressListener {
    default void onStarted(long totalItems) {}

    default void onProgress(long processedItems, long totalItems, Business error) {}

    default void onCompleted(long totalItems, List<Business> allErrors) {}

    default void onCancelled() {}
}
