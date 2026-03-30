package com.chao.failfast.internal.chain;

import com.chao.failfast.internal.chain.pipeline.ChainCore;
import com.chao.failfast.internal.core.ResponseCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Chain Terms 接口覆盖测试")
class ChainTermsTest {

    // 实现所�?Term 接口的测试类
    static class AllTermsChain extends ChainCore<AllTermsChain> implements
            ObjectTerm<AllTermsChain>,
            StringTerm<AllTermsChain>,
            NumberTerm<AllTermsChain>
            // ... 其他接口�?ChainTest 中已充分覆盖，这里主要验�?NO_OP �?detail 重载的有效�?
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

    @Test
    @DisplayName("verify new StringTerm methods (isJson, isCreditCard, isBase64)")
    void testNewStringTermMethods() {
        AllTermsChain chain = AllTermsChain.create();

        // 1. isJson
        // Valid
        chain.isJson("{\"a\":1}");
        assertThat(chain.isValid()).isTrue();

        // Invalid
        chain.isJson("{invalid}", ERR, "Invalid JSON");
        assertThat(chain.isValid()).isFalse();
        assertThat(chain.getCauses().get(0).getDetail()).isEqualTo("Invalid JSON");

        // Reset
        chain = AllTermsChain.create();

        // 2. isCreditCard
        // Valid
        chain.isCreditCard("79927398713");
        assertThat(chain.isValid()).isTrue();

        // Invalid
        chain.isCreditCard("123", ERR, "Invalid Card");
        assertThat(chain.isValid()).isFalse();
        assertThat(chain.getCauses().get(0).getDetail()).isEqualTo("Invalid Card");

        // Reset
        chain = AllTermsChain.create();

        // 3. isBase64
        // Valid
        chain.isBase64("SGVsbG8=");
        assertThat(chain.isValid()).isTrue();

        // Invalid
        chain.isBase64("!!!!", ERR, "Invalid Base64");
        assertThat(chain.isValid()).isFalse();
        assertThat(chain.getCauses().get(0).getDetail()).isEqualTo("Invalid Base64");
    }

    @Test
    @DisplayName("verify new StringTerm methods (isJson, isCreditCard, isBase64)")
    void testNewStringTermMethods2() {
        AllTermsChain chain = AllTermsChain.create();

        // 1. isJson
        // Valid
        chain.isJson("{\"a\":1}");
        assertThat(chain.isValid()).isTrue();

        // Invalid
        chain.isJson("{invalid}", ERR);
        assertThat(chain.isValid()).isFalse();

        // Reset
        chain = AllTermsChain.create();

        // 2. isCreditCard
        // Valid
        chain.isCreditCard("79927398713");
        assertThat(chain.isValid()).isTrue();

        // Invalid
        chain.isCreditCard("123", ERR);
        assertThat(chain.isValid()).isFalse();

        // Reset
        chain = AllTermsChain.create();

        // 3. isBase64
        // Valid
        chain.isBase64("SGVsbG8=");
        assertThat(chain.isValid()).isTrue();

        // Invalid
        chain.isBase64("!!!!", ERR);
        assertThat(chain.isValid()).isFalse();
    }

    @Test
    @DisplayName("isJson")
    void testIsJson() {
        AllTermsChain chain = AllTermsChain.create();
        chain.isJson("");
        assertThat(chain.isValid()).isFalse();
        chain.isJson(null);
        assertThat(chain.isValid()).isFalse();
    }

    @Test
    @DisplayName("isBase64")
    void testIsBase64() {
        AllTermsChain chain = AllTermsChain.create();
        chain.isBase64("");
        assertThat(chain.isValid()).isFalse();
        chain.isBase64(null);
        assertThat(chain.isValid()).isFalse();
    }
    @Test
    @DisplayName("isCreditCard")
    void testIsCreditCard() {
        AllTermsChain chain = AllTermsChain.create();
        chain.isCreditCard("s");
        assertThat(chain.isValid()).isFalse();
        chain.isCreditCard(null);
        assertThat(chain.isValid()).isFalse();
    }

}
