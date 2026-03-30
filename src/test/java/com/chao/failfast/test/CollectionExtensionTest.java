package com.chao.failfast.test;

import com.chao.failfast.Failure;
import com.chao.failfast.constant.FailureConst;
import com.chao.failfast.exception.Business;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CollectionExtensionTest {
    @Test
    void uniqueElementsShouldFailOnDuplicates() {
        Business e = assertThrows(Business.class, () -> Failure.begin()
                .uniqueElements(List.of(1, 1))
                .fail());
        assertEquals(FailureConst.UNIQUE_ELEMENTS_ERROR, e.getResponseCode());
    }

    @Test
    void containsAllShouldFailWhenMissing() {
        Business e = assertThrows(Business.class, () -> Failure.begin()
                .containsAll(List.of(1, 2), List.of(1, 3))
                .fail());
        assertEquals(FailureConst.CONTAINS_ALL_ERROR, e.getResponseCode());
    }

    @Test
    void noneMatchShouldFailWhenMatched() {
        Business e = assertThrows(Business.class, () -> Failure.begin()
                .noneMatch(List.of(1, 2), x -> x == 2)
                .fail());
        assertEquals(FailureConst.NONE_MATCH_ERROR, e.getResponseCode());
    }
}

