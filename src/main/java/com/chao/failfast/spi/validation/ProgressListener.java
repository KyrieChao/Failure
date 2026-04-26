package com.chao.failfast.spi.validation;

import com.chao.failfast.exception.Business;

import java.util.List;

public interface ProgressListener {
    default void onStarted(long totalItems) {}

    default void onProgress(long processedItems, long totalItems, Business error) {}

    default void onCompleted(long totalItems, List<Business> allErrors) {}

    default void onCancelled() {}
}
