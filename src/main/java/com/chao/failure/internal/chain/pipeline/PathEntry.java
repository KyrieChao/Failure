package com.chao.failure.internal.chain.pipeline;

/**
 * Path entry record for value and path storage.
 *
 * @param <T> Value type
 * @author Kyrie Chao
 * @version 1.3.1
 */

public record PathEntry<T>(T value, String path) {
}

