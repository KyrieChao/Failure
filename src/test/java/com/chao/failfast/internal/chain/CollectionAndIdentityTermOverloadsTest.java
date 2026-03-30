package com.chao.failfast.internal.chain;

import com.chao.failfast.internal.chain.pipeline.ChainCore;
import com.chao.failfast.internal.core.ResponseCode;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CollectionAndIdentityTermOverloadsTest {

    static class TermsChain extends ChainCore<TermsChain> implements CollectionTerm<TermsChain>, IdentityTerm<TermsChain> {
        TermsChain() {
            super(true, null);
        }

        @Override
        public TermsChain core() {
            return this;
        }
    }

    @Test
    void collectionTermOverloadsWithCodeOnlyAreCovered() {
        TermsChain chain = new TermsChain();

        List<Integer> col = new ArrayList<>(List.of(1, 2, 3));
        chain.containsAll(col, List.of(1, 2), ResponseCode.VALIDATION_ERROR_400);
        chain.noneMatch(col, n -> n < 0, ResponseCode.VALIDATION_ERROR_400);
        chain.uniqueElements(col, ResponseCode.VALIDATION_ERROR_400);

        assertThat(chain.isValid()).isTrue();
    }

    @Test
    void identityTermEqualsToUsesPathAwareCheck() {
        TermsChain chain = new TermsChain();
        chain.equalsTo("a.b", 1, 2, ResponseCode.VALIDATION_ERROR_400, "x");
        assertThat(chain.isValid()).isFalse();
        assertThat(chain.getCauses()).hasSize(1);
        assertThat(chain.getCauses().get(0).getPath()).isEqualTo("a.b");
    }
}

