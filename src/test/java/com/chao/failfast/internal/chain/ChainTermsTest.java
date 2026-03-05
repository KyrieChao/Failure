package com.chao.failfast.internal.chain;

import com.chao.failfast.internal.core.ResponseCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Chain Terms 接口覆盖测试")
class ChainTermsTest {

    // 实现所有 Term 接口的测试类
    static class AllTermsChain extends ChainCore<AllTermsChain> implements
            ObjectTerm<AllTermsChain>,
            StringTerm<AllTermsChain>,
            NumberTerm<AllTermsChain>
            // ... 其他接口在 ChainTest 中已充分覆盖，这里主要验证 NO_OP 和 detail 重载的有效性
    {
        protected AllTermsChain() {
            super(true, null);
        }

        public static AllTermsChain create() {
            return new AllTermsChain();
        }

        @Override
        public AllTermsChain core() {
            return this;
        }
    }

    private static final ResponseCode ERR = ResponseCode.of(400, "Error");
    private static final String DETAIL = "Detail";

    @Test
    @DisplayName("verify ObjectTerm overrides")
    void testObjectTerm() {
        AllTermsChain chain = AllTermsChain.create();

        // Test detail overload
        chain.notNull(null, ERR, DETAIL);
        assertThat(chain.isValid()).isFalse();
        assertThat(chain.getCauses().get(0).getDetail()).isEqualTo(DETAIL);

        // Reset
        chain = AllTermsChain.create();
        // Test NO_OP (default behavior) by using default method that uses it internally
        chain.notNull(new Object());
        assertThat(chain.isValid()).isTrue();
    }

    @Test
    @DisplayName("verify StringTerm overrides")
    void testStringTerm() {
        AllTermsChain chain = AllTermsChain.create();

        chain.notBlank("", ERR, DETAIL);
        assertThat(chain.isValid()).isFalse();
        assertThat(chain.getCauses().get(0).getDetail()).isEqualTo(DETAIL);
    }

    // ChainTest.java 已经包含了对所有 Term 接口所有方法的详细测试（包括 detail 重载）。
    // 这里只作为补充验证 ChainCore 和 Term 接口的集成。
}
